package com.gzc.trigger.http;

import com.alibaba.fastjson.JSON;
import com.gzc.api.IMarketController;
import com.gzc.api.dto.market.*;
import com.gzc.api.response.Response;
import com.gzc.domain.activity.model.entity.ActivityAccountEntity;
import com.gzc.domain.activity.model.entity.SkuProductEntity;
import com.gzc.domain.activity.model.entity.SkuRechargeEntity;
import com.gzc.domain.activity.model.entity.UnpaidActivityOrderEntity;
import com.gzc.domain.activity.model.valobj.OrderTradeTypeVO;
import com.gzc.domain.activity.service.IRaffleQuotaService;
import com.gzc.domain.activity.service.IRaffleSkuProductService;
import com.gzc.domain.credit.model.entity.CreditAccountEntity;
import com.gzc.domain.credit.model.entity.CreditTradeEntity;
import com.gzc.domain.credit.model.valobj.TradeNameVO;
import com.gzc.domain.credit.model.valobj.TradeTypeVO;
import com.gzc.domain.credit.service.ICreditAdjustService;
import com.gzc.domain.rebate.model.entity.BehaviorEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.gzc.domain.rebate.model.valobj.BehaviorTypeVO;
import com.gzc.domain.rebate.service.IBehaviorRebateService;
import com.gzc.domain.strategy.model.valobj.RuleWeightVO;
import com.gzc.domain.strategy.service.raffle.IRaffleRule;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@RestController()
@RequestMapping("/api/${app.config.api-version}/raffle/activity")
@DubboService(version = "1.0")
public class MarketController implements IMarketController {

    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyyMMdd");

    @Resource
    private IBehaviorRebateService behaviorRebateService;
    @Resource
    private IRaffleQuotaService raffleQuotaService;
    @Resource
    private IRaffleRule raffleRule;
    @Resource
    private ICreditAdjustService creditAdjustService;
    @Resource
    private IRaffleSkuProductService raffleActivitySkuProductService;

    /**
     * 判断是否签到接口
     */
    @RequestMapping(value = "/is_calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> isUserCalenderSignRebate(@RequestParam String userId) {
        try {
            String bizId = dateFormatDay.format(new Date());
            // 幂等处理：每个用户每天只能签到一次
            List<BehaviorRebateOrderEntity> behaviorRebateOrderEntities = behaviorRebateService.queryOrderByOutBusinessNo(userId, bizId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(!behaviorRebateOrderEntities.isEmpty()) // 只要不为空，则表示已经做了签到
                    .build();
        } catch (Exception e) {
            log.error("查询用户是否完成日历签到返利失败 userId:{}", userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * 签到接口
     */
    @RequestMapping(value = "/calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> calenderSignRebate(@RequestParam String userId) {

        try {
            BehaviorEntity behaviorEntity = BehaviorEntity.builder()
                    .userId(userId)
                    .behaviorVO(BehaviorTypeVO.SIGN)
                    .outBusinessNo(dateFormatDay.format(new Date()))
                    .build();
            List<String> rebateOrder = behaviorRebateService.createRebateOrder(behaviorEntity);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (AppException e) {
            log.error("日历签到返利异常 userId:{} ", userId, e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("日历签到返利失败 userId:{}", userId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * 查询账户额度
     */
    @RequestMapping(value = "/query_user_activity_account", method = RequestMethod.POST)
    @Override
    public Response<UserActivityAccountResponseDTO> queryUserActivityAccount(@RequestBody UserActivityAccountRequestDTO request) {
        Long activityId = request.getActivityId();
        String userId = request.getUserId();
        try {
            // 1. 参数校验
            if (StringUtils.isBlank(userId) || null == activityId) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            ActivityAccountEntity activityAccountEntity = raffleQuotaService.queryActivityAccountEntity(activityId, userId);
            UserActivityAccountResponseDTO userActivityAccountResponseDTO = UserActivityAccountResponseDTO.builder()
                    .totalCount(activityAccountEntity.getTotalCount())
                    .totalCountSurplus(activityAccountEntity.getTotalCountSurplus())
                    .dayCount(activityAccountEntity.getDayCount())
                    .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                    .monthCount(activityAccountEntity.getMonthCount())
                    .monthCountSurplus(activityAccountEntity.getMonthCountSurplus())
                    .build();
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(userActivityAccountResponseDTO)
                    .build();
        } catch (Exception e) {
            log.error("查询用户活动账户失败 userId:{} activityId:{}", userId, activityId, e);
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    /**
     * 查询账户积分值
     */
    @RequestMapping(value = "/query_user_credit_account", method = RequestMethod.POST)
    @Override
    public Response<BigDecimal> queryCreditAccount(@RequestParam String userId) {
        try {
            log.info("查询用户积分值开始 userId:{}", userId);
            CreditAccountEntity creditAccountEntity = creditAdjustService.queryUserCreditAccount(userId);
            log.info("查询用户积分值完成 userId:{} adjustAmount:{}", userId, creditAccountEntity.getAdjustAmount());
            return Response.<BigDecimal>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(creditAccountEntity.getAdjustAmount())
                    .build();
        } catch (Exception e) {
            log.error("查询用户积分值失败 userId:{}", userId, e);
            return Response.<BigDecimal>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 查询活动下配置的sku组合
     */
    @RequestMapping(value = "/query_sku_product_list_by_activity_id", method = RequestMethod.POST)
    @Override
    public Response<List<SkuProductResponseDTO>> querySkuListByActivityId(@RequestParam Long activityId) {
        try {
            // 1. 参数校验
            if (null == activityId) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            // 2. 查询商品&封装数据
            List<SkuProductEntity> skuProductEntities = raffleActivitySkuProductService.querySkuProductEntityByActivityId(activityId);
            List<SkuProductResponseDTO> skuProductResponseDTOS = new ArrayList<>(skuProductEntities.size());
            for (SkuProductEntity skuProductEntity : skuProductEntities) {

                SkuProductResponseDTO.ActivityCount activityCount = new SkuProductResponseDTO.ActivityCount();
                activityCount.setTotalCount(skuProductEntity.getActivityCount().getTotalCount());
                activityCount.setMonthCount(skuProductEntity.getActivityCount().getMonthCount());
                activityCount.setDayCount(skuProductEntity.getActivityCount().getDayCount());

                SkuProductResponseDTO skuProductResponseDTO = new SkuProductResponseDTO();
                skuProductResponseDTO.setSku(skuProductEntity.getSku());
                skuProductResponseDTO.setActivityId(skuProductEntity.getActivityId());
                skuProductResponseDTO.setActivityCountId(skuProductEntity.getActivityCountId());
                skuProductResponseDTO.setStockCount(skuProductEntity.getStockCount());
                skuProductResponseDTO.setStockCountSurplus(skuProductEntity.getStockCountSurplus());
                skuProductResponseDTO.setProductAmount(skuProductEntity.getProductAmount());
                skuProductResponseDTO.setActivityCount(activityCount);
                skuProductResponseDTOS.add(skuProductResponseDTO);
            }

            return Response.<List<SkuProductResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(skuProductResponseDTOS)
                    .build();
        } catch (Exception e) {
            log.error("查询sku商品集合失败 activityId:{}", activityId, e);
            return Response.<List<SkuProductResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 积分兑换sku抽奖次数
     */
    @RequestMapping(value = "/credit_pay_exchange_sku", method = RequestMethod.POST)
    @Override
    public Response<Boolean> creditExchangeSku(@RequestBody SkuProductShopCartRequestDTO requestDTO) {
        try {
            // 1. 创建积分兑换sku抽奖次数的 待支付订单
            UnpaidActivityOrderEntity skuRechargeOrder = raffleQuotaService.createSkuRechargeOrder(SkuRechargeEntity.builder()
                    .userId(requestDTO.getUserId())
                    .sku(requestDTO.getSku())
                    .outBusinessNo(RandomStringUtils.randomNumeric(12))
                    .orderTradeType(OrderTradeTypeVO.credit_pay_trade)
                    .build());


            // 2. 扣减积分
            String creditOrderId = creditAdjustService.createCreditOrder(CreditTradeEntity.builder()
                    .userId(skuRechargeOrder.getUserId())
                    .tradeName(TradeNameVO.CONVERT_SKU)
                    .tradeType(TradeTypeVO.REVERSE)
                    .amount(skuRechargeOrder.getPayAmount())
                    .outBusinessNo(skuRechargeOrder.getOutBusinessNo())
                    .build());

            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("积分兑换商品失败 userId:{} sku:{}", requestDTO.getUserId(), requestDTO.getSku(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * curl --request POST \
     * --url http://localhost:8091/api/v1/raffle/strategy/query_raffle_strategy_rule_weight \
     * --header 'content-type: application/json' \
     * --data '{
     * "userId":"xiaofuge",
     * "activityId": 100301
     * }'
     */
    @RequestMapping(value = "/query_raffle_strategy_rule_weight", method = RequestMethod.POST)
    @Override
    public Response<List<RaffleStrategyRuleWeightResponseDTO>> queryUserRuleWeight(@RequestBody RaffleStrategyRuleWeightRequestDTO request) {
        Long activityId = request.getActivityId();
        String userId = request.getUserId();
        try {
            // 1. 参数校验
            if (StringUtils.isBlank(userId) || null == activityId) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            // 2. 查询用户抽奖总次数
            Integer userTotalCount = raffleQuotaService.queryRaffleActivityAccountPartakeCount(activityId, userId);
            // 3. 查询规则
            List<RaffleStrategyRuleWeightResponseDTO> raffleStrategyRuleWeightList = new ArrayList<>();
            List<RuleWeightVO> ruleWeightVOList = raffleRule.queryRuleWeightDetailsByActivityId(activityId);
            for (RuleWeightVO ruleWeightVO : ruleWeightVOList) {
                // 转换对象
                List<RaffleStrategyRuleWeightResponseDTO.StrategyAward> strategyAwards = new ArrayList<>();
                List<RuleWeightVO.Award> awardList = ruleWeightVO.getAwardList();
                for (RuleWeightVO.Award award : awardList) {
                    RaffleStrategyRuleWeightResponseDTO.StrategyAward strategyAward = new RaffleStrategyRuleWeightResponseDTO.StrategyAward();
                    strategyAward.setAwardId(award.getAwardId());
                    strategyAward.setAwardTitle(award.getAwardTitle());
                    strategyAwards.add(strategyAward);
                }
                // 封装对象
                RaffleStrategyRuleWeightResponseDTO raffleStrategyRuleWeightResponseDTO = new RaffleStrategyRuleWeightResponseDTO();
                raffleStrategyRuleWeightResponseDTO.setRuleWeightCount(ruleWeightVO.getWeight());
                raffleStrategyRuleWeightResponseDTO.setStrategyAwards(strategyAwards);
                raffleStrategyRuleWeightResponseDTO.setUserActivityAccountTotalUseCount(userTotalCount);

                raffleStrategyRuleWeightList.add(raffleStrategyRuleWeightResponseDTO);
            }
            Response<List<RaffleStrategyRuleWeightResponseDTO>> response = Response.<List<RaffleStrategyRuleWeightResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(raffleStrategyRuleWeightList)
                    .build();
            log.info("查询抽奖策略权重规则配置完成 userId:{} activityId：{} response: {}", userId, activityId, JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
            log.error("查询抽奖策略权重规则配置失败 userId:{} activityId：{}", userId, activityId, e);
            return Response.<List<RaffleStrategyRuleWeightResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
