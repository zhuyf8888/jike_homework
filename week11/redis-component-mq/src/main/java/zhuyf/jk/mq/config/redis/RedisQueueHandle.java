package zhuyf.jk.mq.config.redis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

/**
 * ClassName: RedisQueueHandle. <br/>
 * Description: redis队列操作类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisQueueHandle {
	
	/**
     * 日志
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
		
	/**
	 * redis操作模板工具类
	 */
	private StringRedisTemplate redisTemplate;
	
	/**
	 * redis连接工厂
	 */
	private RedisConnectionFactory factory;
	
	/**
	 * 默认的构造方法
	 */
	public RedisQueueHandle() {}
	
	/**
	 * 构造redis模板的方法
	 * @param redisTemplate redis模板
	 */
	public RedisQueueHandle(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.factory = redisTemplate.getConnectionFactory();
	}
	
	public StringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.factory = redisTemplate.getConnectionFactory();
	}

	public RedisConnectionFactory getFactory() {
		return factory;
	}
	
	/**
	 * 获取队尾数据
	 * @param timeout 超时时间
	 * @param rawKey 消息主题二进制数据
	 * @return
	 */
    public String takeFromTail(int timeout,byte[] rawKey) { 
    	
    	// 消息结果
    	String result = null;
    	RedisConnection connection = null;
    	try {
	    	connection = RedisConnectionUtils.getConnection(factory);
	        List<byte[]> results = connection.bRPop(timeout, rawKey);  
	        if(CollectionUtils.isEmpty(results)){  
	            return null;  
	        }  
	        result = (String)redisTemplate.getValueSerializer().deserialize(results.get(1)); 
    	} catch (Exception e) {
    		logger.warn("通过redis.bRPop获取消息异常",e);
		} finally {
    		RedisConnectionUtils.releaseConnection(connection, factory);
    	}
    	
    	return result;
    }  
      
    /**
     * 获取队尾数据,一直阻塞
     * @param rawKey 消息主题二进制数据
     * @return
     */
    public String takeFromTail(byte[] rawKey) {  
        return takeFromTail(0,rawKey);  
    }  
      
    /**
     * 从队列的头，插入 
     * @param msg消息内容
     */
    public void pushFromHead(String msgTopic,String msg){  
    	redisTemplate.opsForList().leftPush(msgTopic, msg);
    }    
          
    /**
     * 从队尾获取消息
     * @param msgTopic 消息主题
     * @return
     */
    public String takeFromTail(String msgTopic){  
    	return redisTemplate.opsForList().rightPop(msgTopic);
    }  
    	
}
