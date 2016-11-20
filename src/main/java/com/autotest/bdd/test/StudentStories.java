package com.autotest.bdd.test;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;
import org.apache.commons.io.IOUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.*;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

/**
 * Created by ZHH on 2016/11/20.
 */
@RunWith(JUnitReportingRunner.class)
public class StudentStories extends JUnitStories {
//
//    public Configuration configuration() {
//        System.out.println(this.getClass());
//        return new MostUsefulConfiguration()
//                .useStoryLoader(new LoadFromURL())
//                .useStoryReporterBuilder(new StoryReporterBuilder().withCodeLocation(codeLocationFromClass(this.getClass())));
//
//    }
public StudentStories() {
    // configure as TraderStory except for
    Configuration configuration = new MostUsefulConfiguration()
            .useStoryLoader(new LoadFromURL());
}

    @Override
//    protected List<String> storyPaths() {
//        List<String> aa =new StoryFinder().findPaths("/Users/ZHH/IdeaProjects/JavaBDD/src/main/java/com/autotest/bdd/test/","**/*.story","");
//        System.out.println(aa);
//        List<String> l=new ArrayList<>();
//        l.add("./tt.story");
//        return aa;
//                Arrays.asList("/Users/ZHH/IdeaProjects/JavaBDD/src/main/java/com/autotest/bdd/test/tt.story");
    protected List<String> storyPaths() {
        String codeLocation = codeLocationFromClass(this.getClass()).getFile();
        return new StoryFinder().findPaths(codeLocation, Arrays.asList("**/*.story"),
                Arrays.asList(""), "file:"+codeLocation);
    }

}
