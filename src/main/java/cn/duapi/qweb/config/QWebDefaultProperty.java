package cn.duapi.qweb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 这个只是为了自动补全参数用具体通过environment直接去读
 *
 * @author qinwei
 * @since 2019/12/29
 */
@ConfigurationProperties(prefix = "qweb.client")
public class QWebDefaultProperty {
    /**
     * eg. http://api.xxxx.com
     */
    String baseUrl;

    /**
     * in millis
     */
    int readTimeout = 10_000;

    /**
     * in millis
     */
    int connectTimeout = 3_000;

    /**
     * verify token
     */
    String accessToken;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
