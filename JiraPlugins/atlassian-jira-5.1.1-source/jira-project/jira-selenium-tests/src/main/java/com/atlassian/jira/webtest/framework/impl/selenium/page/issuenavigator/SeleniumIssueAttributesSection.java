package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.model.SimpleSearchSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueAttributesSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issuenavigator.IssueAttributesSection}.
 *
 * @since v4.3
 */
public class SeleniumIssueAttributesSection extends AbstractSeleniumSimpleFilterSection<IssueAttributesSection> implements IssueAttributesSection
{
    
    protected SeleniumIssueAttributesSection(SimpleSearchFilter parent, SeleniumContext context)
    {
        super(parent, context, SimpleSearchSection.ISSUE_ATRIBUTES, IssueAttributesSection.class);
    }
}
