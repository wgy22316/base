package cn.mwee.base_common.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by liaomengge on 2019/5/6.
 */
@Data
@ConfigurationProperties(prefix = "mwee.mq")
public class MQProperties {

    private String type;
}
