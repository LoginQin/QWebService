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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import cn.duapi.qweb.annotation.QWebConfig;
import cn.duapi.qweb.doc.QWebDocumentRender;
import cn.duapi.qweb.exception.ClientErrorException;
import cn.duapi.qweb.utils.JsonUtils;
import cn.duapi.qweb.utils.RequestUtils;
import cn.duapi.qweb.view.JsonView;
import cn.duapi.qweb.view.JsonViewRender;
import cn.duapi.qweb.view.MarkpageView;
import cn.duapi.qweb.view.TextView;

/**
 * The QWebService Exporter
 *
 * @author qinwei
 */
public class WebServiceExporter extends RemoteExporter implements HttpRequestHandler, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(WebServiceExporter.class);

    /**
     * Reference the adapter of the original project in order to obtain unified information
     */
    @Autowired(required = false)
    RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Autowired(required = false)
    QWebViewHandler qWebViewHandler;

    @Autowired
    Environment environment;

    QWebViewHandler defaultRpcViewHandler = new JsonViewRender();

    PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");


    /**
     * 接口方法
     */
    Map<String, Method> methodMaps = new HashMap<>();

    /**
     * 实际实现的方法体
     */
    Map<String, Method> implMethodMaps = new TreeMap<>();

    /**
     * 方法匹配模式
     */
    static final String METHOD_URL_REGX = "/([^/]+)\\.do";
    /**
     * 接口模式
     */
    private boolean isInterfaceMode;

    private static Object[] EMPTY_ARGS = new Object[]{};

    private String renderDocument;

    /**
     * interface verification
     */
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String methodName = getFirstMatchFromURL(request.getRequestURI(), METHOD_URL_REGX);
        Method method = methodMaps.get(methodName);

        if (StringUtils.isEmpty(methodName) && this.isNeedToRenderDocument()) {
            renderDocument(request, response);
            return;
        }

        boolean isRpc = StringUtils.startsWithIgnoreCase(request.getHeader("User-Agent"), "QWebRPC");

        try {
            if (method == null) {
                throw new NoSuchMethodException("The Public WebService [" + AopUtils.getTargetClass(this.getService()).getSimpleName() + "] No This Method: " + methodName);
            }

            String authorization = request.getHeader("Authorization");
            if (!StringUtils.isEmpty(this.getAccessToken()) && !this.accessToken.equals(authorization)) {
                unauthorized(request, response);
                return;
            }

            Object[] params;
            if (isRpc) {
                params = deserializeToParams(request, method.getGenericParameterTypes());
            } else {
                // Normal Controller mode
                ServletWebRequest webRequest = new ServletWebRequest(request, response);
                ModelAndViewContainer mavContainer = new ModelAndViewContainer();
                RequestParamsParser parser = new RequestParamsParser(this.getService(), implMethodMaps.get(methodName),
                        requestMappingHandlerAdapter);
                params = parser.getMethodArgumentValues(webRequest, mavContainer, EMPTY_ARGS);
            }

            Object invokeResult = method.invoke(this.getService(), params);

            ModelAndView modelAndView;
            QWebViewHandler viewRender = getViewRender(isRpc);
            if (invokeResult instanceof ModelAndView) {
                // if method result is return ModelAndView , use to instead
                modelAndView = (ModelAndView) invokeResult;
            } else {
                modelAndView = viewRender.getResultView(methodName, invokeResult);
            }

            modelAndView.getView().render(modelAndView.getModel(), request, response);

        } catch (InvocationTargetException e) {
            ModelAndView exceptionView = getViewRender(isRpc).getExceptionView(methodName, e.getTargetException());
            logger.error(e.getTargetException().getMessage(), e);
            try {
                exceptionView.getView().render(exceptionView.getModel(), request, response);
            } catch (Exception e1) {
                renderError(e1, request, response);
            }

        } catch (Throwable e) {
            handleThrowable(request, response, methodName, isRpc, e);
        }

    }

    private void handleThrowable(HttpServletRequest request, HttpServletResponse response, String methodName, boolean isRpc, Throwable e) {
        ModelAndView exceptionView;
        boolean isNoMethodErr = false;
        QWebViewHandler viewRender = getViewRender(isRpc);
        if ((e instanceof NoSuchMethodException || e instanceof TypeMismatchException)) {
            // Shield the error message
            exceptionView = viewRender.getExceptionView(methodName, new ClientErrorException("No Such Method or Params Error"));
            isNoMethodErr = true;
        } else {
            exceptionView = viewRender.getExceptionView(methodName, e);
        }
        try {
            exceptionView.getView().render(null, request, response);
            if (isNoMethodErr) {
                logger.warn(e.getMessage());
            } else {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e1) {
            renderError(e1, request, response);
        }
    }

    private QWebViewHandler getViewRender(boolean isRpc) {
        if (isRpc) {
            return defaultRpcViewHandler;
        }

        // if this public Service implement QWebViewHandler interfaces, instead default render
        if (this.getService() instanceof QWebViewHandler) {
            return ((QWebViewHandler) this.getService());
        }

        if (this.qWebViewHandler != null) {
            return qWebViewHandler;
        }

        return defaultRpcViewHandler;
    }

    /**
     * 根据指定type类型数组反序列化回对象数组
     *
     * @param request
     * @param types
     * @return
     */
    public static Object[] deserializeToParams(HttpServletRequest request, Type[] types) {
        Object[] params = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            String param = request.getParameter("q[" + i + "]");
            params[i] = JsonUtils.toObject(param, types[i]);
        }

        return params;

    }

    private void renderDocument(HttpServletRequest request, HttpServletResponse response) {
        String document = QWebDocumentRender.generateSimpleAPIDocument(implMethodMaps, RequestUtils.getRequestContextUri(request), this.getRenderDocument());
        MarkpageView view = new MarkpageView(document);
        try {
            view.getView().render(view.getModel(), request, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean isNeedToRenderDocument() {
        return !StringUtils.isEmpty(this.getRenderDocument());
    }

    public void renderError(Exception e, HttpServletRequest request, HttpServletResponse response) {
        logger.error(e.getMessage(), e);
        TextView view = new TextView(HttpStatus.OK, "ERROR");
        try {
            view.getView().render(view.getModel(), request, response);
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
        }
    }

    /**
     * 未验证
     *
     * @param request
     * @param response
     */
    public void unauthorized(HttpServletRequest request, HttpServletResponse response) {
        JsonView view = new JsonView(HttpStatus.UNAUTHORIZED.value(), "401");
        view.setMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        try {
            logger.warn(RequestUtils.getRequestContextUri(request) + ", unauthorized");
            view.getView().render(view.getModel(), request, response);
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.isInterfaceMode = this.getServiceInterface() != null;
        initRpcMethods();
        parseAccessToken();
    }

    private void initRpcMethods() {
        String className = getSimpleClassName();
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

            implMethodMaps.put(name, methodImpl);
            logger.debug("QWebService Method: " + className + "." + methodImpl.getName() + getSimpleParameterTypeString(method.getParameterTypes()));
        }
    }

    private void parseAccessToken() {
        if (this.accessToken.startsWith("${")) {
            this.accessToken = placeholderHelper.replacePlaceholders(this.accessToken, new PropertyPlaceholderHelper.PlaceholderResolver() {
                @Override
                public String resolvePlaceholder(String s) {
                    return environment.getProperty(s);
                }
            });
        }
    }

    private static String getSimpleParameterTypeString(Class<?>[] clazzs) {
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


    protected String getSimpleClassName() {
        return this.isInterfaceMode ? this.getServiceInterface().getSimpleName() : getActualClass().getSimpleName();
    }

    /**
     * 获取Aop代理后实际的目标对象
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
        } catch (SecurityException | NoSuchMethodException e) {
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


    public String getRenderDocument() {
        return renderDocument;
    }

    public void setRenderDocument(String renderDocument) {
        this.renderDocument = renderDocument;
    }

}
