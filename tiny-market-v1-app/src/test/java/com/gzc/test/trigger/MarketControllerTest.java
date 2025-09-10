package com.gzc.test.trigger;

import com.alibaba.fastjson.JSON;
import com.gzc.api.IMarketController;
import com.gzc.api.dto.market.RaffleStrategyRuleWeightRequestDTO;
import com.gzc.api.dto.market.RaffleStrategyRuleWeightResponseDTO;
import com.gzc.api.dto.market.UserActivityAccountRequestDTO;
import com.gzc.api.dto.market.UserActivityAccountResponseDTO;
import com.gzc.api.response.Response;
import com.gzc.domain.activity.service.armory.IActivityArmory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MarketControllerTest {

    @Resource
    private IMarketController marketController;
    @Resource
    private IActivityArmory activityArmory;

    /**
     * 清mq队列，清缓存，先装配再抽奖
     */
    @Before
    public void setUp() {
        log.info("装配活动：{}", activityArmory.assembleActivitySku(9011L));
    }

    @Test
    public void test_calenderSignRebate(){
        marketController.calenderSignRebate("gzc");
    }

    @Test
    public void test_isCalendarSignRebate() {
        Response<Boolean> response = marketController.isUserCalenderSignRebate("gzc");
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    @Test
    public void test_queryUserActivityAccount() {
        UserActivityAccountRequestDTO request = new UserActivityAccountRequestDTO();
        request.setActivityId(100301L);
        request.setUserId("gzc");

        // 查询数据
        Response<UserActivityAccountResponseDTO> response = marketController.queryUserActivityAccount(request);

        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }


    @Test
    public void test_queryRaffleStrategyRuleWeight() {
        RaffleStrategyRuleWeightRequestDTO request = new RaffleStrategyRuleWeightRequestDTO();
        request.setUserId("gzc");
        request.setActivityId(100301L);

        Response<List<RaffleStrategyRuleWeightResponseDTO>> response = marketController.queryUserRuleWeight(request);
        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }
}
