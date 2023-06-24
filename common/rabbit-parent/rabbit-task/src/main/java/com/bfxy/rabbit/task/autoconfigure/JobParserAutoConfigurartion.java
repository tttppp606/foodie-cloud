package com.bfxy.rabbit.task.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bfxy.rabbit.task.parser.ElasticJobConfParser;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

import lombok.extern.slf4j.Slf4j;

/**
 * 1、使用@EnableElasticJob注解将此类注入spring容器
 * 2、注入spring的条件是配置文件中zookeeper相关有值
 * 3、注入spring的同时，读取配置文件的注册中心zookeeper数据到编写的实体类JobZookeeperProperties中
 * 4、ZookeeperRegistryCenter第一个bean实现类连接zookeeper
 */
@Slf4j
@Configuration
//当配置文件中存在前缀为elastic.job.zk，后缀为namespace和serverLists时，才会加载这个类
@ConditionalOnProperty(prefix = "elastic.job.zk", name = {"namespace", "serverLists"}, matchIfMissing = false)
//当满足上述条件，加载这个类时，会将配置文件的值读入JobZookeeperProperties这个类中，
//     与JobZookeeperProperties类中的@ConfigurationProperties注解一起使用
@EnableConfigurationProperties(JobZookeeperProperties.class)
public class JobParserAutoConfigurartion {

	/**
	 * 启动zookeeper组件，根据esjob的要求需要将ZookeeperRegistryCenter组入spring中
	 * @param jobZookeeperProperties
	 * @return
	 */
	@Bean(initMethod = "init")
	public ZookeeperRegistryCenter zookeeperRegistryCenter(JobZookeeperProperties jobZookeeperProperties) {
		ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(jobZookeeperProperties.getServerLists(),
				jobZookeeperProperties.getNamespace());
		zkConfig.setBaseSleepTimeMilliseconds(jobZookeeperProperties.getBaseSleepTimeMilliseconds());
		zkConfig.setMaxSleepTimeMilliseconds(jobZookeeperProperties.getMaxSleepTimeMilliseconds());
		zkConfig.setConnectionTimeoutMilliseconds(jobZookeeperProperties.getConnectionTimeoutMilliseconds());
		zkConfig.setSessionTimeoutMilliseconds(jobZookeeperProperties.getSessionTimeoutMilliseconds());
		zkConfig.setMaxRetries(jobZookeeperProperties.getMaxRetries());
		zkConfig.setDigest(jobZookeeperProperties.getDigest());
		log.info("初始化job注册中心配置成功, zkaddress : {}, namespace : {}", jobZookeeperProperties.getServerLists(), jobZookeeperProperties.getNamespace());
		return new ZookeeperRegistryCenter(zkConfig);
	}

	/**
	 * 启动esjob核心类（配置esjob参数并启动）
	 * ElasticJobConfParser是自定义的
	 * @param jobZookeeperProperties
	 * @param zookeeperRegistryCenter
	 * @return
	 */
	@Bean
	public ElasticJobConfParser elasticJobConfParser(JobZookeeperProperties jobZookeeperProperties, ZookeeperRegistryCenter zookeeperRegistryCenter) {
		return new ElasticJobConfParser(jobZookeeperProperties, zookeeperRegistryCenter);
	}
	
}
