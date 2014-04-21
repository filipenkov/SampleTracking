/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.ofbiz.core.entity.GenericEntityException;

public abstract class AbstractCopyScheme extends AbstractSchemeAwareAction
{
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            getSchemeManager().copyScheme(getScheme());
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(e.getMessage());
        }

        return getRedirect(getRedirectURL());
    }
}
