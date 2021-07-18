package zhuyf.jk.lock;

/**
 * InterfaceName: DistributedLock. <br/>
 * Description: 分布式锁接口类. <br/>
 * Date: 2021年7月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public interface DistributedLock {	
 
    /**
     * 加锁控制
     * @param key 加锁的键
     * @param requestId 加锁的键值
     * @param expire 过期时间
     * @return
     */
    public boolean setLock(String key,String requestId,long expire);
 
    /**
     * 获取加锁的键值
     * @param key 加锁的键
     * @return
     */
    public String get(String key);
 
    /**
     * 解锁
     * @param key 解锁的key
     * @param requestId 解锁的请求值ID
     * @return
     */
    public boolean releaseLock(String key,String requestId);
 
}

