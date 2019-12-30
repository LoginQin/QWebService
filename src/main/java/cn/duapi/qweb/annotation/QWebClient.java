package cn.duapi.qweb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 特殊指定每个客户端的参数. 否则使用默认参数.
 * 可以使用${}形式的占位符引用, 但是名称要和参数名称一致, 因为比如base-url != baseUrl, 在springboot里不一致
 *
 * @author qinwei
 * @since 2019/12/29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface QWebClient {

    /**
     * domain and root path overwrite default value
     *
     * @return
     */
    String baseUrl() default "";

    /**
     * relative path
     *
     * @return
     */
    String path();

    /**
     * verify accessToken
     *
     * @return
     */
    String accessToken() default "";

    /**
     * millis
     *
     * @return
     */
    int connectTimeout() default 0;

    /**
     * millis
     *
     * @return
     */
    int readTimeout() default 0;

    /**
     * like @Primary
     *
     * @return
     */
    boolean primary() default true;

    /**
     * @return the <code>@Qualifier</code> value for the qweb client.
     */
    String qualifier() default "";
}
