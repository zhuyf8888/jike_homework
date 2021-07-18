package zhuyf.jk.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: RedisLock <br/>
 * Description: 分布式锁注解.如果使用了该注解，则必须加锁成功，才能执行后续流程。加锁时，默认取第一个参数作为加锁的key值 .<br/>
 * 如果加锁的key前缀为空，则默认使用类名+方法名作为前缀.<br/>
 * Date: 2021年7月18日 <br/>
 *
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

	/**
     * 需要加锁的key前缀
     * @return
     */
	String lockPre() default "";
	
	/**
     * 需要加锁的key数组
     * @return
     */
	String[] lockArgs();
	
	/**
	 * 设置过期时间，单位毫秒，默认3秒
	 * @return
	 */
	int expireTime() default 3000;
	
	/**
	 * 设置等待超时时间，单位毫秒,默认30秒
	 * @return
	 */
	int overTime() default 30000;
	
}
