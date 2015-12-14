package com.yy.commons.leopard.qwebservice.utils;

import java.lang.reflect.Type;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import com.yy.commons.leopard.qwebservice.exception.JsonRuntimeException;

public class JsonUtils {

    public static ObjectMapper JSON_MAPPER = new ObjectMapper(); // can reuse, share

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            String str = JSON_MAPPER.writeValueAsString(obj);
            return str;
        } catch (Exception e) {
            throw new JsonRuntimeException(e.getMessage(), e);
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
