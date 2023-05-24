package com.imooc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * 此模块是一个单独的模块，与turbine模块、业务模块都没关系。
 * 需要在显示的页面上输入turbine或者业务模块的actuator地址，进行显示
 * http://localhost:port/hystrix
 */
@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashboardApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HystrixDashboardApp.class).
                web(WebApplicationType.SERVLET).run(args);
    }
}
