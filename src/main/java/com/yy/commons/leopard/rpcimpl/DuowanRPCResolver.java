package com.yy.commons.leopard.rpcimpl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.remoting.support.RemoteExporter;

import com.duowan.common.rpc.server.RPCServiceExporter;
import com.yy.commons.leopard.qwebservice.AbstractRPCResolver;
import com.yy.commons.leopard.qwebservice.exception.RPCInvokeException;

/**
 * 多玩RPC解析器 多玩RPC的协议为
 * <P>
 * URL => /接口名称/方法名
 * <P>
 * 客户端使用 com.duowan.common.rpc.client.RPCProxyFactoryBean 创建连接到服务器
 * <P>
 * DUOWAN RPC有个弊端:
 * <P>
 * 服务器上, 不支持执行被代理后的接口.
 * 
 * @author qinwei
 * 
 */
public class DuowanRPCResolver extends AbstractRPCResolver {

	RPCServiceExporter serviceExporter = new RPCServiceExporter();

	private static final String PROTOL = "duowan-protol";

	public DuowanRPCResolver(RemoteExporter masterExporter, ApplicationContext content) {
		super(masterExporter, content);
		serviceExporter.setService(masterExporter.getService());
		serviceExporter.setServiceInterface(masterExporter.getServiceInterface());
		serviceExporter.setApplicationContext(content);
		try {
			serviceExporter.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void invoke(HttpServletRequest request, HttpServletResponse response) throws RPCInvokeException {
		try {
			serviceExporter.handleRequest(request, response);
		} catch (ServletException e) {
			throw new RPCInvokeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RPCInvokeException(e.getMessage(), e);
		}

	}

	@Override
	public String getSuportProtol() {
		return PROTOL;
	}

}
