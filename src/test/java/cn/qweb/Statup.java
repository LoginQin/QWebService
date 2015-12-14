package cn.qweb;

import java.util.Properties;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.yy.commons.leopard.qwebservice.WebServiceExporter;
import com.yy.commons.leopard.qwebservice.WebServiceUrlHandlerMapping;

public class Statup {

    ApplicationContext applicationContext;

    /**
     * 手动配置示例
     * 
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        WebServiceExporter ex = new WebServiceExporter();
        //        ex.setService(notfoundreviewDao);
        //        ex.setServiceInterface(NotfoundreviewDao.class);
        WebServiceUrlHandlerMapping map = new WebServiceUrlHandlerMapping();
        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;

        // 获取bean工厂并转换为DefaultListableBeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();

        // 通过BeanDefinitionBuilder创建bean定义
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceExporter.class);
        // 设置属性userAcctDAO,此属性引用已经定义的bean:userAcctDAO
        beanDefinitionBuilder.addPropertyReference("service", "notfoundreviewDaoMysqlImpl");
        beanDefinitionBuilder.addPropertyValue("serviceInterface", "com.duowan.kkdict.dao.NotfoundreviewDao");
        // 注册bean
        defaultListableBeanFactory.registerBeanDefinition("notfoundreviewDaoExporter", beanDefinitionBuilder.getRawBeanDefinition());

        Properties pro = new Properties();
        pro.setProperty("/admin/notfoundreview/", "notfoundreviewDaoExporter");

        // 通过BeanDefinitionBuilder创建bean定义
        BeanDefinitionBuilder beanDefinitionBuilder2 = BeanDefinitionBuilder.genericBeanDefinition(WebServiceUrlHandlerMapping.class);
        // 设置属性userAcctDAO,此属性引用已经定义的bean:userAcctDAO
        beanDefinitionBuilder2.addPropertyValue("mappings", pro);
        defaultListableBeanFactory.registerBeanDefinition("urlMap2", beanDefinitionBuilder2.getRawBeanDefinition());
    }
}
