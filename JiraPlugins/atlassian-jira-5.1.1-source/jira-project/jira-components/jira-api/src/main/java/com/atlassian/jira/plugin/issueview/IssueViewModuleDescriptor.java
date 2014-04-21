package com.atlassian.jira.plugin.issueview;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

/**
 * An issue view allows you to view an issue in different ways  (eg XML, Word, PDF)
 *
 * @see IssueView
 */
public interface IssueViewModuleDescriptor extends JiraResourcedModuleDescriptor<IssueView>
{
    public IssueView getIssueView();

    public String getFileExtension();

    public String getContentType();

    public String getURLWithoutContextPath(String issueKey);
}
