/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import java.io.IOException;

public class FogBugzRemoteException extends IOException {
	public FogBugzRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public FogBugzRemoteException(Throwable cause) {
		super(cause);
	}

	public FogBugzRemoteException(String message) {
		super(message);
	}
}
