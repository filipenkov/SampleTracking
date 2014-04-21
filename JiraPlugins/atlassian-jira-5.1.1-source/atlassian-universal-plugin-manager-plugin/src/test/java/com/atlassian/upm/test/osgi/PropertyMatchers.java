package com.atlassian.upm.test.osgi;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.atlassian.upm.Functions;

import com.google.common.base.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.atlassian.upm.Functions.applyEach;
import static com.atlassian.upm.test.osgi.MatcherFunctions.MatcherFactory;
import static com.atlassian.upm.test.osgi.MatcherFunctions.MatcherFactoryTransformer;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

public final class PropertyMatchers
{
    public static final class PropertyExtractor<Parent, Child> implements Function<Parent, Child>, SelfDescribing
    {
        private final String name;
        private final Function<Parent, Child> propertyFn;

        public PropertyExtractor(String name, Function<Parent, Child> propertyFn)
        {
            this.name = name;
            this.propertyFn = propertyFn;
        }

        public Child apply(@Nullable Parent parent)
        {
            return propertyFn.apply(parent);
        }

        public void describeTo(Description description)
        {
            description.appendText(name);
        }
    }

    public static final class Property<Parent, OuterChild, InnerChild> implements MatcherFactory<Parent>
    {
        private final PropertyTransformer<Parent, OuterChild> propertyTransformer;
        private final MatcherFactoryTransformer<InnerChild, OuterChild> transformer;

        private Property(PropertyExtractor<Parent, OuterChild> extractor,
            MatcherFactoryTransformer<InnerChild, OuterChild> transformer)
        {
            this.propertyTransformer = new PropertyTransformer<Parent, OuterChild>(extractor);
            this.transformer = transformer;
        }

        public RefinedProperty<Parent, OuterChild, InnerChild> by(MatcherFactory<InnerChild>... subProperties)
        {
            return new RefinedProperty<Parent, OuterChild, InnerChild>(propertyTransformer, transformer, subProperties);
        }

        public Matcher<? super Parent> apply(@Nullable Parent expected)
        {
            return propertyTransformer.apply(
                transformer.apply(
                    new EqualToFactory<InnerChild>())).apply(expected);
        }
    }

    public static final class RefinedProperty<Parent, OuterChild, InnerChild> implements MatcherFactory<Parent>
    {
        private final PropertyTransformer<Parent, OuterChild> propertyTransformer;
        private final MatcherFactoryTransformer<InnerChild, OuterChild> transformer;
        private final Iterable<MatcherFactory<InnerChild>> subProperties;

        private RefinedProperty(PropertyTransformer<Parent, OuterChild> propertyTransformer,
            MatcherFactoryTransformer<InnerChild, OuterChild> transformer,
            MatcherFactory<InnerChild>... subProperties)
        {
            this.propertyTransformer = propertyTransformer;
            this.transformer = transformer;
            this.subProperties = copyOf(asList(subProperties));
        }

        public Matcher<? super Parent> apply(@Nullable Parent expected)
        {
            return propertyTransformer.apply(
                transformer.apply(
                    new SubPropertyMatcherFactory<InnerChild>(subProperties))).apply(expected);
        }
    }

    private static final class PropertyMatcher<Parent, Child> extends TypeSafeDiagnosingMatcher<Parent>
    {
        private final PropertyExtractor<Parent, Child> extractor;
        private final Matcher<? super Child> childMatcher;

        private PropertyMatcher(PropertyExtractor<Parent, Child> extractor, Matcher<? super Child> childMatcher)
        {
            this.extractor = extractor;
            this.childMatcher = childMatcher;
        }

        protected boolean matchesSafely(Parent item, Description mismatchDescription)
        {
            Child actual = extractor.apply(item);
            if (!childMatcher.matches(actual))
            {
                childMatcher.describeMismatch(actual,
                    mismatchDescription
                        .appendDescriptionOf(extractor)
                        .appendText(" "));
                return false;
            }
            return true;
        }

        public void describeTo(Description description)
        {
            description
                .appendDescriptionOf(extractor)
                .appendText(" ")
                .appendDescriptionOf(childMatcher);
        }
    }

    private static final class PropertyMatcherFactory<Parent, Child> implements MatcherFactory<Parent>
    {
        private final PropertyExtractor<Parent, Child> extractor;
        private final MatcherFactory<Child> childMatcherFactory;

        private PropertyMatcherFactory(PropertyExtractor<Parent, Child> extractor, MatcherFactory<Child> childMatcherFactory)
        {
            this.extractor = extractor;
            this.childMatcherFactory = childMatcherFactory;
        }

        public Matcher<? super Parent> apply(@Nullable Parent expected)
        {
            return new PropertyMatcher<Parent, Child>(extractor, childMatcherFactory.apply(extractor.apply(expected)));
        }
    }

    private static final class PropertyTransformer<Parent, Child> implements MatcherFactoryTransformer<Child, Parent>
    {
        private final PropertyExtractor<Parent, Child> extractor;

        private PropertyTransformer(PropertyExtractor<Parent, Child> extractor)
        {
            this.extractor = extractor;
        }

        public MatcherFactory<Parent> apply(@Nullable MatcherFactory<Child> childMatcherFactory)
        {
            return new PropertyMatcherFactory<Parent, Child>(extractor, childMatcherFactory);
        }
    }

    private static final class EqualToFactory<T> implements MatcherFactory<T>
    {
        public Matcher<? super T> apply(@Nullable T expected)
        {
            return equalTo(expected);
        }
    }

    private static final class SubPropertyMatcher<Child> extends TypeSafeDiagnosingMatcher<Child>
    {
        private final List<Matcher<? super Child>> subPropertyMatchers;

        private SubPropertyMatcher(@Nullable Child expected,
            Iterable<MatcherFactory<Child>> subProperties)
        {
            this.subPropertyMatchers = copyOf(applyEach(subProperties, expected));
        }

        protected boolean matchesSafely(Child item, Description mismatchDescription)
        {
            boolean matches = true;
            for (org.hamcrest.Matcher<? super Child> subPropertyMatcher : subPropertyMatchers)
            {
                if (!subPropertyMatcher.matches(item))
                {
                    if (!matches)
                    {
                        mismatchDescription.appendText(", ");
                    }
                    subPropertyMatcher.describeMismatch(item, mismatchDescription);
                    matches = false;
                }
            }
            return matches;
        }

        public void describeTo(Description description)
        {
            for (int i = 0; i < subPropertyMatchers.size(); ++i)
            {
                if (i != 0)
                {
                    description.appendText(i < subPropertyMatchers.size() - 1 ? ", " : " and ");
                }
                description.appendDescriptionOf(subPropertyMatchers.get(i));
            }
        }
    }

    private static final class SubPropertyMatcherFactory<Child> implements MatcherFactory<Child>
    {
        private final Iterable<MatcherFactory<Child>> subProperties;

        private SubPropertyMatcherFactory(Iterable<MatcherFactory<Child>> subProperties)
        {
            this.subProperties = subProperties;
        }

        public Matcher<? super Child> apply(@Nullable Child expected)
        {
            return new SubPropertyMatcher<Child>(expected, subProperties);
        }
    }

    public static final class Binder<T>
    {
        private final String name;
        private final T expected;

        private Binder(String name, T expected)
        {
            this.name = name;
            this.expected = expected;
        }

        public Matcher<? super T> by(MatcherFactory<T>... properties)
        {
            return property(name, com.google.common.base.Functions.<T>identity()).by(properties).apply(expected);
        }
    }

    public static <Parent, Child> Property<Parent, Child, Child> property(String name, Function<Parent, Child> propertyFn)
    {
        return new Property<Parent, Child, Child>(
            new PropertyExtractor<Parent, Child>(name, propertyFn),
            MatcherFunctions.<Child> identity());
    }

    public static <Parent, Child> Property<Parent, Child, Child> property(String name)
    {
        return property(name, Functions.<Parent, Child> getter(name));
    }

    public static <Parent, Child> Property<Parent, Iterable<Child>, Child> iterableProperty(String name, Function<Parent, Iterable<Child>> propertyFn)
    {
        return new Property<Parent, Iterable<Child>, Child>(
            new PropertyExtractor<Parent, Iterable<Child>>(name, propertyFn),
            MatcherFunctions.<Child> iterable());
    }

    public static <Parent, Child> Property<Parent, Iterable<Child>, Child> iterableProperty(String name)
    {
        return iterableProperty(name, Functions.<Parent, Iterable<Child>> getter(name));
    }

    public static <K, V> Property<Entry<K, V>, K, K> key(String name)
    {
        return property(name, new Function<Entry<K, V>, K>()
            {
                public K apply(@Nullable Entry<K, V> entry)
                {
                    return entry.getKey();
                }
            });
    }

    public static <K, V> Property<Entry<K, V>, V, V> value(String name)
    {
        return property(name, new Function<Entry<K, V>, V>()
            {
                public V apply(@Nullable Entry<K, V> entry)
                {
                    return entry.getValue();
                }
            });
    }

    // ewww, i need a better way to compose this stuff
    public static <K, V> Property<Entry<K, Iterable<V>>, Iterable<V>, V> iterableValue(String name)
    {
        return iterableProperty(name, new Function<Entry<K, Iterable<V>>, Iterable<V>>()
            {
                public Iterable<V> apply(@Nullable Entry<K, Iterable<V>> entry)
                {
                    return entry.getValue();
                }
            });
    }

    public static <Parent, K, V> Property<Parent, Map<K, V>, Entry<K, V>>
        mapProperty(String name, Function<Parent, Map<K, V>> propertyFn)
    {
        return new Property<Parent, Map<K, V>, Entry<K, V>>(
            new PropertyExtractor<Parent, Map<K, V>>(name, propertyFn),
            MatcherFunctions.<K, V> map());
    }

    public static <Parent, K, V> Property<Parent, Map<K, V>, Entry<K, V>>
        mapProperty(String name)
    {
        return mapProperty(name, Functions.<Parent, Map<K, V>> getter(name));
    }

    public static <T> Binder<T> matches(String name, T expected)
    {
        return new Binder<T>(name, expected);
    }
}