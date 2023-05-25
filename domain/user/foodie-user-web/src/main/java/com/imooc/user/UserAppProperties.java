package com.imooc.user;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 从配置文件中拉取的信息中转站
 */
@Configuration
//此类里读取的配置可以动态刷新
@RefreshScope
@Data
public class UserAppProperties {

    @Value("${userservice.registration.disabled}")
    private String disableRegistration;
}
