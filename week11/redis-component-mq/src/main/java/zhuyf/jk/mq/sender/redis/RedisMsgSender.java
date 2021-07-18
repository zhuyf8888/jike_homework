package zhuyf.jk.mq.sender.redis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import zhuyf.jk.mq.bean.RedisMessage;
import zhuyf.jk.mq.config.redis.RedisQueueHandle;
import zhuyf.jk.mq.exception.RedisMsgException;

/**
 * ClassName: RedisMsgSender. <br/>
 * Description: 生产消息实现类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
@Component
public class RedisMsgSender implements zhuyf.jk.mq.sender.RedisMsgSender {
	
	/**
     * 日志
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RedisQueueHandle redisQueueHandle;

	@Override
	public <T> void produce(RedisMessage<T> msg) throws RedisMsgException {

		// 如果消息对象为空，则不做处理
		if(msg == null) {
			logger.warn("消息对象为空！");
			return;
		}
		
		// 设置消息创建时间
		if(msg.getCreateTime() < 1) {
			msg.setCreateTime(System.currentTimeMillis());
		}
		// 转换消息对象为json字符串
		String jsonMsg = JSON.toJSONString(msg);
		// 发送redis消息
		this.sendMessage(msg.getMsgTopic(), jsonMsg);		
	}
	
	/**
	 * 发送redis消息
	 * @param msgTopic 消息主题
	 * @param msg 消息内容
	 * @throws RedisMsgException 消息异常
	 */
	public void sendMessage(String msgTopic,String msg) throws RedisMsgException {
		
		// 判断消息是否为空，如果为空，则不做处理
		if(StringUtils.isBlank(msgTopic) || StringUtils.isBlank(msg)) {
			logger.warn("消息主题或内容为空,消息主题：{},消息内容：{}",msgTopic,msg);
			return;
		}
		
        try {
        	redisQueueHandle.pushFromHead(msgTopic, msg);
        } catch (Exception e) {
            throw new RedisMsgException("发送redis消息发生异常", e);
        }
    }
	
}
