package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.scheme.AbstractViewSchemes;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

@WebSudoRequired
public class ViewSchemes extends AbstractViewSchemes
{
    private final WorkflowSchemeManager workflowSchemeManager;

    public ViewSchemes(final WorkflowSchemeManager workflowSchemeManager)
    {
        this.workflowSchemeManager = workflowSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return workflowSchemeManager;
    }

    public GenericValue getDefaultEntity(GenericValue scheme) throws GenericEntityException
    {
        return ((WorkflowSchemeManager) getSchemeManager()).getDefaultEntity(scheme);
    }

    public List getNonDefaultEntities(GenericValue scheme) throws GenericEntityException
    {
        return ((WorkflowSchemeManager) getSchemeManager()).getNonDefaultEntities(scheme);
    }

    public String getRedirectURL()
    {
        return null;
    }

    public boolean isActive(GenericValue scheme) throws GenericEntityException
    {
        return getProjects(scheme).size() > 0;
    }
}
