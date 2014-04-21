/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer;

import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IterableMatcher<T> extends TypeSafeMatcher<Iterable<T>> {
    private final Iterable<T> expected;

    public IterableMatcher(Iterable<T> expected) {
        this.expected = expected;
    }


    public static <T> IterableMatcher<T> hasOnlyElements(T... elements) {
        return new IterableMatcher<T>(Arrays.asList(elements));
    }

	public static <T> TypeSafeMatcher<Iterable<T>> contains(final T element) {
		return new TypeSafeMatcher<Iterable<T>>() {
			@Override
			public boolean matchesSafely(Iterable<T> given) {
				return Iterables.contains(given, element);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an iterable containing element " + element);
			}

		};
	}

    @Override
    public boolean matchesSafely(Iterable<T> given) {
        final Set<T> s = asSet(expected);


        for (T t : given) {
            if (!s.remove(t)) {
                return false;
            }
        }
        return s.isEmpty();
    }

    private Set<T> asSet(Iterable<T> iterable) {
        final Set<T> s = new HashSet<T>();

        for (T t : iterable) {
            s.add(t);
        }
        return s;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an iterable containing just elements " + asSet(expected));
    }
}
