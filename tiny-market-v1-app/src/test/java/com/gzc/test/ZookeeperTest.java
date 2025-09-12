package com.gzc.test;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ZookeeperTest {

    @Resource
    private CuratorFramework zookeeperClient;

    @Test
    public void createNode() throws Exception {
        String path = "/big-market-dcc/config/downgradeSwitch/test/a";

        if (zookeeperClient.checkExists().forPath(path) == null){
            zookeeperClient.create().creatingParentContainersIfNeeded().forPath(path);
        }
    }

    @Test
    public void setData() throws Exception {
        zookeeperClient.setData().forPath("/big-market-dcc/config/downgradeSwitch/test/a", "gzc".getBytes(StandardCharsets.UTF_8));
    }
    @Test
    public void getData() throws Exception {
        String data = new String(zookeeperClient.getData().forPath("/big-market-dcc/config/downgradeSwitch/test/a"), StandardCharsets.UTF_8);
        log.info("res: {}" , data);
    }

}
