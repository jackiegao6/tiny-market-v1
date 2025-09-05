package com.gzc.test;

import com.gzc.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Random;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Resource
    private IRedisService redisService;

    @Test
    public void test() {
        RMap<Object, Object> map = redisService.getMap("strategy_id_100001");

        map.put(1,101);
        map.put(2,101);
        map.put(3,101);
        map.put(4,110);
        map.put(5,102);
        map.put(6,103);
        map.put(7,104);
        map.put(8,105);
        map.put(9,1066);

        Random random = new Random();
        log.info("抽到的奖品号码为:{}", redisService.getFromMap("strategy_id_100001", random.nextInt(1,10)).toString());
    }
}
