package com.autotest.bdd;

import ognl.Ognl;
import ognl.OgnlException;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpressionEngine {
    
    public static class Context implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private transient Random random = new Random();
        
        private Map<String,String> randResult = new HashMap<String,String>();
        
        private AtomicInteger seq = new AtomicInteger();
        
        private Map<String,Long> tsResult = new HashMap<String,Long>();
        
        public Properties getConfig() {
            return Configuration.getProperties();
        }
        
        public int getSeq() {
            return seq.getAndIncrement();
        }
        
        public String rand(String key) {
            if (randResult.containsKey(key)) return randResult.get(key);
            String value = Long.toString(Math.abs(random.nextLong()), 36);
            randResult.put(key, value);
            return value;
        }
        
        public long ts(String key) {
            if (tsResult.containsKey(key)) return tsResult.get(key);
            long ts = System.currentTimeMillis();
            while (tsResult.containsValue(ts)) {
                try {
                    Thread.sleep(random.nextInt(10) + 1);
                } catch (InterruptedException ignore) {}
                ts = System.currentTimeMillis();
            }
            tsResult.put(key, ts);
            return ts;
        }
        
    }
    
    public static Context context = new Context();
    
    private static final Logger logger = Logger.getLogger(ExpressionEngine.class);
    
    public static String evaluate(String expression) {
        try {
            return String.valueOf(Ognl.getValue(expression, context));
        } catch (OgnlException e) {
            logger.error("计算表达式[" + expression + "]失败", e);
            return "!!!" + expression + "!!!";
        }
    }
    
}
