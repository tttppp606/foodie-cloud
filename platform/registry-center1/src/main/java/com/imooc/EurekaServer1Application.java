package com.imooc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer/* 启动后加载eureka相关jar包*/
public class EurekaServer1Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(EurekaServer1Application.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
