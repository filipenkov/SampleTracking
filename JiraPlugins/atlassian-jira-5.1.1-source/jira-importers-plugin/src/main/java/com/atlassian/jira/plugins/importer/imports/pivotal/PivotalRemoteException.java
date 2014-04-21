/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import java.io.IOException;

public class PivotalRemoteException extends IOException {
	public PivotalRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public PivotalRemoteException(Throwable cause) {
		super(cause);
	}

	public PivotalRemoteException(String message) {
		super(message);
	}
}
