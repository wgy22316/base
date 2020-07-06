package cn.ly.base_common.graceful.tomcat;

import cn.ly.base_common.graceful.GracefulProperties;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by liaomengge on 2018/12/18.
 */
@ConditionalOnProperty(prefix = "ly.graceful.tomcat", name = "enabled")
@ConditionalOnClass({ApplicationListener.class, Tomcat.class})
public class TomcatAutoConfiguration {

    @Autowired
    private GracefulProperties gracefulProperties;

    @Bean
    public TomcatShutdown tomcatShutdown() {
        return new TomcatShutdown(gracefulProperties);
    }

    @Bean
    public ServletWebServerFactory tomcatCustomizer(TomcatShutdown tomcatShutdown) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(tomcatShutdown);
        return tomcat;
    }
}
