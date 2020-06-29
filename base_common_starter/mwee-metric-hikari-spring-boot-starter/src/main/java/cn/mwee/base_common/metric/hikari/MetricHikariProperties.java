package cn.mwee.base_common.metric.hikari;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by liaomengge on 2019/7/24.
 */
@Data
@ConfigurationProperties("mwee.metric-hikari")
public class MetricHikariProperties {

    private boolean enabled;
    private long initialDelay = 120L;//单位：秒
    private long statsInterval = 20L;//单位：秒
}
