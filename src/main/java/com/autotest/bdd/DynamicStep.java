package com.autotest.bdd;

import org.apache.log4j.Logger;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.AbstractStepResult.Failed;
import org.jbehave.core.steps.AbstractStepResult.Successful;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepCreator.ParametrisedStep;//jar包内变更
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepResult;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicStep implements Step {
    
    private static final Logger logger = Logger.getLogger(DynamicStep.class);
    
    private static Field methodField;
    
    private static final Pattern pattern = Pattern.compile("<(.*?)>");
    
    static Step lastStep;

    static String lastStepAsString;
    
    private Map<String,String> parameters;

    private String parametrisedStep;

    private String previousNonAndStep;

    private String stepAsString;

    private List<StepCandidate> stepCandidates;
    
    private File signalFile;

    public DynamicStep(String stepAsString, String previousNonAndStep, List<StepCandidate> stepCandidates, Map<String,String> parameters) {
        this.stepAsString = stepAsString;
        this.previousNonAndStep = previousNonAndStep;
        this.stepCandidates = stepCandidates;
        this.parameters = parameters;
    }

    @Override
    public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
        parameterizeStep();
        return createTarget().doNotPerform(storyFailureIfItHappened);
    }

    @Override
    public String asString(Keywords keywords) {
        return null;
    }

    @Override
    public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
        // 环境检查未通过，则不执行
        if (Environment.isUnsatisfied()) {
            return doNotPerform(storyFailureIfItHappened);
        }
        
        parameterizeStep();
        reportBeforeStep();
        Step step = createTarget();
        StepResult stepResult = step.perform(storyFailureIfItHappened);
        
        if (Boolean.parseBoolean(Configuration.getProperty("step.pause_on_error", "false"))) {
            if (stepResult instanceof Failed) {
                try {
                    signalFile = File.createTempFile("bdd-pause", null);
                    signalFile.deleteOnExit();
                    
                    logger.error("执行步骤 \""  + ConsoleReporter.formalizeStep(stepResult.parametrisedStep()) + "\" 时发生错误");
                    logger.error(String.format("暂停执行，使用 del \"%s\" 恢复执行", signalFile.getCanonicalPath()));
                    while (signalFile.exists()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignore) {}
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        
        if (Boolean.parseBoolean(Configuration.getProperty("step.redo", "false"))) {
            if (stepResult instanceof Failed && canRedo()) {
                logger.warn("重新执行：" + lastStepAsString);
                StepResult lastStepResult = lastStep.perform(storyFailureIfItHappened);
                if (lastStepResult instanceof Successful) {
                    stepResult = step.perform(storyFailureIfItHappened);
                }
            }
        }
        
        lastStep = step;
        lastStepAsString = stepAsString;
        
        return stepResult;
    }
    
    private boolean canRedo() {
        if (lastStep == null || !(lastStep instanceof ParametrisedStep)) return false;
        if (methodField == null) {
            try {
                methodField = ParametrisedStep.class.getDeclaredField("method");
                methodField.setAccessible(true);
            } catch (Exception ignore) {
                return false;
            }
        }
        try {
            Method method = (Method) methodField.get(lastStep);
            return method.isAnnotationPresent(Redoable.class);
        } catch (Exception ignore) {}
        
        return false;
    }

    private Step createTarget() {
        Step step = StepCreator.createPendingStep(parametrisedStep, previousNonAndStep);
        
        Set<Object> stepInstances = Environment.getCurrentStepInstances();
        
        for (StepCandidate candidate : stepCandidates) {
            // 根据当前系统和子系统过滤
            if (!stepInstances.contains(candidate.getStepsInstance())) {
                continue;
            }
            
            if (candidate.ignore(parametrisedStep)) {
                return StepCreator.createIgnorableStep(parametrisedStep);
            }
            
            if (matchesCandidate(parametrisedStep, previousNonAndStep, candidate)) {
                if (candidate.isPending()) {
                    ((PendingStep) step).annotatedOn(candidate.getMethod());
                } else {
                    step = candidate.createMatchedStep(parametrisedStep, parameters);
                    if (candidate.isComposite()) {
                        throw new UnsupportedOperationException();
                    }
                }
                break;
            }
        }
        
        return step;
    }
    
    private boolean matchesCandidate(String step, String previousNonAndStep, StepCandidate candidate) {
        if (previousNonAndStep != null) {
            return candidate.matches(step, previousNonAndStep);
        }
        return candidate.matches(step);
    }
    
    private void parameterizeStep() {
        Matcher matcher = pattern.matcher(stepAsString);
        
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            if (parameters.containsKey(name)) {
                matcher.appendReplacement(buf, parameters.get(name).replace("\\", "\\\\").replace("$", "\\$"));
            } else {
                matcher.appendReplacement(buf, matcher.group().replace("\\", "\\\\").replace("$", "\\$"));
            }
        }
        matcher.appendTail(buf);
        
        parametrisedStep = buf.toString();
    }

    //StoryRunner在jbehave4中已经废弃，Replaced by PerformableTree。
    private void reportBeforeStep() {
        try {
//            StoryRunner storyRunner = Environment.getStoryRunner();
            PerformableTree performableTree=Environment.getPerformableTree();
//            Field reporterField = StoryRunner.class.getDeclaredField("reporter");
            Field reporterField = PerformableTree.class.getDeclaredField("reporter");
            reporterField.setAccessible(true);
            
//            StoryReporter reporter = ((ThreadLocal<StoryReporter>) reporterField.get(storyRunner)).get();
            StoryReporter reporter = ((ThreadLocal<StoryReporter>) reporterField.get(performableTree)).get();
            reporter.beforeStep(parametrisedStep);
            
            Environment.instance.beforeStep(parametrisedStep);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
