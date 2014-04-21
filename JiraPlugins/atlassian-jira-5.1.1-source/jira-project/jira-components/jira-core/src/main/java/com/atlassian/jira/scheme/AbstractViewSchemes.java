/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public abstract class AbstractViewSchemes extends AbstractSchemeAwareAction
{
    public List getSchemes() throws GenericEntityException
    {
        return getSchemeManager().getSchemes();
    }

    public List getProjects(GenericValue scheme) throws GenericEntityException
    {
        return getSchemeManager().getProjects(scheme);
    }
}
