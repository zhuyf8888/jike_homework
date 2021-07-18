package zhuyf.jk.mq.sender;

import zhuyf.jk.mq.bean.RedisMessage;
import zhuyf.jk.mq.exception.RedisMsgException;

/**
 * InterfaceName: RedisMsgSender. <br/>
 * Description: 生产消息接口类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public interface RedisMsgSender {

	/**
	 * 生产消息
	 * @param msg 消息对象
	 * @param <T>
	 * @throws RedisMsgException 消息异常对象
	 */
    public <T> void produce(RedisMessage<T> msg) throws RedisMsgException;

}
