package com.autotest.bdd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Configuration {
    
    public static class SystemEntry {
        
        private String subsystem;
        
        private String system;
        
        SystemEntry(String system, String subsystem) {
            this.system = system;
            this.subsystem = subsystem;
        }

        public String getSubsystem() {
            return subsystem;
        }

        public String getSystem() {
            return system;
        }
        
    }
    
    private static Properties properties;
    
    private static final String storyHome;
    
    static {
        storyHome = System.getProperty("story.home");
        if (storyHome == null) throw new RuntimeException("未配置story.home");
        
        File directory = new File(storyHome);
        if (!directory.isDirectory()) throw new RuntimeException("配置的story.home(" + storyHome + ")不是目录");
        
        try {
            Properties defaultProperties = null;    // 默认配置，从config.properties文件中读取
            Properties properties = null;           // 指定配置，从config-*.properties文件中读取
            
            {
                File config = new File(directory, "config.properties");
                if (config.isFile()) {
                    defaultProperties = new Properties();
                    defaultProperties.load(new InputStreamReader(new FileInputStream(config), "UTF-8"));
                }
            }
            
            {
                String configName = System.getProperty("config.name");
                if (configName != null) {
                    File config = new File(directory, String.format("config-%s.properties", configName));
                    if (config.isFile()) {
                        properties = new Properties();
                        properties.load(new InputStreamReader(new FileInputStream(config), "UTF-8"));
                    }
                }
            }
            
            if (defaultProperties == null && properties == null) {
                throw new RuntimeException("不存在任何配置文件");
            }
            
            Configuration.properties = new Properties();
            if (defaultProperties != null) Configuration.properties.putAll(defaultProperties);
            if (properties != null)        Configuration.properties.putAll(properties);
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件失败", e);
        }
    }

    public static boolean contains(String... keys) {
        String system = Environment.getCurrentSystem();
        String subsystem = Environment.getCurrentSubsystem();
        
        for (String key : keys) {
            if (system != null) {
                String mappedKey;
                
                if (subsystem != null) {
                    mappedKey = String.format("%s.%s.%s", system, subsystem, key);
                    if (properties.containsKey(mappedKey)) continue;
                }
                
                mappedKey = String.format("%s.%s", system, key);
                if (properties.containsKey(mappedKey)) continue;
            }
            
            if (properties.containsKey(key)) continue;
            
            return false;
        }
        
        return true;
    }
    
    public static Properties getProperties() {
        return properties;
    }
    
    public static String getProperty(String key) {
        return getProperty(key, null);
    }
    
    public static String getProperty(String key, String defaultValue) {
        String system = Environment.getCurrentSystem();
        String subsystem = Environment.getCurrentSubsystem();
        
        if (system != null) {
            String mappedKey;
            
            if (subsystem != null) {
                mappedKey = String.format("%s.%s.%s", system, subsystem, key);
                if (properties.containsKey(mappedKey)) return properties.getProperty(mappedKey);
            }
            
            mappedKey = String.format("%s.%s", system, key);
            if (properties.containsKey(mappedKey)) return properties.getProperty(mappedKey);
        }
        
        return properties.getProperty(key, defaultValue);
    }
    
    public static Collection<String> getSteps() {
        Set<String> steps = new LinkedHashSet<String>();
        
        if (properties.containsKey("steps")) {
            steps.addAll(Arrays.asList(properties.getProperty("steps").split(",\\s*")));
        }
        
        for (String system : getSystems()) {
            String mappedKey = String.format("%s.steps", system);
            if (properties.containsKey(mappedKey)) {
                steps.addAll(Arrays.asList(properties.getProperty(mappedKey).split(",\\s*")));
            }
            
            for (String subsystem : getSubsystems(system)) {
                mappedKey = String.format("%s.%s.steps", system, subsystem);
                if (properties.containsKey(mappedKey)) {
                    steps.addAll(Arrays.asList(properties.getProperty(mappedKey).split(",\\s*")));
                }
            }
            
        }
        
        return steps;
    }
    
    public static String getStoryHome() {
        return storyHome;
    }
    
    public static String getStoryName() {
        return System.getProperty("story.name");
    }
    
    public static Collection<String> getSubsystems(String system) {
        String subsystems = properties.getProperty(String.format("%s.%s", system, "subsystems"));
        if (subsystems == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(subsystems.split(",\\s*"));
        }
    }
    
    public static Collection<String> getSystems() {
        String systems = properties.getProperty("systems");
        if (systems == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(systems.split(",\\s*"));
        }
    }
    
    public static long getTimeout() {
        return Long.parseLong(getProperty("timeout", "5000"));
    }
    
    public static void setTimeout(long timeout) {
        properties.setProperty("timeout", String.valueOf(timeout));
    }

}
