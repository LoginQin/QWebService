package cn.duapi.qweb.serializ;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import cn.duapi.qweb.exception.RPCInvokeException;
import cn.duapi.qweb.model.InvokeResult;

public class MsgDeserializer extends JsonDeserializer<InvokeResult> {

    @Override
    public InvokeResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		Object data = null;
        int status = node.get("status").asInt();
        if (status == 200) {
            data = node.get("data").toString();
		} else {
			data = new RPCInvokeException(node.get("message").asText());
		}
        InvokeResult result = new InvokeResult();
        result.setStatus(status);
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
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (JsonMappingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
}
