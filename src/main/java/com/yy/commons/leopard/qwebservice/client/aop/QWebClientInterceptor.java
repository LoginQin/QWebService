package com.yy.commons.leopard.qwebservice.client.aop;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

import com.github.kevinsawicki.http.HttpRequest;
import com.yy.commons.leopard.qwebservice.serializ.InvokeResultDeserializer;
import com.yy.commons.leopard.qwebservice.utils.QWebViewUtils;

public class QWebClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor {


	static InvokeResultDeserializer invokeResultDeserializer = new InvokeResultDeserializer();

	long readTimeout = -1;

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object arg[] = invocation.getArguments();
		Map<String, String> param = new HashMap<String, String>();

		if (arg != null) {
			for (int i = 0; i < arg.length; i++) {
                param.put("__qwebparam[" + i + "]", QWebViewUtils.toJson(arg[i]));
			}
		}

        String resultStr = HttpRequest.post(this.getMethodURL(invocation.getMethod().getName())).form(param).body();
		return invokeResultDeserializer.deserializ(resultStr, invocation.getMethod().getGenericReturnType());
	}

	String getMethodURL(String methodName) {
		return this.getServiceUrl() + "/" + methodName + ".do";
	}

	@Override
	public void afterPropertiesSet() {
	}

	public void setServiceUrl(String serviceUrl) {
		super.setServiceUrl(serviceUrl.trim());
	}

	// public static void main(String[] args) {
	// String data = "[{\"appName\":\"kkdict\",\"appKey\":\"654321\"},\"zidian\",\"\"]";
	// List<String> list = Json.toObject(data, List.class);
	// }

	public long getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

}
