package com.gzc.types.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RateLimiterAccessInterceptor {


    String key() default "all";

    double permitsPerSecond();

    double limit2blacklist() default 0;

    String fallbackMethod();

}
