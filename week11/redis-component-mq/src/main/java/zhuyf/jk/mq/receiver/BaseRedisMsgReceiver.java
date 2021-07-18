package zhuyf.jk.mq.receiver;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import zhuyf.jk.mq.bean.RedisMessage;
import zhuyf.jk.mq.common.RedisMqConstant;
import zhuyf.jk.mq.config.RedisMqProperties;
import zhuyf.jk.mq.exception.RedisMsgException;
import zhuyf.jk.mq.manage.RedisMessageManage;
import zhuyf.jk.mq.sender.RedisMsgSender;
import zhuyf.jk.mq.util.ThreadLocalHolder;

/**
 * ClassName: BaseRedisMsgReceiver <br/>
 * Description: 消息接受基础对象.<br/>
 * Date: 2021年7月18日 <br/>
 *
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public abstract class BaseRedisMsgReceiver implements RedisMsgReceiver {
	
	/**
     *  日志
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     *  消息管理对象
     */
    @Autowired
	private RedisMessageManage messageManage;
    
    /**
     *  消息属性配置对象
     */
    @Autowired
    private RedisMqProperties mqProperties;
    
	@Override
	public void accept(String msg) {

		// 消息体判空
		if(StringUtils.isBlank(msg)) {
			logger.warn("接受的消息为空！");
			return;
		}
		
		// 转换消息对象
		RedisMessage<?> msgObj = null;
		try {		
			msgObj = (RedisMessage<?>)JSON.parseObject(msg, this.getMessageType());
		} catch (Exception e) {
			logger.warn("转换JSON消息发生异常",e);
			return;
		}
		
		// 检测消息对象
		if(msgObj == null) {
			logger.warn("转换的接受消息对象为空指针！");
			return;
		}
		
		if(msgObj.getContent() == null) {
			logger.warn("接受的消息内容为空，消息主题：{}",msgObj.getMsgTopic());
			return;
		}
		
		// 处理消息对象
		boolean isRetry = false;
		try {
			// 设置当前线程的消息对象
			ThreadLocalHolder.setContext(msgObj.getContent());
			this.consume(msgObj.getContent());
		} catch (RedisMsgException e) {
			logger.warn("处理消息发生异常，消息主题：{}，消息内容：{},异常信息：{}",msgObj.getMsgTopic(),msg,e);	
			isRetry = true;
		} finally {
			// 清空当前线程的消息对象
			ThreadLocalHolder.clearContext();
		}
		
		if(isRetry) {
			this.retry(msgObj);
		}
	}
	
	/**
	 * 获取消息对象的实际class类型
	 * @return
	 */
	protected abstract Type getMessageType();

	/**
	 * 处理消息
	 * @param obj 消费对象
	 * @throws RedisMsgException 异常对象
	 */
	protected abstract void consume(Object obj) throws RedisMsgException;

	/**
	 * 获取消息生产者对象
	 * @return 返回消息发送对象
	 */
	protected abstract RedisMsgSender getProduceMsg();
	
	/**
	 * 注册消息主题
	 * @param msgTopic 消息主题
	 * @param acceptMsg 接受对象
	 */
	protected void reg(String msgTopic, RedisMsgReceiver acceptMsg) {
		messageManage.regAcceptMsg(msgTopic, acceptMsg);
	}
	
	/**
	 * 1.检测消息是否满足重试次数
	 * 2.如果满足，则重新发送消息
	 * @param msgObj 消息对象
	 */
	protected void retry(RedisMessage<?> msgObj) {
		
		if(msgObj == null)  {
			return;
		}
		
		// 获取消息重试次数配置
		Integer retryCount = mqProperties.getTopicRetryCountInt(msgObj.getMsgTopic());
		if(retryCount == null || retryCount < RedisMqConstant.MQ_RETRY_MIN_COUNT) {
			return;
		}
		
		// 判断消息重试次数是否达到上限,如果未达到上限，则重新发送消息
		if(retryCount.intValue() > msgObj.getRetryCount()) {
			msgObj.setRetryCount(msgObj.getRetryCount()+ RedisMqConstant.MQ_RETRY_MIN_COUNT);
			try {
				getProduceMsg().produce(msgObj);
				logger.info("消息主题：{}，业务处理失败，已经重试发送消息！",msgObj.getMsgTopic());
			} catch (RedisMsgException e) {
				logger.warn("消息主题：{}重试发送异常！",msgObj.getMsgTopic(),e);
			}
		}
	}
	
}
