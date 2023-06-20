package com.imooc.search;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SearchApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SearchApp.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
