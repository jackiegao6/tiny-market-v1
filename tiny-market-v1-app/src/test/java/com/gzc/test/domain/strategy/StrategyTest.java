package com.gzc.test.domain.strategy;

import com.gzc.domain.strategy.service.armory.IStrategyArmory;
import com.gzc.domain.strategy.service.armory.IStrategyDispatch;
import com.gzc.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 策略领域测试
 * @create 2023-12-23 11:33
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyTest {

    @Resource
    private IStrategyArmory strategyArmory;
    @Resource
    private IStrategyDispatch strategyDispatch;
    @Resource
    private IRedisService redisService;

    /**
     * 策略ID；100001L、100002L 装配的时候创建策略表写入到 Redis Map 中
     */
    @Before
    public void test_strategyArmory() {
        boolean success = strategyArmory.assembleLotteryStrategy(100001L);
        log.info("测试结果：{}", success);
    }

    /**
     * 从装配的策略中随机获取奖品ID值
     */
    @Test
    public void test_getRandomAward() {
        log.info("测试结果：{} - 奖品ID值", strategyDispatch.getRandomAwardId(100001L));
    }

    @Test
    public void test_getRandomAward_with_ruleWeightValue(){
        log.info("测试结果：{} - 4000 权重 奖品ID值", strategyDispatch.getRandomAwardId(100001L, "4000"));
        log.info("测试结果：{} - 5000 权重 奖品ID值", strategyDispatch.getRandomAwardId(100001L, "5000"));
        log.info("测试结果：{} - 6000 权重 奖品ID值", strategyDispatch.getRandomAwardId(100001L, "6000"));
    }

}
