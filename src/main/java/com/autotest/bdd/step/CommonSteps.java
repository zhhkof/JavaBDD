package com.autotest.bdd.step;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import com.autotest.bdd.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.annotations.*;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Created by zhh on 16-11-21.
 */
public class CommonSteps extends StepsSupport {

    private String fsp = File.separator;

    public static class UniversalNamespaceResolver implements NamespaceContext {

        private Document sourceDocument;

        public UniversalNamespaceResolver(Document document) {
            sourceDocument = document;
        }

        public String getNamespaceURI(String prefix) {
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return sourceDocument.lookupNamespaceURI(null);
            } else {
                return sourceDocument.lookupNamespaceURI(prefix);
            }
        }

        public String getPrefix(String namespaceURI) {
            return sourceDocument.lookupPrefix(namespaceURI);
        }

        @SuppressWarnings("rawtypes")
        public Iterator getPrefixes(String namespaceURI) {
            throw new UnsupportedOperationException();
        }

    }

    private static final Logger logger = Logger.getLogger(CommonSteps.class);

    private static Set<String> marks = new HashSet<String>();

    public CommonSteps(ConfigurableEmbedder embedder) {
        super(embedder);
    }

    @AfterStories
    public void afterStories() {
        // 保存本次运行的上下文
        File restoreFile = new File(Configuration.getProperty("restore.file", "bdd-context"));
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(restoreFile));
            out.writeObject(ExpressionEngine.context);//随机数保存
            out.writeObject(marks);
        } catch (IOException e) {
            logger.error("保存运行的上下文失败", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        // 停止测试辅助用HTTP服务
        RemoteWebDriver.quit();
        HttpServer.stop();
    }

//    @Then(value = "在$content中，XPath:$xpath的值应该是$value", priority = 1)
//    public void assertByXPath(String content, String xpath, String value) throws ParserConfigurationException, SAXException, IOException, XPathException {
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true);
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(new ByteArrayInputStream(Environment.getCurrentExample().get(content).getBytes("UTF-8")));
//
//        XPathFactory xpathFactory = XPathFactory.newInstance();
//        XPath xpath0 = xpathFactory.newXPath();
//        xpath0.setNamespaceContext(new UniversalNamespaceResolver(document));
//        XPathExpression expression = xpath0.compile(xpath);
//
//        String result = (String) expression.evaluate(document, XPathConstants.STRING);
//        assertValue(value, result);
//    }

//    @Then(value = "- $step", priority = Integer.MAX_VALUE)
//    public void assertInContext(String step) {
//        try {
//            Environment.setInContext(true);
//            then(step);
//        } finally {
//            Environment.setInContext(false);
//        }
//    }

    @BeforeStories
    @SuppressWarnings("unchecked")
    public void beforeStories() {
        RemoteWebDriver.open();
        // 检查是否处于恢复模式，若是，则恢复上一次运行的上下文
        if ("on".equals(Configuration.getProperty("restore.enabled", "off"))) {
            File restoreFile = new File(Configuration.getProperty("restore.file", "bdd-context"));
            if (!restoreFile.exists()) {
                logger.warn("文件" + restoreFile.getAbsolutePath() + "不存在，无法恢复上一次运行的上下文");
            } else {
                ObjectInputStream in = null;
                try {
                    in = new ObjectInputStream(new FileInputStream(restoreFile));
                    ExpressionEngine.context = (ExpressionEngine.Context) in.readObject();
                    marks = (Set<String>) in.readObject();
                } catch (IOException e) {
                    logger.error("恢复上一次运行的上下文失败", e);
                } catch (ClassNotFoundException e) {
                    logger.error("恢复上一次运行的上下文失败", e);
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        }

        // 启动测试辅助用HTTP服务
        try {
            HttpServer.start();
        } catch (IOException e) {
            logger.error("启动HTTP服务失败", e);
        }
        System.out.println("执行到这就是都准备好了呀");

        //jira bug 提交代码模块，暂不处理，后续借鉴。
//        if (("true").equals(Configuration.getProperty("bug.submit"))) {
//            Environment.addFailedCallback(new Runnable() {
//                @Override
//                public void run() {
////                    Throwable cause = Environment.getCurrentCause();
//                    String resultUrl = System.getProperty("result.url");
//                    String resultKey = System.getProperty("result.key");
//                    String jiraFtpUrl = Configuration.getProperty("jira.ftp.url", "192.168.2.21");
//                    String jiraFtpUser = Configuration.getProperty("jira.ftp.user", "ftpuser");
//                    String jiraFtpPwd = Configuration.getProperty("jira.ftp.pwd", "ftp78kxtw");
//                    String jiraHttpServer = Configuration.getProperty("jira.httpserver", "http://jira.sihuatech.com/");
//                    String s[] = Environment.getCurrentStoryPath().split("\\.");
//                    StringBuffer descript = new StringBuffer("失败的步骤为:" + Environment.getCurrentStep());
//                    descript.append(" 对应的key： " + resultKey);
//                    descript.append(" bamboo上的url为： " + resultUrl);
//                    // if (cause.getCause().toString().contains("TimeoutException")||cause.getCause().toString().contains("AssertionError")) {
//                    SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");// 设置日期格式
//                    String fileNameimg = df.format(new Date()) + ".jpeg";
//                    String fileNametxt = df.format(new Date()) + ".txt";
//                    try {
//                        InputStream input = JiraSubmit.takeScreenShot(RemoteWebDriver.instance());
//                        JiraSubmit.uploadFile(jiraFtpUrl, jiraFtpUser, jiraFtpPwd, fileNameimg, input);
//                        descript.append(" 截图为: " + jiraHttpServer + fileNameimg);
//                        File logFile = new File("jiralog.txt");
//                        if (logFile.exists()) {
//                            InputStream inputLog = new FileInputStream(logFile);
//                            JiraSubmit.uploadFile(jiraFtpUrl, jiraFtpUser, jiraFtpPwd, fileNametxt, inputLog);
//                            descript.append(" 日志为: " + jiraHttpServer + fileNametxt);
//                        } else {
//                            descript.append(" 未截取到日志！");
//                        }
//                    } catch (IOException e) {
//                        System.out.println("submit fail!");
//                        e.printStackTrace();
//                    }
//                    // }
//                    JiraSubmit.submit("jira-admin", "2.21j1ra", "BDDRST", s[0], descript.toString(), "IT Bug", Configuration.getProperty("jira.module", "debug"));
//                }
//            });
//        }
    }

    //    @Given(value = "$address可访问", priority = -1)
    @Given(value = "$address can be accessed", priority = -1)
    public void checkConnection(String address) {
        try {
            if (address.startsWith("http://") || address.startsWith("https://")) {
                // HTTP(S)检查
                HttpURLConnection conn = (HttpURLConnection) new URL(address).openConnection();
                conn.setConnectTimeout((int) Configuration.getTimeout() / 2);
                conn.setReadTimeout((int) Configuration.getTimeout() / 2);
                assertTrue(address + "不可访问", conn.getResponseCode() != -1);
            } else {
                int index = address.indexOf(':');
                if (index == -1) {
                    // ICMP检查
                    assertTrue(address + "不可访问", InetAddress.getByName(address).isReachable((int) Configuration.getTimeout()));
                } else {
                    // TCP检查
                    String host = address.substring(0, index);
                    int port = Integer.parseInt(address.substring(index + 1));

                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), (int) Configuration.getTimeout());
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            fail(address + "不可访问");
        }
    }

    //    @Given(value = "$name成功", priority = -1)
//    public void checkMark(String name) {
//        Assert.assertTrue(marks.contains(name));
//    }
    protected org.openqa.selenium.remote.RemoteWebDriver getDriver() {
        return RemoteWebDriver.instance();
    }

    @Given("start test")
    public void startTest() throws IOException {
        System.out.println("start test");
        String browser = Configuration.getProperty("browser", "firefox");
        if ("chrome".equals(browser)) {
            getDriver().get("chrome://downloads/");
        } else {
            getDriver().get("about:blank");
        }
        getDriver().get(Configuration.getProperty("base.url"));
    }


    @When("open webpage $url")
    public void openBrowser(String url) {
        getDriver().get(url);
    }


    //    @When("关闭浏览器")
    @When("close browser")//不建议使用，关了再看有实例化和session的问题，建议restart。
    public void closeBrowser() {
        getDriver().quit();
    }

//    @Then("在$content中，应该包含$sub")
//    public void contains(String content, String sub) {
//        assertTrue(content.contains(sub));
//    }
//
//    @Then(value = "在$content中，应该包含$sub1或$sub2", priority = 1)
//    public void contains2(String content, String sub1, String sub2) {
//        assertTrue(content.contains(sub1) || content.contains(sub2));
//    }
//
//    @Then("在$content中，包含$sub的个数为$n")
//    public void containsNumber(String content, String sub, int n) {
//        assertTrue(StringUtils.countMatches(content, sub) == n);
//    }
//
//    @When(value = "- $step", priority = Integer.MAX_VALUE)
//    public void doInContext(String step) {
//        try {
//            Environment.setInContext(true);
//            when(step);
//        } finally {
//            Environment.setInContext(false);
//        }
//    }
//
//
//    @When(value = "获取$content中XPath:$xpath的值为$var", priority = 1)
//    public void getByXPath(String content, String xpath, String var) throws ParserConfigurationException, SAXException, IOException, XPathException {
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true);
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        String xml = Environment.getCurrentExample().get(content);
//        logger.debug("截取到的"+content+"内容："+xml);
//        if (!xml.startsWith("<")) {
//            xml = xml.substring(xml.indexOf("<"));
//        }
//        logger.debug("实际用来解析的"+content+"内容："+xml);
//        ByteArrayInputStream s = new ByteArrayInputStream(xml.getBytes("UTF-8"));
//        Document document = builder.parse(s);
//
//        XPathFactory xpathFactory = XPathFactory.newInstance();
//        XPath xpath0 = xpathFactory.newXPath();
//        xpath0.setNamespaceContext(new UniversalNamespaceResolver(document));
//        XPathExpression expression = xpath0.compile(xpath);
//
//        String result = (String) expression.evaluate(document, XPathConstants.STRING);
//        Environment.getCurrentExample().put(var, result);
//    }
//
//    @When("获取文件$path的内容为$var")
//    public void getFromFile(String path, String var) throws IOException {
//        getFromFileWithEncoding("UTF-8", path, var);
//    }
//
//    @When("获取$encoding编码文件$path的内容为$var")
//    public void getFromFileWithEncoding(String encoding, String path, String var) throws IOException {
//        InputStream in;
//
//        try {
//            // 判断文件路径是否为URL，若是，直接打开URL
//            URL url = new URL(path);
//            in = url.openStream();
//        } catch (MalformedURLException ignore) {
//            // 文件路径不是合法的URL，则作为本地文件路径处理
//            File file;
//            if(fsp.equals("\\")){
//                path = path.replace("/", "\\");
//            }else{
//                path = path.replace("\\", "/");
//            }
//            if (path.startsWith("/") || (path.length() > 1 && path.charAt(1) == ':')) {
//                file = new File(path);
//            } else {
//                file = new File(Configuration.getStoryHome(), path);
//            }
//            in = new FileInputStream(file);
//        }
//
//        try {
//            Environment.getCurrentExample().put(var, IOUtils.toString(in, encoding));
//        } finally {
//            in.close();
//        }
//    }
//
//    @When(value = "LOG $msg", priority = Integer.MAX_VALUE)
//    public void log(String msg) {
//        // EMPTY
//    }
//
//    @Then(value = "$name成功", priority = -1)
//    public void mark(String name) {
//        marks.add(name);
//    }
//
//    @Then("在$content中，应该不包含$sub")
//    public void notContains(String content, String sub) {
//        assertFalse(content.contains(sub));
//    }
//
//    @When("发送$var到接口：$url")
//    public void postToURL(String var, String url) throws IOException, URISyntaxException {
//        postToURL(var, url, null);
//    }
//
//    @When(value = "发送$var到接口：$url，获取返回内容为$var1", priority = 1)
//    public void postToURL(String var, String url, String var1) throws IOException, URISyntaxException {
//        postToURL(null, var, url, var1);
//    }
//
//    @When(value = "添加报头$headers并发送$var到接口：$url，获取返回内容为$var1", priority = 2)
//    public void postToURL(String headers, String var, String url, String var1) throws IOException, URISyntaxException {
//        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
//            url = Configuration.getProperty("base.url") + url;
//        }
//
//        URL _url = new URL(url);
//        HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
//        try {
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
///*            conn.setConnectTimeout((int) Configuration.getTimeout() / 2);
//            conn.setReadTimeout((int) Configuration.getTimeout() / 2);*/
//            conn.setConnectTimeout(60000);
//            conn.setReadTimeout(60000);
//            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
//
//            if (headers != null) {
//                for (String s : headers.split(",")) {
//                    String[] parts = s.split(":");
//                    conn.setRequestProperty(parts[0].trim(), parts[1].trim());
//                }
//            }
//
//            String value = Environment.getCurrentExample().get(var);
//            if (value == null) {
//                value = var; // 有歧义性，但为了向下兼容先这么写
//            }
//
//            OutputStream out = conn.getOutputStream();
//            IOUtils.write(value.getBytes("UTF-8"), out);
//            IOUtils.closeQuietly(out);
//
//            String content = IOUtils.toString(conn.getInputStream(), "UTF-8");
//            if (var1 != null) {
//                Environment.getCurrentExample().put(var1, content);
//            }
//        } finally {
//            conn.disconnect();
//        }
//    }
//
//    @Deprecated
//    @When(value = "添加hearder：$var0并发送$var1到接口：$url，获取返回内容为$var2", priority = 2)
//    public void postToURL0(String var0, String var, String url, String var1) throws IOException, URISyntaxException {
//        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
//            url = Configuration.getProperty("base.url") + url;
//        }
//
//        HttpClient client = new DefaultHttpClient();
//
//        HttpPost post = new HttpPost();
//        post.setURI(new URL(url).toURI());
//        if (var0 != null) {
//            String hearders = Environment.getCurrentExample().get(var0);
//            String[] hds;
//            String[] hd;
//            if (hearders == null || "".equals(hearders)) {
//                hds = var0.split(",");
//            } else {
//                hds = hearders.split(",");
//            }
//            for (int x = 0; x < hds.length; x++) {
//                hd = hds[x].split(":");
//                post.setHeader(hd[0], hd[1]);
//            }
//        }
//        String value = Environment.getCurrentExample().get(var);
//        if(StringUtils.isNotBlank(value)) {
//            post.setEntity(new StringEntity(Environment.getCurrentExample().get(var), null, "UTF-8"));
//        } else {
//            post.setEntity(new StringEntity(var, null, "UTF-8"));
//        }
//
//        HttpResponse response = client.execute(post);
//
//        if (var1 != null) {
//            String content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
//            Environment.getCurrentExample().put(var1, content);
//        }
//    }
//
//    @When("替换$content中的$from为$to")
//    public void replace(String content, String from, String to) {
//        Map<String,String> example = Environment.getCurrentExample();
//        String text = example.get(content);
//        text = text.replace(from, to);
//        example.put(content, text);
//    }
//
//    @When("替换$content中匹配模式$from的字符串为$to")
//    public void replaceRegexp(String content, String from, String to) {
//        Map<String,String> example = Environment.getCurrentExample();
//        String text = example.get(content);
//        text = text.replaceAll(from, to.replace("\\", "\\\\").replace("$", "\\$"));
//        example.put(content, text);
//    }
//
    @When("restart brower")
    public void restartBrowser() {
        RemoteWebDriver.restart();
    }

    //    @When("等候$n秒")
    @When("wait $n")
    public void sleep(long n) throws InterruptedException {
        Thread.sleep(n * 1000);
    }

    @When("控制台打印 $value")
    public void printConfig(String value){
        System.out.println(value);
    }

//    @When(value = "等候$n毫秒", priority = 1)
//    public void sleepMillis(long n) throws InterruptedException {
//        Thread.sleep(n);
//    }
//
//    @When("切换到$browser浏览器")
//    public void switchBrowser(String browser) {
//        RemoteWebDriver.switchTo(browser);
//    }
//
//    @When(value = "切换到$subsystem子系统", priority = 1)
//    public void switchSubsystem(String subsystem) {
//        String system = Environment.getCurrentSystem();
//        if (system == null) {
//            throw new IllegalStateException("切换子系统前必须先切换系统");
//        }
//
//        if (Configuration.getSubsystems(system).contains(subsystem)) {
//            Environment.setCurrentSubsystem(subsystem);
//        } else {
//            throw new IllegalArgumentException("未定义系统" + system + "的子系统：" + subsystem);
//        }
//    }
//
//    @When("切换到$system系统")
//    public void switchSystem(String system) {
//        if (Configuration.getSystems().contains(system)) {
//            Environment.setCurrentSystem(system);
//            Environment.setCurrentSubsystem(null);
//        } else {
//            throw new IllegalArgumentException("未定义系统：" + system);
//        }
//    }
//
//    @When(value = "切换到$system系统的$subsystem子系统", priority = 2)
//    public void switchSystemAndSubsystem(String system, String subsystem) {
//        if (Configuration.getSystems().contains(system)) {
//            if (Configuration.getSubsystems(system).contains(subsystem)) {
//                Environment.setCurrentSystem(system);
//                Environment.setCurrentSubsystem(subsystem);
//            } else {
//                throw new IllegalArgumentException("未定义系统" + system + "的子系统：" + subsystem);
//            }
//        } else {
//            throw new IllegalArgumentException("未定义系统：" + system);
//        }
//    }
//
//    @Then(value = "$n分钟内，$step", priority = Integer.MAX_VALUE)
//    public void thenInMinutes(long n, String step) {
//        long originTimeout = Configuration.getTimeout();
//        try {
//            Configuration.setTimeout(n * 1000 * 60);
//            then(step);
//        } finally {
//            Configuration.setTimeout(originTimeout);
//        }
//    }
//
//    @Then(value = "$n秒内，$step", priority = Integer.MAX_VALUE)
//    public void thenInSeconds(long n, String step) {
//        long originTimeout = Configuration.getTimeout();
//        try {
//            Configuration.setTimeout(n * 1000);
//            then(step);
//        } finally {
//            Configuration.setTimeout(originTimeout);
//        }
//    }
//
//    @When(value = "$n分钟内，$step", priority = Integer.MAX_VALUE)
//    public void whenInMinutes(long n, String step) {
//        long originTimeout = Configuration.getTimeout();
//        try {
//            Configuration.setTimeout(n * 1000 * 60);
//            when(step);
//        } finally {
//            Configuration.setTimeout(originTimeout);
//        }
//    }
//
//    @When(value = "$n秒内，$step", priority = Integer.MAX_VALUE)
//    public void whenInSeconds(long n, String step) {
//        long originTimeout = Configuration.getTimeout();
//        try {
//            Configuration.setTimeout(n * 1000);
//            when(step);
//        } finally {
//            Configuration.setTimeout(originTimeout);
//        }
//    }

}
