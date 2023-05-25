package com.imooc.order;


import com.imooc.item.service.ItemService;
import com.imooc.order.fallback.itemservice.ItemCommentsFeignClient;
import com.imooc.user.service.AddressService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
// 扫描 mybatis 通用 mapper 所在的包
@MapperScan(basePackages = "com.imooc.order.mapper")
// 扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
@EnableDiscoveryClient
@EnableScheduling
/**
 * 1、feign的调用者才使用；
 * 2、要调用哪里的接口，就扫描basePackages哪里的包或者配置clients哪里的类
 */
@EnableFeignClients(clients = {
        ItemCommentsFeignClient.class,
        AddressService.class,
        ItemService.class
}
//        basePackages = {
//        "com.imooc.user.service",
//        "com.imooc.item.service",
//        "com.imooc.order.fallback.itemservice"}
)
@EnableHystrix
public class OrderApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
