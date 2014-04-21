/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteCustomField extends JiraWebActionSupport
{
    private CustomField customField;
    private Long id;

    private final CustomFieldManager customFieldManager;
    private final CustomFieldService customFieldService;

    public DeleteCustomField(CustomFieldService customFieldService, CustomFieldManager customFieldManager)
    {
        this.customFieldService = customFieldService;
        this.customFieldManager = customFieldManager;
    }

    public void doValidation()
    {
        customFieldService.validateDelete(getJiraServiceContext(), getId());
    }

    @RequiresXsrfCheck
    public String doExecute() throws Exception
    {
        customFieldManager.removeCustomField(getCustomField());

        return getRedirect("ViewCustomFields.jspa");
    }

    public CustomField getCustomField()
    {
        if (customField == null)
        {
            customField = customFieldManager.getCustomFieldObject(getId());
        }
        return customField;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
}
