package com.bfxy.rabbit.task.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.bfxy.rabbit.task.autoconfigure.JobParserAutoConfigurartion;

/**
 * 启动注解，主项目的application需要添加次注解，才能启动该组件
 *
 * 创建一个注解的步骤：1、@interface；2、添加元注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
/**
 * 让该组件的启动类注入spring容器
 */
@Import(JobParserAutoConfigurartion.class)
public @interface EnableElasticJob {

}
