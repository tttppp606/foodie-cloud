package com.imooc.item.service.impl;

import com.imooc.item.mapper.ItemsMapperCustom;
import com.imooc.pojo.DecreaseStock;
import com.imooc.utils.JsonUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Component
public class MqReceiveDecreaseStock {

    @Autowired
    private ItemsMapperCustom itemsMapperCustom;


    /**
     * @RabbitListener,@QueueBinding,@Queue,@Exchange
     * @RabbitHandler的组合使用
     * @param message
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
                    value = @Queue(value = "queue-decreaseStock",durable = "true"),
                    exchange = @Exchange(name = "exchange-decreaseStock",
                    durable = "true",
                    type = "topic",
                    ignoreDeclarationExceptions = "true"),
                    key = "routingKey.decreaseStock"
                    )
    )
    @RabbitHandler
    public void onMessage(Message message, Channel channel)throws Exception{
        //1、接受消息，进行业务处理
        System.err.println("------------------消费端消息为：" + message.getPayload());
        String payload = (String)message.getPayload();
        DecreaseStock decreaseStock = JsonUtils.jsonToPojo(payload, DecreaseStock.class);
        String specId = decreaseStock.getItemSpecId();
        Integer buyCounts = decreaseStock.getBuyCounts();

        int result = itemsMapperCustom.decreaseItemSpecStock(specId, buyCounts);
        if (result != 1) {
            throw new RuntimeException("订单创建失败，原因：库存不足!");
        }


        //2、处理成功以后获取delivery_tag，并进行手工签收ack操作，因为配置文件中定义了
        //spring.rabbitmq.listener.simple.acknowledge-mode=manual
        Long deliveryTag = (Long)message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag,false);


    }


}
