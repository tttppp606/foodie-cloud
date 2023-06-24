# rabbit-parent

包含ES组件的封装，可以实现消息的可靠性发送

1、依赖引入

        <dependency>
            <groupId>com.bfxy.base.rabbit</groupId>
            <artifactId>rabbit-core-producer</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

2、注入

    @Autowired
    private ProducerClient producerClient;

5、配置文件

spring.rabbitmq.addresses=43.154.106.172:5672,43.154.166.55:5672,43.154.33.30:5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin
spring.rabbitmq.virtual-host=/
spring.rabbitmq.connection-timeout=15000
spring.rabbitmq.publisher-confirms=true
spring.rabbitmq.publisher-returns=true
spring.rabbitmq.template.mandatory=true
spring.rabbitmq.listener.simple.auto-startup=false

// 分布式定时任务配置
elastic.job.zk.serverLists=43.154.87.31:2181
elastic.job.zk.namespace=elastic-job

// 消息入库状态跟踪的配置
rabbit.producer.druid.type=com.alibaba.druid.pool.DruidDataSource
rabbit.producer.druid.jdbc.url=jdbc:mysql://43.154.87.31:3306/broker_message?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true
rabbit.producer.druid.jdbc.driver-class-name=com.mysql.cj.jdbc.Driver
rabbit.producer.druid.jdbc.username=root
rabbit.producer.druid.jdbc.password=1990425Lc
rabbit.producer.druid.jdbc.initialSize=5
rabbit.producer.druid.jdbc.minIdle=1
rabbit.producer.druid.jdbc.maxActive=100
rabbit.producer.druid.jdbc.maxWait=60000
rabbit.producer.druid.jdbc.timeBetweenEvictionRunsMillis=60000
rabbit.producer.druid.jdbc.minEvictableIdleTimeMillis=300000
rabbit.producer.druid.jdbc.validationQuery=SELECT 1 FROM DUAL
rabbit.producer.druid.jdbc.testWhileIdle=true
rabbit.producer.druid.jdbc.testOnBorrow=false
rabbit.producer.druid.jdbc.testOnReturn=false
rabbit.producer.druid.jdbc.poolPreparedStatements=true
rabbit.producer.druid.jdbc.maxPoolPreparedStatementPerConnectionSize= 20
rabbit.producer.druid.jdbc.filters=stat,wall,log4j
rabbit.producer.druid.jdbc.useGlobalDataSourceStat=true

4、编写方法-发送

        String uniqueId = UUID.randomUUID().toString();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("itemSpecId", itemSpecId);
        attributes.put("buyCounts", buyCounts);
        Message message = new Message(uniqueId, "exchange-1",
                "routingKey.abc",
                attributes,
                0);

        producerClient.send(message);
