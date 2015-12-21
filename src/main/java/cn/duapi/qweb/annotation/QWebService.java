package cn.duapi.qweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.type.NullType;

import org.springframework.stereotype.Component;

/**
 * 标注某个类方法开放QWebService接口
 * <P>
 * { url="/publicUrl/", api=Interfaces.class, value＝beanName }
 * <P>
 * QWebService是服务的出口, 所以理论上被标注的bean应该是 @Primary, 并且被注解的类最好不要被CGLIB代理
 * 
 * <P>
 * url = "/rpc/yourservice/" [require 必须] 指定要发布的URL
 * <P>
 * api = YourService.class [optional 可选] 指定要发布的接口方法, 建议指定接口来定义需要发布的各种方法
 * <P>
 * value = beanName [optional 可选] 指定bean的名称, QWebService注解同时也是@Component,
 * 可以指定bean名称
 * <P>
 * 指定了api, 会采用接口模式, 如果不指定, 直接发布类, 可以用Http访问该类方法.
 * <P>
 * 但只有接口模式才能支持RPC客户端访问, 即需要QWebProxyFactoryBean或者HessianProxyFactoryBean,
 * 必须是接口模式
 * 
 * @author qinwei
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface QWebService {

    String value() default "";

    String url();

    Class<?> api() default NullType.class;

}
