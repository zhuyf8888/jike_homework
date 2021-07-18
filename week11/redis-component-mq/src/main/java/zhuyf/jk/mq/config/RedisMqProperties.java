package zhuyf.jk.mq.config;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import zhuyf.jk.mq.common.RedisMqConstant;

/**
 * ClassName: RedisMqProperties <br/>
 * Description: 消息属性配置. <br/>
 * Date: 2021年07月18日 上午10:18:03 <br/>
 *
 * @author zyf
 * @version 1.0.0
 * @since 1.7
 */
@Component
@ConfigurationProperties(prefix = "redis.mq")
public class RedisMqProperties {
	
	/**
	 * 消息主题线程数配置，默认为default#10
	 */
	private String topicThreadCounts;
	
	/**
	 * 消息主题线程数配置map
	 */
	private Map<String,Integer> topicThreadCountsMap = Maps.newHashMap();

	/**
	 * 消息处理失败后重试次数配置，默认为default#10
	 */
	private String topicRetryCounts;
	
	/**
	 *消息主题线程数配置map
	 */
	private Map<String,Integer> topicRetryCountsMap = Maps.newHashMap();

	public String getTopicThreadCounts() {
		return topicThreadCounts;
	}

	public void setTopicThreadCounts(String topicThreadCounts) {
		this.topicThreadCounts = topicThreadCounts;
		topicThreadCountsMap.putAll(this.resolveTopicConf(topicThreadCounts));
	}

	public String getTopicRetryCounts() {
		return topicRetryCounts;
	}

	public void setTopicRetryCounts(String topicRetryCounts) {
		this.topicRetryCounts = topicRetryCounts;
		topicRetryCountsMap.putAll(this.resolveTopicConf(topicRetryCounts));
	}	
	
	/**
	 * 获取主题处理线程数
	 * @param topic 消息主题
	 * @return
	 */
	public Integer getTopicThreadCountInt(String topic) {
		if(topicThreadCountsMap.containsKey(topic)) {
			return topicThreadCountsMap.get(topic);
		} else {
			return topicThreadCountsMap.get(RedisMqConstant.MQ_DEFAULT_KEY);
		}
	}
	
	/**
	 * 获取主题重试次数
	 * @param topic 消息主题
	 * @return
	 */
	public Integer getTopicRetryCountInt(String topic) {
		if(topicRetryCountsMap.containsKey(topic)) {
			return topicRetryCountsMap.get(topic);
		} else {
			return topicRetryCountsMap.get(RedisMqConstant.MQ_DEFAULT_KEY);
		}
	}
	
	/**
	 * 解析主题配置
	 * @param topicConf 主题配置
	 * @return
	 */
	private Map<String,Integer> resolveTopicConf(String topicConf) {
		
		// 如果配置为空，则直接返回，采用系统默认
		if(StringUtils.isBlank(topicConf)) {
			return null;
		}
		
		// 解析主题配置字符串
		Map<String,Integer> topicConfMap = Maps.newHashMap();
		String[] topicConfArray = topicConf.split(RedisMqConstant.MQ_TOPIC_OUT_SPLIT);
		for(String topicConfItem : topicConfArray) {
			if(StringUtils.isBlank(topicConfItem)) {
				continue;
			}
			String[] topicConfItemArray = topicConfItem.split(RedisMqConstant.MQ_TOPIC_IN_SPLIT);
			if(topicConfItemArray.length == RedisMqConstant.MQ_TOPIC_THREAD_SPLIT_COUNT) {
				topicConfMap.put(topicConfItemArray[0], Integer.parseInt(topicConfItemArray[1]));
			}			
		}
		return topicConfMap;
	}

}
