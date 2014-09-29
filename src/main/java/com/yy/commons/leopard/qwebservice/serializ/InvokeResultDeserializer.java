package com.yy.commons.leopard.qwebservice.serializ;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.type.JavaType;

import com.yy.commons.leopard.qwebservice.exception.RPCInvokeException;
import com.yy.commons.leopard.qwebservice.model.InvokeResult;

@SuppressWarnings("unchecked")
public class InvokeResultDeserializer {

	static ConcurrentHashMap<Type, JavaType> cache = new ConcurrentHashMap<Type, JavaType>();
	static ObjectMapper mapper = new ObjectMapper();
	static {
		SimpleModule module = new SimpleModule("InvokeResult", new Version(1, 0, 0, null));
		module.addDeserializer(InvokeResult.class, new MsgDeserializer());
		mapper.registerModule(module);
	}

	public Object deserializ(String str, Type type) throws RPCInvokeException {
		try {
			Object result = mapper.readValue(str, InvokeResult.class);
			if (result instanceof RPCInvokeException) {
				throw (RPCInvokeException) result;
			}
			if (result instanceof String) {
				return mapper.readValue((String) result, getJavaType(type));
			}
		} catch (Exception e) {
			throw new RPCInvokeException(e.getMessage());
		}
		throw new RPCInvokeException("can't deserializ " + type.getClass());
	}

	JavaType getJavaType(Type type) {// cache result 性能并不明显.
		return mapper.constructType(type);
	}

}
