package cn.duapi.qweb.doc;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.util.ClassUtils;

import cn.duapi.qweb.annotation.QWebConfig;

public class QWebDocumentRender {

    static LocalVariableTableParameterNameDiscoverer localVariable = new LocalVariableTableParameterNameDiscoverer();

    private QWebDocumentRender() {
    }

    public static String generateSimpleAPIDocument(Map<String, Method> implMethodMaps, String apiUrl, String mainDesc) {

        StringBuilder sb = new StringBuilder();
        sb.append("Simple QWeb API Document\n");
        sb.append("========================\n");
        sb.append(mainDesc + "\n\n");
        sb.append("*Discard  doc=\"\" Settings in `@QWebService` to disable this document.*\n\n");

        Set<Class<?>> userBean = new HashSet<Class<?>>();

        for (Entry<String, Method> entry : implMethodMaps.entrySet()) {

            Method currMethod = entry.getValue();

            String[] parameterNames = localVariable.getParameterNames(entry.getValue());

            Class<?>[] paramsTypes = currMethod.getParameterTypes();

            QWebConfig config = currMethod.getAnnotation(QWebConfig.class);

            String methodDesc = config != null ? config.doc() : "";

            sb.append("##" + currMethod.getName()).append("\n");
            sb.append(methodDesc + "\n");
            sb.append("#####url:\n+ " + apiUrl).append(currMethod.getName()).append(".do\n\n");
            sb.append("#####params:\n");

            if (parameterNames.length == paramsTypes.length) {

                for (int i = 0; i < paramsTypes.length; i++) {
                    Class<?> clazz = paramsTypes[i];

                    if (ClassUtils.isPrimitiveOrWrapper(clazz) || String.class.equals(clazz) || Date.class.equals(clazz)) {
                        sb.append("+ ").append(parameterNames[i]).append(" | `").append(clazz.getSimpleName()).append("`\n");
                    } else {
                        userBean.add(clazz);
                        extractDefinedClass(sb, parameterNames[i], clazz, "\t");
                    }

                }

            }

            sb.append("#####return:\n+ `" + currMethod.getReturnType().getSimpleName() + "`");
            sb.append("\n\n");
        }

        return sb.toString();
    }

    static void extractDefinedClass(StringBuilder sb, String parameterName, Class<?> clazz, String placeHold) {
        sb.append("+ *").append(parameterName).append("* | `").append(clazz.getSimpleName()).append("`\n");
        for (PropertyDescriptor des : BeanUtils.getPropertyDescriptors(clazz)) {
            if (des.getWriteMethod() != null) {
                sb.append(placeHold + "+ " + des.getDisplayName() + " | `" + des.getPropertyType().getSimpleName() + "`\n");
            }
        }
    }

}
