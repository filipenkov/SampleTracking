package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.model.SimpleSearchSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.CustomFieldsSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issuenavigator.CustomFieldsSection}.
 *
 * @since v4.3
 */
public class SeleniumCustomFieldsSection extends AbstractSeleniumSimpleFilterSection<CustomFieldsSection> implements CustomFieldsSection
{

    protected SeleniumCustomFieldsSection(SimpleSearchFilter parent, SeleniumContext context)
    {
        super(parent, context, SimpleSearchSection.CUSTOM_FIELDS, CustomFieldsSection.class);
    }
}
