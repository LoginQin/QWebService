package cn.duapi.qweb.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Markpage View, Markdown document view
 * 
 * @link http://github.com/LoginQin/markpage
 * @author qinwei
 * 
 */
public class MarkpageView extends AbstractView {

    String data;

    final static String MarkpageScript = "<script src=\"http://cdnresource.duowan.com/kkdict/1/markpage.js\" type=\"text/javascript\"></script>";

    public MarkpageView(String data) {
        this.data = data;
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String getBody(HttpServletRequest request, HttpServletResponse response) {
        return "<meta charset=\"utf8\"><pre id=\"markpage\">\n" + this.data + "</pre>" + MarkpageScript;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
