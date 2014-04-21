package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.rest.FieldHtmlFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.plugin.issuenav.action.ActionUtils.*;

/**
 * A webwork action to produce JSON with field edit html.  This is an action and not a REST resource mainly because our
 * fields API is still so heavily tied to webwork.  All methods on this action should return JSON content.
 *
 * @since 5.0
 */
public class EditAction extends BaseEditAction
{
    private EditFields fields;

    public EditAction(final IssueService issueService, final FieldHtmlFactory fieldHtmlFactory)
    {
        super(issueService, fieldHtmlFactory);
    }

    public String doDefault() throws Exception
    {
        ActionContext.getResponse().setContentType("application/json");

        final IssueService.IssueResult result = issueService.getIssue(getLoggedInUser(), issueId);
        final Issue issue = result.getIssue();
        if (!result.isValid() || result.getIssue() == null)
        {
            this.fields = new EditFields(Collections.<FieldHtmlBean>emptyList(), getXsrfToken(),
                    ErrorCollection.of(result.getErrorCollection()));
            setErrorReturnCode(getLoggedInUser());
            return JSON;
        }

        if (!issueService.isEditable(issue, getLoggedInUser()))
        {
            this.fields = new EditFields(Collections.<FieldHtmlBean>emptyList(), getXsrfToken(),
                    ErrorCollection.of(getText("editissue.error.no.edit.permission")));
            setErrorReturnCode(getLoggedInUser());
            return JSON;
        }

        List<FieldHtmlBean> editFields = fieldHtmlFactory.getEditFields(getLoggedInUser(), this, this, issue, false);
        fields = new EditFields(editFields, getXsrfToken(), ErrorCollection.of(result.getErrorCollection()));

        return JSON;
    }

    public String getJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(fields);
    }
}
