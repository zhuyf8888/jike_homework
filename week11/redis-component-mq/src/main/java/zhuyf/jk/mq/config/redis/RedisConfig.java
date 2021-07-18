package zhuyf.jk.mq.config.redis;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import zhuyf.jk.mq.common.RedisMqConstant;
import zhuyf.jk.mq.config.RedisMqProperties;
import zhuyf.jk.mq.manage.RedisMessageManage;
import zhuyf.jk.mq.receiver.RedisMsgReceiver;

/**
 * ClassName: RedisConfig. <br/>
 * Description: redis相关配置对象. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
@Configuration
public class RedisConfig {
	
	/**
     *  消息属性配置对象
     */
	@Autowired
	private RedisMqProperties mqProperties;

    /**
     * 创建发布订阅模式连接工厂
     * @param connectionFactory
     * @param messageManage
     * @return
     */
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, RedisMessageManage messageManage) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        Map<String, RedisMsgReceiver> acceptMsgMap = messageManage.getAcceptMsgMap();
        if(acceptMsgMap != null && !acceptMsgMap.isEmpty()) {
        	for(Entry<String, RedisMsgReceiver> entry : acceptMsgMap.entrySet()) {
        		MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(entry.getValue(), "accept");
                container.addMessageListener(messageListenerAdapter, new PatternTopic(entry.getKey()));
        	}
        }
        return container;
    }
    
    /**
     * 创建生产消费者模式连接工厂
     *
     * @param stringRedisTemplate redis操作模板
     * @return
     */
    @Bean
    public RedisMessageQueueListenerContainer container(RedisQueueHandle redisQueueHandle,RedisMessageManage messageManage) {
    	RedisMessageQueueListenerContainer container = new RedisMessageQueueListenerContainer();
        container.setRedisQueueHandle(redisQueueHandle);
        Map<String, RedisMsgReceiver> acceptMsgMap = messageManage.getAcceptMsgMap();
        if(acceptMsgMap != null && !acceptMsgMap.isEmpty()) {
        	for(Entry<String, RedisMsgReceiver> entry : acceptMsgMap.entrySet()) {
        		RedisQueueListener redisQueueListener = new RedisQueueListener(entry.getValue(), RedisMqConstant.MQ_ACCEPT_METHOD);
        		Integer threadCounts = mqProperties.getTopicThreadCountInt(entry.getKey());
        		container.addListener(entry.getKey(), redisQueueListener, threadCounts);
        	}
        }
        return container;
    }
    
    /**
     * 获取redis操作工具对象
     * @param stringRedisTemplate redis操作模板
     * @return
     */
    @Bean
    public RedisQueueHandle getRedisQueueHandle(StringRedisTemplate stringRedisTemplate) {
    	return new RedisQueueHandle(stringRedisTemplate);
    }    
    
}
