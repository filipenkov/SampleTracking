/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class Immutables {

	private Immutables() {

	}

	public static <E, A> ImmutableList<E> transformThenCopyToList(Iterable<A> iterable, Function<? super A, E> transform) {
		return ImmutableList.copyOf(Iterables.transform(iterable, transform));
	}

    public static <E, A> ImmutableSet<E> transformThenCopyToSet(Iterable<A> iterable, Function<? super A, E> transform) {
        return ImmutableSet.copyOf(Iterables.transform(iterable, transform));
    }

    public static <A> ImmutableList<A> filterThenCopyToList(Iterable<A> iterable, Predicate<? super A> transform) {
        return ImmutableList.copyOf(Iterables.filter(iterable, transform));
    }

}
