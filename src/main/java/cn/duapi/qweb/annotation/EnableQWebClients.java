package cn.duapi.qweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import cn.duapi.qweb.QWebClientRegistrar;


/**
 * QWebClient auto create proxy
 * <p>
 * set scan basePackage
 *
 * @author qinwei
 * @since 2019/12/29
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({QWebClientRegistrar.class})
public @interface EnableQWebClients {
    /**
     * scan base package
     *
     * @return
     */
    String[] value();
}
