/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.issue;

import com.atlassian.jira.action.JiraNonWebActionSupport;
import org.ofbiz.core.entity.GenericValue;

/**
 * Any action that alters an existing issue should subclass this.
 * <p/>
 * Remember to call super.doValidation() in your validation method.
 */
public abstract class AbstractIssueAction extends JiraNonWebActionSupport
{
    /**
     * The issue being used in the action
     */
    private GenericValue issue;

    // GETTER AND SETTER methods --------------------------------------------------------------------------------
    public GenericValue getIssue()
    {
        return issue;
    }

    public void setIssue(final GenericValue issue)
    {
        this.issue = issue;
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();

        if (getIssue() == null)
        {
            addErrorMessage(getText("admin.errors.did.not.specify.issue"));
        }
    }
}
