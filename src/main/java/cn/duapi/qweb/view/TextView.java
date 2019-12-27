package cn.duapi.qweb.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

public class TextView extends AbstractView {

    private String message;

    public TextView(HttpStatus httpCode, final String message) {
        this.setStatus(httpCode);
        this.message = message;
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
