/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class ExternalRuntimeException extends NestableRuntimeException {
	public ExternalRuntimeException() {
	}

	public ExternalRuntimeException(String s) {
		super(s);
	}

	public ExternalRuntimeException(Throwable throwable) {
		super(throwable);
	}

	public ExternalRuntimeException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
