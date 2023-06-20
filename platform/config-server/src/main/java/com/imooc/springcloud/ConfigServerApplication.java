package com.imooc.springcloud;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * 验证：localhost:20008/main/config-consumer-dev.yml
 */
@SpringBootApplication
//eureka服务注册和发现
@EnableDiscoveryClient
//表明是config服务端
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ConfigServerApplication.class).
                web(WebApplicationType.SERVLET).
                run(args);
    }
}
