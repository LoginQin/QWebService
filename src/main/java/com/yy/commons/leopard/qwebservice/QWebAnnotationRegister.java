package com.yy.commons.leopard.qwebservice;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import com.yy.commons.leopard.qwebservice.annotation.QWebService;
import com.yy.commons.leopard.qwebservice.utils.JsonUtils;

public class QWebAnnotationRegister implements ApplicationContextAware, InitializingBean {

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.err.print("QwebStart....");

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
            // 设置属性userAcctDAO,此属性引用已经定义的bean:userAcctDAO
            beanDefinitionBuilder.addPropertyReference("service", beanName);

            if (StringUtils.isEmpty(qwebAnn.api())) {
                Class<?>[] clazzs = targetBean.getClass().getInterfaces();
                System.out.println(JsonUtils.toJson(clazzs));
                if (clazzs != null && clazzs.length > 0) {
                    beanDefinitionBuilder.addPropertyValue("serviceInterface", targetBean.getClass().getInterfaces()[0].getName());
                }
            } else {
                beanDefinitionBuilder.addPropertyValue("serviceInterface", qwebAnn.api());
            }

            // 注册bean
            defaultListableBeanFactory.registerBeanDefinition(beanName + "Exporter", beanDefinitionBuilder.getRawBeanDefinition());
            pro.setProperty(qwebAnn.value(), beanName + "Exporter");
            System.err.print("register-->" + beanName + "Exporter for URL " + qwebAnn.value());
        }

        handlerMappingDefinitionBuilder.addPropertyValue("mappings", pro);
        defaultListableBeanFactory.registerBeanDefinition("qwebUrlMap", handlerMappingDefinitionBuilder.getRawBeanDefinition());

    }
}
