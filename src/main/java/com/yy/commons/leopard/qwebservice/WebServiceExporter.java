package com.yy.commons.leopard.qwebservice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.util.StringUtil;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.duowan.leopard.web.mvc.JsonView;
import com.yy.commons.leopard.rpcimpl.DuowanRPCResolver;
import com.yy.commons.leopard.rpcimpl.HessianRPCResolver;

/**
 * The QWebService Exporter
 * <P>
 * 发布一个类的公有方法或者接口为WebService,以便客户端能以HTTP形式访问方法或者接口, 服务器以JSON方式返回, JSON符合Leopard返回规范.
 * <P>
 * 如果只发布一个service(类, 默认将发布所有public方法),并且这种模式是非接口模式, 客户端只能通过简单controller形式传递参数访问
 * <P>
 * 不支持RPC的(因为Java-RPC默认需要接口, 客户端利用接口访问)
 * <P>
 * RPC支持hessian, duowan-rpc协议.
 * <P>
 * 协议由客户端定义 hessian为:/hessian-protol/
 * <P>
 * duowan-rpc: /duowan-protol/[publicService]
 * 
 * @author qinwei
 * 
 */
public class WebServiceExporter extends RemoteExporter implements HttpRequestHandler, InitializingBean {
	static final Logger logger = LoggerFactory.getLogger("WebServiceExporter");
	// 引用原项目的adapter以便获取统一信息
	@Autowired
	RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	// 接口方法
	Map<String, Method> methodMaps = new HashMap<String, Method>();

	// 实际实现的方法体
	Map<String, Method> implMethodMaps = new HashMap<String, Method>();

	// 协议URL匹配模式
	private static final String PROTOL_URL_REGX = "/(\\w+-protol)/";
	// 方法匹配模式
	static final String METHOD_URL_REGX = "/([^/]+)\\.do";
	// 接口模式
	private boolean isInterfaceMode;
	// RPC解决者
	List<AbstractRPCResolver> RPCResolvers = new ArrayList<AbstractRPCResolver>();

	static ObjectMapper mapper = new ObjectMapper();

	// private PathMatcher pathMatcher = new AntPathMatcher();

	public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		String methodName = getFirstMatchFromURL(request.getServletPath(), METHOD_URL_REGX);
		String protolTypeName = getFirstMatchFromURL(request.getServletPath(), PROTOL_URL_REGX);
		Method method = methodMaps.get(methodName);
		JsonView view = new JsonView();
		View viewx = view.getView();

		try {
			for (AbstractRPCResolver resolver : RPCResolvers) {
				if (resolver.isSupport(protolTypeName)) {
					resolver.invoke(request, response);
					return;
				}
			}
			if (method == null) {
				throw new NoSuchMethodException("The Public WebService No This Method: " + methodName);
			}
			Object[] params = null;
			boolean isQWebClientProtol = request.getParameter("__qwebparam[0]") != null;
			if (isQWebClientProtol) {
				Type[] types = method.getGenericParameterTypes();
				params = new Object[types.length];
				for (int i = 0; i < types.length; i++) {
					String param = request.getParameter("__rpcparam[" + i + "]");
					params[i] = mapper.readValue(param, getJavaType(types[i]));
				}
			} else {
				// 普通controller 模式.
				ServletWebRequest webRequest = new ServletWebRequest(request, response);
				ModelAndViewContainer mavContainer = new ModelAndViewContainer();
				RequestParamsParser parser = new RequestParamsParser(this.getService(), implMethodMaps.get(methodName), requestMappingHandlerAdapter);
				params = parser.getMethodArgumentValues(webRequest, mavContainer, null);
			}
			view.setData(method.invoke(this.getService(), params));
			view.setStatus(200);
			viewx.render(view.getModel(), request, response);
		} catch (InvocationTargetException e) {
			view.setStatus(-500);
			view.setMessage(e.getTargetException().getMessage());
			logger.error(e.getTargetException().getMessage(), e);
			try {
				viewx.render(view.getModel(), request, response);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Throwable e) {
			view.setStatus(-500);
			view.setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
			try {
				viewx.render(view.getModel(), request, response);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.isInterfaceMode = this.getServiceInterface() != null ? true : false;
		String ClassName = getSimpleClassName();
		for (Method method : checkAndGetMethods()) {
			if ((!Modifier.isPublic(method.getModifiers()))) {
				continue;
			}
			String name = method.getName();
			if (methodMaps.containsKey(name)) {
				throw new RuntimeException("Method:" + name + ", has been mapping to url. There may be a method with the same name.");
			}
			methodMaps.put(name, method);
			Method methodImpl = getImplMethod(method);
			if (methodImpl != null) {
				implMethodMaps.put(name, methodImpl);
			}
			System.err.println("QWebService Method: " + ClassName + "." + name);
		}
		prepareRPCResolver();
	}

	JavaType getJavaType(Type type) {
		JavaType resultType;
		resultType = mapper.constructType(type);
		return resultType;
	}

	// add the RPC resolver..
	protected void prepareRPCResolver() {
		if (isInterfaceMode) {// if isInterface Mode
			this.RPCResolvers.add(new HessianRPCResolver(this, requestMappingHandlerAdapter.getApplicationContext()));
			this.RPCResolvers.add(new DuowanRPCResolver(this, requestMappingHandlerAdapter.getApplicationContext()));
		}
	}

	protected String getSimpleClassName() {
		return this.isInterfaceMode ? this.getServiceInterface().getSimpleName() : getActualClass().getSimpleName();
	}

	/**
	 * 获取Aop代理后实际的目标对象
	 * 
	 * @return
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
		if (StringUtil.isBlank(url)) {
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

}
