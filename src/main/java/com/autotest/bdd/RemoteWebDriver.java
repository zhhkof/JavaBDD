package com.autotest.bdd;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.internal.SocketLock;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class RemoteWebDriver {

    private static String browser;

    private static org.openqa.selenium.remote.RemoteWebDriver driver;
    
    private static boolean environmentCallbackInitialized = false;
    
    public static org.openqa.selenium.remote.RemoteWebDriver instance() {
        return driver;
    }
    
    public static void open() {
        open(null);
    }
    
    public static void open(String _browser) {
        if (driver != null) return;
        
        if (_browser == null) {
            _browser = Configuration.getProperty("browser", "chrome"); //默认chrome
        }
        browser = _browser;
        
        if ("firefox".equals(browser)) {
            try {
                useFirefox();
            } catch (Exception e) {
                throw new RuntimeException("启动Firefox失败", e);
            }
        } else if ("chrome".equals(browser)) {
            try {
                useChrome();
            } catch (Exception e) {
                throw new RuntimeException("启动Chrome失败", e);
            }
        } else if ("ie".equals(browser)) {
            useInternetExplorer();
        } else {
            throw new RuntimeException("不支持的浏览器：" + browser);
        }
        
        driver.manage().timeouts().implicitlyWait(Configuration.getTimeout(), TimeUnit.MILLISECONDS).setScriptTimeout(Configuration.getTimeout(), TimeUnit.MILLISECONDS);

        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();
        
        // TODO: 原因？
        if ("firefox".equals(browser)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        
        driver.get("about:blank");

        if (!environmentCallbackInitialized) {
            environmentCallbackInitialized = true;
            Environment.addBeforeStepCallback(new Runnable() {
                @Override
                public void run() {
                    if (driver == null || !Boolean.parseBoolean(Configuration.getProperty("firefox.echo", "false"))) return;
                    try {
                        driver.executeScript("window.status = arguments[0];", Environment.getCurrentStep());
                    } catch (org.openqa.selenium.NoSuchWindowException ignore) {}
                }
            });

            Environment.addBeforeScenarioCallback(new Runnable() {
                @Override
                public void run() {
                    if (driver == null || !Boolean.parseBoolean(Configuration.getProperty("firefox.echo", "false"))) return;
                    driver.executeScript("window.status = '开始场景：' + arguments[0];", Environment.getCurrentScenario());
                }
            });

            Environment.addAfterScenarioCallback(new Runnable() {
                @Override
                public void run() {
                    if (driver == null || !Boolean.parseBoolean(Configuration.getProperty("firefox.echo", "false"))) return;
                    driver.executeScript("window.status = '完成场景：' + arguments[0];", Environment.getCurrentScenario());
                }
            });
        }
    }

    public static void quit() {
        if (driver == null) return;

        if ("firefox".equals(browser)) {
            if (!Boolean.parseBoolean(Configuration.getProperty("firefox.reuse", "true"))) {
                driver.quit();
            }
        } else if ("chrome".equals(browser)) {
            driver.quit();
        } else if ("ie".equals(browser)) {
            driver.quit();
        } else {
            throw new RuntimeException("不支持的浏览器：" + browser);
        }

        driver = null;
    }

    public static void restart() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
        open(browser);
    }

    public static void switchTo(String _browser) {
        if (_browser != null) {
            _browser = _browser.toLowerCase();
        }
        
        if (browser != null && browser.equals(_browser)) return;
        
        quit();
        open(_browser);
    }
    
    private static void useChrome() {
        driver = new ChromeDriver();
    }
    
    private static void useFirefox() throws MalformedURLException {
        if (Boolean.parseBoolean(Configuration.getProperty("firefox.reuse", "true"))) {
            // 检查是否存在WebDriver服务
            boolean canReuse = false;

            System.setProperty("webdriver.firefox.useExisting", "true");
            System.setProperty("webdriver.reap_profile", "false");

            InetSocketAddress address = new InetSocketAddress("localhost", SocketLock.DEFAULT_PORT);
            Socket socket = new Socket();
            try {
                socket.bind(address);
            } catch (IOException e) {
                // 端口被占用，则认为WebDriver服务存在
                canReuse = true;
            } finally {
                try {
                    socket.close();
                } catch (IOException ignore) {
                	ignore.printStackTrace();
                }
            }

            if (canReuse) {
                URL url = new URL("http", "localhost", SocketLock.DEFAULT_PORT, "/hub");
                driver = new org.openqa.selenium.remote.RemoteWebDriver(url, DesiredCapabilities.firefox());
            }
        }

        if (driver == null) {
            FirefoxBinary binary = new FirefoxBinary(new File(Configuration.getProperty("firefox.bin")));
            FirefoxProfile profile = new FirefoxProfile();
            profile.setAlwaysLoadNoFocusLib(true);
            profile.setEnableNativeEvents(false);

            profile.setPreference("dom.disable_window_status_change", false);
            profile.setPreference("network.proxy.type", 0);
            //取消火狐浏览器启动时插件检查窗口
            profile.setPreference("extensions.blocklist.enabled", false);

            if (Configuration.getProperty("firefox.download.dir") != null) {
            	String fdd = Configuration.getProperty("firefox.download.dir");
                if(File.separator.equals("\\")){
                	fdd = fdd.replace("/", "\\");
                }else{
                	fdd = fdd.replace("\\", "/");
                }
                File file = new File(fdd);
            	if(file.isAbsolute()){
            		profile.setPreference("browser.download.dir", fdd);
            	}else{
            		String path = System.getProperty("story.home") + File.separator + fdd;
                    if(File.separator.equals("\\")){
                    	path = path.replace("/", "\\");
                    }else{
                        path = path.replace("\\", "/");
                    }
            	    profile.setPreference("browser.download.dir", path);
            	}
            	profile.setPreference("browser.download.folderList",2);
                profile.setPreference("browser.download.manager.showAlertOnComplete", false);
                profile.setPreference("browser.download.manager.showWhenStarting", false);
                profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream;video/vnd.dlna.mpeg-tts");
            }

            driver = new FirefoxDriver(binary, profile);
        }
    }
    
    private static void useInternetExplorer() {
        driver = new InternetExplorerDriver();
    }

}
