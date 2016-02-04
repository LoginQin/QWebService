package cn.duapi.qweb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import cn.duapi.qweb.annotation.QWebConfig;
import cn.duapi.qweb.doc.QWebDocumentRender;
import cn.duapi.qweb.exception.ClientErrorException;
import cn.duapi.qweb.rpcimpl.HessianRPCResolver;
import cn.duapi.qweb.utils.JsonUtils;
import cn.duapi.qweb.utils.RequestUtils;
import cn.duapi.qweb.view.JsonViewRender;
import cn.duapi.qweb.view.MarkpageView;
import cn.duapi.qweb.view.TextView;

/**
 * The QWebService Exporter
 * <P>
 * 发布一个类的公有方法或者接口为WebService,以便客户端能以HTTP形式访问方法或者接口, 服务器以JSON方式返回,
 * JSON符合Leopard返回规范.
 * <P>
 * 如果只发布一个service(类, 默认将发布所有public方法),并且这种模式是非接口模式, 客户端只能通过简单controller形式传递参数访问
 * <P>
 * 不支持RPC的(因为Java-RPC默认需要接口, 客户端利用接口访问)
 * <P>
 * RPC支持hessian, duowan-rpc协议(已废除).
 * <P>
 * 协议由客户端定义 hessian为:/hessian-protol/[publicService]
 * <P>
 * 
 * @author qinwei
 * 
 */
public class WebServiceExporter extends RemoteExporter implements HttpRequestHandler, InitializingBean {

    static final Logger          logger = Logger.getLogger(WebServiceExporter.class);
    // 引用原项目的adapter以便获取统一信息
    @Autowired(required = false)
    RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    
    // 接口方法
    Map<String, Method> methodMaps = new HashMap<String, Method>();

    // 实际实现的方法体
    Map<String, Method> implMethodMaps = new TreeMap<String, Method>();

    // 协议URL匹配模式
    private static final String PROTOL_URL_REGX = "/(\\w+-protol)/";
    // 方法匹配模式
    static final String         METHOD_URL_REGX = "/([^/]+)\\.do";
    // 接口模式
    private boolean             isInterfaceMode;
    // RPC解决者
    List<AbstractRPCResolver>   RPCResolvers    = new ArrayList<AbstractRPCResolver>();

    private static Object[] EMPTY_ARGS = new Object[] {};

    private String renderDocument;

    // private PathMatcher pathMatcher = new AntPathMatcher();

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String methodName = getFirstMatchFromURL(request.getRequestURI(), METHOD_URL_REGX);
        String protolTypeName = getFirstMatchFromURL(request.getRequestURI(), PROTOL_URL_REGX);
        Method method = methodMaps.get(methodName);
        QWebViewHandler viewRender = new JsonViewRender();

        //if this public Service implement QWebViewHandler interfaces, instead default render
        if (this.getService() instanceof QWebViewHandler) {
            viewRender = ((QWebViewHandler) this.getService());
        }

        if (StringUtils.isEmpty(methodName) && this.isNeedToRenderDocument()) {

            String document = QWebDocumentRender.generateSimpleAPIDocument(implMethodMaps, RequestUtils.getRequestContextUri(request), this.getRenderDocument());
            MarkpageView view = new MarkpageView(document);
            try {
                view.getView().render(view.getModel(), request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            for (AbstractRPCResolver resolver : RPCResolvers) {
                if (resolver.isSupport(protolTypeName)) {
                    resolver.invoke(request, response);
                    return;
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("The Public WebService [" + AopUtils.getTargetClass(this.getService()).getSimpleName() + "] No This Method: " + methodName);
            }

            Object[] params;
            boolean isQWebClientProtol = request.getParameter("__qwebparam[0]") != null;
            if (isQWebClientProtol) {
                Type[] types = method.getGenericParameterTypes();
                params = new Object[types.length];

                for (int i = 0; i < types.length; i++) {
                    String param = request.getParameter("__qwebparam[" + i + "]");
                    params[i] = JsonUtils.toObject(param, types[i]);
                }
            } else {
                // 普通controller 模式.
                ServletWebRequest webRequest = new ServletWebRequest(request, response);
                ModelAndViewContainer mavContainer = new ModelAndViewContainer();
                RequestParamsParser parser = new RequestParamsParser(this.getService(), implMethodMaps.get(methodName), requestMappingHandlerAdapter);
                params = parser.getMethodArgumentValues(webRequest, mavContainer, EMPTY_ARGS);
            }

            Object invokeResult = method.invoke(this.getService(), params);

            ModelAndView modelAndView;

            if (invokeResult instanceof ModelAndView) {
                //if method result is return ModelAndView , use to instead
                modelAndView = (ModelAndView) invokeResult;
            } else {
                modelAndView = viewRender.getResultView(methodName, invokeResult);
            }

            modelAndView.getView().render(modelAndView.getModel(), request, response);

        } catch (InvocationTargetException e) {
            ModelAndView exceptionView = viewRender.getExceptionView(methodName, e.getTargetException());
            logger.error(e.getTargetException().getMessage(), e);
            try {
                // Is need to print out to client?
                exceptionView.getView().render(exceptionView.getModel(), request, response);
            } catch (Exception e1) {
                printError(e1, request, response);
            }

        } catch (Throwable e) {
            ModelAndView exceptionView;
            if ((e instanceof NoSuchMethodException || e instanceof TypeMismatchException)) {
                // Shield the error message
                exceptionView = viewRender.getExceptionView(methodName, new ClientErrorException("No Such Method or Params Error"));
                logger.warn(e.getMessage());
            } else {
                exceptionView = viewRender.getExceptionView(methodName, e);
            }
            try {
                exceptionView.getView().render(null, request, response);
            } catch (Exception e1) {
                printError(e1, request, response);
            }
        }

    }

    private boolean isNeedToRenderDocument() {
        return !StringUtils.isEmpty(this.getRenderDocument());
    }

    public void printError(Exception e, HttpServletRequest request, HttpServletResponse response) {
        logger.error(e.getMessage(), e);
        TextView view = new TextView(500, "ERROR");
        try {
            view.getView().render(view.getModel(), request, response);
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.isInterfaceMode = this.getServiceInterface() != null;
        String ClassName = getSimpleClassName();
        Set<String> handleViewMethodNames = toStringNameSet(QWebViewHandler.class.getDeclaredMethods());
        Method[] methods = checkAndGetMethods();

        for (Method method : methods) {

            // ignore not public or implement QWebViewHandler method
            if ((!Modifier.isPublic(method.getModifiers()) || handleViewMethodNames.contains(method.getName()))) {
                continue;
            }

            String name = method.getName();
            if (methodMaps.containsKey(name)) {
                throw new RuntimeException("Method:" + name + ", has been mapping to url. There may be a method with the same name.");
            }

            methodMaps.put(name, method);
            Method methodImpl = getImplMethod(method);

            QWebConfig config = methodImpl.getAnnotation(QWebConfig.class);

            if (config != null && config.ignore()) {
                continue;
            }

            if (methodImpl != null) {
                implMethodMaps.put(name, methodImpl);
            }

            logger.info("QWebService Method: " + ClassName + "." + methodImpl.getName() + getSimpleParameterTypeString(method.getParameterTypes()));
        }

        prepareRPCResolver();
    }

    static String getSimpleParameterTypeString(Class<?>[] clazzs) {
        List<String> data = new ArrayList<String>();
        for (Class<?> clazz : clazzs) {
            data.add(clazz.getSimpleName());
        }
        return "[" + StringUtils.collectionToDelimitedString(data, ",") + "]";
    }

    private static Set<String> toStringNameSet(Method[] methodList) {
        Set<String> sets = new HashSet<String>();
        if (methodList == null) {
            return sets;
        }

        for (Method method : methodList) {
            sets.add(method.getName());
        }
        return sets;
    }

    // add the RPC resolver..
    protected void prepareRPCResolver() {
        if (isInterfaceMode) {// if isInterface Mode
            this.RPCResolvers.add(new HessianRPCResolver(this, getApplicationContentByHandlerAdapter()));
        }
    }

    protected ApplicationContext getApplicationContentByHandlerAdapter() {
        return requestMappingHandlerAdapter == null ? null : requestMappingHandlerAdapter.getApplicationContext();
    }

    protected String getSimpleClassName() {
        return this.isInterfaceMode ? this.getServiceInterface().getSimpleName() : getActualClass().getSimpleName();
    }

    /**
     * 获取Aop代理后实际的目标对象
     * 
     */
    public Class<?> getActualClass() {
        return AopUtils.getTargetClass(this.getService());
    }

    protected Method[] checkAndGetMethods() {
        return this.getServiceInterface() != null ? this.getServiceInterface().getDeclaredMethods() : getActualClass().getDeclaredMethods();
    }

    protected Method getImplMethod(Method method) {
        try {
            return getActualClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (SecurityException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    protected String getFirstMatchFromURL(String url, String REGX) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        Pattern p = Pattern.compile(REGX, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    // for test
    public static void main(String[] args) {
        String REGX = "/(\\w+-protol)/";
        Pattern p = Pattern.compile(REGX, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("/testxx/hessian-protol/ResourceWebService/");
        while (m.find()) {
            System.out.println(m.group(1));
        }
    }

    public String getRenderDocument() {
        return renderDocument;
    }

    public void setRenderDocument(String renderDocument) {
        this.renderDocument = renderDocument;
    }

}
