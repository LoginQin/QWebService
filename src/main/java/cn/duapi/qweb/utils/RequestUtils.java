package cn.duapi.qweb.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {

    public static String getRequestContextUri(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestURI;
        if ("/".equals(contextPath)) {
            requestURI = request.getRequestURI();
        } else {
            String uri = request.getRequestURI();
            requestURI = uri.substring(contextPath.length());
        }
        if (requestURI.indexOf("//") != -1) {
            requestURI = requestURI.replaceAll("/+", "/");
        }
        return requestURI;
    }

    public static HttpServletRequest getCurrentRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }
}
