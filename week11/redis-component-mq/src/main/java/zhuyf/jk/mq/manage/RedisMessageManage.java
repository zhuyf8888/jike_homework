package zhuyf.jk.mq.manage;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import zhuyf.jk.mq.receiver.RedisMsgReceiver;

/**
 * ClassName: RedisMessageManage. <br/>
 * Description: 消息管理类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
@Component
public class RedisMessageManage {
	
	/**
     * 日志
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 消息接受者集合对象   
	 */
	private Map<String, RedisMsgReceiver> acceptMsgMap = Maps.newHashMap();
	   	   
    /** 
     * 注册消息接受对象
     * @param msgTopic 消息主题
     * @param acceptMsg 消接受者对象
     */
	public void regAcceptMsg(String msgTopic, RedisMsgReceiver acceptMsg) {
		
		if(StringUtils.isBlank(msgTopic) || acceptMsg == null) {
			logger.error("消息接受者注册主题:{}失败",msgTopic);
			return;
		}
		
		acceptMsgMap.put(msgTopic, acceptMsg);
	}
	   
	public Map<String, RedisMsgReceiver> getAcceptMsgMap() {
		return acceptMsgMap;
	}	
	
}
