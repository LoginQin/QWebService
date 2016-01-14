package cn.duapi.qweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * QWebService Config
 * <P>
 * Giving some config settings to QWeb Method
 * <UL>
 * <LI>ignore: ignore this method to be public, default is false</LI>
 * <LI>doc: writing some description for this method, if open the simple api
 * document</LI>
 * </UL>
 * 
 * @author qinwei
 * @since 2016 V1.2
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface QWebConfig {

    String doc() default "";

    boolean ignore() default false;
}
