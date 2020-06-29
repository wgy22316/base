package cn.mwee.base_common.health_check.health;

import cn.mwee.base_common.health_check.health.domain.HealthInfo;
import org.apache.commons.collections4.MapUtils;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import java.util.Map;
import java.util.Objects;

/**
 * Created by liaomengge on 2019/7/11.
 */
public class ActiveMQHealthCheck extends HealthCheck {

    @Override
    protected void doHealthCheck(HealthInfo healthInfo) throws Exception {
        Map<String, ConnectionFactory> connectionFactoryMap =
                super.applicationContext.getBeansOfType(ConnectionFactory.class);
        if (MapUtils.isNotEmpty(connectionFactoryMap)) {
            for (Map.Entry<String, ConnectionFactory> entry : connectionFactoryMap.entrySet()) {
                ConnectionFactory connectionFactory = entry.getValue();
                Connection connection = null;
                try {
                    connection = connectionFactory.createConnection();
                    connection.start();
                    healthInfo.withMetaData(connection.getMetaData().getJMSProviderName());
                } finally {
                    if (Objects.nonNull(connection)) {
                        connection.close();
                    }
                }
            }
        }
    }
}
