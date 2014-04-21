package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.plugin.issuenav.service.SearcherService;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import webwork.action.ActionContext;

import static com.atlassian.jira.plugin.issuenav.action.ActionUtils.*;

/**
 * A webwork action to produce JSON with search field edit html.  This is an action and not a REST resource mainly because our
 * fields API is still so heavily tied to webwork.  All methods on this action should return JSON content.
 *
 * @since 5.0
 */
public class SearchRendererEditAction extends JiraWebActionSupport
{
    private final SearcherService searcherService;

    private String fieldId;
    private String jqlContext;
    private String editHtml;
    private ErrorCollection errors;

    public SearchRendererEditAction(SearcherService searcherService)
    {
        this.searcherService = searcherService;
    }

    public String doDefault()
    {
        ActionContext.getResponse().setContentType("application/json");

        ServiceOutcome<String> outcome = searcherService.getEditHtml(getFieldId(), getJqlContext(), this);
        
        if (outcome.isValid())
        {
            this.editHtml = outcome.getReturnedValue();
            return JSON;
        }
        else
        {
            this.errors = ErrorCollection.of(outcome.getErrorCollection());
            setErrorReturnCode(getLoggedInUser());
            return ERROR;
        }
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(final String fieldId)
    {
        this.fieldId = fieldId;
    }

    public String getJqlContext()
    {
        return jqlContext;
    }

    public void setJqlContext(String jqlContext)
    {
        this.jqlContext = jqlContext;
    }

    public String getJson()
    {
        return editHtml;
    }

    public String getErrorJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(errors);
    }
}
