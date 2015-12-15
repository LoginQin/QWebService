package cn.duapi.qweb.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TextView extends AbstractView {

    private String message;
    private int httpCode;

    public TextView(int httpCode, final String message) {
        this.message = message;
    }

    @Override
    public int getHttpCode() {
        return this.httpCode;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String getContentType() {
        return "text/plain; charset=UTF-8";
    }

    @Override
    public String getBody(HttpServletRequest request, HttpServletResponse response) {
        String message = this.getMessage();
        return message;
    }
}
