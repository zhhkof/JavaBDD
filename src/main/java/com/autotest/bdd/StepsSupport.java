package com.autotest.bdd;

// TODO: 2016/11/20  
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.StepCandidate;

import java.util.List;

/**
 * Created by ZHH on 2016/11/20.
 */
public class StepsSupport {
    private Configuration configuration;
    private ConfigurableEmbedder embedder;
    private List<StepCandidate> stepCandidatas;
    public StepsSupport(ConfigurableEmbedder embedder){
        this.configuration=embedder.configuration();
        this.embedder=embedder;
    }
    private void runStep(String text){
        if(stepCandidatas==null){
            
        }
    }

}
