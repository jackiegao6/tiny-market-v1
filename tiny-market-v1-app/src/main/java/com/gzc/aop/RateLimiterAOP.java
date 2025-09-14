package com.gzc.aop;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.gzc.types.annotation.DCCValue;
import com.gzc.types.annotation.RateLimiterAccessInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * RateLimiterAOP 为切面入口，管理所有被添加了 @RateLimiterAccessInterceptor 自定义注解的方法
 * 注意被 AOP 管理的类，会成为代理类。
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAOP {

    /**
     * 个人限频记录1分钟
     * 类型：Cache<String, RateLimiter>
     * 作用：存储某个 key（比如 userId、IP）对应的 RateLimiter。
     * 过期策略：放进去 1 分钟后自动失效，防止内存里堆积太多用户的限流器。
     * 使用场景：限制每个用户 1 分钟内的请求速率。
     */
    private final Cache<String, RateLimiter> loginRecord = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    /**
     * 个人限频黑名单1h - 分布式业务场景，可以记录到 Redis 中
     * 如果某用户违规次数超过阈值（blacklistCount），就视为进入黑名单，拦截请求。
     * <p/>
     * 类型：Cache<String, Long>
     * 作用：存储某个 key（用户 ID/IP）对应的 违规次数。
     * 过期策略：计数在 1 小时后失效，相当于“黑名单有效期 1 小时”。
     * 使用场景：如果用户在短时间内多次被限流，就把他“记黑”，后续直接拒绝请求。
     */
    private final Cache<String, Long> blacklist = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    @DCCValue("rateLimiterSwitch:open")
    private String rateLimiterSwitch;

    /**
     * 切点
     * 拦截被 @RateLimiterAccessInterceptor 注解标记的方法
     */
    @Pointcut("@annotation(com.gzc.types.annotation.RateLimiterAccessInterceptor)")
    public void aopPoint(){}

    /**
     * 环绕通知，可以在方法执行前后都加逻辑，还能决定是否继续执行原方法
     * @param joinPoint
     * @param rateLimiterAccessInterceptor
     * @return
     * @throws Throwable
     */
    @Around("aopPoint() && @annotation(rateLimiterAccessInterceptor)")
    public Object doRouter(ProceedingJoinPoint joinPoint, RateLimiterAccessInterceptor rateLimiterAccessInterceptor) throws Throwable {

        // 0. 限流开关【open 开启、close 关闭】关闭后，不会走限流策略
        if (StringUtils.isBlank(rateLimiterSwitch) || "close".equals(rateLimiterSwitch)) {
            return joinPoint.proceed();
        }

        String key = rateLimiterAccessInterceptor.key();
        if (StringUtils.isBlank(key)){
            throw new RuntimeException("userId is null");
        }

        // 获取拦截字段
        String keyAttr = getAttrValue(key, joinPoint.getArgs());
        log.info("aop attr: {}", keyAttr);

        // 黑名单拦截
        if (!"all".equals(keyAttr) && rateLimiterAccessInterceptor.limit2blacklist() != 0 && null != blacklist.getIfPresent(keyAttr) && blacklist.getIfPresent(keyAttr) > rateLimiterAccessInterceptor.limit2blacklist()) {
            log.info("限流-黑名单拦截(1h)：{}", keyAttr);
            return fallbackMethodResult(joinPoint, rateLimiterAccessInterceptor.fallbackMethod());
        }

        // 获取限流 -> Guava 缓存1分钟
        RateLimiter rateLimiter = loginRecord.getIfPresent(keyAttr);
        if (rateLimiter == null){
            rateLimiter = RateLimiter.create(rateLimiterAccessInterceptor.permitsPerSecond());
            loginRecord.put(keyAttr, rateLimiter);
        }

        // 限流拦截
        if (!rateLimiter.tryAcquire()){
            // 没拿到令牌
            log.info("限流-超频次拦截：{}", keyAttr);

            if (rateLimiterAccessInterceptor.limit2blacklist() != 0){
                // 准备加入黑名单
                if (blacklist.getIfPresent(keyAttr) == null){
                    blacklist.put(keyAttr, 1L);
                }else {
                    blacklist.put(keyAttr, blacklist.getIfPresent(keyAttr) + 1L);
                }
            }
            return fallbackMethodResult(joinPoint, rateLimiterAccessInterceptor.fallbackMethod());
        }

        return joinPoint.proceed();
    }


    /**
     * 调用用户配置的回调方法，当拦截后，返回回调结果。
     */
    private Object fallbackMethodResult(JoinPoint jp, String fallbackMethod) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        Method method = jp.getTarget().getClass().getMethod(fallbackMethod, methodSignature.getParameterTypes());
        return method.invoke(jp.getThis(), jp.getArgs());
    }

    /**
     * 实际根据自身业务调整，主要是为了获取通过某个值做拦截
     */
    public String getAttrValue(String attr, Object[] args) {
        if (args[0] instanceof String) {
            return args[0].toString();
        }
        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                // filedValue = BeanUtils.getProperty(arg, attr);
                // fix: 使用lombok时，uId这种字段的get方法与idea生成的get方法不同，会导致获取不到属性值，改成反射获取解决
                filedValue = String.valueOf(this.getValueByName(arg, attr));
            } catch (Exception e) {
                log.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }

    /**
     * 获取对象的特定属性值
     *
     * @param item 对象
     * @param name 属性名
     * @return 属性值
     * @author tang
     */
    private Object getValueByName(Object item, String name) {
        try {
            Field field = getFieldByName(item, name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(item);
            field.setAccessible(false);
            return o;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
     *
     * @param item 对象
     * @param name 属性名
     * @return 该属性对应方法
     * @author tang
     */
    private Field getFieldByName(Object item, String name) {
        try {
            Field field;
            try {
                field = item.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

}
