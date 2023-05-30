//package com.imooc.springcloud.filter;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Component
//@Slf4j
//public class AuthFilter implements GlobalFilter, Ordered {
//
//    private static final String TOKEN = "token";
//    private static final String USERNAME = "lichuang";
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        log.info("Auth start");
//        HttpHeaders header = exchange.getRequest().getHeaders();
//        String token = header.getFirst(TOKEN);
//        String username = header.getFirst(USERNAME);
//
//        ServerHttpResponse response = exchange.getResponse();
//        if (StringUtils.isBlank(token)) {
//            log.error("token not found");
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
//        }
//
//        AuthResponse resp = authService.verify(token, username);
//        if (resp.getCode() != 1L) {
//            log.error("invalid token");
//            response.setStatusCode(HttpStatus.FORBIDDEN);
//            return response.setComplete();
//        }
//
//        // TODO 将用户信息存放在请求header中传递给下游业务
//        ServerHttpRequest buildReuqest = exchange.getRequest()
//                .mutate()//获取request的builder
//                .header("imooc-user-name", username)//builder构建内容
//                .build();//生成request
//
//        //todo 如果响应中需要放数据，也可以放在response的header中
//        response.setStatusCode(HttpStatus.OK);
//        response.getHeaders().add("imooc-username",username);
//
//        //把封装的request和response传入下一个filter的chain中
//        //chain.filter------>Contract to allow a WebFilter to delegate to the next in the chain.
//        return chain.filter(exchange.mutate()
//                .request(buildReuqest)
//                .response(response)
//                .build());
//    }
//
//    @Override
//    public int getOrder() {
//        return 0;
//    }
//}
