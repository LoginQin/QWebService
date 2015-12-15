package cn.duapi.qweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.type.NullType;

import org.springframework.stereotype.Component;

/**
 * 标注某个类方法开放WebService接口
 * 
 * @author qinwei
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface QWebService {

    String value();

    Class<?> api() default NullType.class;

}
