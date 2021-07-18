package zhuyf.jk.mq.config.redis;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

/**
 * ClassName: RedisQueueListener. <br/>
 * Description: 消息监听接口类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisQueueListener {

	/**
	 * 消息接受者对象
	 */
	private Object delegate;
	
	/**
	 * 调用方法对象
	 */
	private Method method;
	
	/**
	 * 自定义队列监听器构造方法
	 * @param delegate 接受者对象
	 * @param method 接受者调用方法
	 */
	public RedisQueueListener(Object delegate,String methodName) {
		this.delegate = delegate;
		this.method = ReflectionUtils.findMethod(delegate.getClass(), methodName,String.class);
	}
	
	/**
	 * 监听获取消息
	 * @param message 消息内容
	 */
	public void onMessage(String message) {
		ReflectionUtils.invokeMethod(this.method, this.delegate, new Object[] {message});
	}
}
