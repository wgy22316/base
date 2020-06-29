package cn.mwee.base_common.logger.servlet;

import cn.mwee.base_common.logger.LoggerProperties;
import cn.mwee.base_common.utils.web.MwWebUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.LoggersEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.LoggersMvcEndpoint.InvalidLogLevelException;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by liaomengge on 2019/1/22.
 */
public class LoggerServlet extends HttpServlet implements EnvironmentAware {

    private static final long serialVersionUID = -4795305296014118807L;

    private Environment environment;

    @Autowired
    private LoggerProperties loggerProperties;

    @Autowired
    private LoggersEndpoint loggersEndpoint;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doHandle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    private void doHandle(HttpServletRequest req, HttpServletResponse resp) {
        RespBody respBody = new RespBody();
        Boolean enabled = this.environment.getProperty("endpoints.loggers.enabled", Boolean.class);
        if (!Boolean.TRUE.equals(enabled)) {
            respBody.setSuccess(false);
            respBody.setMsg("endpoints.loggers.enabled必须开启");
            MwWebUtil.renderJson(resp, respBody);
            return;
        }

        LogLevel logLevel;
        try {
            logLevel = this.getLogLevel(loggerProperties.getLevel());
        } catch (InvalidLogLevelException e) {
            respBody.setSuccess(false);
            respBody.setMsg(e.getMessage());
            MwWebUtil.renderJson(resp, respBody);
            return;
        }

        try {
            loggersEndpoint.setLogLevel(loggerProperties.getPkg(), LogLevel.valueOf(logLevel.name()));
            respBody.setMsg("设置package[" + loggerProperties.getPkg() + "],级别[" + loggerProperties.getLevel() + "]成功");
        } catch (Exception e) {
            respBody.setSuccess(false);
            respBody.setMsg("设置package[" + loggerProperties.getPkg() + "],级别[" + loggerProperties.getLevel() + "]失败");
        }
        MwWebUtil.renderJson(resp, respBody);
    }

    private LogLevel getLogLevel(String level) {
        try {
            return (level != null) ? LogLevel.valueOf(level.toUpperCase(Locale.ENGLISH)) : null;
        } catch (IllegalArgumentException ex) {
            throw new InvalidLogLevelException(level);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Data
    private static class RespBody {
        private boolean success;
        private String msg;

        public RespBody() {
            this.success = true;
        }
    }
}
