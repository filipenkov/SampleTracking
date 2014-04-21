/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.Multimap;

import java.util.Arrays;
import java.util.Collection;

/**
 * A MultiHashMap implementation backed by a HashSet. It also quietly rejects Empty Values strings
 */
public class SetMultiHashMap<K, V> extends ForwardingMultimap<K, V> {

	private final Multimap<K,V> map;

	public SetMultiHashMap(Multimap<K, V> map) {
		this.map = map;
	}

	@Override
	protected Multimap<K, V> delegate() {
		return map;
	}

	@Override
	public boolean put(K key, V value) {
		if (value instanceof Object[]) {
			return this.putAll(key, Arrays.asList(value));
		} else if (value instanceof Collection) {
			return this.putAll(key, (Collection<? extends V>) value);
		} else if (value != null && !"".equals(value)) {
			return super.put(key, value);
		} else {
			return false;
		}
	}
}
