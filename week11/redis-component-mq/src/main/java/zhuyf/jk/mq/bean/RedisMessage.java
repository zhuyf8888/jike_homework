package zhuyf.jk.mq.bean;

import java.io.Serializable;

/**
 * ClassName: RedisMessage<T>. <br/>
 * Description: 统一消息对象. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisMessage<T> implements Serializable {
	
	/**
	 * 消息ID
	 */
    private String msgId;
    
    /**
     * 消息类型
     */
    private String msgTopic;
    
    /**
     * 重试次数
     */
    private int retryCount;
   
    /**
     * 消息创建时间（时间搓）
     */
    private long createTime;
   
    /**
     * 消息内容（泛型）
     */
    private T content;

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}	

	public String getMsgTopic() {
		return msgTopic;
	}

	public void setMsgTopic(String msgTopic) {
		this.msgTopic = msgTopic;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}
    
}
