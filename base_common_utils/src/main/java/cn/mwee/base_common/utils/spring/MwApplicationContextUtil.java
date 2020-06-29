package cn.mwee.base_common.utils.spring;

import cn.mwee.base_common.support.datasource.DBContext;
import cn.mwee.base_common.support.datasource.enums.DbType;
import cn.mwee.base_common.utils.log4j2.MwLogger;
import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * {type your description }
 *
 * @since: 15/11/16.
 * @author: liaomengge
 */
public class MwApplicationContextUtil {

    private static final Logger logger = MwLogger.getInstance(MwApplicationContextUtil.class);

    private static ConfigurableApplicationContext context = null;
    private volatile static MwApplicationContextUtil instance = null;

    private String springConfigFile = "resources/spring-context.xml";
    private String log4j2ConfigFile = "resources/log4j2.xml";
    private DbType defaultDbType = DbType.MASTER;

    public static MwApplicationContextUtil getInstance() {
        if (instance == null) {
            synchronized (MwApplicationContextUtil.class) {
                if (instance == null) {
                    instance = new MwApplicationContextUtil();
                    instance.init();
                }
            }
        }
        return instance;
    }

    public static MwApplicationContextUtil getInstance(String springConfigFile) {
        if (instance == null) {
            synchronized (MwApplicationContextUtil.class) {
                if (instance == null) {
                    instance = new MwApplicationContextUtil(springConfigFile);
                    instance.init();
                }
            }
        }
        return instance;
    }

    public static MwApplicationContextUtil getInstance(String springConfigFile, String log4j2ConfigFile) {
        if (instance == null) {
            synchronized (MwApplicationContextUtil.class) {
                if (instance == null) {
                    instance = new MwApplicationContextUtil(springConfigFile, log4j2ConfigFile);
                    instance.init();
                }
            }
        }
        return instance;
    }

    public static MwApplicationContextUtil getInstance(String springConfigFile, DbType dbType) {
        if (instance == null) {
            synchronized (MwApplicationContextUtil.class) {
                if (instance == null) {
                    instance = new MwApplicationContextUtil(springConfigFile, dbType);
                    instance.init();
                }
            }
        }
        return instance;
    }

    public ConfigurableApplicationContext getContext() {
        return context;
    }

    public void close() {
        context.close();
    }

    private MwApplicationContextUtil() {
    }

    private MwApplicationContextUtil(String springConfigFile) {
        this.springConfigFile = springConfigFile;
    }

    private MwApplicationContextUtil(String springConfigFile, String log4j2ConfigFile) {
        this.springConfigFile = springConfigFile;
        this.log4j2ConfigFile = log4j2ConfigFile;
    }

    private MwApplicationContextUtil(String springConfigFile, DbType dbType) {
        this.springConfigFile = springConfigFile;
        this.defaultDbType = dbType;
    }

    private void init() {
        //加载log4j2.xml
        File configFile = new File(log4j2ConfigFile);
        if (!configFile.exists()) {
            logger.error("log4j2 config file:" + configFile.getAbsolutePath() + " not exist");
            return;
        }
        logger.info("log4j2 config file:" + configFile.getAbsolutePath());

        try {
            //注:这一句必须放在整个应用第一次LoggerFactory.getLogger(XXX.class)前执行
            System.setProperty("log4j.configurationFile", configFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("log4j2 initialize error:" + e.getLocalizedMessage());
            return;
        }

        //加载spring配置文件
        configFile = new File(springConfigFile);

        if (!configFile.exists()) {
            logger.error("spring config file:" + configFile.getAbsolutePath() + " not exist");
            return;
        }

        if (context == null) {
            DBContext.setDBKey(defaultDbType);
            context = new FileSystemXmlApplicationContext(springConfigFile);

            //做HA时, 很多场景会启动一个standby实例(做为冗余), 仅当master宕机时, standby实例才会激活
            //在启动时, 先关闭所有conn, 释放这些不必要的连接资源
            String[] dsList = context.getBeanNamesForType(DataSource.class);
            for (String ds : dsList) {
                DataSource dataSource = context.getBean(ds, DataSource.class);
                if (dataSource != null) {
                    java.sql.Connection connection;
                    try {
                        connection = dataSource.getConnection();
                        connection.close();
                    } catch (SQLException e) {
                        System.err.println("spring initialize error:" + e.getLocalizedMessage());
                        System.exit(0);
                    }
                }
            }

            String[] redisConnList = context.getBeanNamesForType(JedisConnectionFactory.class);
            for (String beanName : redisConnList) {
                JedisConnectionFactory connectionFactory = context.getBean(beanName, JedisConnectionFactory.class);
                if (connectionFactory != null) {
                    connectionFactory.getConnection().close();
                }
            }
        }

    }
}
