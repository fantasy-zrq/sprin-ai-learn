package com.zrq.cn.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author zrq
 * 2026/1/19 19:38
 */
@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<Long,Object> ragStatusCache(){
        return Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(4096)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
