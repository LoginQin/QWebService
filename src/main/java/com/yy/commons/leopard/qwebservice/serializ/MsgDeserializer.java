package com.yy.commons.leopard.qwebservice.serializ;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.yy.commons.leopard.qwebservice.exception.RPCInvokeException;

public class MsgDeserializer extends JsonDeserializer {
	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		Object data = null;
		if (node.get("status").asInt() == 200) {
			data = node.get("data").asText();
		} else {
			data = new RPCInvokeException(node.get("message").asText());
		}
		return data;
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
