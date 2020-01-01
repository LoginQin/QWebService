package cn.duapi.qweb.client.aop;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;
import org.springframework.util.StringUtils;

import com.github.kevinsawicki.http.HttpRequest;

import cn.duapi.qweb.config.QWebDefaultProperty;
import cn.duapi.qweb.exception.RPCInvokeException;
import cn.duapi.qweb.exception.RemoteCauseException;
import cn.duapi.qweb.serializ.InvokeResultDeserializer;
import cn.duapi.qweb.utils.JsonUtils;

public class QWebClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor {

    static InvokeResultDeserializer invokeResultDeserializer = new InvokeResultDeserializer();

    final static String USER_AGENT = "QWebRPC/2.0";

    @Autowired(required = false)
    QWebDefaultProperty qWebDefaultProperty;

    /**
     * Invoke accessToken
     */
    String accessToken = "";

    /**
     * milliseconds
     */
    int readTimeout;

    /**
     * milliseconds
     */
    int connectTimeout;

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
                    .connectTimeout(this.getConnectTimeout()) //
                    .readTimeout(this.getReadTimeout()) //
                    .authorization(this.getAccessToken()) //
                    .userAgent(USER_AGENT)
                    .acceptGzipEncoding() //
                    .form(param)
                    .body();
            return invokeResultDeserializer.deserializ(resultStr, invocation.getMethod().getGenericReturnType());
        } catch (RemoteCauseException e) {
            throw e;
        } catch (Exception e) {
            throw new RPCInvokeException("INVOKE ERROR=>" + getClassAndMethodName(invocation), e);
        }
    }

    public int getConnectTimeout() {
        if (connectTimeout > 0) {
            return connectTimeout;
        }

        return qWebDefaultProperty.getConnectTimeout();
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    String getClassAndMethodName(MethodInvocation invocation) {
        return invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName() + "()";
    }

    String getMethodURL(String methodName) {
        return this.getServiceUrl() + "/" + methodName + ".do";
    }

    @Override
    public void afterPropertiesSet() {
        if (qWebDefaultProperty == null) {
            qWebDefaultProperty = new QWebDefaultProperty();
        }
    }

    @Override
    public void setServiceUrl(String serviceUrl) {
        super.setServiceUrl(serviceUrl.trim());
    }

    public int getReadTimeout() {
        if (readTimeout > 0) {
            return readTimeout;
        }

        return qWebDefaultProperty.getReadTimeout();
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getAccessToken() {
        if (StringUtils.hasText(this.accessToken)) {
            return accessToken;
        }

        return qWebDefaultProperty.getAccessToken();
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
