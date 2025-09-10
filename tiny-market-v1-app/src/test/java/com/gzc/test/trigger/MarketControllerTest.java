package com.gzc.test.trigger;

import com.gzc.api.IMarketController;
import com.gzc.domain.activity.service.armory.IActivityArmory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

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
        marketController.calenderSignRebate("dd");

    }
}
