package com.yy.commons.leopard.rpcimpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.remoting.support.RemoteExporter;

import com.yy.commons.leopard.qwebservice.AbstractRPCResolver;
import com.yy.commons.leopard.qwebservice.exception.RPCInvokeException;

/**
 * TODO 待拆分实现..
 * 
 * @author qinwei
 * 
 */
public class QWebRPCResolver extends AbstractRPCResolver {
	private static final String PROTOL = "qweb-protol";

	public QWebRPCResolver(RemoteExporter masterExporter, ApplicationContext content) {
		super(masterExporter, content);
	}

	@Override
	public void invoke(HttpServletRequest request, HttpServletResponse response) throws RPCInvokeException {

	}

	@Override
	public String getSuportProtol() {
		return PROTOL;
	}

}
