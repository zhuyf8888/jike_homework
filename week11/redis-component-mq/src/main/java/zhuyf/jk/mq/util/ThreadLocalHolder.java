package zhuyf.jk.mq.util;

import org.springframework.util.Assert;

/**
 * ClassName: ThreadLocalHolder <br/>
 * Description: 线程池管理工具类.<br/>
 * Date: 2021年7月18日 <br/>
 *
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class ThreadLocalHolder {

	/**
	 * 线程对象
	 */
	private static final ThreadLocal<Object> OBJ_HOLDER = new ThreadLocal<Object>();
	
	/**
	 * 清空当前线程的对象
	 */
	public static void clearContext() {
		OBJ_HOLDER.remove();
	}

	/**
	 * 获取当前线程的对象
	 * @return
	 */
	public static Object getContext() {
		return OBJ_HOLDER.get();
	}

	/**
	 * 设置当前线程的对象
	 * @param context
	 */
	public static void setContext(Object context) {
		Assert.notNull(context, "Only non-null LoginUserInfo instances are permitted");
		OBJ_HOLDER.set(context);
	}
		
}
