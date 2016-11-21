package com.autotest.bdd;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.KnownFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.*;
import org.jbehave.core.model.OutcomesTable.Outcome;
import org.jbehave.core.reporters.StackTraceFormatter;
import org.jbehave.core.reporters.StoryReporter;

import java.util.List;
import java.util.Map;

import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

public class ConsoleReporter implements StoryReporter {
    
    private ThreadLocal<Throwable> cause = new ThreadLocal<Throwable>();
    
    private final Keywords keywords;
    
    private long stepStartAt;

    public ConsoleReporter(Keywords keywords) {
        this.keywords = keywords;
    }

    @Override
    public void afterExamples() {
        System.out.println();
    }

    @Override
    public void afterScenario() {
        if (cause.get() != null && !(cause.get() instanceof KnownFailure)) {
            System.out.println();
            System.out.println(new StackTraceFormatter(true).stackTrace(cause.get()));
        }
    }
    
    @Override
    public void afterStory(boolean givenStory) {
        System.out.println();
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        System.out.println(keywords.examplesTable());
        for (String step : steps) {
            System.out.println(step);
        }
        if (!table.getHeaders().isEmpty()) {
            printTable(table);
        }
    }
    
    @Override
    public void beforeScenario(String scenarioTitle) {
        cause.set(null);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(String.format("%s %s", keywords.scenario(), scenarioTitle));
    }

    @Override
    public void beforeStep(String step) {
        stepStartAt = System.currentTimeMillis();
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        System.out.println("================================================================================");
        System.out.println(story.getDescription().asString());
        System.out.println();
        if (!story.getMeta().isEmpty()) {
            Meta meta = story.getMeta();
            printMeta(meta);
        }
    }
    
    @Override
    public void dryRun() {
        System.out.println(keywords.dryRun());
    }

    @Override
    public void example(Map<String,String> tableRow) {
        System.out.println(String.format("\n%s %s", keywords.examplesTableRow(), tableRow));
    }

    @Override
    public void failed(String step, Throwable storyFailure) {
        if (storyFailure instanceof UUIDExceptionWrapper) {
            cause.set(storyFailure.getCause());
            System.out.println(String.format("ERROR >> %s\n(%s)", formalizeStep(step), storyFailure.getCause()));
        } else {
            throw new ClassCastException(storyFailure + " should be an instance of UUIDExceptionWrapper");
        }
    }
    
    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        failed(step, table.failureCause());
        printOutcomesTable(table);
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
        if (!narrative.isEmpty()) {
            System.out.println(
                String.format(
                    "%s\n%s %s\n%s %s\n%s %s", 
                    keywords.narrative(), 
                    keywords.inOrderTo(), 
                    narrative.inOrderTo(), 
                    keywords.asA(), 
                    narrative.asA(), 
                    keywords.iWantTo(), 
                    narrative.iWantTo())
            );
        }
    }

    @Override
    public void lifecyle(Lifecycle lifecycle) {
        //Empty
    }

    @Override
    public void notPerformed(String step) {
        System.out.println(String.format("      .. %s", formalizeStep(step)));
    }

    @Override
    public void pending(String step) {
        System.out.println(String.format("      ?? %s", formalizeStep(step)));
    }

    @Override
    public void pendingMethods(List<String> methods) {
        for (String method : methods) {
            System.out.println(method);
        }
    }

    @Override
    public void restarted(String step, Throwable cause) {
        System.out.println(String.format("%s %s", formalizeStep(step), cause.getMessage()));
    }

    @Override
    public void restartedStory(Story story, Throwable throwable) {
        //Empty
    }

    @Override
    public void scenarioMeta(Meta meta) {
        if (!meta.isEmpty()) {
            printMeta(meta);
        }
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        System.out.println(filter);
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        System.out.println(String.format("%s (%s %ds)", keywords.storyCancelled(), keywords.duration(), storyDuration.getDurationInSecs()));
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        System.out.println(filter);
    }

    @Override
    public void successful(String step) {
        System.out.println(String.format("[%.3fs] %s", (System.currentTimeMillis() - stepStartAt) / 1000f, formalizeStep(step)));
    }
    
    public static String formalizeStep(String step) {
        return step.replace(PARAMETER_VALUE_START, "[").replace(PARAMETER_VALUE_END, "]");
    }

    private void printMeta(Meta meta) {
        System.out.println(keywords.meta());
        for (String name : meta.getPropertyNames()) {
            System.out.println(String.format("%s%s %s", keywords.metaProperty(), name, meta.getProperty(name)));
        }
        System.out.println();
    }

    private void printOutcomesTable(OutcomesTable table) {
        System.out.println();

        System.out.print('|');
        for (String field : table.getOutcomeFields()) {
            System.out.print(field);
            System.out.print('|');
        }
        System.out.println();
        
        for (Outcome<?> outcome : table.getOutcomes()) {
            System.out.println(String.format("|%s|%s|%s|%s|", outcome.getDescription(), outcome.getValue(), outcome.getMatcher(), (outcome.isVerified() ? keywords.yes() : keywords.no())));
        }
        System.out.println();
        System.out.println();
    }
    
    private void printTable(ExamplesTable table) {
        System.out.println();

        List<String> headers = table.getHeaders();
        System.out.print('|');
        for (String header : headers) {
            System.out.print(header);
            System.out.print('|');
        }
        System.out.println();
        
        List<Map<String, String>> rows = table.getRows();
        for (Map<String, String> row : rows) {
            System.out.print('|');
            for (String header : headers) {
                System.out.print(row.get(header));
                System.out.print('|');
            }
            System.out.println();
        }
    }

}
