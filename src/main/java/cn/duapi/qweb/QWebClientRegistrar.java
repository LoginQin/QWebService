package cn.duapi.qweb;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import cn.duapi.qweb.annotation.EnableQWebClients;
import cn.duapi.qweb.annotation.QWebClient;
import cn.duapi.qweb.client.QWebProxyFactoryBean;
import cn.duapi.qweb.config.QWebDefaultProperty;

/**
 * PRC服务提供装配器
 * <p>
 * 扫描指定包路径, 并自动注册带有@QWebClient的接口.
 *
 * @author qinwei
 * @since 2019/12/27
 */
public class QWebClientRegistrar
        implements ImportBeanDefinitionRegistrar, BeanFactoryAware, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;

    private ConfigurableListableBeanFactory beanFactory;

    private final static Logger LOG = LoggerFactory.getLogger(QWebClientRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry beanDefinitionRegistry) {
        if (annotationMetadata.getAnnotationAttributes(EnableQWebClients.class.getName()) == null) {
            return;
        }
        if (beanFactory == null) {
            return;
        }

        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(resourceLoader);

        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                QWebClient.class);

        scanner.addIncludeFilter(annotationTypeFilter);

        registerClientConfiguration(beanDefinitionRegistry);

        for (String basePackage : getBasePackages(annotationMetadata)) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata metadata = beanDefinition.getMetadata();
                    Assert.isTrue(metadata.isInterface(), "@QWebClient can only be specified on an interface");
                    Map<String, Object> attributes = metadata
                            .getAnnotationAttributes(QWebClient.class.getCanonicalName());
                    registerQWebClient(beanDefinitionRegistry, metadata, attributes);
                }
            }
        }
    }

    private String getQualifier(Map<String, Object> client) {
        if (client == null) {
            return null;
        }
        String qualifier = (String) client.get("qualifier");
        if (StringUtils.hasText(qualifier)) {
            return qualifier;
        }
        return null;
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                // 这里覆盖, 否则扫描不到接口
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    return true;
                }
                return isCandidate;
            }
        };
    }

    private void registerClientConfiguration(BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(QWebDefaultProperty.class);
        registry.registerBeanDefinition(
                QWebDefaultProperty.class.getSimpleName(),
                builder.getBeanDefinition());
    }

    private void registerQWebClient(BeanDefinitionRegistry registry,
                                    AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        try {
            Class<?> aClass = Class.forName(className);
            String[] beanNamesForType = beanFactory.getBeanNamesForType(aClass);
            if (beanNamesForType != null && beanNamesForType.length > 0) {
                LOG.warn("QWebClient already has implement ignore auto create : {}, {}", className, beanNamesForType);
                return;
            }
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return;
        }
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(QWebProxyFactoryBean.class);

        String baseUrl = getUrl(attributes);
        String path = getPath(attributes);
        String accessToken = resolve((String) attributes.getOrDefault("accessToken", ""));

        definition.addPropertyValue("serviceUrl", baseUrl + path);
        definition.addPropertyValue("serviceInterface", className);
        definition.addPropertyValue("accessToken", accessToken);
        definition.addPropertyValue("connectTimeout", attributes.get("connectTimeout"));
        definition.addPropertyValue("readTimeout", attributes.get("readTimeout"));
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        String alias = ClassUtils.getShortName(className) + "$QWebClient";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

        // has a default, won't be null
        boolean primary = (Boolean) attributes.get("primary");

        beanDefinition.setPrimary(primary);

        String qualifier = getQualifier(attributes);
        if (StringUtils.hasText(qualifier)) {
            alias = qualifier;
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
                new String[]{alias});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }


    // PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return environment.resolvePlaceholders(value);
        }
        return value;
    }

    static String getUrl(String url) {
        if (StringUtils.hasText(url) && !(url.startsWith("#{") && url.contains("}"))) {
            if (!url.contains("://")) {
                url = "http://" + url;
            }
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(url + " is malformed", e);
            }
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    static String getPath(String path) {
        if (StringUtils.hasText(path)) {
            path = path.trim();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    private String getUrl(Map<String, Object> attributes) {
        String url = resolve((String) attributes.get("baseUrl"));
        return getUrl(url);
    }

    private String getPath(Map<String, Object> attributes) {
        String path = resolve((String) attributes.get("path"));
        return getPath(path);
    }

    Set<String> getBasePackages(AnnotationMetadata metadata) {
        Set<String> packages = new HashSet<>();
        Map<String, Object> attr = metadata.getAnnotationAttributes(EnableQWebClients.class.getName());
        String[] pac = (String[]) attr.get("value");
        Collections.addAll(packages, pac);
        return packages;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }
}
