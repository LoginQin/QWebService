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

    final static String USER_AGENT = "QWebRPC/2.0";

    /**
     * Invoke accessToken
     */
    String accessToken = "";

    /**
     * milliseconds
     */
    int readTimeout = 10_000;

    /**
     * milliseconds
     */
    int connectTimeout = 3_000;


    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object arg[] = invocation.getArguments();
        Map<String, String> param = new HashMap<String, String>();

        if (arg != null) {
            for (int i = 0; i < arg.length; i++) {
                param.put("q[" + i + "]", JsonUtils.toJson(arg[i]));
            }
        }

        try {
            String resultStr = HttpRequest //
                    .post(this.getMethodURL(invocation.getMethod().getName())) //
                    .useProxy("127.0.0.1", 8888)
                    .connectTimeout(this.connectTimeout) //
                    .readTimeout(this.readTimeout) //
                    .authorization(accessToken) //
                    .userAgent(USER_AGENT)
                    .acceptGzipEncoding() //
                    .form(param)
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

    @Override
    public void setServiceUrl(String serviceUrl) {
        super.setServiceUrl(serviceUrl.trim());
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
