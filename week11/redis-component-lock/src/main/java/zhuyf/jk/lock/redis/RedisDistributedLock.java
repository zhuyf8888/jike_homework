package zhuyf.jk.lock.redis;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import zhuyf.jk.lock.DistributedLock;

/**
 * ClassName: RedisDistributedLock. <br/>
 * Description: redis分布式锁操作类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
@Component
public class RedisDistributedLock implements DistributedLock {
	
	/**
     * 日志
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
    /**
     * redis操作模板类
     */
    @Autowired
    private StringRedisTemplate redisTemplate;
 
    /**
     * 解锁脚本
     */
    public static final String UNLOCK_LUA;
    
    /**
     * redis设置key方式，不存在则设置
     */
    private static final String SET_IF_NOT_EXIST = "NX";
    
    /**
     * redis设置key的过期时间
     */
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    
    /**
     * 解锁lua脚本参数
     */
    private static final String EMPTY_STR = "";
 
    /**
     * 初始化解锁的lua脚本
     */
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }
  
    /**
     * 加锁控制
     * @param key 加锁的键
     * @param requestId 加锁的键值
     * @param expire 过期时间
     * @return
     */
    @Override
    public boolean setLock(final String key,final String requestId,final long expire) {
    	
    	// 设置加锁的键值
        try {        	
        	String result = redisTemplate.execute(new RedisCallback<String>() {        		
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                	 JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                     return commands.set(key, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expire);
                }
            });
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            logger.warn("set redis occured an exception", e);
        }
        return false;
    }
 
    /**
     * 获取加锁的键值
     * @param key 加锁的键
     * @return
     */
    @Override
    public String get(final String key) {
    	
    	// 获取指定锁的键值
        try {
        	String result = redisTemplate.execute(new RedisCallback<String>() {        		
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                	JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                    return commands.get(key);
                }
            });
            return result;
        } catch (Exception e) {
            logger.warn("get redis occured an exception", e);
        }
        return EMPTY_STR;
    }
 
    /**
     * 解锁
     * @param key 解锁的key
     * @param requestId 解锁的请求值ID
     * @return
     */
    @Override
    public boolean releaseLock(String key,String requestId) {
    	
        // 释放锁的时候，有可能因为持锁之后方法执行时间大于锁的有效期，此时有可能已经被另外一个线程持有锁，所以不能直接删除
        try {
        	
        	// 解锁的键和值
            final List<String> keys = Lists.newArrayList(key);
            final List<String> args = Lists.newArrayList(requestId);
 
            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            // spring自带的执行脚本方法中，集群模式直接抛出不支持执行脚本的异常，所以只能拿到原redis的connection来执行脚本
            Long result = redisTemplate.execute(new RedisCallback<Long>() {        		
                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                	// 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                	Object nativeConnection = connection.getNativeConnection();                    
                    // 集群模式
                    if (nativeConnection instanceof JedisCluster) {
                        return (Long)((JedisCluster)nativeConnection).eval(UNLOCK_LUA, keys, args);
                    }     
                    // 单机模式
                    else if (nativeConnection instanceof Jedis) {
                        return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, args);
                    }
                    return 0L;
                }
            });
            return result != null && result > 0;
        } catch (Exception e) {
            logger.warn("release lock occured an exception", e);
        } 
        return false;
    }
 
}

