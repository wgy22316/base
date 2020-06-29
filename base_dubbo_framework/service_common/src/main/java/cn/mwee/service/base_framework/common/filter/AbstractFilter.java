package cn.mwee.service.base_framework.common.filter;

import cn.mwee.base_common.utils.log4j2.MwLogger;
import cn.mwee.service.base_framework.common.config.FilterConfig;
import cn.mwee.service.base_framework.common.config.ServiceConfig;
import com.alibaba.dubbo.rpc.Filter;
import com.timgroup.statsd.StatsDClient;
import lombok.Setter;

/**
 * Created by liaomengge on 17/7/11.
 */
public abstract class AbstractFilter implements Filter {

    protected final static MwLogger logger = MwLogger.getInstance(AbstractFilter.class);

    protected static final String SKIP_METHOD = "ping";

    //坑1:不要用@Autowired注入,拿不到对象,改用setter
    @Setter
    protected ServiceConfig serviceConfig;

    @Setter
    protected FilterConfig filterConfig = new FilterConfig();

    @Setter
    protected StatsDClient statsDClient;

    protected String getMetricsPrefixName() {
        return serviceConfig.getServiceName();
    }
}
