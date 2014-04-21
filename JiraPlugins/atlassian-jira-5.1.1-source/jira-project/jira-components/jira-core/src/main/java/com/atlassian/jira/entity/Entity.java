package com.atlassian.jira.entity;

import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.project.ProjectCategory;

/**
 * Holds Entity Factory classes.
 *
 * @since v4.4
 */
public interface Entity
{
    public static final EntityFactory<ProjectCategory> PROJECT_CATEGORY = new ProjectCategoryFactory();
    public static final EntityFactory<IssueSecurityLevel> ISSUE_SECURITY_LEVEL = new IssueSecurityLevelFactory();
    public static final EntityFactory<IssueLink> ISSUE_LINK = new IssueLinkFactory();
    public static final EntityFactory<RemoteIssueLink> REMOTE_ISSUE_LINK = new RemoteIssueLinkFactory();


    /**
     * Entity Names as defined in entitymodel.xml.
     */
    public class Name
    {
        public static final String SCHEME_ISSUE_SECURITIES = "SchemeIssueSecurities";
    }
}
