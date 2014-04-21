package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.model.SimpleSearchSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter;
import com.atlassian.jira.webtest.framework.page.issuenavigator.WorkRatioSection;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issuenavigator.WorkRatioSection}.
 *
 * @since v4.3
 */
public class SeleniumWorkRatioSection extends AbstractSeleniumSimpleFilterSection<WorkRatioSection> implements WorkRatioSection
{

    protected SeleniumWorkRatioSection(SimpleSearchFilter parent, SeleniumContext context)
    {
        super(parent, context, SimpleSearchSection.WORK_RATIO, WorkRatioSection.class);
    }
}
