/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import com.google.common.base.Function;

import javax.annotation.Nonnull;

public interface NamedExternalObject {

	static final class IdFunction implements Function<NamedExternalObject, String> {
		public String apply(@Nonnull NamedExternalObject from) {
			return from.getId();
		}
	}

	static final class NameFunction implements Function<NamedExternalObject, String> {
		public String apply(@Nonnull NamedExternalObject from) {
			return from.getName();
		}
	};

	static IdFunction ID_FUNCTION = new IdFunction();

	static NameFunction NAME_FUNCTION = new NameFunction();

	String getId();

	String getName();

}