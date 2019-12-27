package cn.duapi.qweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import cn.duapi.qweb.QWebAnnotationRegister;

/**
 * Enable QWebService Annotation Service
 *
 * @author qinwei
 * @since 2019/12/27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(QWebAnnotationRegister.class)
public @interface EnableQWebService {
}
