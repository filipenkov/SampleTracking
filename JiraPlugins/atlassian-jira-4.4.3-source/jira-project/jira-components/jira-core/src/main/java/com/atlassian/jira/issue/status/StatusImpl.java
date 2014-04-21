/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.status;

import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.ofbiz.core.entity.GenericValue;

public class StatusImpl extends IssueConstantImpl implements Status
{
    public StatusImpl(GenericValue genericValue, TranslationManager translationManager, JiraAuthenticationContext authenticationContext)
    {
        super(genericValue, translationManager, authenticationContext);
    }
}
