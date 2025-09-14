package com.gzc.trigger.http;

import com.alibaba.fastjson.JSON;
import com.gzc.api.IBeforeRaffleController;
import com.gzc.api.dto.RaffleAwardListRequestDTO;
import com.gzc.api.dto.RaffleAwardListResponseDTO;
import com.gzc.api.dto.RaffleRequestDTO;
import com.gzc.api.dto.RaffleResponseDTO;
import com.gzc.api.response.Response;
import com.gzc.domain.activity.service.IRaffleOrder;
import com.gzc.domain.activity.service.armory.IActivityArmory;
import com.gzc.domain.strategy.model.entity.RaffleAwardEntity;
import com.gzc.domain.strategy.model.entity.RaffleFactorEntity;
import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.service.armory.IStrategyArmory;
import com.gzc.domain.strategy.service.raffle.IRaffleAwardService;
import com.gzc.domain.strategy.service.raffle.IRaffleRule;
import com.gzc.domain.strategy.service.raffle.IRaffleStrategy;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/strategy")
@DubboService(version = "1.0")
public class BeforeRaffleStrategyController implements IBeforeRaffleController {

    @Resource
    private IActivityArmory activityArmory;
    @Resource
    private IStrategyArmory strategyArmory;
    @Resource
    private IRaffleAwardService raffleAwardService;
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Resource
    private IRaffleRule raffleRule;
    @Resource
    private IRaffleOrder raffleOrder;

    /**
     * 活动装配|策略装配 - 数据预热
     *
     * @param activityId 活动ID
     * @return 装配结果
     * 接口：<a href="http://localhost:8098/api/v1/raffle/activity/armory">/api/v1/raffle/activity/armory</a>
     */
    @RequestMapping(value = "/armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> armory(@RequestParam Long activityId) {
        try {
            // 1. 活动装配 活动本身的信息 活动总次数的统计量 活动涉及的sku的库存
            boolean res_activity = activityArmory.assembleActivitySkuByActivityId(activityId);
            // 2. 策略装配 该策略涉及到的奖品列表信息 各个奖品的库存 该策略生成的用于抽奖的哈希表
            boolean res_strategy = strategyArmory.assembleLotteryStrategyByActivityId(activityId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("数据预热环节，失败 activityId:{}", activityId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.ARMORY_ERROR.getCode())
                    .info(ResponseCode.ARMORY_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "/query_raffle_award_list", method = RequestMethod.POST)
    @Override
    public Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(@RequestBody RaffleAwardListRequestDTO requestDTO) {

        // 1. 参数校验
        Long activityId = requestDTO.getActivityId();
        if (StringUtils.isBlank(requestDTO.getUserId()) || null == activityId) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        try {
            // 2. 从 strategy_award 表中 查询策略的奖品配置
            List<StrategyAwardEntity> strategyAwardEntities = raffleAwardService.queryRaffleStrategyAwardListByActivityId(activityId);

            // 3. 获取有规则的奖品的 规则模型
            String[] treeIds = strategyAwardEntities.stream().map(StrategyAwardEntity::getRuleModels)
                    .filter(ruleModel -> ruleModel != null && !ruleModel.isEmpty())
                    .toArray(String[]::new);

            // 4. 从 strategy_tree_node 表中 在规则模型名字中过滤次数规则 再查询 次数规则值
            Map<String, Integer> lockCountMap = raffleRule.queryTreeLockCount(treeIds);

            // 5. 从 raffle_activity_account_day 表中 获取用户今日的参与量
            Integer dayPartakeCount = raffleOrder.queryRaffleActivityAccountDayPartakeCount(activityId, requestDTO.getUserId());

            //6. 填充数据
            List<RaffleAwardListResponseDTO> raffleAwardListResponseDTOS = new ArrayList<>(strategyAwardEntities.size());
            for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
                Integer awardRuleLockCount = lockCountMap.get(strategyAward.getRuleModels());

                raffleAwardListResponseDTOS.add(RaffleAwardListResponseDTO.builder()
                        .awardId(strategyAward.getAwardId())
                        .awardTitle(strategyAward.getAwardTitle())
                        .awardSubtitle(strategyAward.getAwardSubtitle())
                        .sort(strategyAward.getSort())
                        // 没有当日参与量限制的奖品 awardRuleLockCount 为空
                        .awardRuleLockCount(awardRuleLockCount)
                        // 没有当日参与量限制的奖品 或者 当日参与量大于次数限制的奖品 isAwardUnlock 为true
                        .isAwardUnlock(null == awardRuleLockCount || dayPartakeCount > awardRuleLockCount)
                        // 没有当日参与量限制的奖品 或者 当日残余量未大于次数限制的奖品 返回解锁还需的次数
                        .waitUnlockCount(null == awardRuleLockCount || awardRuleLockCount <= dayPartakeCount ? 0 : awardRuleLockCount - dayPartakeCount)
                        .build());
            }
            Response<List<RaffleAwardListResponseDTO>> response = Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(raffleAwardListResponseDTOS)
                    .build();
            return response;
        } catch (Exception e) {
            log.error("查询抽奖奖品列表配置失败 userId:{} activityId：{}", requestDTO.getUserId(), requestDTO.getActivityId(), e);
            return Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "/strategy_armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> strategyArmory(@RequestParam Long strategyId) {
        try {
            boolean armoryStatus = strategyArmory.assembleLotteryStrategy(strategyId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(armoryStatus)
                    .build();
        } catch (Exception e) {
            log.error("抽奖策略装配失败 strategyId：{}", strategyId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    // todo
    @RequestMapping(value = "/random_raffle", method = RequestMethod.POST)
    @Override
    public Response<RaffleResponseDTO> randomRaffle(@RequestBody RaffleRequestDTO requestDTO) {

        try {
            log.info("随机抽奖开始 strategyId: {}", requestDTO.getStrategyId());
            // 调用抽奖接口
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    .userId("system")
                    .strategyId(requestDTO.getStrategyId())
                    .build());
            // 封装返回结果
            Response<RaffleResponseDTO> response = Response.<RaffleResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(RaffleResponseDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
            log.info("随机抽奖完成 strategyId: {} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        } catch (AppException e) {
            log.error("随机抽奖失败 strategyId：{} {}", requestDTO.getStrategyId(), e.getInfo());
            return Response.<RaffleResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("随机抽奖失败 strategyId：{}", requestDTO.getStrategyId(), e);
            return Response.<RaffleResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
