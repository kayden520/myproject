package com.zzjr.mq.rocketmq.message.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.common.message.Message;
import com.zzjr.mq.rocketmq.constant.DelayTimeLevel;
import com.zzjr.mq.rocketmq.message.DelayMessageInterface;

/**
 * 延迟消息基础类
 *
 * @author <a href="mailto:shenchenbo@zuozh.com">Shen.Chenbo</a>
 * @since JDK 1.6 Created on 2016年8月18日 Copyright 2016 ZZJR All Rights Reserved.
 */
public class DelayMessage<T> implements DelayMessageInterface<T> {
	private final String topic;
	private final String tags;

	public DelayMessage(String topic, String tags) {
		this.topic = topic;
		this.tags = tags;
	}

	@Override
	public Message getInstance(T t, DelayTimeLevel timeLevel) {
		Message message = new Message(this.topic, this.tags, JSON.toJSONString(t).getBytes());
		message.setDelayTimeLevel(timeLevel.getValue());
		return message;
	}
}
