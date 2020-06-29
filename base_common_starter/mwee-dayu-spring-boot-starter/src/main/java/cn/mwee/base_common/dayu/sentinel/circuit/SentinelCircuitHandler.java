package cn.mwee.base_common.dayu.sentinel.circuit;

import cn.mwee.base_common.dayu.consts.DayuConst;
import cn.mwee.base_common.utils.error.MwExceptionUtil;
import cn.mwee.base_common.utils.error.MwThrowableUtil;
import cn.mwee.base_common.utils.log4j2.MwLogger;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.timgroup.statsd.StatsDClient;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Created by liaomengge on 2019/10/30.
 */
@AllArgsConstructor
public class SentinelCircuitHandler {

    private static final Logger logger = MwLogger.getInstance(SentinelCircuitHandler.class);

    private StatsDClient statsDClient;

    public <R> R doHandle(String resource, SentinelCircuitBreaker<R> circuitBreaker) {
        if (StringUtils.isBlank(resource)) {
            return circuitBreaker.execute();
        }
        R result;
        Entry sentinelEntry = null;
        try {
            sentinelEntry = SphU.entry(resource, EntryType.OUT);
            result = circuitBreaker.execute();
        } catch (BlockException e) {
            result = handleBlockException(resource, circuitBreaker, e);
        } catch (Throwable t) {
            logger.warn("Resource[{}], request sentinel circuit handle failed ==> {}", resource,
                    MwThrowableUtil.getStackTrace(t));
            Tracer.trace(t);
            throw t;
        } finally {
            Optional.ofNullable(sentinelEntry).ifPresent(val -> val.exit());
        }
        return result;
    }

    private <R> R handleBlockException(String resource, SentinelCircuitBreaker<R> circuitBreaker, BlockException e) {
        if (e instanceof DegradeException || MwExceptionUtil.unwrap(e) instanceof DegradeException) {
            Optional.ofNullable(statsDClient).ifPresent(val -> statsDClient.increment(DayuConst.METRIC_SENTINEL_FALLBACK_PREFIX + resource));
            return circuitBreaker.fallback();
        }
        Optional.ofNullable(statsDClient).ifPresent(val -> statsDClient.increment(DayuConst.METRIC_SENTINEL_BLOCKED_PREFIX + resource));
        return circuitBreaker.block();
    }
}
