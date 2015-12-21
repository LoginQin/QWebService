package cn.duapi.qweb;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.lang.model.type.NullType;

import org.apache.log4j.Logger;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import cn.duapi.qweb.annotation.QWebService;

public class QWebAnnotationRegister implements ApplicationContextAware, InitializingBean {

    final static Logger logger = Logger.getLogger(QWebAnnotationRegister.class);

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("QWebService Start Loading..");

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(QWebService.class);

        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;

        // 获取bean工厂并转换为DefaultListableBeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();

        Properties pro = new Properties();

        // 通过BeanDefinitionBuilder创建bean定义
        BeanDefinitionBuilder handlerMappingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceUrlHandlerMapping.class);

        for (Entry<String, Object> bean : beans.entrySet()) {

            String beanName = bean.getKey();

            Object targetBean = bean.getValue();// this bean maybe a proxy bean, so use AopUtils.getTargetClass

            Class<?> targetClazz = AopUtils.getTargetClass(targetBean);

            if (ClassUtils.isCglibProxyClass(targetClazz)) {
                // 被CGLIB代理的类,是动态代码, 映射不到参数名称, 忽略
                String errorMsg = "!!!WARN The bean name=[" + beanName
                        + "] is EnhanceByCGLIB, QWebService Can't Work OK! This Error will be ignore! You'd better choose a clean bean without any proxy to use QWebService";
                logger.error(errorMsg);
                System.err.println(errorMsg);
                continue;
            }

            QWebService qwebAnn = AnnotationUtils.findAnnotation(targetClazz, QWebService.class);

            // 通过BeanDefinitionBuilder创建bean定义
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceExporter.class);

            beanDefinitionBuilder.addPropertyReference("service", beanName);

            Class<?> firstImplementInterface = getFirstIntefacesByDefault(targetBean);

            boolean isInterfaceMode = (firstImplementInterface != null);

            // interface mode
            if (!NullType.class.equals(qwebAnn.api())) {
                //set by api value
                beanDefinitionBuilder.addPropertyValue("serviceInterface", qwebAnn.api());
            } else if (isInterfaceMode) {
                //get first interface default, not include  QWebViewHandler
                beanDefinitionBuilder.addPropertyValue("serviceInterface", firstImplementInterface.getName());
            }

            // 注册bean
            defaultListableBeanFactory.registerBeanDefinition(beanName + "Exporter", beanDefinitionBuilder.getRawBeanDefinition());
            pro.setProperty(qwebAnn.url(), beanName + "Exporter");
            logger.info("register-->" + beanName + "Exporter for URL=" + qwebAnn.url());
        }

        handlerMappingDefinitionBuilder.addPropertyValue("mappings", pro);

        defaultListableBeanFactory.registerBeanDefinition("qwebUrlMap", handlerMappingDefinitionBuilder.getRawBeanDefinition());

    }

    protected Class<?> getFirstIntefacesByDefault(Object target) {

        for (Class<?> clazz : target.getClass().getInterfaces()) {
            //ignore QWebViewHandler
            if (clazz.equals(QWebViewHandler.class)) {
                continue;
            }
            return clazz;
        }

        return null;
    }
}
