package com.zzjr.mq.rocketmq.messageListeners;

import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;

/**
 * 监听消费类 接口 异步模式 可多线程
 *
 * @author <a href="mailto:shenchenbo@zuozh.com">Shen.Chenbo</a>
 * @since JDK 1.6 Created on 2016年7月8日 Copyright 2016 ZZJR All Rights Reserved.
 */
public interface DefaultConcurrentMessageListener extends MessageListenerConcurrently {
}
