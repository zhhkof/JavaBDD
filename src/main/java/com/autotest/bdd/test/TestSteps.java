package com.autotest.bdd.test;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by ZHH on 2016/11/20.
 */
public class TestSteps {
    @Given("I start test $url")
    public void startTest(String url) {
        System.out.println("start test" + url);
    }

    @When("send tcp request $req to $url")
    public void postToUrl(String var, String url) throws IOException, URISyntaxException {
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
//            url = Configuration.getProperty("base.url") + url;//依赖sihua架构，暂删
            url = "http://www.baidu.com";
        }

        URL _url = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
/*            conn.setConnectTimeout((int) Configuration.getTimeout() / 2);
            conn.setReadTimeout((int) Configuration.getTimeout() / 2);*/
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

//            if (headers != null) {//暂不考虑header
//                for (String s : headers.split(",")) {
//                    String[] parts = s.split(":");
//                    conn.setRequestProperty(parts[0].trim(), parts[1].trim());
//                }
//            }

//            String value = Environment.getCurrentExample().get(var);//先当作配置项取值，为空则直接赋值
//            if (value == null) {//依赖sihua架构，暂删
//                value = var; // 有歧义性，但为了向下兼容先这么写
//            }

            OutputStream out = conn.getOutputStream();
//            IOUtils.write(value.getBytes("UTF-8"), out);
            IOUtils.write(var.getBytes("UTF-8"), out);
            IOUtils.closeQuietly(out);

            String content = IOUtils.toString(conn.getInputStream(), "UTF-8");
            System.out.println(content);
//            if (var1 != null) {//依赖sihua架构，暂删
//                Environment.getCurrentExample().put(var1, content);
//            }
        } finally {
            conn.disconnect();
        }
    }

    @Given("There is a student")
    public void initStudent() {
        System.out.println("sssss");
    }
    @Then("he is a student")
    public void heis() {
        System.out.println("ssssssssssss");
    }
}
