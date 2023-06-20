package com.imooc.springcloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 用于gateway的过滤器限流，生成KeyResolver和RedisRateLimiter
 *
 * 1、KeyResolver指明以什么标准限流：可以是hostname，也可以是IP等
 *
 * 2、RedisRateLimiter指明令牌的指标
 *
 */
@Configuration
@Slf4j
public class RedisLimiterConfiguration {

//    @Bean
//    public KeyResolver HostNameKeyResolver() {
//        return exchange -> (
//            Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())
//        );
//    }

//    @Bean
//    public KeyResolver HostNameKeyResolver() {
//        return exchange -> {
//            String hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
//            return Mono.just(hostAddress);
//        };
//    }

    @Bean
    //不同模块可以定义不同的KeyResolver，可以是根据客户IP，也可以根据用户名来生成key
    @Primary
    public KeyResolver HostNameKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                String hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                return Mono.just(hostAddress);
            }
        };
    }

    @Bean("redisLimiterUser")
    @Primary
    public RedisRateLimiter redisLimiterUser() {
        //第一个参数是每秒多少令牌，第二个参数是最大多少令牌
        return new RedisRateLimiter(1, 5);
    }

    @Bean("redisLimiterItem")
    public RedisRateLimiter redisLimiterItem() {
        return new RedisRateLimiter(20, 50);
    }
}