package com.atlassian.jira.security.util;

import com.atlassian.jira.scheme.SchemeManager;
import org.ofbiz.core.entity.GenericEntityException;

/**
 * Created by IntelliJ IDEA.
 * User: owenfellows
 * Date: 02-Aug-2004
 * Time: 14:40:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractGroupToSchemeMapper extends AbstractGroupMapper
{
    private SchemeManager schemeManager;

    public AbstractGroupToSchemeMapper(SchemeManager schemeManager) throws GenericEntityException
    {
        this.schemeManager = schemeManager;
        setGroupMapping(init());
    }

    protected SchemeManager getSchemeManager()
    {
        return schemeManager;
    }
}
