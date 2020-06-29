package cn.mwee.base_common.helper.retrofit.client.interceptor;

import cn.mwee.base_common.helper.retrofit.consts.RetrofitMetricsConst;
import cn.mwee.base_common.utils.error.MwExceptionUtil;
import cn.mwee.base_common.utils.log4j2.MwLogger;
import cn.mwee.base_common.utils.url.MwMoreUrlUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.timgroup.statsd.StatsDClient;
import lombok.AllArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by liaomengge on 2019/11/5.
 */
@AllArgsConstructor
public class SentinelRetrofitInterceptor implements Interceptor {

    private static final Logger logger = MwLogger.getInstance(SentinelRetrofitInterceptor.class);

    private StatsDClient statsDClient;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response;

        Entry hostWithPathEntry = null;
        String hostWithPathResource = null;
        try {
            String url = this.buildReqUrl(request);
            hostWithPathResource = request.method() + ":" + url;
            hostWithPathEntry = SphU.entry(hostWithPathResource, EntryType.OUT);
            response = chain.proceed(request);
        } catch (BlockException e) {
            if (StringUtils.isNotBlank(hostWithPathResource)) {
                logger.warn("Resource[{}], Retrofit Block Exception...", hostWithPathResource);
                String methodSuffix = MwMoreUrlUtil.getUrlSuffix(hostWithPathResource);
                Optional.ofNullable(statsDClient).ifPresent(val -> statsDClient.increment(methodSuffix + RetrofitMetricsConst.REQ_EXE_BLOCKED));
            }
            throw MwExceptionUtil.unchecked(e);
        } catch (Throwable t) {
            Tracer.trace(t);
            throw t;
        } finally {
            if (hostWithPathEntry != null) {
                hostWithPathEntry.exit();
            }
        }
        return response;
    }

    private String buildReqUrl(Request request) {
        return request.url().toString();
    }
}
