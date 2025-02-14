package com.github.liaomengge.base_common.influx;

import com.github.liaomengge.base_common.influx.consts.InfluxConst;
import com.github.liaomengge.base_common.utils.thread.LyThreadFactoryBuilderUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by liaomengge on 2020/7/21.
 */
@Slf4j
public class InfluxDBConnection {
    
    @Getter
    private InfluxDB influxDB;
    private InfluxDBProperties influxDBProperties;

    @Autowired(required = false)
    private BatchOptions batchOptions;

    public InfluxDBConnection(InfluxDBProperties influxDBProperties) {
        this.influxDBProperties = influxDBProperties;
    }

    public InfluxDBConnection(InfluxDB influxDB, InfluxDBProperties influxDBProperties) {
        this.influxDB = influxDB;
        this.influxDBProperties = influxDBProperties;
    }

    @PostConstruct
    public void init() {
        if (Objects.isNull(influxDB)) {
            try {
                InfluxDBProperties.AdditionalConfig additionalConfig = influxDBProperties.getAdditionalConfig();
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(additionalConfig.getConnectTimeout().getSeconds(), TimeUnit.SECONDS)
                        .readTimeout(additionalConfig.getReadTimeout().getSeconds(), TimeUnit.SECONDS)
                        .writeTimeout(additionalConfig.getWriteTimeout().getSeconds(), TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .connectionPool(new ConnectionPool(additionalConfig.getMaxConnections(), 5L, TimeUnit.MINUTES));
                influxDB = InfluxDBFactory.connect(influxDBProperties.getUrl(), influxDBProperties.getUsername(),
                        influxDBProperties.getPassword(), builder);
            } catch (Exception e) {
                log.error("connect influx db fail", e);
                return;
            }

        }

        try {
            createDatabase(influxDBProperties.getDb());
            influxDB.setDatabase(influxDBProperties.getDb());
        } catch (Exception e) {
            log.error("create influx db fail", e);
            return;
        } finally {
            influxDB.setRetentionPolicy(influxDBProperties.getRetentionPolicy());
            influxDB.setConsistency(ConsistencyLevel.valueOf(influxDBProperties.getConsistencyLevel()));
            influxDB.setLogLevel(influxDBProperties.getLogLevel());
        }

        if (influxDBProperties.isGzipEnabled()) {
            influxDB.enableGzip();
        }

        if (influxDBProperties.isBatchEnabled()) {
            BatchOptions defaultBatchOptions = batchOptions;
            if (Objects.isNull(defaultBatchOptions)) {
                defaultBatchOptions = BatchOptions.DEFAULTS
                        .actions(InfluxConst.DEFAULT_BATCH_ACTIONS_LIMIT)
                        .flushDuration(InfluxConst.DEFAULT_BATCH_INTERVAL_DURATION)
                        .jitterDuration(InfluxConst.DEFAULT_JITTER_INTERVAL_DURATION)
                        .bufferLimit(InfluxConst.DEFAULT_BUFFER_LIMIT)
                        .threadFactory(LyThreadFactoryBuilderUtil.build("influx-batch-options"))
                        .exceptionHandler((batch, exception) -> log.error("influx db batch insert fail", exception));
            }
            influxDB.enableBatch(defaultBatchOptions);
        }
    }

    public void close() {
        if (Objects.nonNull(influxDB)) {
            influxDB.close();
        }
    }

    private void createDatabase(String database) {
        influxDB.query(new Query("CREATE DATABASE " + database, ""));
    }
}
