package com.zzjr.mq.rocketmq.messageListeners;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.zzjr.mq.rocketmq.bean.Action;
import com.zzjr.mq.rocketmq.bean.ConsumeContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Description: 抽象实现MessageListener.
 *
 * @param <E> 消息体MessageEntity
 * @author <a href="mailto:zhuhouji@zuozh.com">zhuhouji</a>
 * @date Create on 2017/3/16
 * @copyright Copyright 2017 ZZJR All Rights Reserved.
 * @since 1.0.3
 */
public abstract class AbstractMessageListener<E> implements MessageListener {
    protected final Logger logger = LogManager.getLogger(this.getClass());

    private static final String GENERIC_ERROR_FORMAT = "'%s' is not specified generic type parameters!";

    private final Class<E> messageEntityClass;

    public AbstractMessageListener() {
        Class<?> c = this.getClass();
        while (!(AbstractMessageListener.class.equals(c.getSuperclass()))) {
            c = c.getSuperclass();
            if (Object.class.equals(c)) {
                throw new RuntimeException(String.format(GENERIC_ERROR_FORMAT, c));
            }
        }
        Type genType = c.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            throw new RuntimeException(String.format(GENERIC_ERROR_FORMAT, c));
        }
        this.messageEntityClass = (Class<E>) ((ParameterizedType) genType).getActualTypeArguments()[0];
    }

    @Override
    public Action consume(MessageExt messageExt, ConsumeContext consumeContext) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Received message:{}.{}", localeString(messageExt), messageExt.toString());
            }
            // default ThreadLocalCache.getUTF8Decoder()
            E infoEmailEntity = JSON.parseObject(messageExt.getBody(), messageEntityClass);
            boolean isSuccess = handleMessage(infoEmailEntity, messageExt, consumeContext);
            if (!isSuccess) {
                logger.error("Handle the message failure!Message:{}.{}", localeString(messageExt), messageExt.toString());
                return Action.ReconsumeLater;
            }
            return Action.CommitMessage;
        } catch (Exception e) {
            logger.error(String.format("Received the message but process failed!Message:%s.%s", localeString(messageExt), messageExt.toString()), e);
            return Action.ReconsumeLater;
        }
    }

    /**
     * 处理订阅消息.
     *
     * @param messageEntity 从MQ订阅的消息实体.
     * @return boolean 是否成功
     */
    protected abstract boolean handleMessage(E messageEntity, MessageExt messageExt, ConsumeContext consumeContext) throws Exception;

    /**
     * 根据本地环境编码构造字符串.
     *
     * @param messageExt
     * @return
     */
    private static String localeString(MessageExt messageExt) {
        return new String(messageExt.getBody());
    }
}
