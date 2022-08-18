package com.cxylk.agent2.common.util;

import java.util.regex.Pattern;

/**
 * @author likui
 * @date 2022/8/17 上午10:31
 * 源码来自于jacoco项目
 * 将字符串与 目标 匹配，例如 <code>?</code> 的通配符表达式
 * 匹配任意单个字符，<code>*</code> 匹配任意数量的任意字符
 * 特点。多个表达式可以用冒号 (:) 分隔。
 * 如果至少有一部分匹配，则表达式匹配。
 **/
public class WildcardMatcher {
    private final Pattern pattern;

    /**
     * Creates a new matcher with the given expression.
     *
     * @param expression
     *            wildcard expressions
     */
    public WildcardMatcher(final String expression) {
        final String[] parts = expression.split("\\:");
        final StringBuilder regex = new StringBuilder(expression.length() * 2);
        boolean next = false;
        for (final String part : parts) {
            if (next) {
                regex.append('|');
            }
            regex.append('(').append(toRegex(part)).append(')');
            next = true;
        }
        pattern = Pattern.compile(regex.toString());
    }

    private static CharSequence toRegex(final String expression) {
        final StringBuilder regex = new StringBuilder(expression.length() * 2);
        for (final char c : expression.toCharArray()) {
            switch (c) {
                case '?':
                    regex.append(".");
                    break;
                case '*':
                    regex.append(".*");
                    break;
                default:
                    regex.append(Pattern.quote(String.valueOf(c)));
                    break;
            }
        }
        return regex;
    }

    /**
     * Matches the given string against the expressions of this matcher.
     *
     * @param s
     *            string to test
     * @return <code>true</code>, if the expression matches
     */
    public boolean matches(final String s) {
        return pattern.matcher(s).matches();
    }
}
