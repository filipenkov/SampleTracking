/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import org.apache.http.StatusLine;

public class PivotalHttpException extends PivotalRemoteException {

	private final StatusLine statusLine;

	public PivotalHttpException(String message, Throwable cause, StatusLine statusLine) {
		super(message, cause);
		this.statusLine = statusLine;
	}

	public PivotalHttpException(Throwable cause, StatusLine statusLine) {
		super(cause);
		this.statusLine = statusLine;
	}

	public PivotalHttpException(String message, StatusLine statusLine) {
		super(message);
		this.statusLine = statusLine;
	}

	public StatusLine getStatusLine() {
		return statusLine;
	}
}
