package cn.duapi.qweb;

import org.springframework.web.servlet.ModelAndView;

/**
 * 获取用于渲染当前QWebViewService结果集的ModelAndView
 * <P>
 * 实现了这个ViewHandler的类, 将覆盖掉原来的JsonView, 而自定义输出内容
 * 
 * @author qinwei
 * 
 */
public interface QWebViewHandler {

    /**
     * 获取用于渲染当前QWebViewService结果集的ModelAndView
     * <P>
     * 可以自定义返回结果集的模板
     */
    public ModelAndView getResultView(String currMethodName, Object result);

    /**
     * 
     * @param currMethodName
     * @param ex
     */
    public ModelAndView getExceptionView(String currMethodName, Throwable ex);
}
