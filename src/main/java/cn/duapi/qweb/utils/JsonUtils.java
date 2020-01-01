package cn.duapi.qweb.utils;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import cn.duapi.qweb.exception.JsonRuntimeException;

/**
 * @Author qinwei
 */
public class JsonUtils {

    /**
     * can reuse, share
     */
    public static ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static ObjectWriter JSON_PRETTY_WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonRuntimeException(e.getMessage(), e);
        }
    }

    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return JSON_MAPPER.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new JsonRuntimeException(e.getMessage(), e);
        }
    }

    public static String toFormatJson(Object obj) {
        try {
            if (obj == null) {
                return null;
            } else {
                return JSON_PRETTY_WRITER.writeValueAsString(obj);
            }
        } catch (Exception e) {
            throw new JsonRuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T toObject(String content, Class<T> valueType) {
        try {
            return JSON_MAPPER.readValue(content, valueType);
        } catch (IOException e) {
            throw new JsonRuntimeException(e.getMessage());
        }
    }


    public static Object toObject(String json, Type type) {
        try {
            return JSON_MAPPER.readValue(json, getJavaType(type));
        } catch (Exception e) {
            throw new JsonRuntimeException(e.getMessage(), e);
        }
    }

    public static JavaType getJavaType(Type type) {
        JavaType resultType;
        resultType = JSON_MAPPER.constructType(type);
        return resultType;
    }


}
