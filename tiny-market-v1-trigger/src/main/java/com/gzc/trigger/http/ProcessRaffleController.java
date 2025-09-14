package com.gzc.trigger.http;

import com.gzc.api.IProcessRaffleController;
import com.gzc.api.dto.ActivityDrawRequestDTO;
import com.gzc.api.dto.ActivityDrawResponseDTO;
import com.gzc.api.response.Response;
import com.gzc.domain.activity.model.entity.partake.UserRaffleOrderEntity;
import com.gzc.domain.activity.service.IRafflePartake;
import com.gzc.domain.activity.service.armory.IActivityArmory;
import com.gzc.domain.award.model.entity.UserAwardRecordEntity;
import com.gzc.domain.award.model.valobj.AwardStateVO;
import com.gzc.domain.award.service.IAwardService;
import com.gzc.domain.strategy.model.entity.RaffleAwardEntity;
import com.gzc.domain.strategy.model.entity.RaffleFactorEntity;
import com.gzc.domain.strategy.service.armory.IStrategyArmory;
import com.gzc.domain.strategy.service.raffle.IRaffleStrategy;
import com.gzc.types.annotation.DCCValue;
import com.gzc.types.annotation.RateLimiterAccessInterceptor;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description 抽奖活动服务 注意；在不引用 application/case 层的时候，就需要让接口实现层来做领域的串联。一些较大规模的系统，需要加入 case 层。
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/activity")
@DubboService(version = "1.0")
public class ProcessRaffleController implements IProcessRaffleController {

    @Resource
    private IRafflePartake raffleActivityPartakeService;
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Resource
    private IAwardService awardService;
    @Resource
    private IActivityArmory activityArmory;
    @Resource
    private IStrategyArmory strategyArmory;
    @DCCValue("degradeSwitch:close")
    private String degradeSwitch;

    /**
     * 抽奖接口
     *
     * @param request 请求对象
     * @return 抽奖结果
     */
    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "150")
    }, fallbackMethod = "drawHystrixError"
    )
    @RateLimiterAccessInterceptor(key = "userId", fallbackMethod = "drawRateLimiterError", permitsPerSecond = 1.0d, limit2blacklist = 3)
    @RequestMapping(value = "/draw", method = RequestMethod.POST)
    @Override
    public Response<ActivityDrawResponseDTO> draw(@RequestBody ActivityDrawRequestDTO request) {
        try {
            // 1. 参数校验
            if (StringUtils.isBlank(request.getUserId()) || null == request.getActivityId()) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            // 0. 降级开关【open 开启、close 关闭】
            if (StringUtils.isNotBlank(degradeSwitch) && "open".equals(degradeSwitch)) {
                return Response.<ActivityDrawResponseDTO>builder()
                        .code(ResponseCode.DEGRADE_STATUS.getCode())
                        .info(ResponseCode.DEGRADE_STATUS.getInfo())
                        .build();
            }

            // 2. 参与活动 - 创建参与记录订单
            UserRaffleOrderEntity orderEntity = raffleActivityPartakeService.createRaffleOrder(request.getUserId(), request.getActivityId());
            log.info("活动抽奖，创建订单 userId:{} activityId:{} orderId:{}", request.getUserId(), request.getActivityId(), orderEntity.getOrderId());
            // 3. 抽奖策略 - 执行抽奖
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    .userId(orderEntity.getUserId())
                    .strategyId(orderEntity.getStrategyId())
                    .endDateTime(orderEntity.getEndDateTime())
                    .build());
            // 4. 存放结果 - 写入中奖记录
            UserAwardRecordEntity userAwardRecord = UserAwardRecordEntity.builder()
                    .userId(orderEntity.getUserId())
                    .activityId(orderEntity.getActivityId())
                    .strategyId(orderEntity.getStrategyId())
                    .orderId(orderEntity.getOrderId())
                    .awardId(raffleAwardEntity.getAwardId())
                    .awardTitle(raffleAwardEntity.getAwardTitle())
                    .awardTime(new Date())
                    .awardState(AwardStateVO.create)
                    .awardConfig(raffleAwardEntity.getAwardConfig())
                    .build();
            awardService.saveUserAwardRecord(userAwardRecord);
            // 5. 返回结果
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ActivityDrawResponseDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardTitle(raffleAwardEntity.getAwardTitle())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();

        } catch (AppException e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    public Response<ActivityDrawResponseDTO> drawRateLimiterError(@RequestBody ActivityDrawRequestDTO request) {

        return Response.<ActivityDrawResponseDTO>builder()
                .code(ResponseCode.RATE_LIMITER.getCode())
                .info(ResponseCode.RATE_LIMITER.getInfo())
                .build();
    }

    public Response<ActivityDrawResponseDTO> drawHystrixError(@RequestBody ActivityDrawRequestDTO request) {
        log.info("活动抽奖熔断 userId:{} activityId:{}", request.getUserId(), request.getActivityId());
        return Response.<ActivityDrawResponseDTO>builder()
                .code(ResponseCode.HYSTRIX.getCode())
                .info(ResponseCode.HYSTRIX.getInfo())
                .build();
    }

}
