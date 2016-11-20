package com.autotest.bdd;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;

import java.text.SimpleDateFormat;

/**
 * Created by ZHH on 2016/11/20.
 */
public class TraderEmbedder extends Embedder {
    @Override
    public EmbedderControls embedderControls() {
        return new EmbedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
    }
    @Override
    public Configuration configuration(){
        Class<? extends TraderEmbedder> embedderClass=this.getClass();
        return new MostUsefulConfiguration()
                //set story load path
                .useStoryLoader(new LoadFromClasspath(embedderClass.getClassLoader()))
                //set report configuration
                .useStoryReporterBuilder(new StoryReporterBuilder()
                    .withCodeLocation(CodeLocations.codeLocationFromClass(embedderClass))
                        .withFormats(Format.CONSOLE,Format.HTML)
                        .withCrossReference(new CrossReference()))
                //设置参数转换
                .useParameterConverters(new ParameterConverters()
                .addConverters(new ParameterConverters.DateConverter(new SimpleDateFormat("yyyy-MM-dd"))))
                .useStepMonitor(new SilentStepMonitor());
    }
    @Override
    public InjectableStepsFactory stepsFactory(){
        return new InstanceStepsFactory(configuration(),new TestSteps());
    }
}
