package com.imooc.springcloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

@Configuration
@Slf4j
public class RoutsConfiguration {

    @Autowired
    private KeyResolver hostNameResolver;

    @Autowired
    @Qualifier("redisLimiterUser")
    private RateLimiter rateLimiterUser;

    @Bean/* 不要遗忘*/
    @Order
    public RouteLocator customizedRoutes(RouteLocatorBuilder/*上下文中会加载的*/ builder){
        return builder.routes()
                .route(r -> r.path("/address/**","/passport/**","/userInfo/**","/center/**")   //user模块controller对客户提供的服务，而service层的服务都是服务之间调用，不走网关
                                .filters(f -> f.requestRateLimiter(c -> {             // 请求过滤器限流
                                    c.setKeyResolver(hostNameResolver);
                                    c.setRateLimiter(rateLimiterUser);
                                    c.setStatusCode(HttpStatus.BAD_GATEWAY);
                                }))
                        .uri("lb://FOODIE-USER-SERVICE")//与注册中心的一致，包括大小写
                )
                .route(r -> r.path("/items/**")   //user模块controller对客户提供的服务，而service层的服务都是服务之间调用，不走网关
                        .uri("lb://FOODIE-ITEM-SERVICE")
                )
                .route(r -> r.path("/orders/**","/myorders/**","/mycomments/**")   //user模块controller对客户提供的服务，而service层的服务都是服务之间调用，不走网关
                        .uri("lb://FOODIE-ORDER-SERVICE")
                )
                .route(r -> r.path("/shopcart/**")   //user模块controller对客户提供的服务，而service层的服务都是服务之间调用，不走网关
                        .uri("lb://FOODIE-CART-SERVICE")
                )
                .build();
    }
}
