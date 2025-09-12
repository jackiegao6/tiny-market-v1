package com.gzc.test.domain.credit;

import com.gzc.domain.credit.model.entity.CreditTradeEntity;
import com.gzc.domain.credit.model.valobj.TradeNameVO;
import com.gzc.domain.credit.model.valobj.TradeTypeVO;
import com.gzc.domain.credit.service.ICreditAdjustService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 积分额度增加服务测试
 * @create 2024-06-01 10:22
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CreditAdjustServiceTest {

    @Resource
    private ICreditAdjustService creditAdjustService;

    @Test
    public void test_createOrder_forward() {
        CreditTradeEntity tradeEntity = new CreditTradeEntity();
        tradeEntity.setUserId("gao66");
        tradeEntity.setTradeName(TradeNameVO.REBATE);
        tradeEntity.setTradeType(TradeTypeVO.FORWARD);
        tradeEntity.setAmount(new BigDecimal("10.19"));
        tradeEntity.setOutBusinessNo("10000990991");
        creditAdjustService.createCreditOrder(tradeEntity);
    }

    @Test
    public void test_createOrder_reverse() {
        CreditTradeEntity tradeEntity = new CreditTradeEntity();
        tradeEntity.setUserId("gao66");
        tradeEntity.setTradeName(TradeNameVO.REBATE);
        tradeEntity.setTradeType(TradeTypeVO.REVERSE);
        tradeEntity.setAmount(new BigDecimal("-10.19"));
        tradeEntity.setOutBusinessNo("20000990991");
        creditAdjustService.createCreditOrder(tradeEntity);
    }


    @Test
    public void test_createOrder_pay() throws InterruptedException {
        CreditTradeEntity tradeEntity = new CreditTradeEntity();
        tradeEntity.setUserId("czg");
        tradeEntity.setTradeName(TradeNameVO.CONVERT_SKU);
        tradeEntity.setTradeType(TradeTypeVO.REVERSE);
        tradeEntity.setAmount(new BigDecimal("-1.68"));
        tradeEntity.setOutBusinessNo("70009240609001");
        creditAdjustService.createCreditOrder(tradeEntity);

        new CountDownLatch(1).await();
    }

}
