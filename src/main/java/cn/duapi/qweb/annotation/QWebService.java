package cn.duapi.qweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.type.NullType;

import org.springframework.stereotype.Component;

/**
 * Annotate the class is QWebService
 * <p>
 * <p>
 * { url="/publicUrl/", api=Interfaces.class, value＝beanName, doc=true|[false] }
 * <p>
 * QWebService is a service exports, and should be @Primary. The annotated
 * classes had better not be CGLIB proxy
 * <p>
 * <p>
 * url = "/rpc/yourservice/" [require] Setting public url
 * <p>
 * api = YourService.class [optional] Specifies the interface method to publish
 * <p>
 * value = beanName [optional] Specify the beanName of the QWebService, the bean
 * has bean annotated by QWebService is also a @Component, so you can specify a
 * bean name
 * <p>
 * doc = "" [optional] defualt="", Auto generate simple api document framework
 * when access /publicUrl/ root url if `doc` value has settings.
 * <p>
 * If setting the `api`, that will use the interface mode, if not specified,
 * worked directly with ClassMode,
 * <p>
 * You can use Http to access the class public method.
 * <p>
 * Only the interface mode can support the RPC client access, that means, if you
 * want to use QWebProxyFactoryBean or HessianProxyFactoryBean, the QWebwervice
 * must be an interface mode
 *
 * @author qinwei
 * @see cn.duapi.qweb.client.QWebProxyFactoryBean
 * @see org.springframework.remoting.caucho.HessianProxyFactoryBean
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface QWebService {

    String value() default "";

    String url();

    Class<?> api();

    String doc() default "";

    String accessToken() default "";

}
