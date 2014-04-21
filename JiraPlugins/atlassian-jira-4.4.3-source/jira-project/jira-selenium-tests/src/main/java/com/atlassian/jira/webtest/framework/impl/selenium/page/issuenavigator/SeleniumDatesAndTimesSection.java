package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.model.SimpleSearchSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.DatesAndTimesSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issuenavigator.DatesAndTimesSection}.
 *
 * @since v4.3
 */
public class SeleniumDatesAndTimesSection extends AbstractSeleniumSimpleFilterSection<DatesAndTimesSection> implements DatesAndTimesSection
{

    protected SeleniumDatesAndTimesSection(SimpleSearchFilter parent, SeleniumContext context)
    {
        super(parent, context, SimpleSearchSection.DATES_AND_TIMES, DatesAndTimesSection.class);
    }
}
