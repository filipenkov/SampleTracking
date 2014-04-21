package com.atlassian.upm.test;

import com.atlassian.upm.Interval;
import com.atlassian.upm.Interval.Bound;
import com.atlassian.upm.Interval.Bound.Type;
import com.atlassian.upm.Interval.Ceiling;
import com.atlassian.upm.Interval.Floor;
import com.atlassian.upm.rest.resources.updateall.UpdateFailed;

import com.google.common.base.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;

public final class UpmMatchers
{
    private UpmMatchers()
    {
        throw new RuntimeException("Cannot instantiate this class");
    }

    public static UpdateFailedMatcher downloadFailedFor(final String name, final String key, final String version)
    {
        return new DownloadFailedUpdateMatcher(version, name, key);
    }

    public static UpdateFailedMatcher updateFailureOf(final String name, final String key, final String version)
    {
        return new UpdateFailureMatcher(name, version, key);

    }

    public static interface UpdateFailedMatcher extends Matcher<UpdateFailed>
    {
    }

    private static final class UpdateFailureMatcher extends TypeSafeDiagnosingMatcher<UpdateFailed> implements UpdateFailedMatcher
    {
        private final String name;
        private final String version;
        private final String key;

        private UpdateFailureMatcher(String name, String version, String key)
        {
            this.name = name;
            this.version = version;
            this.key = key;
        }

        @Override
        protected boolean matchesSafely(UpdateFailed item, Description mismatchDescription)
        {
            if (!item.getType().equals(UpdateFailed.Type.INSTALL))
            {
                mismatchDescription.appendText("is a ").appendValue(item.getType().toString().toLowerCase()).appendText(" failure");
                return false;
            }

            boolean matches = true;
            if (!name.equals(item.getName()) || !version.equals(item.getVersion()) || !key.equals(item.getKey()))
            {
                matches = false;
                mismatchDescription.appendText("update failure for ").appendValue(item.getName())
                    .appendText(" (").appendValue(item.getKey()).appendText(") version ").appendValue(item.getVersion());
            }
            return matches;
        }

        public void describeTo(Description description)
        {
            description.appendText("update failure for ").appendValue(name)
                .appendText(" (").appendValue(key)
                .appendText(") version ").appendValue(version);
        }
    }

    private static final class DownloadFailedUpdateMatcher extends TypeSafeDiagnosingMatcher<UpdateFailed> implements UpdateFailedMatcher
    {
        private final String version;
        private final String name;
        private final String key;

        private DownloadFailedUpdateMatcher(String version, String name, String key)
        {
            this.version = version;
            this.name = name;
            this.key = key;
        }

        @Override
        protected boolean matchesSafely(UpdateFailed item, Description mismatchDescription)
        {
            if (!item.getType().equals(UpdateFailed.Type.DOWNLOAD))
            {
                mismatchDescription.appendText("is a ").appendValue(item.getType().toString().toLowerCase()).appendText(" failure");
                return false;
            }

            boolean matches = true;
            if (!name.equals(item.getName()) || !version.equals(item.getVersion()) || !key.equals(item.getKey()))
            {
                matches = false;
                mismatchDescription.appendText("download failure for ").appendValue(item.getName())
                    .appendText(" (").appendValue(item.getKey()).appendText(") version ").appendValue(item.getVersion());
            }
            return matches;
        }

        public void describeTo(Description description)
        {
            description.appendText("download failure for ").appendValue(name)
                .appendText(" (").appendValue(key)
                .appendText(") version ").appendValue(version);
        }
    }

    private static final class IntervalBoundMatcher<T extends Comparable<T>, B extends Bound<? extends T>> extends TypeSafeDiagnosingMatcher<B>
    {
        private final String type;
        private final Matcher<? super Type> typeMatcher;
        private final Matcher<? super T> valueMatcher;

        public IntervalBoundMatcher(String type, Matcher<? super Type> typeMatcher, Matcher<? super T> valueMatcher)
        {
            this.type = type;
            this.typeMatcher = typeMatcher;
            this.valueMatcher = valueMatcher;
        }

        protected boolean matchesSafely(B item, Description mismatchDescription)
        {
            Type type = item.getType();
            if (!typeMatcher.matches(type))
            {
                typeMatcher.describeMismatch(type, mismatchDescription.appendText("type "));
                return false;
            }

            T value = item.getValue();
            if (!valueMatcher.matches(value))
            {
                valueMatcher.describeMismatch(value, mismatchDescription.appendText("value "));
                return false;
            }

            return true;
        }

        public void describeTo(Description description)
        {
            description
                .appendText(type + " of ")
                .appendDescriptionOf(valueMatcher)
                .appendText(" with type ")
                .appendDescriptionOf(typeMatcher);
        }
    }

    public static <T extends Comparable<T>, B extends Bound<? extends T>> Matcher<? super B> hasTypeAndValue(String type, Matcher<? super Type> typeMatcher, Matcher<? super T> valueMatcher)
    {
        return new IntervalBoundMatcher<T, B>(type, typeMatcher, valueMatcher);
    }

    private static final class IntervalMatcher<T extends Comparable<T>, I extends Interval<T>> extends TypeSafeDiagnosingMatcher<I>
    {
        private final Matcher<? super Floor<T>> floorMatcher;
        private final Matcher<? super Ceiling<T>> ceilingMatcher;

        public IntervalMatcher(Matcher<? super Floor<T>> floorMatcher,
            Matcher<? super Ceiling<T>> ceilingMatcher)
        {
            this.floorMatcher = floorMatcher;
            this.ceilingMatcher = ceilingMatcher;
        }

        protected boolean matchesSafely(I item, Description mismatchDescription)
        {
            Floor<T> floor = item.getFloor();
            if (!floorMatcher.matches(floor))
            {
                floorMatcher.describeMismatch(floor, mismatchDescription.appendText("floor "));
                return false;
            }

            Ceiling<T> ceiling = item.getCeiling();
            if (!ceilingMatcher.matches(ceiling))
            {
                ceilingMatcher.describeMismatch(ceiling, mismatchDescription.appendText("ceiling "));
                return false;
            }

            return true;
        }

        public void describeTo(Description description)
        {
            description
                .appendText("interval from ")
                .appendDescriptionOf(floorMatcher)
                .appendText(" to ")
                .appendDescriptionOf(ceilingMatcher);
        }
    }

    public static <T extends Comparable<T>, I extends Interval<T>> Matcher<? super I> hasBounds(Matcher<? super Floor<T>> floorMatcher,
        Matcher<? super Ceiling<T>> ceilingMatcher)
    {
        return new IntervalMatcher<T, I>(floorMatcher, ceilingMatcher);
    }

    public static final class IsIntervalContaining<T extends Comparable<T>, I extends Interval<T>> extends TypeSafeDiagnosingMatcher<I>
    {
        private final T element;

        public IsIntervalContaining(T element)
        {
            this.element = element;
        }

        protected boolean matchesSafely(I item, Description mismatchDescription)
        {
            if (!item.contains(element))
            {
                mismatchDescription.appendText("is not contained in interval ").appendValue(item);
                return false;
            }
            return true;
        }

        public void describeTo(Description description)
        {
            description.appendText("element ").appendValue(element);
        }
    }

    public static <T extends Comparable<T>, I extends Interval<T>> Matcher<? super I> intervalContaining(T element)
    {
        return new IsIntervalContaining<T, I>(element);
    }

    public static <T extends Comparable<T>, I extends Interval<T>> Matcher<? super I> intervalContaining(Iterable<T> elements)
    {
        return allOf(transform(elements, new Function<T, Matcher<? super I>>()
        {
            public Matcher<? super I> apply(T element)
            {
                return new IsIntervalContaining<T, I>(element);
            }
        }));
    }

    public static <T extends Comparable<T>> Matcher<? super Interval<T>> intervalContaining(T... elements)
    {
        return UpmMatchers.<T, Interval<T>>intervalContaining(asList(elements));
    }

    private static final class ContainedInInterval<T extends Comparable<T>, I extends Interval<T>> extends TypeSafeDiagnosingMatcher<T>
    {
        private final I interval;

        public ContainedInInterval(I interval)
        {
            this.interval = interval;
        }

        protected boolean matchesSafely(T item, Description mismatchDescription)
        {
            if (!interval.contains(item))
            {
                mismatchDescription.appendText("does not contain ").appendValue(item);
                return false;
            }
            return true;
        }

        public void describeTo(Description description)
        {
            description.appendText("interval ").appendValue(interval);
        }
    }

    public static <T extends Comparable<T>, I extends Interval<T>> Matcher<? super T> containedInInterval(I interval)
    {
        return new ContainedInInterval<T, I>(interval);
    }
}
