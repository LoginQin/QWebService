package com.yy.commons.leopard.qwebservice.utils;

import java.text.SimpleDateFormat;

import org.codehaus.jackson.map.ObjectMapper;

import com.yy.commons.leopard.qwebservice.exception.JsonRuntimeException;

/**
 * 如果需要设置JsonView输出的各种参数, 可以引用这个JSON_VIEW_RENDER来进行配置
 * 
 * @author qinwei
 * 
 */
public class QWebViewUtils {

    public static ObjectMapper JSON_VIEW_RENDER = new ObjectMapper(); // can reuse, share

    static {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JSON_VIEW_RENDER.setDateFormat(fmt);
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            String str = JSON_VIEW_RENDER.writeValueAsString(obj);
            return str;
        } catch (Exception e) {
            throw new JsonRuntimeException(e.getMessage(), e);
        }
    }

}
