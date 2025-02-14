package com.github.liaomengge.base_common.design.patterns.strategy.factory;

import com.github.liaomengge.base_common.design.patterns.strategy.annotation.IStrategy;
import com.github.liaomengge.base_common.design.patterns.strategy.annotation.StrategyType;
import com.github.liaomengge.base_common.design.patterns.strategy.handler.StrategyHandler;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

/**
 * Created by liaomengge on 2021/6/3.
 */
public class StrategyFactory implements ApplicationContextAware {

    public Map<String, StrategyHandlerSpec> strategyHandlerClassMap = Maps.newConcurrentMap();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, StrategyHandler> stringStrategyHandlerMap =
                applicationContext.getBeansOfType(StrategyHandler.class);
        for (Map.Entry<String, StrategyHandler> entry : stringStrategyHandlerMap.entrySet()) {
            String strategyBeanName = entry.getKey();
            StrategyHandler strategyHandler = entry.getValue();
            if (Objects.isNull(strategyHandler)) {
                continue;
            }
            Class<?> strategyClass = strategyHandler.getClass();
            StrategyType strategyType = AnnotationUtils.findAnnotation(strategyClass, StrategyType.class);
            if (Objects.nonNull(strategyType)) {
                strategyHandlerClassMap.put(getStrategyTypeName(strategyType, strategyClass),
                        new StrategyHandlerSpec(strategyBeanName, strategyClass));
            } else {
                if (strategyHandler instanceof IStrategy) {
                    strategyHandlerClassMap.put(getStrategyTypeName((IStrategy) strategyHandler),
                            new StrategyHandlerSpec(strategyBeanName, strategyClass));
                }
            }
        }
    }

    public StrategyHandler getHandler(IStrategy iStrategy) {
        StrategyHandlerSpec strategyHandlerSpec = strategyHandlerClassMap.get(getStrategyTypeName(iStrategy));
        if (Objects.isNull(strategyHandlerSpec)) {
            return null;
        }
        if (!StrategyHandler.class.isAssignableFrom(strategyHandlerSpec.getBeanClass())) {
            return null;
        }
        return (StrategyHandler) applicationContext.getBean(strategyHandlerSpec.getBeanName(),
                strategyHandlerSpec.getBeanClass());
    }

    public StrategyHandler getHandler(String value, String category) {
        StrategyHandlerSpec strategyHandlerSpec = strategyHandlerClassMap.get(category + '#' + value);
        if (Objects.isNull(strategyHandlerSpec)) {
            return null;
        }
        return (StrategyHandler) applicationContext.getBean(strategyHandlerSpec.getBeanName(),
                strategyHandlerSpec.getBeanClass());
    }

    private String getStrategyTypeName(StrategyType strategyType, Class<?> strategyClass) {
        String value = strategyType.value();
        if (StringUtils.isBlank(value)) {
            value = strategyClass.getTypeName();
        }
        return strategyType.category() + '#' + value;
    }

    private String getStrategyTypeName(IStrategy iStrategy) {
        if (Objects.nonNull(iStrategy.strategy())) {
            return iStrategy.strategy().category() + '#' + iStrategy.strategy().value();
        }
        return iStrategy.category() + '#' + iStrategy.value();
    }

    @Data
    @AllArgsConstructor
    private class StrategyHandlerSpec {
        private String beanName;
        private Class<?> beanClass;
    }
}
