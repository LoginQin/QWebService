package cn.duapi.qweb;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * @author qinwei
 */
public class WebServiceUrlHandlerMapping extends SimpleUrlHandlerMapping {
    @Override
    protected void registerHandlers(Map<String, Object> urlMap) throws BeansException {
        if (urlMap.isEmpty()) {
            logger.warn("Neither 'urlMap' nor 'mappings' set on SimpleUrlHandlerMapping");
        } else {
            for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                String url = entry.getKey();
                Object handler = entry.getValue();
                // Prepend with slash if not already present.
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                if (!url.endsWith("/")) {
                    url += "/";
                }
                // Remove whitespace from handler bean name.
                if (handler instanceof String) {
                    handler = ((String) handler).trim();
                }
                // match all url in this path ..
                url += "**";
                registerHandler(url, handler);
            }
            setOrder(Ordered.HIGHEST_PRECEDENCE);
        }
    }




}
