package cn.duapi.qweb.view;

import java.lang.reflect.InvocationTargetException;

import org.springframework.web.servlet.ModelAndView;

import cn.duapi.qweb.QWebViewHandler;
import cn.duapi.qweb.model.JsonViewCode;

/**
 * This render is default Json render
 *
 * @author qinwei
 */
public class JsonViewRender implements QWebViewHandler {

    @Override
    public ModelAndView getResultView(String currMethodName, Object result) {
        JsonView view = new JsonView();
        view.setData(result);
        view.setCode(JsonViewCode.OK);
        return view;
    }

    @Override
    public ModelAndView getExceptionView(String currMethodName, Throwable ex) {
        JsonView view = new JsonView();
        if (ex instanceof InvocationTargetException) {
            view.setCode(JsonViewCode.REMOTE_EXCEPTION);
            Throwable targetException = ((InvocationTargetException) ex).getTargetException();
            view.setMessage(targetException.getMessage());
            view.setData(targetException.getClass().getName());
        } else {
            view.setMessage(ex.getMessage());
            view.setCode(JsonViewCode.ERROR);
        }
        return view;
    }
}
