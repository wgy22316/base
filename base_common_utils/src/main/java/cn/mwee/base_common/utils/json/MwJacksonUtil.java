package cn.mwee.base_common.utils.json;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.Map;

public final class MwJacksonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private MwJacksonUtil() {
    }

    public static String bean2Json(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T json2Bean(String jsonStr, Class<T> objClass) throws IOException {
        return objectMapper.readValue(jsonStr, objClass);
    }

    public static <T> T json2Bean(String jsonStr, TypeReference<?> typeReference) throws IOException {
        return objectMapper.readValue(jsonStr, typeReference);
    }

    public static <T, K, V> T map2Bean(Map<K, V> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }

    public static <T, K, V> T map2Bean(Map<K, V> map, TypeReference<?> typeReference) {
        return objectMapper.convertValue(map, typeReference);
    }

    public static <K, V> Map<K, V> bean2Map(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<Map<K, V>>() {
        });
    }

    public static <K, V> Map<K, V> bean2Map(Object obj, TypeReference<?> typeReference) {
        return objectMapper.convertValue(obj, typeReference);
    }
}
