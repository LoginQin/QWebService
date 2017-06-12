package cn.duapi.qweb.client.aop;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

import com.github.kevinsawicki.http.HttpRequest;

import cn.duapi.qweb.exception.RPCInvokeException;
import cn.duapi.qweb.serializ.InvokeResultDeserializer;
import cn.duapi.qweb.utils.JsonUtils;

public class QWebClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor {

    static InvokeResultDeserializer invokeResultDeserializer = new InvokeResultDeserializer();

    int readTimeout = 10000; //milliseconds

    int connectTimeout = 3000; //milliseconds

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object arg[] = invocation.getArguments();
        Map<String, String> param = new HashMap<String, String>();

        if (arg != null) {
            for (int i = 0; i < arg.length; i++) {
                param.put("__qwebparam[" + i + "]", JsonUtils.toJson(arg[i]));
            }
        }

        try {
            String resultStr = HttpRequest//
                    .post(this.getMethodURL(invocation.getMethod().getName()))//
                    .connectTimeout(this.connectTimeout)//
                    .readTimeout(this.readTimeout)//
                    .form(param)//
                    .body();
            return invokeResultDeserializer.deserializ(resultStr, invocation.getMethod().getGenericReturnType());
        } catch (Exception e) {
            throw new RPCInvokeException("INVOKE ERROR=>" + getClassAndMethodName(invocation), e);
        }
    }

    String getClassAndMethodName(MethodInvocation invocation) {
        return invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName() + "()";
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

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

}
