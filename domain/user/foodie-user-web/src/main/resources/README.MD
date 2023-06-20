一、本地环境：

[application.yml](application.yml)里改为dev

[bootstrap.properties](bootstrap.properties)

1、Eureka 注册中心(本地)放开

2、spring.cloud.config.profile=dev

redis、mq、mysql都是连接的腾讯云服务器


二、docker环境：

[application.yml](application.yml)里改为prod

[bootstrap.properties](bootstrap.properties)

1、Eureka 注册中心(docker)放开

2、spring.cloud.config.profile=prod

redis、mq、mysql都是连接的docker


