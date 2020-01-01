package cn.duapi.qweb.client;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

import cn.duapi.qweb.client.aop.QWebClientInterceptor;

/**
 * Quickly WebService Proxy Bean
 *
 * @author qinwei
 */
public class QWebProxyFactoryBean extends QWebClientInterceptor implements FactoryBean<Object> {

    private Object serviceProxy;

    /**
     * getRpcClient
     *
     * @param url
     * @param interfaces
     * @param accessToken
     * @param <T>
     * @return
     */
    public static <T> T getProxy(String url, Class<T> interfaces, String accessToken) {
        return getProxy(url, interfaces, accessToken, 3_000, 10_000);
    }

    /**
     * GET Rpc Client
     *
     * @param url
     * @param interfaces
     * @param accessToken
     * @param connectTimeout millis
     * @param readTimeout    millis
     * @param <T>
     * @return
     */
    public static <T> T getProxy(String url, Class<T> interfaces, String accessToken, int connectTimeout, int readTimeout) {
        QWebClientInterceptor qWebClientInterceptor = new QWebClientInterceptor();
        qWebClientInterceptor.setAccessToken(accessToken);
        qWebClientInterceptor.setServiceUrl(url);
        qWebClientInterceptor.setReadTimeout(readTimeout);
        qWebClientInterceptor.setConnectTimeout(connectTimeout);
        qWebClientInterceptor.afterPropertiesSet();
        return ProxyFactory.getProxy(interfaces, qWebClientInterceptor);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.serviceProxy = new ProxyFactory(getServiceInterface(), this).getProxy(getBeanClassLoader());
    }

    @Override
    public Object getObject() {
        return this.serviceProxy;
    }

    @Override
    public Class<?> getObjectType() {
        return getServiceInterface();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
