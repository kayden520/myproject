package com.zzjr.mq.rocketmq.exceptions;

/**
 * Created by lenovo on 2016/8/3.
 */
public class RocketMQClientException extends RuntimeException {
	private static final long serialVersionUID = 5755356574640041094L;


	public RocketMQClientException() {
	}


	public RocketMQClientException(String message) {
		super(message);
	}


	public RocketMQClientException(Throwable cause) {
		super(cause);
	}


	public RocketMQClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
