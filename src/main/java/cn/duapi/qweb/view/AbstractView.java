package cn.duapi.qweb.view;

import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * 抽象视图
 * 
 * @from Leopard
 */
public abstract class AbstractView extends ModelAndView {

    private AbstractUrlBasedView view = new AbstractUrlBasedView() {

        @Override
        protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            String body = AbstractView.this.getBody(request, response);
            if (body == null) {
                return;
            }
            response.setContentType(AbstractView.this.getContentType());
            response.setContentLength(body.getBytes().length);
            response.setCharacterEncoding(getCharacterEncoding());
            // Flush byte array to servlet output stream.

            Writer out = response.getWriter();
            out.write(body);
            out.flush();
        }
    };

    public AbstractView() {
        super.setView(view);
    }

    public int getHttpCode() {
        return 200;
    }
    public abstract String getContentType();

    public String getCharacterEncoding() {
        return "UTF-8";
    }

    public abstract String getBody(HttpServletRequest request, HttpServletResponse response);

}
