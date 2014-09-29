package com.yy.commons.leopard.qwebservice.client;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

import com.yy.commons.leopard.qwebservice.client.aop.QWebClientInterceptor;

/**
 * Quickly WebService Proxy Bean
 * 
 * @author qinwei
 * 
 */
public class QWebProxyFactoryBean extends QWebClientInterceptor implements FactoryBean<Object> {

	private Object serviceProxy;

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
