package zhuyf.jk.mq.common;

/**
 * ClassName: RedisMqConstant. <br/>
 * Description: 基础常量配置. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisMqConstant {
		
	/**
	 * 消息重试最小次数
	 */
    public static final int MQ_RETRY_MIN_COUNT = 1;
    
    /**
	 * 消息主题和线程数分割后的属性个数，如xxx#10，分割后的长度为2
	 */
    public static final int MQ_TOPIC_THREAD_SPLIT_COUNT = 2;
    
    /**
	 * 默认消息主题的key
	 */
    public static final String MQ_DEFAULT_KEY = "default";
	
	/**
	 * 消息主题之间逗号分割符
	 */
    public static final String MQ_TOPIC_OUT_SPLIT = ",";
	
	/**
	 * 消息主题与线程数井号分割符
	 */
    public static final String MQ_TOPIC_IN_SPLIT = "#";
    
    /**
	 * 消息接受者默认的接受方法名
	 */
    public static final String MQ_ACCEPT_METHOD = "accept";
    
    
    
}
