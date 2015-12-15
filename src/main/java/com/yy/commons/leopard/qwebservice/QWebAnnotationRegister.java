package com.yy.commons.leopard.qwebservice;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.lang.model.type.NullType;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import com.yy.commons.leopard.qwebservice.annotation.QWebService;

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

            Object targetBean = bean.getValue();

            QWebService qwebAnn = targetBean.getClass().getAnnotation(QWebService.class);
            // 通过BeanDefinitionBuilder创建bean定义
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceExporter.class);

            beanDefinitionBuilder.addPropertyReference("service", beanName);

            if (NullType.class.equals(qwebAnn.api())) {

                Class<?>[] clazzs = targetBean.getClass().getInterfaces();

                if (clazzs != null && clazzs.length > 0) {
                    beanDefinitionBuilder.addPropertyValue("serviceInterface", targetBean.getClass().getInterfaces()[0].getName());
                }

            } else {
                // interface mode
                beanDefinitionBuilder.addPropertyValue("serviceInterface", qwebAnn.api());
            }

            // 注册bean
            defaultListableBeanFactory.registerBeanDefinition(beanName + "Exporter", beanDefinitionBuilder.getRawBeanDefinition());
            pro.setProperty(qwebAnn.value(), beanName + "Exporter");
            logger.info("register-->" + beanName + "Exporter for URL=" + qwebAnn.value());
        }

        handlerMappingDefinitionBuilder.addPropertyValue("mappings", pro);

        defaultListableBeanFactory.registerBeanDefinition("qwebUrlMap", handlerMappingDefinitionBuilder.getRawBeanDefinition());

    }
}
