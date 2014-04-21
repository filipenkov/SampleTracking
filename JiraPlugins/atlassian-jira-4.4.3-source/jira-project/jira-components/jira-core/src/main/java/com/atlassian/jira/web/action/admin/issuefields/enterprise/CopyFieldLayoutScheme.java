package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Iterator;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class CopyFieldLayoutScheme extends AbstractEditFieldLayoutSchemeAction
{
    public CopyFieldLayoutScheme(FieldLayoutManager fieldLayoutManager)
    {
        super(fieldLayoutManager);
    }

    protected void doValidation()
    {
        validateName();

        if (!invalidInput())
        {
            validateId();

            if (!invalidInput())
            {
                for (Iterator iterator = getFieldLayoutSchemes().iterator(); iterator.hasNext();)
                {
                    FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) iterator.next();
                    if (getFieldLayoutSchemeName().equals(fieldLayoutScheme.getName()))
                    {
                        addError("fieldLayoutSchemeName", getText("admin.errors.fieldlayout.scheme.name.exists"));
                    }
                }

                if (!invalidInput())
                {
                    validateFieldLayoutScheme();
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        FieldLayoutScheme fieldLayoutScheme = new FieldLayoutSchemeImpl(getFieldLayoutManager(), null);
        fieldLayoutScheme.setName(getFieldLayoutSchemeName());
        fieldLayoutScheme.setDescription(getFieldLayoutSchemeDescription());
        fieldLayoutScheme.store();

        for (Iterator iterator = getFieldLayoutScheme().getEntities().iterator(); iterator.hasNext();)
        {
            FieldLayoutSchemeEntity fieldLayoutSchemeEntity = (FieldLayoutSchemeEntity) iterator.next();

            FieldLayoutSchemeEntity newEntity = new FieldLayoutSchemeEntityImpl(getFieldLayoutManager(), null, getConstantsManager());
            newEntity.setIssueTypeId(fieldLayoutSchemeEntity.getIssueTypeId());
            newEntity.setFieldLayoutId(fieldLayoutSchemeEntity.getFieldLayoutId());
            fieldLayoutScheme.addEntity(newEntity);
        }

        return redirectToView();
    }

    protected String getInitialName()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyof",getFieldLayoutScheme().getName());
    }

}
