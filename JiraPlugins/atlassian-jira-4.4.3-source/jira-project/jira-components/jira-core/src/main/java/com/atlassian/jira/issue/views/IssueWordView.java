package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.FileIconBean;

/**
 * A view of an issue that produces a full XML view of an issue.  It is also valid RSS.
 */
public class IssueWordView extends AbstractIssueHtmlView
{
    public IssueWordView(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, CommentManager commentManager, FileIconBean fileIconBean, FieldScreenRendererFactory fieldScreenRendererFactory, IssueViewUtil issueViewUtil)
    {
        super(authenticationContext, applicationProperties, commentManager, fileIconBean, fieldScreenRendererFactory, issueViewUtil);
    }

    protected String getLinkToPrevious(Issue issue)
    {
        return null; // we don't want a link in the 'word' view.
    }

    protected boolean printCssLinks()
    {
        return false;
    }

    public void writeHeaders(Issue issue, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams)
    {
        WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
        requestHeaders.addHeader("content-disposition", "attachment;filename=\"" + issue.getKey() + ".doc\";");
    }
}
