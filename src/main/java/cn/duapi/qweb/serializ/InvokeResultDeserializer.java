package cn.duapi.qweb.serializ;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.type.JavaType;

import cn.duapi.qweb.exception.RPCInvokeException;
import cn.duapi.qweb.model.InvokeResult;
import cn.duapi.qweb.utils.JsonUtils;

@SuppressWarnings("unchecked")
public class InvokeResultDeserializer {

	static ConcurrentHashMap<Type, JavaType> cache = new ConcurrentHashMap<Type, JavaType>();

    static ObjectMapper mapper = JsonUtils.JSON_MAPPER;

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
            return result;
		} catch (Exception e) {
			throw new RPCInvokeException(e.getMessage());
		}
        //throw new RPCInvokeException("can't deserializ " + type.getClass());
	}

	JavaType getJavaType(Type type) {// cache result 性能并不明显.
		return mapper.constructType(type);
	}

}
