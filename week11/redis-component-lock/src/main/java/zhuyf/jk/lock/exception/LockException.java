package zhuyf.jk.lock.exception;
/**
 * ClassName: LockException <br/>
 * Description: 分布式锁异常类.<br/>
 * Date: 2021年7月18日 <br/>
 *
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class LockException extends RuntimeException {

	/**
	 * 错误编码
	 */
	private String code;
	
	/**
	 * 构造方法
	 * @param message
	 */
	public LockException(final String code,final String message) {
		super(message);
		this.code = code;
	}
	
	/**
	 * 构造方法
	 * @param message
	 */
	public LockException(final String message) {
		super(message);
	}

	/**
	 * 构造方法
	 * @param cause
	 */
	public LockException(final Throwable cause) {
		super(cause);
	}

	/**
	 * 构造方法
	 * @param message
	 * @param cause
	 */
	public LockException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
