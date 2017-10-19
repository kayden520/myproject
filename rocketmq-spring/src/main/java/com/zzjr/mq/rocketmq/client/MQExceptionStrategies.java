package com.zzjr.mq.rocketmq.client;

/**
 * Description: MQ消息发送异常处理策略枚举.
 *
 * @author <a href="mailto:zhuhouji@zuozh.com">zhuhouji</a>
 * @Date Create on 2016/9/13
 * @copyright Copyright 2015 ZZJR All Rights Reserved.
 * @since 1.0.3
 */
public enum MQExceptionStrategies implements MQExceptionStrategy {
    /**
     * 忽略异常
     */
    IGNORE_EXCEPTION {
        @Override
        public void handle(Exception e) {
            // do nothing
        }
    },
    /**
     * 抛出Runtime异常
     */
    THROW_RUNTIME_EXCEPTION {
        @Override
        public void handle(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
