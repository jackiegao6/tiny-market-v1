package com.gzc.config;

import com.gzc.types.annotation.DCCValue;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class DCCValueBeanFactory implements BeanPostProcessor {

    private static final String BASE_CONFIG_PATH = "/big-market-dcc";
    private static final String BASE_CONFIG_PATH_CONFIG = BASE_CONFIG_PATH + "/config";

    private final CuratorFramework zookeeperClient;
    private final Map<String, Object> dccObjMap = new HashMap<>();

    public DCCValueBeanFactory(CuratorFramework zookeeperClient) throws Exception {
        this.zookeeperClient = zookeeperClient;

        if (zookeeperClient.checkExists().forPath(BASE_CONFIG_PATH_CONFIG) == null){
            zookeeperClient.create().creatingParentsIfNeeded().forPath(BASE_CONFIG_PATH_CONFIG);
        }

        CuratorCache curatorCache = CuratorCache.build(zookeeperClient, BASE_CONFIG_PATH_CONFIG);
        curatorCache.start();

        curatorCache.listenable().addListener((type, oldData, newData)->{
            switch (type){
                case NODE_CHANGED:
                    String dccValuePathConfig = newData.getPath();
                    Object objBean = dccObjMap.get(dccValuePathConfig);
                    if (objBean == null) return;

                    try{
                        Field field = objBean.getClass().getDeclaredField(dccValuePathConfig.substring(dccValuePathConfig.lastIndexOf("/") + 1));
                        field.setAccessible(true);
                        field.set(objBean, newData.getData());
                        field.setAccessible(false);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DCCValue.class))
                continue;

            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            String value = dccValue.value();
            if (StringUtils.isBlank(value)) {
                throw new AppException("dcc value is not be configured");
            }
            String[] split = value.split(":");
            String key = split[0];
            String defaultValue = split.length == 2 ? split[1] : null;

            String keyPath = BASE_CONFIG_PATH_CONFIG.concat("/").concat(key);
            try {
                if (zookeeperClient.checkExists().forPath(keyPath) == null){
                    zookeeperClient.create().creatingParentsIfNeeded().forPath(keyPath);
                }
                if (!StringUtils.isBlank(defaultValue)){
                    field.setAccessible(true);
                    field.set(bean, defaultValue);
                    field.setAccessible(false);
                }else {
                    defaultValue = new String(zookeeperClient.getData().forPath(keyPath));
                    field.setAccessible(true);
                    field.set(bean, defaultValue);
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            dccObjMap.put(keyPath, bean);
        }
        return bean;
    }
}
