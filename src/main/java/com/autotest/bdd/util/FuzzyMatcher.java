package com.autotest.bdd.util;

/**
 * Created by zhh on 16-11-21.
 */

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringEscapeUtils;

public class FuzzyMatcher {

    interface MatchAttempt {
        boolean match(String expect, String actual);
    }

    static class ExactAttempt implements MatchAttempt {

        @Override
        public boolean match(String expect, String actual) {
            return expect.equals(actual);
        }

    }

    static class StringAttempt implements MatchAttempt {

        static final Pattern pattern = Pattern.compile("^\"(.*)\"$");

        @Override
        public boolean match(String expect, String actual) {
            Matcher matcher = pattern.matcher(expect);
            if (matcher.matches()) {
                expect = StringEscapeUtils.unescapeJava(matcher.group(1));
                return expect.equals(actual);
            }
            return false;
        }

    }

    static class SetAttempt implements MatchAttempt {

        private MatchAttempt exactAttempt = new ExactAttempt();

        private MatchAttempt stringAttempt = new StringAttempt();

        @Override
        public boolean match(String expect, String actual) {
            String[] expects = expect.split("或");
            for (String expect0 : expects) {
                if (exactAttempt.match(expect0, actual) || stringAttempt.match(expect0, actual)) {
                    return true;
                }
            }
            return false;
        }

    }

    static class WildcardAttempt implements MatchAttempt {

        static final Pattern pattern = Pattern.compile("^[*?]+$");

        @Override
        public boolean match(String expect, String actual) {
            if (pattern.matcher(expect).matches()) return false;

            String regex =
                    expect.replace("(", "\\(").replace(")", "\\)").replace("[", "\\[").replace("]", "\\]").replace(".", "\\.")
                            .replace("*", ".*").replace("?", ".");
            try {
                return Pattern.matches(regex, actual);
            } catch (PatternSyntaxException e) {
                return false;
            }
        }

    }

    static class RegexpAttempt implements MatchAttempt {

        @Override
        public boolean match(String expect, String actual) {
            try {
                return Pattern.matches(expect, actual);
            } catch (PatternSyntaxException e) {
                return false;
            }
        }

    }

    private static List<MatchAttempt> attempts = Arrays.asList(new ExactAttempt(), new StringAttempt(), new SetAttempt(), new WildcardAttempt(), new RegexpAttempt());

    public static boolean match(String expect, String actual) {
        for (MatchAttempt attempt : attempts) {
            if (attempt.match(expect, actual)) return true;
        }
        return false;
    }

}
