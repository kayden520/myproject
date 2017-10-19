package com.zzjr.mq.rocketmq.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.zzjr.mq.rocketmq.message.impl.DefaultMessage;
import com.zzjr.mq.rocketmq.producer.DefaultProducer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Description: 使用线程池的方式发送mq消息
 *
 * @author <a href="mailto:maoling@zuozh.com">zhuhouji</a>
 * @date Create on 2016/9/11
 * @copyright Copyright 2015 ZZJR All Rights Reserved.
 * @since 1.0.3
 */
public class CameraMQClient extends SpringBeanAutowiringSupport {
    private static final Logger logger = LogManager.getLogger(CameraMQClient.class);
    /**
     * 消息bean后缀
     */
    private static final String MESSAGE = "Message";
    /**
     * 消息实体bean后缀匹配模式
     */
    private static final Pattern PATTERN_ENTITY_BEAN_NAME = Pattern.compile("(?<=[^A-Z]+)([A-Z]+[a-z]*$)|$");

    /**
     * 初始化线程池大小为20个
     */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private DefaultProducer defaultProducerFactory;

    private static CameraMQClient instance;

    private CameraMQClient(){
    }

    public static CameraMQClient getInstance(){
        if (instance == null) {
            instance = new CameraMQClient();
        }
        return instance;
    }

    /**
     * 发送消息到MQ.
     * <p>
     * 默认忽略异常
     * </p>
     *
     * @param entity 消息实体对象
     * @param <T>    消息实体类型
     *
     * @return 发送结果
     */
    public static <T> void send(T entity){
        send(entity, MQExceptionStrategies.IGNORE_EXCEPTION);
    }

    /**
     * 发送消息到MQ.
     *
     * @param entity   消息实体对象.
     * @param strategy 异常处理策略
     * @param <T>      消息实体类型
     *
     * @return 发送结果
     */
    public static <T> void send(T entity, MQExceptionStrategy strategy){
        send(getInstance().defaultProducerFactory, entity, strategy);
    }

    /**
     * 发送消息到MQ.
     *
     * @param producer 消息生产者
     * @param entity   消息实体对象.
     * @param <T>      消息实体类型
     *
     * @return 发送结果
     */
    public static <T> void send(DefaultProducer producer, T entity){
        send(producer, entity, MQExceptionStrategies.IGNORE_EXCEPTION);
    }

    /**
     * 发送消息到MQ.
     *
     * @param producer 消息生产者
     * @param entity   消息实体对象.
     * @param strategy 异常处理策略
     * @param <T>      消息实体类型
     *
     * @return 发送结果
     */
    public static <T> void send(DefaultProducer producer, T entity, MQExceptionStrategy strategy){

        final DefaultProducer defaultProducer = producer;
        final T flnalEntity = entity;
        final MQExceptionStrategy mQExceptionStrategy = strategy;
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run(){
                try {
                    String messageBeanName = getMessageBeanName(flnalEntity.getClass().getSimpleName());
                    DefaultMessage<T> defaultMessage = getInstance().beanFactory.getBean(messageBeanName,DefaultMessage.class);
                    Message message = defaultMessage.getInstance(flnalEntity);
                    SendResult result = defaultProducer.getProducer().send(message);
                    if (result.getSendStatus() != SendStatus.SEND_OK) {
                        logger.error(
                            "Send message failed!Send result:{}.Message:{}.",
                            JSON.toJSONString(result),
                            JSON.toJSONString(flnalEntity)
                        );
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "Send result:{}.Message:{}.",
                            JSON.toJSONString(result),
                            JSON.toJSONString(flnalEntity)
                        );
                    }
                } catch (Exception e) {
                    logger.error(String.format("Send message failed!Message:%s.", JSON.toJSONString(flnalEntity)), e);
                    mQExceptionStrategy.handle(e);
                }
            }
        });
    }

    /**
     * 获取Spring管理的message的bean名称.
     *
     * @param entityBeanName 消息实体的简单类名称
     *
     * @return message的bean名称
     */
    private static String getMessageBeanName(String entityBeanName){
        return StringUtils.uncapitalize(PATTERN_ENTITY_BEAN_NAME.matcher(entityBeanName).replaceFirst(MESSAGE));
    }

}
