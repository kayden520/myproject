package com.zzjr.mq.rocketmq.consumer;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.zzjr.mq.rocketmq.bean.Action;
import com.zzjr.mq.rocketmq.bean.ConsumeContext;
import com.zzjr.mq.rocketmq.bean.Subscription;
import com.zzjr.mq.rocketmq.exceptions.RocketMQClientException;
import com.zzjr.mq.rocketmq.messageListeners.MessageListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费者bean
 *
 * @author <a href="mailto:shenchenbo@zuozh.com">Shen.Chenbo</a>
 * @since JDK 1.6 Created on 2016年7月8日 Copyright 2016 ZZJR All Rights Reserved.
 */
public class DefaultConsumer {

	protected final Logger consumerLOG = LogManager.getLogger(DefaultConsumer.class);

	private final ConcurrentHashMap<String/* Topic */, MessageListener> subscribeTable = new ConcurrentHashMap<String, MessageListener>();
	private DefaultMQPushConsumer consumer;

	private final String namesrv;
	private final String producerGroupName;
	private final String instanceName;
	private final Map<Subscription, MessageListener> subscriptionTable;
	private final int consumeMessageBatchMaxSize;                                                       // 每次消费时拉取得消息条数
	private final int consumeThreadMin;                                                                // 最小线程数
	private final int consumeThreadMax;                                                                 // 最大线程数

	private final static int DEFAULT_CONSUMEMESSAGEBATCHMAXSIZE = 1;
	private final static int DEFAULT_CONSUMETHREADMIN = 20;
	private final static int DEFAULT_CONSUMETHREADMAX = 64;

	public DefaultConsumer(String namesrv, String producerGroupName, String instanceName,
						   Map<Subscription, MessageListener> subscriptionTable) {
		this(namesrv, producerGroupName, instanceName, subscriptionTable, DEFAULT_CONSUMEMESSAGEBATCHMAXSIZE,
				DEFAULT_CONSUMETHREADMIN, DEFAULT_CONSUMETHREADMAX);
	}

	public DefaultConsumer(String namesrv, String producerGroupName, String instanceName,
						   Map<Subscription, MessageListener> subscriptionTable,
						   int consumeMessageBatchMaxSize) {
		this(namesrv, producerGroupName, instanceName, subscriptionTable, consumeMessageBatchMaxSize,
				DEFAULT_CONSUMETHREADMIN, DEFAULT_CONSUMETHREADMAX);
	}

	public DefaultConsumer(String namesrv, String producerGroupName, String instanceName,
						   Map<Subscription, MessageListener> subscriptionTable, int consumeThreadMin,
						   int consumeThreadMax) {
		this(namesrv, producerGroupName, instanceName, subscriptionTable, DEFAULT_CONSUMEMESSAGEBATCHMAXSIZE,
				consumeThreadMin, consumeThreadMax);
	}

	public DefaultConsumer(String namesrv, String producerGroupName, String instanceName,
						   Map<Subscription, MessageListener> subscriptionTable,
						   int consumeMessageBatchMaxSize, int consumeThreadMin, int consumeThreadMax) {
		this.namesrv = namesrv;
		this.producerGroupName = producerGroupName;
		this.instanceName = instanceName;
		this.subscriptionTable = subscriptionTable;
		this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
		this.consumeThreadMin = consumeThreadMin;
		this.consumeThreadMax = consumeThreadMax;
	}

	public void init() throws MQClientException {
		consumerLOG.info("start DefaultMQProducer initialize! producerGroupName:{}, namesrv:{}", producerGroupName,
				namesrv);

		if (null == namesrv || null == producerGroupName || null == instanceName || null == subscriptionTable) {
			throw new RuntimeException("properties not set");
		}

		Iterator<Map.Entry<Subscription, MessageListener>> it = this.subscriptionTable.entrySet().iterator();

		consumer = new DefaultMQPushConsumer(producerGroupName);
		consumer.setNamesrvAddr(namesrv);
		consumer.setInstanceName(instanceName);
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.setMessageModel(MessageModel.CLUSTERING);
		consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
		consumer.setConsumeThreadMin(consumeThreadMin);
		consumer.setConsumeThreadMax(consumeThreadMax);

		while (it.hasNext()) {
			Map.Entry<Subscription, MessageListener> next = it.next();
			consumer.subscribe(next.getKey().getTopic(), next.getKey().getExpression());
			this.subscribeTable.put(next.getKey().getTopic(), next.getValue());
		}
		consumer.registerMessageListener(new MessageListenerImpl());
		consumer.start();
		consumerLOG.info("the DefaultMQProducer start success!");
	}

	public void destroy() {
		consumerLOG.info("start DefaultMQProducer shutdown!");

		consumer.shutdown();

		consumerLOG.info("DefaultMQProducer shutdown success!");
	}

	class MessageListenerImpl implements MessageListenerConcurrently {

		@Override
		public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext contextExt) {
			MessageExt msg = msgs.get(0);
			MessageListener listener = DefaultConsumer.this.subscribeTable.get(msg.getTopic());
			if (null == listener) {
				throw new RocketMQClientException("MessageListener is null");
			}

			final ConsumeContext context = new ConsumeContext();
			Action action = listener.consume(msg, context);
			if (action != null) {
				switch (action) {
					case CommitMessage:
						return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
					case ReconsumeLater:
						return ConsumeConcurrentlyStatus.RECONSUME_LATER;
					default:
						break;
				}
			}
			return null;
		}
	}

}
