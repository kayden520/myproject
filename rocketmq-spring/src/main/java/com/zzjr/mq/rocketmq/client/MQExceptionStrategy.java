package com.zzjr.mq.rocketmq.client;

/**
 * Description: MQ消息发送异常处理策略.
 *
 * @author <a href="mailto:zhuhouji@zuozh.com">zhuhouji</a>
 * @Date Create on 2016/9/13
 * @copyright Copyright 2015 ZZJR All Rights Reserved.
 * @since 1.0.3
 */
public interface MQExceptionStrategy {

    /**
     * 处理异常.
     *
     * @param e 发送MQ消息时抛出的异常
     */
    void handle(Exception e);
}
