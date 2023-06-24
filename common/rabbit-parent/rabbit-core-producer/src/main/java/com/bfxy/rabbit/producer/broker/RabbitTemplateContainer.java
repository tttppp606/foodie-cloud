package com.bfxy.rabbit.producer.broker;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.bfxy.rabbit.api.Message;
import com.bfxy.rabbit.api.MessageType;
import com.bfxy.rabbit.api.exception.MessageRunTimeException;
import com.bfxy.rabbit.common.convert.GenericMessageConverter;
import com.bfxy.rabbit.common.convert.RabbitMessageConverter;
import com.bfxy.rabbit.common.serializer.Serializer;
import com.bfxy.rabbit.common.serializer.SerializerFactory;
import com.bfxy.rabbit.common.serializer.impl.JacksonSerializerFactory;
import com.bfxy.rabbit.producer.service.MessageStoreService;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * 	$RabbitTemplateContainer池化封装
 * 	每一个topic 对应一个RabbitTemplate（因为一种topic一种路由规则）
 *	1.	提高发送的效率，不是原来的单例模式，依靠@Autowired
 * 	2. 	可以根据不同的需求制定化不同的RabbitTemplate, 比如每一个topic 都有自己的routingKey规则
 * @author Alienware
 */
@Slf4j
@Component
public class RabbitTemplateContainer implements RabbitTemplate.ConfirmCallback {

	/**
	 * mynote:优雅的注释
	 */
	private Map<String /* TOPIC */, RabbitTemplate> rabbitMap = Maps.newConcurrentMap();

	private Splitter splitter = Splitter.on("#");
	
	private SerializerFactory serializerFactory = JacksonSerializerFactory.INSTANCE;

	/**
	 * mynote:onnectionFactory：开放一个用于连接RabbitMQ节点的工厂类。也用于大多数连接和套接字配置。
	 */
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@Autowired
	private MessageStoreService messageStoreService;

	/**
	 * 背景：利用@Autowired注入的单例------>从池化的rabbitTemplate中获取rabbitTemplate
	 * 作用：如果rabbitMap中的存在key对应的RabbitTemplate，就返回RabbitTemplate，否者就创建一个
	 * @param message
	 * @return
	 * @throws MessageRunTimeException
	 */
	public RabbitTemplate getTemplate(Message message) throws MessageRunTimeException {
		Preconditions.checkNotNull(message);
		String topic = message.getTopic();
		RabbitTemplate rabbitTemplate = rabbitMap.get(topic);
		if(rabbitTemplate != null) {
			return rabbitTemplate;
		}
		log.info("#RabbitTemplateContainer.getTemplate# topic: {} is not exists, create one", topic);

		// mynote： connectionFactory为AMQ协议的连接，可用于创建RabbitTemplate，
		//          配置的连接信息如ip，端口，账户，密码等均包含在connectionFactory里
		RabbitTemplate newTemplate = new RabbitTemplate(connectionFactory);
		// todo ？？topic做Exchangge
		newTemplate.setExchange(topic);
		newTemplate.setRoutingKey(message.getRoutingKey());
		// mynote：增加里重试功能
		newTemplate.setRetryTemplate(new RetryTemplate());
		
		//	添加序列化反序列化和converter对象
		Serializer serializer = serializerFactory.create();
		GenericMessageConverter gmc = new GenericMessageConverter(serializer);
		RabbitMessageConverter rmc = new RabbitMessageConverter(gmc);
		newTemplate.setMessageConverter(rmc);
		
		String messageType = message.getMessageType();
		if(!MessageType.RAPID.equals(messageType)) {
			//mynote： 本类继承类RabbitTemplate.ConfirmCallback接口，并且实现类方法confirm，如下
			//         只要不是迅速消息RAPID，就可以把new的tempalte设置有回调函数（实现ConfirmCallback接口的类）
			newTemplate.setConfirmCallback(this);
		}

		//mynote：可以返回value的put方法
		rabbitMap.putIfAbsent(topic, newTemplate);
		
		return rabbitMap.get(topic);
	}

	/**
	 * 	无论是 confirm 消息 还是 reliant 消息 ，发送消息以后 broker都会去回调confirm
	 */
	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		// 	具体的消息应答
		/**
		 * 第三方工具，splitter.splitToList：用于已某种分隔符将String转变为list
		 * 需要定义分隔符是什么：	private Splitter splitter = Splitter.on("#");
		 */
		List<String> strings = splitter.splitToList(correlationData.getId());
		String messageId = strings.get(0);
		//string转化为long
		long sendTime = Long.parseLong(strings.get(1));
		String messageType = strings.get(2);
		if(ack) {
			//	当Broker 返回ACK成功时, 就是更新一下日志表里对应的消息发送状态为 SEND_OK
			// 	如果当前消息类型为reliant 我们就去数据库查找并进行更新
			if(MessageType.RELIANT.endsWith(messageType)) {
				this.messageStoreService.succuess(messageId);
			}
			log.info("send message is OK, confirm messageId: {}, sendTime: {}", messageId, sendTime);
		} else {
			log.error("send message is Fail, confirm messageId: {}, sendTime: {}", messageId, sendTime);
			
		}
	}
}
