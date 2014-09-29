package com.yy.commons.leopard.qwebservice;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

/**
 * 请求参数解析
 * 
 * @author qinwei
 * 
 */
public class RequestParamsParser extends HandlerMethod {

	public RequestParamsParser(Object bean, Method method, RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
		super(bean, method);
		this.argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
		this.dataBinderFactory = new ServletRequestDataBinderFactory(null, requestMappingHandlerAdapter.getWebBindingInitializer());
	}

	private final static Logger logger = LoggerFactory.getLogger(RequestParamsParser.class);

	private HandlerMethodArgumentResolverComposite argumentResolvers;

	private WebDataBinderFactory dataBinderFactory;
	// ServletRequestDataBinder ServletRequestDataBinderFactory
	// new DefaultDataBinderFactory(this.webBindingInitializer)

	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	/**
	 * 从请求解析出参数
	 * 
	 * @param request
	 * @param mavContainer
	 * @param providedArgs
	 * @return
	 * @throws Exception
	 */
	public Object[] getMethodArgumentValues(NativeWebRequest request, ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception {

		MethodParameter[] parameters = getMethodParameters();
		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			MethodParameter parameter = parameters[i];
			parameter.initParameterNameDiscovery(parameterNameDiscoverer);
			GenericTypeResolver.resolveParameterType(parameter, getBean().getClass());

			args[i] = resolveProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}

			if (argumentResolvers.supportsParameter(parameter)) {
				try {
					// 进来这里之前, dataBinderFactory = ServletRequestDataBinder 而不是defaultDataBinderFactory
					args[i] = argumentResolvers.resolveArgument(parameter, mavContainer, request, dataBinderFactory);
					continue;
				} catch (Exception ex) {
					if (logger.isTraceEnabled()) {
						logger.trace(getArgumentResolutionErrorMessage("Error resolving argument", i), ex);
					}
					throw ex;
				}
			}

			if (args[i] == null) {
				String msg = getArgumentResolutionErrorMessage("No suitable resolver for argument", i);
				throw new IllegalStateException(msg);
			}
		}
		return args;
	}

	private String getArgumentResolutionErrorMessage(String message, int index) {
		MethodParameter param = getMethodParameters()[index];
		message += " [" + index + "] [type=" + param.getParameterType().getName() + "]";
		return getDetailedErrorMessage(message);
	}

	/**
	 * Adds HandlerMethod details such as the controller type and method signature to the given error message.
	 * 
	 * @param message
	 *            error message to append the HandlerMethod details to
	 */
	protected String getDetailedErrorMessage(String message) {
		StringBuilder sb = new StringBuilder(message).append("\n");
		sb.append("HandlerMethod details: \n");
		sb.append("Controller [").append(getBeanType().getName()).append("]\n");
		sb.append("Method [").append(getBridgedMethod().toGenericString()).append("]\n");
		return sb.toString();
	}

	/**
	 * Attempt to resolve a method parameter from the list of provided argument values.
	 */
	private Object resolveProvidedArgument(MethodParameter parameter, Object... providedArgs) {
		if (providedArgs == null) {
			return null;
		}
		for (Object providedArg : providedArgs) {
			if (parameter.getParameterType().isInstance(providedArg)) {
				return providedArg;
			}
		}
		return null;
	}

	public WebDataBinderFactory getDataBinderFactory() {
		return dataBinderFactory;
	}

	public void setDataBinderFactory(WebDataBinderFactory dataBinderFactory) {
		this.dataBinderFactory = dataBinderFactory;
	}

}
