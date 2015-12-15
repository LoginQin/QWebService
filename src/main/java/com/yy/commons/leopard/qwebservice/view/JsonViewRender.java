package com.yy.commons.leopard.qwebservice.view;

import org.springframework.web.servlet.ModelAndView;

import com.yy.commons.leopard.qwebservice.QWebViewHandler;

/**
 * This render is default Json render
 * 
 * @author qinwei
 * 
 */
public class JsonViewRender implements QWebViewHandler {

    @Override
    public ModelAndView getResultView(String currMethodName, Object result) {
        JsonView view = new JsonView();
        view.setData(result);
        view.setStatus(200);
        return view;
    }

    @Override
    public ModelAndView getExceptionView(String currMethodName, Throwable ex) {
        JsonView view = new JsonView();
        view.setStatus(-400);
        view.setMessage(ex.getMessage());
        return view;
    }
}
