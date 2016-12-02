package com.autotest.bdd;

import org.apache.log4j.Logger;
import org.jbehave.core.io.StoryNameResolver;
import org.jbehave.core.reporters.FreemarkerProcessor;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.TemplateProcessor;
import org.jbehave.core.reporters.TemplateableViewGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.*;

public class ViewGeneratorEx extends FreemarkerViewGenerator {
    
    private static final Logger logger = Logger.getLogger(ViewGeneratorEx.class);
    
    private Map<String,Field> fields = new HashMap<String,Field>();

    public ViewGeneratorEx() throws Exception {
        for (Class<?> clazz : new Class[] { TemplateableViewGenerator.class, FreemarkerProcessor.class }) {
            for (Field field : clazz.getDeclaredFields()) {
                fields.put(field.getName(), field);
                field.setAccessible(true);
            }
        }
        
        Field templateLoadingFromField = FreemarkerProcessor.class.getDeclaredField("templateLoadingFrom");
        templateLoadingFromField.setAccessible(true);
        templateLoadingFromField.set(get("processor"), this.getClass());
    }

//    @Override
//    public Properties defaultViewProperties() {
//        Properties properties = new Properties(super.defaultViewProperties());
//        properties.setProperty("encoding", "UTF-8");
////        properties.setProperty("decorateNonHtml", "true");
////        properties.setProperty("defaultFormats", "stats");
////        properties.setProperty("reportsViewType", TemplateableViewGenerator.Reports.ViewType.LIST.name());
////        properties.setProperty("viewDirectory", "view");
//        properties.setProperty("views", "ftl/jbehave-views.ftl");
//        properties.setProperty("maps", "ftl/jbehave-maps.ftl");
//        properties.setProperty("navigator", "ftl/jbehave-navigator.ftl");
//        properties.setProperty("reports", "ftl/jbehave-reports.ftl");
//        properties.setProperty("decorated", "ftl/jbehave-report-decorated.ftl");
//        properties.setProperty("nonDecorated", "ftl/jbehave-report-non-decorated.ftl");
//        return properties;
//    }

    @Override
    public void generateReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        super.generateReportsView(outputDirectory, formats, viewProperties);
        String outputName = "view/" + Configuration.getStoryName() + ".xml";
        String reportsTemplate = "ftl/junit-reports.ftl";
        Map<String, Object> dataModel = new HashMap<String, Object>();

        //ReportsTable该静态方法在jbehave里被废弃，新增Reports
//        dataModel.put("reportsTable", new ReportsTable(this.<List<Report>>get("reports"), this.<StoryNameResolver>get("nameResolver")));//jar包内变更
        dataModel.put("reports", new Reports((this.<Reports>get("reports")).getReports(), this.<StoryNameResolver>get("nameResolver")));
        dataModel.put("date", new Date());
        dataModel.put("timeFormatter", new TimeFormatter());
        dataModel.put("storyName", Configuration.getStoryName());
        try {
            File file = new File(outputDirectory, outputName);
            file.getParentFile().mkdirs();
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            this.<TemplateProcessor>get("processor").process(reportsTemplate, dataModel, writer);
        } catch (Exception e) {
            throw new ViewGenerationFailedForTemplate(outputName, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T get(String name) {
        try {
            return (T) fields.get(name).get(this);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    private <T> void set(String name, T value) {
        try {
            fields.get(name).set(this, value);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

}
