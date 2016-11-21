package com.autotest.bdd;

import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.MetaFilter;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.ReportsCount;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class ProxyEmbedderMonitor implements EmbedderMonitor {
    
    private EmbedderMonitor target;
    
    public ProxyEmbedderMonitor(EmbedderMonitor target) {
        this.target = target;
    }

    @Override
    public void runningEmbeddable(String name) {
        target.runningEmbeddable(name);
    }

    @Override
    public void embeddableFailed(String name, Throwable cause) {
        target.embeddableFailed(name, cause);
    }

    @Override
    public void embeddableNotConfigurable(String name) {
        target.embeddableNotConfigurable(name);
    }

    @Override
    public void embeddablesSkipped(List<String> classNames) {
        target.embeddablesSkipped(classNames);
    }

    @Override
    public void metaNotAllowed(Meta meta, MetaFilter filter) {
        target.metaNotAllowed(meta, filter);
    }

    @Override
    public void runningStory(String path) {
        Environment.setCurrentStoryPath(path);
        target.runningStory(path);
    }

    @Override
    public void storyFailed(String path, Throwable cause) {
        target.storyFailed(path, cause);
    }

    @Override
    public void storiesSkipped(List<String> storyPaths) {
        target.storiesSkipped(storyPaths);
    }

    @Override
    public void storiesNotAllowed(List<Story> notAllowed, MetaFilter filter) {
        target.storiesNotAllowed(notAllowed, filter);
    }

    @Override
    public void storiesNotAllowed(List<Story> notAllowed, MetaFilter filter, boolean verbose) {
        target.storiesNotAllowed(notAllowed, filter, verbose);
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, MetaFilter metaFilter) {
        target.scenarioNotAllowed(scenario,metaFilter);
    }

    @Override
    public void batchFailed(BatchFailures failures) {
        target.batchFailed(failures);
    }

    @Override
    public void beforeOrAfterStoriesFailed() {
        target.beforeOrAfterStoriesFailed();
    }

    @Override
    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        target.generatingReportsView(outputDirectory, formats, viewProperties);
    }

    @Override
    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties, Throwable cause) {
        target.reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause);
    }

    @Override
    public void reportsViewGenerated(ReportsCount count) {
        target.reportsViewGenerated(count);
    }

    @Override
    public void reportsViewFailures(ReportsCount count) {
        target.reportsViewFailures(count);
    }

    @Override
    public void reportsViewNotGenerated() {
        target.reportsViewNotGenerated();
    }

    @Override
    public void runningWithAnnotatedEmbedderRunner(String className) {
        target.runningWithAnnotatedEmbedderRunner(className);
    }

    @Override
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        target.annotatedInstanceNotOfType(annotatedInstance, type);
    }

    @Override
    public void mappingStory(String storyPath, List<String> metaFilters) {
        target.mappingStory(storyPath, metaFilters);
    }

    @Override
    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        target.generatingMapsView(outputDirectory, storyMaps, viewProperties);
    }

    @Override
    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties, Throwable cause) {
        target.mapsViewGenerationFailed(outputDirectory, storyMaps, viewProperties, cause);
    }

    @Override
    public void generatingNavigatorView(File outputDirectory, Properties viewResources) {
        target.generatingNavigatorView(outputDirectory, viewResources);
    }

    @Override
    public void navigatorViewGenerationFailed(File outputDirectory, Properties viewResources, Throwable cause) {
        target.navigatorViewGenerationFailed(outputDirectory, viewResources, cause);
    }

    @Override
    public void navigatorViewNotGenerated() {
        target.navigatorViewNotGenerated();
    }

    @Override
    public void processingSystemProperties(Properties properties) {
        target.processingSystemProperties(properties);
    }

    @Override
    public void systemPropertySet(String name, String value) {
        target.systemPropertySet(name, value);
    }

    @Override
    public void storyTimeout(Story story, StoryDuration storyDuration) {
        target.storyTimeout(story, storyDuration);
    }

    @Override
    public void usingThreads(int threads) {
        target.usingThreads(threads);
    }

    @Override
    public void usingExecutorService(ExecutorService executorService) {
        target.usingExecutorService(executorService);
    }

    @Override
    public void usingControls(EmbedderControls embedderControls) {
        target.usingControls(embedderControls);
    }

    @Override
    public void invalidTimeoutFormat(String s) {
        target.invalidTimeoutFormat(s);
    }

    @Override
    public void usingTimeout(String s, long l) {
        target.usingTimeout(s,l);
    }

}
