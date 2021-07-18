package zhuyf.jk.lock.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import zhuyf.jk.lock.DistributedLock;
import zhuyf.jk.lock.annotation.RedisLock;
import zhuyf.jk.lock.exception.LockException;

/**
 * ClassName: RedisLockAspect <br/>
 * Description: 分布式加锁拦截器，使用指定的参数拼接作为key.<br/>
 * date: 2021年7月18日 <br/>
 *
 * @author zyf
 * @version 1.0.0
 */
@Aspect
@Order(-10)
@Component
public class RedisLockAspect {

    /**
     * 日志
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * 分布式锁key前缀
     */
    private static final String LOCK_PRE = "redis_lock_";
    
    @Autowired
    private DistributedLock distributedLock;
    
    /**
     * 参数之间分隔符
     */
    public static final String PARAM_PARAM_SP = "_";
    
    /**
     * 参数与值之间分隔符
     */
    public static final String PARAM_VALUE_SP = "=";
    
    /**
     * 循环获取锁的间隔时间毫秒
     */
    public static final int LOOP_SLEEP_TIME = 5;

    /**
     * 定义切面，扫描所有service的实现类
     */
    @Pointcut("@annotation(zhuyf.jk.lock.annotation.RedisLock)")
    public void lockPointcat() {}

    /**
     * 使用环绕通知对service实现类的参数进行检查
     * @param joinPoint
     * @throws Throwable
     */
    @Around("lockPointcat()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

    	if(joinPoint.getArgs() == null) {
    		return joinPoint.proceed();
    	}
    	
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = joinPoint.getTarget().getClass().getMethod(
                methodSignature.getName(), methodSignature.getParameterTypes());
        
        // 查找带有加锁控制的注解
        RedisLock redisLock = targetMethod.getAnnotation(RedisLock.class);
        if (redisLock == null) {
            return joinPoint.proceed();
        }
        
        // 获取第一个参数对象作为加锁的key
        Object param = joinPoint.getArgs()[0];
        if(param == null) {
        	return joinPoint.proceed();
        }

        logger.debug("正在进行对类{}中方法{}进行加锁控制", joinPoint.getTarget().getClass().getName(), targetMethod.getName());        
        
        // 分布式加锁方法前缀
        StringBuilder lockStr = new StringBuilder(LOCK_PRE);
        lockStr.append(joinPoint.getTarget().getClass().getSimpleName());
        lockStr.append(PARAM_PARAM_SP);
        lockStr.append(targetMethod.getName());
        lockStr.append(PARAM_PARAM_SP);
        
        // 获取加锁的key
        String lockKey = null;
        try {
        	lockKey = this.getLockKey(lockStr.toString(),param, redisLock.lockArgs());
        } catch(Exception e) {
        	logger.error("获取加锁的key异常！",e);
        }
        if(StringUtils.isBlank(lockKey)) {
        	throw new LockException("加锁的key为空，退出执行！");
        }
                
        // 加锁的键值
        String requestId = null;
        // 加锁异常标识
        boolean lockExFlag = false;
        try {
	        requestId = this.tryGetLock(lockKey,redisLock.expireTime(),redisLock.overTime());
        } catch(Exception e) {
        	lockExFlag = true;
        	logger.error("加锁发生异常！",e);
        }        
        
        if(!lockExFlag && !StringUtils.isBlank(requestId)) {
        	
            // 加锁成功，执行后续流程
        	try {
        		return joinPoint.proceed();
        	} catch (Exception e) {
        		throw e;
			} finally {
        		// 解锁
        		this.distributedLock.releaseLock(lockKey, requestId);
        	}
        }
        
        throw new LockException("获取加锁失败，退出执行！");
    }
    
    /**
     * 尝试获取锁
     * @param key 加锁的key
     * @param expire 过期时间毫秒
     * @param overtime 超时时间毫秒
     * @return
     */
    private String tryGetLock(String key,int expire,int overtime) {
    	
    	// 加锁的值
		String requestId = null;
		// 超时的时间点
		long endTime = System.currentTimeMillis()+overtime;
    	while(true) {
    		
    		// 判断是否超时
    		if(System.currentTimeMillis() >= endTime) {
    			logger.warn("尝试获取锁超时退出！");
    			break;
    		}
    		
    		// 加锁的值
    		requestId = String.valueOf(System.currentTimeMillis()+expire);
    		boolean falg = distributedLock.setLock(key, requestId, expire);
    		if(falg) {
    			break;
    		}
    		
    		// 清空加锁的值
    		requestId = null;
    		// 睡眠防止cpu持续占用
    		try {
				Thread.sleep(LOOP_SLEEP_TIME);
			} catch (InterruptedException e) {
				logger.warn("加锁睡眠发生异常",e);
			}
    	}
    	
    	return requestId;
    }
    
    /**
     * 获取加锁的key值
     * @param lockPre 分布式锁key前缀
     * @param param
     * @param args
     * @return
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    private String getLockKey(String lockPre,Object param,String[] args) throws Exception {
    	
    	// 加锁的key值
    	StringBuilder lockKey = new StringBuilder(lockPre);
    	// 根据参数生产加锁的key值
    	if (ClassUtils.isPrimitiveOrWrapper(param.getClass()) || param instanceof String) { 
    		
        	// 直接使用arg作为加锁key值
    		lockKey.append(param.toString());
        } else if(args != null && args.length > 0){        	
        	
        	// 遍历arg对象的参数作为加锁key
        	StringBuilder lockKeyTemp = new StringBuilder();
        	for(String arg : args) {
        		Field field = param.getClass().getDeclaredField(arg);
        		field.setAccessible(true);
        		Object val = field.get(param);
        		if(val == null) {
        			continue;
        		}
        		if(lockKeyTemp.length() > 0) {
        			lockKeyTemp.append(PARAM_PARAM_SP);
        		}
        		lockKeyTemp.append(field.getName());
        		lockKeyTemp.append(PARAM_VALUE_SP);
        		lockKeyTemp.append(val.toString());
        	}
        	lockKey.append(lockKeyTemp);
        }
    	
    	return lockKey.toString();
    }
    
    public static void main(String[] args) {
    	System.out.println(System.currentTimeMillis());
    }
    
}
