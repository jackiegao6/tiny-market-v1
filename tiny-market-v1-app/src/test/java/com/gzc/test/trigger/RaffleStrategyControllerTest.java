package com.gzc.test.trigger;

import com.alibaba.fastjson.JSON;
import com.gzc.api.IBeforeRaffleController;
import com.gzc.api.dto.RaffleAwardListRequestDTO;
import com.gzc.api.dto.RaffleAwardListResponseDTO;
import com.gzc.api.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 营销抽奖服务测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleStrategyControllerTest {

    @Resource
    private IBeforeRaffleController beforeRaffleService;

    @Test
    public void test_queryRaffleAwardList() {
        RaffleAwardListRequestDTO request = new RaffleAwardListRequestDTO();
        request.setUserId("gzc");
        request.setActivityId(100301L);
        Response<List<RaffleAwardListResponseDTO>> response = beforeRaffleService.queryRaffleAwardList(request);

        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    @Test
    public void test_armory() {
        Response<Boolean> response = beforeRaffleService.armory(100301L);
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

}
