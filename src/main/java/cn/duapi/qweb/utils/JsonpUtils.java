package cn.duapi.qweb.utils;

import org.springframework.util.StringUtils;

public class JsonpUtils {

    private static String CALLBACK_REGEX = "^[a-zA-Z0-9_\\.]+$";

    protected static boolean isValidCallback(String callback) {
        return callback.matches(CALLBACK_REGEX);
    }

    public static void checkCallback(String callback) {
        if (StringUtils.isEmpty(callback)) {
            throw new IllegalArgumentException("参数callback不能为空.");
        }
        if (!isValidCallback(callback)) {
            throw new IllegalArgumentException("非法callback[" + callback + "].");
        }
    }

    public static void checkVar(String var) {
        if (StringUtils.isEmpty(var)) {
            throw new IllegalArgumentException("参数var不能为空.");
        }
        if (!isValidCallback(var)) {
            throw new IllegalArgumentException("非法var[" + var + "].");
        }
    }
}
