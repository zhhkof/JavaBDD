package com.autotest.bdd.step;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepResult;

import com.autotest.bdd.DynamicStep;
import com.autotest.bdd.Environment;
import com.autotest.bdd.util.FuzzyMatcher;

public abstract class StepsSupport {

    private Configuration configuration;

    private ConfigurableEmbedder embedder;

    private List<StepCandidate> stepCandidates;

    public StepsSupport(ConfigurableEmbedder embedder) {
        this.configuration = embedder.configuration();
        this.embedder = embedder;
    }

    private void runStep(String text) {
        if (stepCandidates == null) {
            List<CandidateSteps> candidateSteps = embedder.stepsFactory().createCandidateSteps();
            stepCandidates = new StepFinder().collectCandidates(candidateSteps);
            Collections.sort(stepCandidates, new Comparator<StepCandidate>() {
                public int compare(StepCandidate o1, StepCandidate o2) {
                    return o2.getPriority().compareTo(o1.getPriority());
                }
            });
        }

        StepResult result = new DynamicStep(text, null, stepCandidates, Environment.getCurrentExample()).perform(null);
        if (result.getFailure() != null) throw result.getFailure();
    }

    protected boolean matchValue(String expect, String actual) {
        return FuzzyMatcher.match(expect, actual);
    }

    protected void assertValue(String expect, String actual) {
        if (FuzzyMatcher.match(expect, actual)) {
            return;
        } else {
            fail(String.format("期望值：[%s]，实际值：[%s]", expect, actual));
        }
    }

    protected void given(String text) {
        runStep(configuration.keywords().given() + " " + text);
    }

    protected String parameterize(String s) {
        Pattern pattern = Pattern.compile("<([^>]*)>");
        Matcher matcher = pattern.matcher(s);

        Map<String,String> example = Environment.getCurrentExample();

        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buf, example.get(matcher.group(1)));
        }
        matcher.appendTail(buf);

        return buf.toString();
    }

    protected void then(String text) {
        runStep(configuration.keywords().then() + " " + text);
    }

    protected void when(String text) {
        runStep(configuration.keywords().when() + " " + text);
    }

}
