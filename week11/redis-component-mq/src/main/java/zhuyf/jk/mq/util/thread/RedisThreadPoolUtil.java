package zhuyf.jk.mq.util.thread;

import java.util.concurrent.*;

/**
 * ClassName: RedisThreadPoolUtil. <br/>
 * Description: 线程池工具类. <br/>
 * Date: 2021年07月18日 <br/>
 * 
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
public class RedisThreadPoolUtil {

	/**
	 * 创一个线程池对象
	 * @param threadName 线程名称
	 * @param threadCount 线程数量
	 * @return
	 */
	public static ExecutorService createFixedThreadPool(String threadName,int threadCount) {

		// 定义线程工厂对象
		ThreadFactory customThreadfactory = new RedisThreadFactoryBuilder()
		        .setNamePrefix(threadName).build();
				
		// 定义线程池对象
		ExecutorService executorService = new ThreadPoolExecutor(threadCount, threadCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),customThreadfactory,new ThreadPoolExecutor.AbortPolicy());
		
		return executorService;
	}
}
