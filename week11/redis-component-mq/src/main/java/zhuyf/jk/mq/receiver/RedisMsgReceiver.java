package zhuyf.jk.mq.receiver;

/**
 * InterfaceName: RedisMsgReceiver. <br/>
 * Description: 接受消息接口类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public interface RedisMsgReceiver {

	/**
	 * 接受消息
	 * @param msg 消息内容
	 */
	public void accept(String msg);
}
