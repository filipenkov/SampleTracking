/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class ViewSchemes extends JiraWebActionSupport
{
    private String fieldLayoutSchemeName;
    private String fieldLayoutSchemeDescription;

    private final FieldLayoutManager fieldLayoutManager;
    private Map schemeProjectsMap;

    public ViewSchemes(FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        schemeProjectsMap = new HashMap();
    }

    public List getFieldLayoutScheme()
    {
        try
        {
            return getFieldLayoutManager().getFieldLayoutSchemes();
        }
        catch (DataAccessException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.fieldlayout.could.not.retrieve"));
            return Collections.EMPTY_LIST;
        }
    }

    public Collection getSchemeProjects(FieldLayoutScheme fieldLayoutScheme)
    {
        if (fieldLayoutScheme == null)
            throw new IllegalArgumentException(getText("admin.errors.fieldlayout.fls.must.not.be.null"));

        if (!schemeProjectsMap.containsKey(fieldLayoutScheme.getId()))
        {
            try
            {
                schemeProjectsMap.put(fieldLayoutScheme.getId(), getFieldLayoutManager().getProjects(fieldLayoutScheme));
            }
            catch (DataAccessException e)
            {
                log.error(e, e);
                addErrorMessage(getText("admin.errors.fieldlayout.could.not.retrieve.projects",fieldLayoutScheme));
                return Collections.EMPTY_LIST;
            }
        }

        Collection projects = (Collection) schemeProjectsMap.get(fieldLayoutScheme.getId());
        Collections.sort((List) projects, OfBizComparators.NAME_COMPARATOR);
        return projects;
    }

    protected FieldLayoutManager getFieldLayoutManager()
    {
        return fieldLayoutManager;
    }

    @RequiresXsrfCheck
    public String doAddScheme()
    {
        if (!TextUtils.stringSet(getFieldLayoutSchemeName()))
        {
            addError("fieldLayoutSchemeName", getText("admin.common.errors.validname"));
        }
        else if (fieldLayoutManager.fieldConfigurationSchemeExists(getFieldLayoutSchemeName()))
        {
            addError("fieldLayoutSchemeName", getText("admin.errors.fieldlayout.scheme.name.exists"));
        }

        if (!invalidInput())
        {
            FieldLayoutScheme fieldLayoutScheme = new FieldLayoutSchemeImpl(fieldLayoutManager, null);
            fieldLayoutScheme.setName(getFieldLayoutSchemeName());
            fieldLayoutScheme.setDescription(getFieldLayoutSchemeDescription());
            fieldLayoutScheme.store();
            // Make a default entry for the scheme
            FieldLayoutSchemeEntity fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, getConstantsManager());
            fieldLayoutSchemeEntity.setIssueTypeId(null);
            fieldLayoutSchemeEntity.setFieldLayoutId(null);
            fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);
            fieldLayoutScheme.store();
            return getRedirect("ViewFieldLayoutSchemes.jspa");
        }

        return getResult();
    }

    public String getFieldLayoutSchemeName()
    {
        return fieldLayoutSchemeName;
    }

    public void setFieldLayoutSchemeName(String fieldLayoutSchemeName)
    {
        this.fieldLayoutSchemeName = fieldLayoutSchemeName;
    }

    public String getFieldLayoutSchemeDescription()
    {
        return fieldLayoutSchemeDescription;
    }

    public void setFieldLayoutSchemeDescription(String fieldLayoutSchemeDescription)
    {
        this.fieldLayoutSchemeDescription = fieldLayoutSchemeDescription;
    }

}
