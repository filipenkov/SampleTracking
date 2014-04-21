package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractFieldScreenSchemeAction extends JiraWebActionSupport
{
    private final FieldScreenSchemeManager fieldScreenSchemeManager;

    private String fieldScreenSchemeName;
    private String fieldScreenSchemeDescription;
    private Long id;
    private Collection fieldScreenSchemes;

    protected AbstractFieldScreenSchemeAction(FieldScreenSchemeManager fieldScreenSchemeManager)
    {
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
    }

    public String getFieldScreenSchemeName()
    {
        return fieldScreenSchemeName;
    }

    public void setFieldScreenSchemeName(String fieldScreenSchemeName)
    {
        this.fieldScreenSchemeName = fieldScreenSchemeName;
    }

    public String getFieldScreenSchemeDescription()
    {
        return fieldScreenSchemeDescription;
    }

    public void setFieldScreenSchemeDescription(String fieldScreenSchemeDescription)
    {
        this.fieldScreenSchemeDescription = fieldScreenSchemeDescription;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public FieldScreenScheme getFieldScreenScheme()
    {
        return fieldScreenSchemeManager.getFieldScreenScheme(getId());
    }

    public Collection getFieldScreenSchemes()
    {
        if (fieldScreenSchemes == null)
        {
            fieldScreenSchemes = getFieldScreenSchemeManager().getFieldScreenSchemes();
        }

        return fieldScreenSchemes;
    }

    protected FieldScreenSchemeManager getFieldScreenSchemeManager()
    {
        return fieldScreenSchemeManager;
    }

    protected void validateName(boolean avoidSameScheme)
    {
        if (!TextUtils.stringSet(getFieldScreenSchemeName()))
        {
            addError("fieldScreenSchemeName", getText("admin.common.errors.validname"));
        }
        else
        {
            // Check that a screen scheme with this name does not already exist
            for (Iterator iterator = getFieldScreenSchemes().iterator(); iterator.hasNext();)
            {
                FieldScreenScheme fieldScreenScheme = (FieldScreenScheme) iterator.next();
                // If we are not avoiding the same scheme look at all schemes, if we are avoiding the same scheme then only
                // look at schemes with different ids 
                if ( ((!avoidSameScheme) || (avoidSameScheme && !getId().equals(fieldScreenScheme.getId())))
                        && getFieldScreenSchemeName().equals(fieldScreenScheme.getName()))
                {
                    addError("fieldScreenSchemeName", getText("admin.errors.screens.duplicate.screen.scheme"));
                }
            }
        }
    }

    protected void validateId()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.required"));
        }
        else
        {
            // Ensure a field screen scheme with this name exists
            if (getFieldScreenScheme() == null)
            {
                addErrorMessage(getText("admin.errors.screens.inexistent.screen.with.id"));
            }
        }
    }

    protected String redirectToView()
    {
        return getRedirect("ViewFieldScreenSchemes.jspa");
    }
}
