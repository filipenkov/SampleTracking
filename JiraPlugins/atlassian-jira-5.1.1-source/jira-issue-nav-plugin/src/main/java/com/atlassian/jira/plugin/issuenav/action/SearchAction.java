package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.plugin.issuenav.service.SearchResults;
import com.atlassian.jira.plugin.issuenav.service.SearcherService;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import webwork.action.ActionContext;

import static com.atlassian.jira.plugin.issuenav.action.ActionUtils.*;

/**
 * A webwork action to produce JSON with searchers, their values (clauses) and search results. This is an action and not a
 * REST resource mainly because our fields API is still so heavily tied to webwork.  All methods on this action should
 * return JSON content.
 *
 * @since 5.0
 */
public class SearchAction extends JiraWebActionSupport
{
    private final SearcherService searcherService;

    private SearchResults results;
    private ErrorCollection errors;
    private String jql;

    public SearchAction(SearcherService searcherService)
    {
        this.searcherService = searcherService;
    }

    public String doDefault()
    {
        ActionContext.getResponse().setContentType("application/json");
        ServiceOutcome<SearchResults> outcome = searcherService.search(this, ActionContext.getParameters());
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
    
    public String doJql() {
        ActionContext.getResponse().setContentType("application/json");
        ServiceOutcome<SearchResults> outcome = searcherService.searchWithJql(this, jql);
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

    public String getJql()
    {
        return jql;
    }

    public void setJql(String jql)
    {
        this.jql = jql;
    }
}
