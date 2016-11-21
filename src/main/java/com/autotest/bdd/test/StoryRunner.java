package com.autotest.bdd.test;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.junit.Test;

import java.util.List;

/**
 * Created by ZHH on 2016/11/20.
 */
public class StoryRunner {
    //教程1
    @Test
    public void runClasspathLoadedStoriesAsJunit(){
        Embedder embedder=new TraderEmbedder();
        System.out.println(CodeLocations.codeLocationFromClass(this.getClass()));
        List<String> storyPaths = new StoryFinder().findPaths(CodeLocations.codeLocationFromClass(this.getClass()),"**/*.story","");
        embedder.runStoriesAsPaths(storyPaths);

    }

    //教程2
//    public Configuration configuration() {
//        return new MostUsefulConfiguration().useStoryLoader(new LoadFromClasspath(this.getClass()))
//                .useStoryReporterBuilder(new StoryReporterBuilder().withCodeLocation(codeLocationFromClass(this.getClass())));
//
//    }
//    @Override
//    protected List<String> storyPaths() {
//        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()),"**/*.story","");
//    }
}
