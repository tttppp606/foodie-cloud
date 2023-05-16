package com.imooc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServer2Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(EurekaServer2Application.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
