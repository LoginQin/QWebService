package cn.duapi.qweb;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import cn.duapi.qweb.annotation.QWebService;
import cn.duapi.qweb.exception.RPCExportException;

/**
 * QWebService Register
 * <p>
 * Scan all QWebService annotation
 *
 * @author qinwei
 */
public class QWebAnnotationRegister implements ApplicationContextAware, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(QWebAnnotationRegister.class);

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
            registerQwebExporter(defaultListableBeanFactory, pro, bean);
        }

        handlerMappingDefinitionBuilder.addPropertyValue("mappings", pro);
        defaultListableBeanFactory.registerBeanDefinition("qwebUrlMap", handlerMappingDefinitionBuilder.getRawBeanDefinition());

    }

    /**
     * 注册Exporter
     *
     * @param defaultListableBeanFactory
     * @param pro
     * @param bean
     */
    private void registerQwebExporter(DefaultListableBeanFactory defaultListableBeanFactory, Properties pro, Entry<String, Object> bean) {
        String beanName = bean.getKey();
        // this bean maybe a proxy bean, so use AopUtils.getTargetClass
        Object targetBean = bean.getValue();

        Class<?> targetClazz = AopUtils.getTargetClass(targetBean);

        QWebService qwebAnn = AnnotationUtils.findAnnotation(targetClazz, QWebService.class);

        // 通过BeanDefinitionBuilder创建bean定义
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceExporter.class);

        beanDefinitionBuilder.addPropertyReference("service", beanName);

        String interfaceName = qwebAnn.api().getName();

        if (interfaceName == null) {
            throw new RPCExportException("the rpc bean [" + beanName + "] should specify an interface!");
        }

        if (!StringUtils.isEmpty(qwebAnn.doc())) {
            beanDefinitionBuilder.addPropertyValue("renderDocument", qwebAnn.doc());
        }

        beanDefinitionBuilder.addPropertyValue("serviceInterface", interfaceName);
        beanDefinitionBuilder.addPropertyValue("accessToken", qwebAnn.accessToken());

        // 注册bean
        defaultListableBeanFactory.registerBeanDefinition(beanName + "Exporter", beanDefinitionBuilder.getRawBeanDefinition());
        pro.setProperty(qwebAnn.url(), beanName + "Exporter");
        logger.debug("register-->" + beanName + "Exporter for URL=" + qwebAnn.url());
    }

}
