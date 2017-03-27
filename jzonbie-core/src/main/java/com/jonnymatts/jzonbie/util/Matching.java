package com.jonnymatts.jzonbie.util;

import com.jonnymatts.jzonbie.model.content.BodyContent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Matching {

    private static final char[] REGEX_CHARACTERS = new char[]{'+', '.', '*', '[', '{', '^', '|', '$', '?'};

    public static boolean mapValuesMatchWithRegex(Map<?, ?> patterns, Map<?, ?> values) {
        if(isNullOrEmpty(patterns) && isNullOrEmpty(values)) return true;

        if(!patterns.keySet().equals(values.keySet())) return false;

        return patterns.entrySet().parallelStream().allMatch(e -> {
            final Object pattern = e.getValue();
            final Object value = values.get(e.getKey());
            return matchRegexRecursively(pattern, value);
        });
    }

    public static boolean listsMatchesRegex(List<?> patterns, List<?> values) {
        if (patterns.size() != values.size())
            return false;

        for(int i = 0; i < patterns.size(); i++) {
            final Object pattern = patterns.get(i);
            final Object value = values.get(i);
            if (!matchRegexRecursively(pattern, value))
                return false;
        }

        return true;
    }

    public static boolean matchRegexRecursively(Object pattern, Object value) {
        if(value instanceof String)
            return stringsMatch((String)pattern, (String) value);
        if(value instanceof Map)
            return mapValuesMatchWithRegex((Map<?,?>) pattern, (Map<?,?>)value);
        if(value instanceof List)
            return listsMatchesRegex((List<?>) pattern, (List<?>)value);
        if(value instanceof Number)
            return numbersEqual((Number)pattern, (Number)value);
        return Objects.equals(pattern, value);
    }

    public static boolean bodyContentsMatch(BodyContent pattern, BodyContent value) {
        return pattern == null || (pattern.matches(value) && value != null);
    }

    private static boolean isNullOrEmpty(Map<?,?> map) {
        return map == null || map.isEmpty();
    }

    private static boolean stringsMatch(String pattern, String value) {
        boolean isRegex = false;
        for(char c : REGEX_CHARACTERS) {
            if (pattern.indexOf(c) > -1) {
                isRegex = true;
                break;
            }
        }

        return isRegex ? value.matches(pattern) : value.equals(pattern);
    }

    private static boolean numbersEqual(Number number1, Number number2) {
        final BigDecimal bigDecimal1 = new BigDecimal(number1.toString());
        final BigDecimal bigDecimal2 = new BigDecimal(number2.toString());
        return bigDecimal1.compareTo(bigDecimal2) == 0;
    }
}
