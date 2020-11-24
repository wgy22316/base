package com.github.liaomengge.base_common.utils.generic;

import lombok.experimental.UtilityClass;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 也可参考Spring提供的泛型解析,见{@link GenericTypeResolver}
 */
@UtilityClass
public class LyGenericUtil {

    public <T> Class<T> getActualTypeArguments4GenericInterface(Class<?> clz) {
        return getActualTypeArguments4GenericInterface(clz, 0);
    }

    /**
     * 获取父接口泛型类型
     *
     * @param clz
     * @param <T>
     * @return
     */
    public <T> Class<T> getActualTypeArguments4GenericInterface(Class<?> clz, int i) {
        Type[] types = clz.getGenericInterfaces();
        if (types[i] instanceof ParameterizedType) {
            return getGenericClass((ParameterizedType) types[i], i);
        }
        return (Class<T>) types[i];
    }

    public <T> Class<T> getActualTypeArguments4GenericClass(Class<?> clz) {
        return getActualTypeArguments4GenericClass(clz, 0);
    }

    /**
     * 获取父类泛型类型
     *
     * @param clz
     * @param <T>
     * @return
     */
    public <T> Class<T> getActualTypeArguments4GenericClass(Class<?> clz, int i) {
        Type type = clz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            return getGenericClass((ParameterizedType) type, i);
        }
        return (Class<T>) type;
    }

    private <T> Class<T> getGenericClass(ParameterizedType parameterizedType, int i) {
        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        if (genericClass instanceof ParameterizedType) { // 处理多级泛型
            return (Class<T>) ((ParameterizedType) genericClass).getRawType();
        }
        if (genericClass instanceof GenericArrayType) { // 处理数组泛型
            return (Class<T>) ((GenericArrayType) genericClass).getGenericComponentType();
        }
        return (Class<T>) genericClass;
    }
}
