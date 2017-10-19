package com.zzjr.mq.rocketmq.message;

import com.alibaba.rocketmq.common.message.Message;
import com.zzjr.mq.rocketmq.constant.DelayTimeLevel;

/**
 * Created by lenovo on 2016/8/18.
 */
public interface DelayMessageInterface<T> {

	Message getInstance(T t, DelayTimeLevel timeLevel);
}
