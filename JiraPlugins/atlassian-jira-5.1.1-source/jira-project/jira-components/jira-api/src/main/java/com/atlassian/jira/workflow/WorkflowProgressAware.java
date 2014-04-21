/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.MutableIssue;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public interface WorkflowProgressAware
{
    public User getRemoteUser();

    public int getAction();

    public void setAction(int action);

    public void addErrorMessage(String error);

    public void addError(String name, String error);

    public Map getAdditionalInputs();

    public MutableIssue getIssue() throws Exception;

    public GenericValue getProject() throws Exception;
}
