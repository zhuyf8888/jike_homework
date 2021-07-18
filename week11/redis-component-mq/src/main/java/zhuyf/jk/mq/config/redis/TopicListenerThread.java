package zhuyf.jk.mq.config.redis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassName: TopicListenerThread. <br/>
 * Description: redis消息监听线程. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class TopicListenerThread implements Runnable {
	
	/**
     * 日志
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 监听的消息主题
	 */
	private byte[] msgTopicKey;
	
	/**
	 * 监听的消息主题
	 */
	private String msgTopic;
	
	/**
	 * 是否阻塞
	 */
	private Boolean blockFlag;
	
	/**
	 * 消息监听容器
	 */
	private RedisMessageQueueListenerContainer container;
	
	/**
	 * redis队列操作工具类
	 */
	private RedisQueueHandle redisQueueHandle;
	
	/**
	 * 构造消息主题监听方法
	 * @param msgTopic 消息主题
	 * @param msgTopicKey 消息主题序列化对象
	 * @param blockFlag 阻塞标识
	 * @param container 消息监听容器
	 */
	public TopicListenerThread(String msgTopic,byte[] msgTopicKey,Boolean blockFlag,RedisMessageQueueListenerContainer container) {
		this.blockFlag = blockFlag;
		this.msgTopic = msgTopic;
		this.msgTopicKey = msgTopicKey;
		this.container = container;
		this.redisQueueHandle = container.getRedisQueueHandle();
	}
	
	/**
	 * 非阻塞模式获取主题消息
	 */
	private void takeByNoblock() {
		
        while(true){  
        	
        	if(container.checkOnMessageFlag(msgTopic)) {
        		// 获取对应主题的消息
            	String msg = redisQueueHandle.takeFromTail(msgTopic);
            	if(StringUtils.isNotBlank(msg)) {
            		container.onMessage(msgTopic,msg);
            		continue;
            	}
        	}        	
        	
        	// 睡眠降低cpu占用
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("获取redis队列的消息睡眠异常，takeByNoblock",e);
			}
            
        }  
        
	}
	
	/**
	 * 阻塞模式获取主题消息
	 */
	private void takeByBlock() {
		
		while(true){ 
			
			if(container.checkOnMessageFlag(msgTopic)) {
				logger.debug("开始监听消息主题：{}",msgTopic);
	        	// 获取对应主题的消息
	        	String msg = redisQueueHandle.takeFromTail(msgTopicKey);
	        	logger.debug("接受到消息：{}",msg);
	        	if(StringUtils.isNotBlank(msg)) {
	        		container.onMessage(msgTopic,msg);
	        		continue;
	        	}
			}			
        	
        	// 睡眠降低cpu占用
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("获取redis队列的消息睡眠异常，takeByBlock",e);
			}
        } 
	}
	
	@Override
	public void run() {			
		if(blockFlag) {
			takeByBlock();
		} else {
			takeByNoblock();
		}
	}
} 
