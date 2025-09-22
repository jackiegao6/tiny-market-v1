package com.gzc.test.domain.rebate;

import com.alibaba.fastjson2.JSON;
import com.gzc.domain.rebate.model.entity.BehaviorEntity;
import com.gzc.domain.rebate.model.valobj.BehaviorTypeVO;
import com.gzc.domain.rebate.service.IBehaviorRebateService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @description 行为返利单测
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BehaviorRebateServiceTest {

    @Resource
    private IBehaviorRebateService behaviorRebateService;

    @Test
    public void test_createRebateOrder_4_skuRecharge_And_CreditAdjust() throws InterruptedException {
        BehaviorEntity behaviorEntity = new BehaviorEntity();
        behaviorEntity.setUserId("gao77");
        behaviorEntity.setBehaviorVO(BehaviorTypeVO.SIGN);
        // 重复的 OutBusinessNo 会报错唯一索引冲突，这也是保证幂等的手段，确保不会多记账
        behaviorEntity.setOutBusinessNo("20250913");

        List<String> orderIds = behaviorRebateService.createRebateOrder(behaviorEntity);
        log.info("请求参数：{}", JSON.toJSONString(behaviorEntity));
        log.info("测试结果：{}", JSON.toJSONString(orderIds));
        new CountDownLatch(1).await();
    }

}
