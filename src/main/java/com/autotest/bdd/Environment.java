package com.autotest.bdd;

import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.embedder.PerformableTree;
import java.util.*;

public class Environment implements StoryReporter {
    
    private static List<Runnable> afterScenarioCallbacks = new ArrayList<Runnable>();
    
    private static List<Runnable> beforeScenarioCallbacks = new ArrayList<Runnable>();
    
    private static List<Runnable> beforeStepCallbacks = new ArrayList<Runnable>();
    
    private static org.jbehave.core.configuration.Configuration configuration;
    
    private static Throwable currentCause;
    
    private static Map<String,String> currentExample;

    private static ExamplesTable currentExamplesTable;
    
    private static String currentScenario;
    
    private static String currentStep;
    
    private static Story currentStory;

    private static String currentStoryPath;
    
    private static String currentSubsystem;
    
    private static String currentSystem;
    
    private static List<Runnable> failedCallbacks = new ArrayList<Runnable>();
    
    private static boolean inContext;
    
    private static StoryRunner storyRunner;

    private static PerformableTree performableTree;
    
    private static final Map<String,Set<Object>> systemStepInstances = new HashMap<String,Set<Object>>();

    private static boolean unsatisfied = false;
    
    static final Environment instance = new Environment();

    private Environment() {}

    @Override
    public void afterExamples() {
        currentExample = null;
        currentExamplesTable = null;
    }

    @Override
    public void afterScenario() {
        for (Runnable callback : afterScenarioCallbacks) {
            try {
                callback.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        currentScenario = null;
    }

    @Override
    public void afterStory(boolean givenStory) {
        currentStory = null;
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        currentExamplesTable = table;
    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        currentScenario = scenarioTitle;
        DynamicStep.lastStep = null;
        for (Runnable callback : beforeScenarioCallbacks) {
            try {
                callback.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeStep(String step) {
        currentStep = step;
        for (Runnable callback : beforeStepCallbacks) {
            try {
                callback.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        currentStory = story;
    }

    @Override
    public void dryRun() {
        // EMPTY
    }
    
    @Override
    public void example(Map<String,String> tableRow) {
        currentExample = tableRow;
    }

    @Override
    public void failed(String step, Throwable cause) {
        currentCause = cause;
        for (Runnable callback : failedCallbacks) {
            try {
                callback.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
        Meta meta = currentStory.getMeta();
        if (meta.hasProperty("环境检查")) {
            unsatisfied = true;
        }
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        // EMPTY
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        // EMPTY
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        // EMPTY
    }

    @Override
    public void ignorable(String step) {
        // EMPTY
    }

    @Override
    public void narrative(Narrative narrative) {
        // EMPTY
    }

    @Override
    public void lifecyle(Lifecycle lifecycle) {

    }

    @Override
    public void notPerformed(String step) {
        // EMPTY
    }

    @Override
    public void pending(String step) {
        // EMPTY
    }

    @Override
    public void pendingMethods(List<String> methods) {
        // EMPTY
    }

    @Override
    public void restarted(String step, Throwable cause) {
        // EMPTY
    }

    @Override
    public void restartedStory(Story story, Throwable throwable) {

    }

    @Override
    public void scenarioMeta(Meta meta) {
        // EMPTY
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        // EMPTY
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        // EMPTY
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        // EMPTY
    }
    
    @Override
    public void successful(String step) {
        // EMPTY
    }
    
    public static void addAfterScenarioCallback(Runnable callback) {
        afterScenarioCallbacks.add(callback);
    }
    
    public static void addBeforeScenarioCallback(Runnable callback) {
        beforeScenarioCallbacks.add(callback);
    }
    
    public static void addBeforeStepCallback(Runnable callback) {
        beforeStepCallbacks.add(callback);
    }
    
    public static void addFailedCallback(Runnable callback) {
        failedCallbacks.add(callback);
    }
    
    public static org.jbehave.core.configuration.Configuration getConfiguration() {
        return configuration;
    }

    public static Throwable getCurrentCause() {
        return currentCause;
    }

    public static Map<String,String> getCurrentExample() {
        return currentExample;
    }

    public static ExamplesTable getCurrentExamplesTable() {
        return currentExamplesTable;
    }
    
    public static String getCurrentScenario() {
        return currentScenario;
    }
    
    public static String getCurrentStep() {
        return currentStep;
    }
    
    public static Set<Object> getCurrentStepInstances() {
        return systemStepInstances.get(systemPrefix());
    }
    
    public static Story getCurrentStory() {
        return currentStory;
    }
    
    public static String getCurrentStoryPath() {
        return currentStoryPath;
    }
    
    public static String getCurrentSubsystem() {
        return currentSubsystem;
    }

    public static String getCurrentSystem() {
        return currentSystem;
    }
    
    public static StoryRunner getStoryRunner() {
        return storyRunner;
    }

    public static PerformableTree getPerformableTree() {
        return performableTree;
    }

    public static boolean inContext() {
        return Environment.inContext;
    }
    
    public static boolean isUnsatisfied() {
        return unsatisfied;
    }
    
    public static void setConfiguration(org.jbehave.core.configuration.Configuration configuration) {
        Environment.configuration = configuration;
    }

    public static void setCurrentStoryPath(String currentStoryPath) {
        Environment.currentStoryPath = currentStoryPath;
        
        String maxStoryHome = "", maxSystem = null, maxSubsystem = null;
        for (String system : Configuration.getSystems()) {
            currentSystem = system;
            
            for (String subsystem : Configuration.getSubsystems(system)) {
                currentSubsystem = subsystem;
                
                String storyHome = Configuration.getProperty("story.home");
                if (storyHome != null) {
                    if (!storyHome.endsWith("/")) storyHome = storyHome + "/";
                    if (storyHome.length() <= maxStoryHome.length()) continue;
                    if (currentStoryPath.startsWith(storyHome)){
                        maxStoryHome = storyHome;
                        maxSystem = system;
                        maxSubsystem = subsystem;
                    }
                }
            }
            currentSubsystem = null;
            
            String storyHome = Configuration.getProperty("story.home");
            if (storyHome != null) {
                if (!storyHome.endsWith("/")) storyHome = storyHome + "/";
                if (storyHome.length() <= maxStoryHome.length()) continue;
                if (currentStoryPath.startsWith(storyHome)){
                    maxStoryHome = storyHome;
                    maxSystem = system;
                    maxSubsystem = null;
                }
            }
        }
        currentSystem = maxSystem;
        currentSubsystem = maxSubsystem;
    }

    public static void setCurrentSubsystem(String currentSubsystem) {
        Environment.currentSubsystem = currentSubsystem;
    }

    public static void setCurrentSystem(String currentSystem) {
        Environment.currentSystem = currentSystem;
    }

    public static void setInContext(boolean inContext) {
        Environment.inContext = inContext;
    }

    public static void setStoryRunner(StoryRunner storyRunner) {
        Environment.storyRunner = storyRunner;
    }

    public static void setPerformableTree(PerformableTree performableTree) {
        Environment.performableTree = performableTree;
    }

    public static String systemPrefix() {
        if (currentSystem != null) {
            if (currentSubsystem != null) {
                return currentSystem + "." + currentSubsystem;
            }
            return currentSystem;
        }
        return "";
    }

    private static Set<Object> filterStepInstances(Map<String,Object> stepInstances, String steps) {
        Set<Object> result = new HashSet<Object>();
        result.add(stepInstances.get("common"));
        
        if (steps != null) { 
            for (String name : steps.split(",\\s*")) {
                result.add(stepInstances.get(name));
            }
        }
        
        return Collections.unmodifiableSet(result);
    }

    static void setStepInstances(Map<String,Object> stepInstances) {
        for (String system : Configuration.getSystems()) {
            currentSystem = system;
            
            for (String subsystem : Configuration.getSubsystems(system)) {
                currentSubsystem = subsystem;
                systemStepInstances.put(systemPrefix(), filterStepInstances(stepInstances, Configuration.getProperty("steps")));
            }
            currentSubsystem = null;
            systemStepInstances.put(systemPrefix(), filterStepInstances(stepInstances, Configuration.getProperty("steps")));
        }
        currentSystem = null;
        systemStepInstances.put(systemPrefix(), filterStepInstances(stepInstances, Configuration.getProperty("steps")));
    }

}
