package cn.duapi.qweb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class WebServiceExporterTest {

    @Test
    public void test1() {
        String REGX = "/(\\w+-protol)/";
        Pattern p = Pattern.compile(REGX, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("/testxx/hessian-protol/ResourceWebService/");
        while (m.find()) {
            System.out.println(m.group(1));
        }
    }
}