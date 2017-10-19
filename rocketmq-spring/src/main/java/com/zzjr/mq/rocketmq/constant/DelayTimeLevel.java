package com.zzjr.mq.rocketmq.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 延迟枚举类
 *
 * @author <a href="mailto:shenchenbo@zuozh.com">Shen.Chenbo</a>
 * @since JDK 1.6 Created on 2016年8月18日 Copyright 2016 ZZJR All Rights Reserved.
 */
@Getter
@AllArgsConstructor//(access = AccessLevel.PRIVATE)
public enum DelayTimeLevel {
	ONE_SECOND(1, 1), FIVE_SECOND(2, 2), TEN_SECOND(3, 3), THITY_SECOND(4, 4);

	private Integer key;

	private Integer  value;

}
