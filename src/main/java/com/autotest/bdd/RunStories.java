package com.autotest.bdd;

/**
 * Created by zhh on 16-11-21.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.UnmodifiableEmbedderControls;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromRelativeFile;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.StoryReporterBuilder.Format;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.junit.runner.RunWith;
import org.reflections.Reflections;

import com.autotest.bdd.step.StepsSupport;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;

@RunWith(JUnitReportingRunner.class)
public class RunStories extends JUnitStories {

    public RunStories() throws Exception {
        useEmbedder(configuredEmbedder());

        /** 配置 */
//        Keywords keywords = new LocalizedKeywords(Locale.SIMPLIFIED_CHINESE, "i18n/keywords", OurStories.class.getClassLoader());
        Keywords keywords=new Keywords();//暂不本地汉化
        useConfiguration(new MostUsefulConfiguration()
                // 执行过程输出到控制台
                .useDefaultStoryReporter(new ConsoleOutput(keywords))
                // 汉化关键词
                .useKeywords(keywords)
                // 在步骤中用<var>使用样例参数
                .useParameterControls(
                        new ParameterControls()
                                .useDelimiterNamedParameters(true)
                                .useNameDelimiterLeft("<")
                                .useNameDelimiterRight(">")
                )
                // 根据当前系统/子系统，动态路由步骤
                .useStepCollector(new DynamicStepCollector(keywords))
                // 使用支持story中Examples读取配置和随机数的解析方法
                .useStoryParser(new RegexStoryParserEx(keywords))
                // 故事从STORY_HOME目录下装载，文件编码固定为UTF-8
                .useStoryLoader(new LoadFromRelativeFile(new File(Configuration.getStoryHome()).toURI().toURL()) {
                    protected String loadContent(String path) {
                        try {
                            return IOUtils.toString(new FileInputStream(new File(path)), "UTF-8");
                        } catch (Exception e) {
                            throw new InvalidStoryResource(path, e);
                        }
                    }
                })
                // 故事报告
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                                // 使用故事报告的回调机制，在Environment中保存一些上下文信息
                                .withReporters(Environment.instance, new ConsoleReporter(keywords))
                                .withFormats(Format.STATS, Format.HTML)
                                .withKeywords(keywords)
                                .withFailureTrace(true)
                )
                .useViewGenerator(new ViewGeneratorEx())
        );

        /** 初始化所有Step实例，这将：
         *  1. 读取配置使用的所有Step名称
         *  2. 扫描com.autotest包下所有StepsSupport类的子类
         *  3. 如果类对应的Step名称包含在配置中，实例化
         *
         *  注意：Step实例是单例的，即所有系统和子系统共享唯一的Step实例
         */
        Collection<String> steps = Configuration.getSteps();
        Map<String,Object> stepInstances = new HashMap<String,Object>();

        Reflections reflections = new Reflections("com.autotest");
        for (Class<?> clazz : reflections.getSubTypesOf(StepsSupport.class)) {
            String name = clazz.getSimpleName().toLowerCase();
            if (name.endsWith("steps")) {
                name = name.substring(0, name.length() - 5);
            }
            if (!steps.contains(name)) continue;
            try {
                Constructor<?> constructor = clazz.getConstructor(ConfigurableEmbedder.class);
                stepInstances.put(name, constructor.newInstance(this));
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }

        org.jbehave.core.configuration.Configuration configuration = configuration();

        Environment.setConfiguration(configuration);
        Environment.setStepInstances(stepInstances);

        useStepsFactory(new InstanceStepsFactory(configuration, stepInstances.values().toArray()));
    }

    public void useEmbedder(Embedder embedder) {
        super.useEmbedder(embedder);
        embedder.useEmbedderMonitor(new ProxyEmbedderMonitor(embedder.embedderMonitor()));

        EmbedderControls embedderControls = embedder.embedderControls();
        if (!(embedderControls instanceof UnmodifiableEmbedderControls)) {
            embedderControls.useStoryTimeoutInSecs(3600).doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
        }
        Environment.setPerformableTree(embedder.performableTree());
//        Environment.setStoryRunner(embedder.storyRunner());    //StoryRunner在jbehave4中已经废弃，Replaced by PerformableTree。
    }

    @Override
    protected List<String> storyPaths() {
        List<String> paths = new ArrayList<String>();

        String storyName = Configuration.getStoryName();
        if (storyName != null) {
            File storiesFile = new File(Configuration.getStoryHome(), storyName + ".stories");
            if (storiesFile.isFile()) {
                paths = storyPaths(storiesFile);
            } else {
                List<String> storyPaths = new StoryFinder().findPaths(Configuration.getStoryHome(), Arrays.asList("**/*.story"), null);
                String fileName = storyName + ".story";
                for (String storyPath : storyPaths) {
                    if (storyPath.equals(fileName) || storyPath.endsWith("/" + fileName)) {
                        paths.add(storyPath);
                    }
                }
            }
        } else {
            paths = new StoryFinder().findPaths(Configuration.getStoryHome(), Arrays.asList("**/*.story"), null);
        }

        return paths;
    }

    private List<String> storyPaths(File storiesFile) {
        List<String> tokens = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(storiesFile), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 解析前后缀
                String prefix = null, postfix = null;
                if (line.startsWith(">>") || line.startsWith("<<")) {
                    prefix = line.substring(0, 2);
                    line = line.substring(2);
                }
                if (line.endsWith(">>") || line.endsWith("<<")) {
                    int len = line.length();
                    postfix = line.substring(len - 2);
                    line = line.substring(0, len - 2);
                }

                if (prefix != null) tokens.add(prefix);
                if (line.length() > 0 && !line.startsWith("-")) tokens.add(line);
                if (postfix != null) tokens.add(postfix);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean restoreEnabled = false, // 是否包含恢复标记
                restoreStarted = false; // 是否已在恢复标记之后

        List<String> paths = new ArrayList<String>();
        for (String token : tokens) {
            if (">>".equals(token)) {
                if (restoreStarted) {
                    throw new RuntimeException("语法错误，不匹配的>>标记");
                }

                if (restoreEnabled) {
                    restoreStarted = true;
                } else {
                    restoreEnabled = true;
                    restoreStarted = true;

                    paths.clear();
                }
            } else if ("<<".equals(token)) {
                if (restoreEnabled) {
                    if (!restoreStarted) {
                        throw new RuntimeException("语法错误，不匹配的<<标记");
                    }
                    restoreStarted = false;
                } else {
                    restoreEnabled = true;
                    restoreStarted = false;
                }
            } else if (!restoreEnabled ^ restoreStarted) {
                // 在>>和<<之中或者非恢复模式，认为Story有效
                paths.add(token + ".story");
            }
        }

        if (restoreEnabled) {
            Configuration.getProperties().put("restore.enabled", "on"); // 标记需要恢复上一次运行的上下文
        }

        return paths;
    }

}
