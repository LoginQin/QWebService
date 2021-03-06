package cn.duapi.qweb.serializ;

import java.lang.reflect.Type;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import cn.duapi.qweb.exception.JsonRuntimeException;
import cn.duapi.qweb.exception.RPCInvokeException;
import cn.duapi.qweb.exception.RemoteCauseException;
import cn.duapi.qweb.model.InvokeResult;
import cn.duapi.qweb.utils.JsonUtils;

public class InvokeResultDeserializer {

    static ObjectMapper mapper = JsonUtils.JSON_MAPPER;

    static {
        SimpleModule module = new SimpleModule("InvokeResult", new Version(1, 0, 0, null, null, null));

        module.addDeserializer(InvokeResult.class, new MsgDeserializer());

        mapper.registerModule(module);
    }

    public Object deserializ(String str, Type type) throws JsonRuntimeException {
        try {
            InvokeResult invokeResult = JsonUtils.toObject(str, InvokeResult.class);

            Object result = invokeResult.getData();

            if (result instanceof RPCInvokeException) {
                throw (RPCInvokeException) result;
            }

            if (result instanceof String) {
                return JsonUtils.toObject((String) result, type);
            }

            return result;
        } catch (RPCInvokeException e) {
            throw new RemoteCauseException(e.getMessage(), e);
        } catch (RemoteCauseException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonRuntimeException("Unexpected REMOTE RETURN=>" + str, e);
        }
    }

    String getLimitString(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        return str.length() > 20 ? str.substring(0, 20) : str;
    }

}
