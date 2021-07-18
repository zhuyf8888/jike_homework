package zhuyf.jk.mq.exception;
/**
 * ClassName: RedisMsgException <br/>
 * Description: 消息异常类.<br/>
 * Date: 2021年7月18日 <br/>
 *
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisMsgException extends Exception {

	/**
	 * 错误编码
	 */
	private String code;
	
	/**
	 * 构造方法
	 * @param message
	 */
	public RedisMsgException(final String code, final String message) {
		super(message);
		this.code = code;
	}
	
	/**
	 * 构造方法
	 * @param message
	 */
	public RedisMsgException(final String message) {
		super(message);
	}

	/**
	 * 构造方法
	 * @param cause
	 */
	public RedisMsgException(final Throwable cause) {
		super(cause);
	}

	/**
	 * 构造方法
	 * @param message
	 * @param cause
	 */
	public RedisMsgException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
