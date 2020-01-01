package cn.duapi.qweb.serializ;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import cn.duapi.qweb.exception.RPCInvokeException;
import cn.duapi.qweb.exception.RemoteCauseException;
import cn.duapi.qweb.model.InvokeResult;
import cn.duapi.qweb.model.JsonViewCode;

/**
 * 自定义反序列化返回值
 *
 * @Author qinwei
 */
public class MsgDeserializer extends JsonDeserializer<InvokeResult> {

    @Override
    public InvokeResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        Object data;
        int code = node.get("code").asInt();
        InvokeResult result = new InvokeResult();
        if (code == JsonViewCode.OK) {
            data = node.get("data").toString();
        } else if (code == JsonViewCode.REMOTE_EXCEPTION) {
            String clazz = node.get("data").asText();
            throw new RemoteCauseException("remote server throw an exception:" + clazz + ", msg=>"
                    + node.get("message").asText(), clazz);
        } else {
            data = new RPCInvokeException(node.get("message").asText());
        }
        result.setCode(code);
        result.setData(data);
        return result;
    }

    // public static void main(String[] args) {
    // String str = "{\"status\":200,\"message\":null,\"data\":2}";
    // ObjectMapper mapper = new ObjectMapper();
    // SimpleModule module = new SimpleModule("InvokeResult", new Version(1, 0, 0, null));
    // module.addDeserializer(InvokeResult.class, new MsgDeserializer());
    // mapper.registerModule(module);
    // try {
    // Object data = mapper.readValue(str, InvokeResult.class);
    // } catch (JsonParseException e) {
    // e.printStackTrace();
    // } catch (JsonMappingException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
}
