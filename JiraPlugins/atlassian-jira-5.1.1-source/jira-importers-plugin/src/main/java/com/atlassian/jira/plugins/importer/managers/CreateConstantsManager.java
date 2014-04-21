/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.plugins.importer.external.ExternalException;

public interface CreateConstantsManager {

    IssueConstant getConstant(String constantValue, String constantType);

    String addConstant(final String constantName, final String constantType) throws ExternalException;

}
