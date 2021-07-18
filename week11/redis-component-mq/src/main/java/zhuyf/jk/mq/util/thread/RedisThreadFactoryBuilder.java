package zhuyf.jk.mq.util.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ClassName: RedisThreadFactoryBuilder <br/>
 * Description:自定义线程工厂生成类.<br/>
 * Date: 2021年07月18日 <br/>
 *
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisThreadFactoryBuilder {

	/**
	 * 线程名称前缀
	 */
	private String namePrefix = null;
	
	/**
	 * 是否为守护线程
	 */
    private boolean daemon = false;
    
    /**
     * 线程优先级
     */
    private int priority = Thread.NORM_PRIORITY;

    public RedisThreadFactoryBuilder setNamePrefix(String namePrefix) {
        if (namePrefix == null) {
            throw new NullPointerException();
        }
        this.namePrefix = namePrefix;
        return this;
    }

    public RedisThreadFactoryBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * 设置线程执行优先级
     * @param priority
     * @return
     */
    public RedisThreadFactoryBuilder setPriority(int priority) {
        if (priority < Thread.MIN_PRIORITY){
            throw new IllegalArgumentException(String.format(
                    "Thread priority (%s) must be >= %s", priority, Thread.MIN_PRIORITY));
        }

        if (priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(String.format(
                    "Thread priority (%s) must be <= %s", priority, Thread.MAX_PRIORITY));
        }

        this.priority = priority;
        return this;
    }

    public ThreadFactory build() {
        return build(this);
    }

    /**
     * 构造一个线程池工厂对象
     * @param builder
     * @return
     */
    private static ThreadFactory build(RedisThreadFactoryBuilder builder) {
        final String namePrefix = builder.namePrefix;
        final Boolean daemon = builder.daemon;
        final Integer priority = builder.priority;
        final AtomicLong count = new AtomicLong(0);
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                if (namePrefix != null) {
                    thread.setName(namePrefix + "-" + count.getAndIncrement());
                }
                if (daemon != null) {
                    thread.setDaemon(daemon);
                }
                if (priority != null) {
                    thread.setPriority(priority);
                }
                return thread;
            }
        };
    }
}
