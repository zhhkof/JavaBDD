package com.autotest.bdd;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.*;

import java.util.*;

public class DynamicStepCollector extends MarkUnmatchedStepsAsPending {
    
    private final Keywords keywords;
    
    private final StepFinder stepFinder;
    
    public DynamicStepCollector(Keywords keywords) {
        this.keywords = keywords;
        this.stepFinder = new StepFinder();
    }

    @Override
    public List<Step> collectScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario, Map<String,String> parameters) {
        List<StepCandidate> allCandidates = stepFinder.collectCandidates(candidateSteps);
        Collections.sort(allCandidates, new Comparator<StepCandidate>() {
            public int compare(StepCandidate o1, StepCandidate o2) {
                return o2.getPriority().compareTo(o1.getPriority());
            }
        });
        
        List<Step> steps = new ArrayList<Step>();
        
        String previousNonAndStep = null;
        for (String stepAsString : scenario.getSteps()) {
            steps.add(new DynamicStep(stepAsString, previousNonAndStep, allCandidates, parameters));
            if (!(keywords.isAndStep(stepAsString) || keywords.isIgnorableStep(stepAsString))) {
                previousNonAndStep = stepAsString;
            }
        }
        
        return steps;
    }
    
}
