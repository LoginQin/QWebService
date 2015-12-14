package com.yy.commons.leopard.qwebservice.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注某个类方法开放WebService接口
 * 
 * @author qinwei
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QWebService {

    String value() default "";

    String api() default "";

}
