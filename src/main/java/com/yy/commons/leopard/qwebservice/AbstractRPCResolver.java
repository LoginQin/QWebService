package com.yy.commons.leopard.qwebservice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.remoting.support.RemoteExporter;

import com.yy.commons.leopard.qwebservice.exception.RPCInvokeException;

/**
 * RPC Resolver
 * 
 * @author qinwei
 * 
 */
public abstract class AbstractRPCResolver {
	// master Exproter
	RemoteExporter masterExproter;

	ApplicationContext content;

	public AbstractRPCResolver(RemoteExporter masterExporter, ApplicationContext content) {
		this.masterExproter = masterExporter;
		this.content = content;
	}

	/**
	 * support the match url
	 * <P>
	 * URL may alway "xxxx-protol"
	 * 
	 * @param protol
	 * @return
	 */
	public boolean isSupport(String protol) {
		return this.getSuportProtol().equalsIgnoreCase(protol);
	}

	public abstract String getSuportProtol();

	public abstract void invoke(HttpServletRequest request, HttpServletResponse response) throws RPCInvokeException;

	public RemoteExporter getMasterExproter() {
		return masterExproter;
	}

	public ApplicationContext getContent() {
		return content;
	}

	public void setMasterExproter(RemoteExporter masterExproter) {
		this.masterExproter = masterExproter;
	}

	public void setContent(ApplicationContext content) {
		this.content = content;
	}
}
