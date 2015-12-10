package com.yy.commons.leopard.qwebservice.view;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.duowan.leopard.web.mvc.JsonpUtil;
import com.yy.commons.leopard.qwebservice.utils.JsonpUtils;
import com.yy.commons.leopard.qwebservice.utils.QWebViewUtils;

public class JsonView extends AbstractView {

    private int status;
    private String message;
    private Object data;

    public JsonView() {

    }

    public JsonView(Object data) {
        this(200, data);
    }

    public JsonView(int status, Object data) {
        this.status = status;
        this.data = data;
        this.message = "";
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getContentType() {
        return "text/plain; charset=UTF-8";
    }

    /**
     * 
     * 1、默认返回json <br/>
     * URL:http://message.game.yy.com/test/json.do<br/>
     * 返回:{"status":200,"message":"","data":{"username":"hctan","nickname":
     * "ahai"}}<br/>
     * 2、自定义callback参数，返回jsonp<br/>
     * URL:http://message.game.yy.com/test/json.do?callback=callback2 <br/>
     * 返回:callback2({"status":200,"message":"","data":{"username":"hctan",
     * "nickname":"ahai"}}); <br/>
     * 3、非法callback参数 <br/>
     * URL:http://message.game.yy.com/test/json.do?callback=callback.aa <br/>
     * 返回:// 非法callback[callback.aa]. <br/>
     * 
     * 4、自定义var参数，返回script <br/>
     * URL:http://message.game.yy.com/test/json.do?var=abc <br/>
     * 返回:var abc =
     * {"status":200,"message":"","data":{"username":"hctan","nickname"
     * :"ahai"}}; <br/>
     * 5、非法var参数 <br/>
     * URL:http://message.game.yy.com/test/json.do?var=var.aa <br/>
     * 返回:// 非法var[var.aa].
     */
    @Override
    public String getBody(HttpServletRequest request, HttpServletResponse response) {
        {
            String callback = request.getParameter("callback");
            if (StringUtils.isNotEmpty(callback)) {
                return this.toJsonp(callback);
            }
        }
        {
            String var = request.getParameter("var");
            if (StringUtils.isNotEmpty(var)) {
                return this.toScript(var);
            }
        }
        return this.toJson();// 普通json
    }

    /**
     * 返回结果.
     * 
     * @return
     */
    protected Map<String, Object> getResult() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("status", this.getStatus());
        map.put("message", this.message);
        map.put("data", this.getData());
        return map;
    }

    protected String toJson() {
        String message = QWebViewUtils.toJson(this.getResult());
        return message;
    }

    protected String toJsonp(String callback) {
        try {
            JsonpUtils.checkCallback(callback);
            String json = this.toJson();
            return callback + "(" + json + ");";
        } catch (Exception e) {
            return "// " + e.getMessage();
        }
    }

    protected String toScript(String var) {
        try {
            JsonpUtil.checkVar(var);
            String json = this.toJson();
            return "var " + var + " = " + json + ";";
        } catch (Exception e) {
            return "// " + e.getMessage();
        }
    }
}
