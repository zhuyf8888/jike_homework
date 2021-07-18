package zhuyf.jk.mq.config.redis;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * ClassName: RedisMessageListenerContainer. <br/>
 * Description: redis消息监听容器. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisMessageQueueListenerContainer implements InitializingBean, DisposableBean {
	
	/**
     * 日志
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());  
    
    /**
     * 消息主题监听线程池名称
     */
    private static final String MSG_TOPIC_LISTENER_THREAD_POOL_NAME = "msg-topic-listener-pool";
    
    /**
     * 消息主题处理线程池名称
     */
    private static final String MSG_TOPIC_HANDLER_THREAD_POOL_NAME = "msg-topic-handler-pool";
    
    /**
     * 缺省的线程数量
     */
    private static final int DEFAULT_THREAD_COUNT = 10;
    
    /**
     * 缺省的处理线程池队列数量
     */
    private static final int DEFAULT_THREAD_QUEUE_COUNT = 500;
    
    /**
     * 初始化状态
     */
    private volatile boolean initialized = false;
    
    /**
     * 运行状态
     */
 	private volatile boolean running = false; 	
    
    /**
     * 消息主题列表
     */
    private Set<String> topicSet = Sets.newConcurrentHashSet();
    
    /**
     * 主题消息监听线程池
     */
    private ExecutorService topicListenerThreadExecutor;
    
    /**
     * 消息处理线程池
     */
    private Map<String,ExecutorService> handleMessageThreadExecutorMap =  Maps.newConcurrentMap();
    
    /**
     * 主题与消息监听器map
     */
	private Map<String,RedisQueueListener> redisQueueListenerMap = Maps.newConcurrentMap();   
	
	/**
	 * 消息主题key序列化
	 */
	private Map<String,byte[]> msgTopicKeyMap = Maps.newConcurrentMap();
	
	/**
	 * redis队列操作工具类
	 */
	private RedisQueueHandle redisQueueHandle;
    
	@Override
	public void afterPropertiesSet() throws Exception {	
		
		if(redisQueueHandle == null) {
			logger.warn("未设置StringRedisTemplate模板对象");
			return;
		}
		
		// 检测消息主题
		if(topicSet.isEmpty()) {
			logger.warn("未监听任何消息主题！");
			return;
		}
		
		// 检测消息主题监听器
		if(redisQueueListenerMap.isEmpty()) {
			logger.warn("未配置消息监听器RedisQueueListener！");
			return;
		}
		
		// 检测消息处理监听器
		if(handleMessageThreadExecutorMap.isEmpty()) {
			logger.warn("初始化消息处理线程池失败！");
			return;
		}
		
		// 初始化消息主题key
		for(String key : topicSet) {
			byte[] keybyte = redisQueueHandle.getRedisTemplate().getStringSerializer().serialize(key);
			msgTopicKeyMap.put(key, keybyte);
		}
		
		// 初始化消息主题监听线程池
		if(topicListenerThreadExecutor == null) {			
			topicListenerThreadExecutor = ThreadPoolUtil.createFixedThreadPool(MSG_TOPIC_LISTENER_THREAD_POOL_NAME, topicSet.size());
		}
		
		initialized = true;
		start();
	}
	
	@Override
	public void destroy() throws Exception {
		initialized = false;	
		stop();
	}
	
	/**
	 * 设置redis访问工具模板对象
	 * @param redisQueueHandle
	 */
	public void setRedisQueueHandle(RedisQueueHandle redisQueueHandle) {
		this.redisQueueHandle = redisQueueHandle;
	}
	
	/**
	 * 获取redis访问工具模板对象
	 */
	public RedisQueueHandle getRedisQueueHandle() {
		return this.redisQueueHandle;
	}
		
	/**
     * 添加消息主题监听器
     * @param msgTopic 消息主题
     * @param listener 消息监听器
     * @param threadCounts 消息处理线程池数量，默认每个主题消息处理线程为10
     */
    public void addListener(String msgTopic,RedisQueueListener listener,Integer threadCounts) {
    	if(threadCounts == null || threadCounts <= 0) {
    		threadCounts = DEFAULT_THREAD_COUNT;
    	}
    	redisQueueListenerMap.put(msgTopic, listener);
    	topicSet.add(msgTopic);    	
    	ExecutorService listenerThreadPool = ThreadPoolUtil.createFixedThreadPool(MSG_TOPIC_HANDLER_THREAD_POOL_NAME, threadCounts);
    	handleMessageThreadExecutorMap.put(msgTopic, listenerThreadPool);
    }
    
    /**
     * 停止redis队列连接池
     */
    public void stop() {

		if (isRunning()) {
			running = false;
			// 停止消息主题监听线程池
			if(topicListenerThreadExecutor != null) {
				topicListenerThreadExecutor.shutdown();
			}
			// 停止消息主题处理线程池
			if(!handleMessageThreadExecutorMap.isEmpty()) {
				for(Entry<String, ExecutorService> entry : handleMessageThreadExecutorMap.entrySet()) {
					if(entry.getValue() != null) {
						entry.getValue().shutdown();
					}
				}
			}
			
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Stopped QueueRedisMessageListenerContainer");
		}
	}
    
    /**
     * 启动redis队列连接，监听消息
     */
    public void start() {
    	
    	if(!this.initialized) {
    		logger.warn("QueueRedisMessageListenerContainer初始化未完成！");
    		return;
    	}
    	
    	// 启动消息监听器
    	for(String msgTopic : topicSet) {
    		
    		// 获取消息主题的序列化对象
    		byte[] msgTopicKey = msgTopicKeyMap.get(msgTopic);
    		if(msgTopicKey == null) {
    			logger.warn("消息主题：{}未序列化！",msgTopic);
        		continue;
    		}
    		
    		// 监听消息
    		final TopicListenerThread topicListenerThread = new TopicListenerThread(msgTopic,msgTopicKey,true,this);
    		topicListenerThreadExecutor.execute(topicListenerThread);
    		
    	}
    	
    	this.running = true;    	
    } 
      
    
    /**
     * 消息处理
     * @param topic 消息主题
     * @param msg 消息内容
     */
    public void onMessage(String topic,final String msg) {
    	
    	// 获取消息主题对应的处理线程池对象
    	ExecutorService executorService = handleMessageThreadExecutorMap.get(topic);
    	if(executorService == null) {
    		logger.warn("redis消息处理线程池未设置！");
    		return;
    	}
    	
    	// 获取消息主题的监听回调器
    	final RedisQueueListener redisQueueListener = redisQueueListenerMap.get(topic);
    	// 消息处理线程池执行回调消息任务
    	executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				redisQueueListener.onMessage(msg);
			}
		});
    }
    
    /**
     * 检测消息接受端队列是否已满，如果当前消息消费过慢，则暂停消费消息，直到队列足够空闲
     * @param topic 主题名称
     * @return
     */
    public boolean checkOnMessageFlag(String topic) {
    	
    	// 获取消息主题对应的处理线程池对象
    	ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)handleMessageThreadExecutorMap.get(topic);
    	if(threadPoolExecutor == null) {
    		logger.warn("主题[{}]redis消息处理线程池未设置！",topic);
    		return false;
    	}
    	
    	// 当前排队线程数
    	int queueSize = threadPoolExecutor.getQueue().size();
    	logger.debug("redis消息处理线程池等待数量：{}",queueSize);
    	if(queueSize >= DEFAULT_THREAD_QUEUE_COUNT) {
    		return false;
    	}
    	
    	return true;
    }
    
    public boolean isRunning() {
		return running;
	}  
    
    public Map<String, byte[]> getMsgTopicKeyMap() {
		return msgTopicKeyMap;
	}

	public void setMsgTopicKeyMap(Map<String, byte[]> msgTopicKeyMap) {
		this.msgTopicKeyMap = msgTopicKeyMap;
	}  

    
}
