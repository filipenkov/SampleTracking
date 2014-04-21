package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.plugin.issuenav.service.SearchRendererValueResults;
import com.atlassian.jira.plugin.issuenav.service.SearcherService;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import webwork.action.ActionContext;

import static com.atlassian.jira.plugin.issuenav.action.ActionUtils.*;

/**
 * A webwork action to produce JSON with search field rendered value html and jql response. This is an action and not a
 * REST resource mainly because our fields API is still so heavily tied to webwork.  All methods on this action should
 * return JSON content.
 *
 * @since 5.0
 */
public class SearchRendererValueAction extends JiraWebActionSupport
{
    private final SearcherService searcherService;

    private SearchRendererValueResults results;
    private ErrorCollection errors;

    public SearchRendererValueAction(SearcherService searcherService)
    {
        this.searcherService = searcherService;
    }

    public String doDefault()
    {
        ActionContext.getResponse().setContentType("application/json");
        ServiceOutcome<SearchRendererValueResults> outcome = searcherService.getViewHtml(this, ActionContext.getParameters());
        if (outcome.isValid())
        {
            this.results = outcome.getReturnedValue();
            return JSON;
        }
        else
        {
            this.errors = ErrorCollection.of(outcome.getErrorCollection());
            // Errors here are all from edit html, though may change in future
            setErrorReturnCode(getLoggedInUser());
            return ERROR;
        }
    }
    
    public String getJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(this.results);
    }

    public String getErrorJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(errors);
    }

}
