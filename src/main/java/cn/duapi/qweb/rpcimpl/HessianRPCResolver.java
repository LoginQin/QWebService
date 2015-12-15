package cn.duapi.qweb.rpcimpl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.remoting.caucho.HessianExporter;
import org.springframework.remoting.support.RemoteExporter;

import cn.duapi.qweb.AbstractRPCResolver;
import cn.duapi.qweb.exception.RPCInvokeException;

/**
 * Hessian协议解析者
 * <P>
 * 匹配路径 URL/hessian-protol
 * <P>
 * Hessian协议的弊端:
 * <P>
 * 所有的对象必须是实现 Serializable, 而可序列化的.
 * 
 * @author qinwei
 * 
 */
public class HessianRPCResolver extends AbstractRPCResolver {
	final static String PROTOL_NAME = "hessian-protol";
	HessianExporter hessianExporter = new HessianExporter();

	public HessianRPCResolver(RemoteExporter masterExporter, ApplicationContext content) {
		super(masterExporter, content);
		hessianExporter.setServiceInterface(masterExporter.getServiceInterface());
		hessianExporter.setService(masterExporter.getService());
		hessianExporter.afterPropertiesSet();
	}

	@Override
	public void invoke(HttpServletRequest request, HttpServletResponse response) throws RPCInvokeException {
		try {
			hessianExporter.invoke(request.getInputStream(), response.getOutputStream());
		} catch (IOException e) {
			throw new RPCInvokeException(e.getMessage(), e);
		} catch (Throwable e) {
			throw new RPCInvokeException(e.getMessage(), e);
		}
	}

	@Override
	public String getSuportProtol() {
		return PROTOL_NAME;
	}
}
