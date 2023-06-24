package com.imooc.order.test;

import com.bfxy.rabbit.api.MessageType;
import com.bfxy.rabbit.producer.broker.ProducerClient;
import com.imooc.controller.BaseController;
import com.imooc.order.service.OrderService;
import com.imooc.pojo.DecreaseStock;
import com.imooc.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bfxy.rabbit.api.Message;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 测试mq的生产端代码，把代码复制粘贴到这里了
 */
@RequestMapping("orders")
@RestController
public class MqTestController extends BaseController {

    @Autowired
    private RabbitSender rabbitSender;

    @PostMapping("/mq")
    public void decreaseItemSpecStock(@RequestParam String itemSpecId, @RequestParam Integer buyCounts) {
        DecreaseStock decreaseStock = DecreaseStock.builder()
                .ItemSpecId(itemSpecId)
                .buyCounts(buyCounts).build();
        String msg = JsonUtils.objectToJson(decreaseStock);

        //附属信息
        HashMap<String, Object> map = new HashMap<>();
        map.put("exchange","exchange-decreaseStock");
        map.put("routingKey","routingKey.decreaseStock");

        rabbitSender.send(msg,map);
    }

    @Autowired
    private ProducerClient producerClient;

    @PostMapping("/rabbit")
    public void rabbitTest(@RequestParam String itemSpecId, @RequestParam Integer buyCounts) {
        String uniqueId = UUID.randomUUID().toString();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("itemSpecId", itemSpecId);
        attributes.put("buyCounts", buyCounts);
        Message message = new Message(uniqueId, "exchange-1",
                "routingKey.abc",
                attributes,
                0);
        message.setMessageType(MessageType.RELIANT);

        producerClient.send(message);
    }
}
