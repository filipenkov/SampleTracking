package com.atlassian.streams.api.common;

import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.transform;

public class Predicates
{
    public static Predicate<String> containsAnyString(Iterable<String> xs)
    {
        return or(transform(xs, containsString()));
    }

    public static Function<String, Predicate<String>> containsString()
    {
        return ContainsStringFunction.INSTANCE;
    }

    private enum ContainsStringFunction implements Function<String, Predicate<String>>
    {
        INSTANCE;

        public Predicate<String> apply(String s)
        {
            return new ContainsString(s);
        }
    }

    private static final class ContainsString implements Predicate<String>
    {
        private final String s;

        public ContainsString(String s)
        {
            this.s = s;
        }

        public boolean apply(String input)
        {
            return input.contains(s);
        }

        @Override
        public String toString()
        {
            return String.format("containsString(%s)", s);
        }
    }

    public static Predicate<String> containsString(String x)
    {
        return containsString().apply(x);
    }

    public static <A> Predicate<A> contains(Iterable<A> xs)
    {
        return new Contains<A>(xs);
    }

    private static final class Contains<A> implements Predicate<A>
    {
        private final Iterable<A> xs;

        private Contains(Iterable<A> xs)
        {
            this.xs = xs;
        }

        public boolean apply(A x)
        {
            return com.google.common.collect.Iterables.contains(xs, x);
        }

        @Override
        public String toString()
        {
            return String.format("contains(%s)", xs);
        }
    }

    public static Predicate<String> containsAnyIssueKey(Iterable<String> keys)
    {
        return or(transform(keys, containsIssueKey()));
    }

    public static Predicate<String> containsIssueKey(String key)
    {
        return containsIssueKey().apply(key);
    }

    public static Function<String, Predicate<String>> containsIssueKey()
    {
        return ContainsIssueKeyFunction.INSTANCE;
    }

    private enum ContainsIssueKeyFunction implements Function<String, Predicate<String>>
    {
        INSTANCE;

        public Predicate<String> apply(String key)
        {
            return new ContainsIssueKey(key);
        }
    }

    private static final class ContainsIssueKey implements Predicate<String>
    {
        private final String key;
        private final Pattern pattern;

        public ContainsIssueKey(String key)
        {
            this.key = key;
            // Find "ONE-1" but not "CR-ONE-1"
            String regex = "(?<!CR-)\\b" + key + "\\b";
            this.pattern = Pattern.compile(regex);
        }

        public boolean apply(String input)
        {
            return pattern.matcher(input).find();
        }

        @Override
        public String toString()
        {
            return String.format("containsIssueKey(%s)", key);
        }
    }
}
