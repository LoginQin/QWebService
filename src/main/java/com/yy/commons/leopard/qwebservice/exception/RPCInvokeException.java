package com.yy.commons.leopard.qwebservice.exception;


public class RPCInvokeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RPCInvokeException(String msg, Throwable e) {
		super(msg, e);
	}

	public RPCInvokeException(String msg) {
		super(msg);
	}

}
