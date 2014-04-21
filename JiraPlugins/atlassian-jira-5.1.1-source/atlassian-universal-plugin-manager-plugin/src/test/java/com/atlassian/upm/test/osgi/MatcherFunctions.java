package com.atlassian.upm.test.osgi;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.atlassian.upm.Pairs.MutablePair;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.atlassian.upm.Pairs.MutablePair.pair;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;

public final class MatcherFunctions
{
    protected static interface MatcherFactory<T>
        extends Function<T, Matcher<? super T>> {}

    protected static interface MatcherFactoryTransformer<F, T>
        extends Function<MatcherFactory<F>, MatcherFactory<T>> {}

    private static final class IdentityTransformer<T> implements MatcherFactoryTransformer<T, T>
    {
        public MatcherFactory<T> apply(@Nullable MatcherFactory<T> factory)
        {
            return factory;
        }
    }

    public static <T> MatcherFactoryTransformer<T, T> identity()
    {
        return new IdentityTransformer<T>();
    }

    private static final class IterableMatcher<T> extends TypeSafeDiagnosingMatcher<Iterable<T>>
    {
        private final List<MutablePair<Matcher<? super T>, Boolean>> innerMatchers;

        private IterableMatcher(MatcherFactory<T> inner, Iterable<T> expected)
        {
            this.innerMatchers = initialize(transform(expected, inner));
        }

        protected boolean matchesSafely(Iterable<T> item, Description mismatchDescription)
        {
            List<MutablePair<T, Boolean>> actuals = initialize(item);
            if (innerMatchers.size() != actuals.size())
            {
                mismatchDescription.appendText(format("contained %d element(s)", actuals.size()));
                return false;
            }

            for (MutablePair<Matcher<? super T>, Boolean> matcher : innerMatchers)
            {
                for (MutablePair<T, Boolean> actual : actuals)
                {
                    if (matcher.getFirst().matches(actual.getFirst()))
                    {
                        matcher.setSecond(true);
                        actual.setSecond(true);
                    }
                }
            }

            boolean matches = true;
            for (MutablePair<Matcher<? super T>, Boolean> matcher : innerMatchers)
            {
                if (!matcher.getSecond())
                {
                    if (!matches)
                    {
                        mismatchDescription.appendText(", ");
                    }
                    mismatchDescription
                        .appendText("did not contain a match for ")
                        .appendDescriptionOf(matcher.getFirst());
                    matches = false;
                }
            }

            for (MutablePair<T, Boolean> actual : actuals)
            {
                if (!actual.getSecond())
                {
                    if (!matches)
                    {
                        mismatchDescription.appendText(", ");
                    }
                    mismatchDescription
                        .appendText("contained unmatched element ")
                        .appendValue(actual.getFirst());
                    matches = false;
                }
            }
            return matches;
        }

        public void describeTo(Description description)
        {
            description.appendText(format("containing elements ("));
            for (int i = 0; i < innerMatchers.size(); ++i)
            {
                if (i != 0)
                {
                    description.appendText(i < innerMatchers.size() - 1 ? ", " : " and ");
                }
                description.appendDescriptionOf(innerMatchers.get(i).getFirst());
            }
            description.appendText(")");
        }

        private static <T> List<MutablePair<T, Boolean>> initialize(Iterable<T> from)
        {
            ImmutableList.Builder<MutablePair<T, Boolean>> pairs = ImmutableList.builder();
            for (final T element : from)
            {
                pairs.add(pair(element, false));
            }
            return pairs.build();
        }
    }

    private static final class IterableMatcherFactory<T> implements MatcherFactory<Iterable<T>>
    {
        private final MatcherFactory<T> inner;

        private IterableMatcherFactory(MatcherFactory<T> inner)
        {
            this.inner = inner;
        }

        public Matcher<? super Iterable<T>> apply(@Nullable Iterable<T> expected)
        {
            return new IterableMatcher<T>(inner, expected);
        }
    }

    private static final class IterableTransformer<T> implements MatcherFactoryTransformer<T, Iterable<T>>
    {
        public MatcherFactory<Iterable<T>> apply(@Nullable MatcherFactory<T> inner)
        {
            return new IterableMatcherFactory<T>(inner);
        }
    }

    public static <T> MatcherFactoryTransformer<T, Iterable<T>> iterable()
    {
        return new IterableTransformer<T>();
    }

    private static final class MapMatcher<K, V> extends TypeSafeDiagnosingMatcher<Map<K, V>>
    {
        private final IterableMatcher<Entry<K, V>> innerMatcher;

        private MapMatcher(MatcherFactory<Entry<K, V>> inner, Map<K, V> expected)
        {
            this.innerMatcher = new IterableMatcher<Entry<K, V>>(inner, expected.entrySet());
        }

        protected boolean matchesSafely(Map<K, V> item, Description mismatchDescription)
        {
            return innerMatcher.matchesSafely(item.entrySet(), mismatchDescription);
        }

        public void describeTo(Description description)
        {
            innerMatcher.describeTo(description);
        }
    }

    private static final class MapMatcherFactory<K, V> implements MatcherFactory<Map<K, V>>
    {
        private final MatcherFactory<Entry<K, V>> inner;

        private MapMatcherFactory(MatcherFactory<Entry<K, V>> inner)
        {
            this.inner = inner;
        }

        public Matcher<? super Map<K, V>> apply(@Nullable Map<K, V> expected)
        {
            return new MapMatcher<K, V>(inner, expected);
        }
    }

    private static final class MapTransformer<K, V> implements MatcherFactoryTransformer<Entry<K, V>, Map<K, V>>
    {
        public MatcherFactory<Map<K, V>> apply(@Nullable MatcherFactory<Entry<K, V>> inner)
        {
            return new MapMatcherFactory<K, V>(inner);
        }
    }

    public static final <K, V> MatcherFactoryTransformer<Entry<K, V>, Map<K, V>> map()
    {
        return new MapTransformer<K, V>();
    }
}
