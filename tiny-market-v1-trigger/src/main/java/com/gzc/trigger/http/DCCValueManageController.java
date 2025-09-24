package com.gzc.trigger.http;

import com.gzc.api.IDCCValueManageController;
import com.gzc.api.response.Response;
import com.gzc.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/raffle/activity")
public class DCCValueManageController implements IDCCValueManageController {

    @Autowired(required = false)
    private CuratorFramework zookeeperClient;

    private static final String BASE_CONFIG_PATH = "/big-market-dcc";
    private static final String BASE_CONFIG_PATH_CONFIG = BASE_CONFIG_PATH + "/config";

    /**
     * 更新配置
     * <p>
     */
    @RequestMapping(value = "/update_config", method = RequestMethod.GET)
    @Override
    public Response<Boolean> updateDCCValue(@RequestParam String key, @RequestParam String value) {
        try {
            log.info("DCC 动态配置值变更开始 key:{} value:{}", key, value);
            String keyPath = BASE_CONFIG_PATH_CONFIG.concat("/").concat(key);
            if (null == zookeeperClient.checkExists().forPath(keyPath)) {
                zookeeperClient.create().creatingParentsIfNeeded().forPath(keyPath);
                log.info("DCC 节点监听 base node {} not absent create new done!", keyPath);
            }
            Stat stat = zookeeperClient.setData().forPath(keyPath, value.getBytes(StandardCharsets.UTF_8));
            log.info("DCC 动态配置值变更完成 key:{} value:{} time:{}", key, value, stat.getCtime());
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("DCC 动态配置值变更失败 key:{} value:{}", key, value, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
