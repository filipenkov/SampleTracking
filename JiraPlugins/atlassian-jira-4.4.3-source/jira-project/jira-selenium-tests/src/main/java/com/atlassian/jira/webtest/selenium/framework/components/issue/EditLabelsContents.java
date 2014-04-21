package com.atlassian.jira.webtest.selenium.framework.components.issue;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.components.LabelsPicker;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents contents of the 'Edit labels' dialog/page.
 *
 * @since v4.2
 */
public final class EditLabelsContents extends AbstractSeleniumPageObject
{
    private final LabelsPicker labelsPicker;
    private final String contextLocator;

    public EditLabelsContents(SeleniumContext context, String contextLocator)
    {
        super(context);
        this.contextLocator = notNull("contextLocator", contextLocator);
        this.labelsPicker = LabelsPicker.newSystemLabelsPicker(contextLocator, context);
    }

    public EditLabelsContents(SeleniumContext context, String contextLocator, int customFieldId)
    {
        super(context);
        this.contextLocator = notNull("contextLocator", contextLocator);
        this.labelsPicker = LabelsPicker.newCustomFieldLabelsPicker(contextLocator, customFieldId, context);
    }

    public LabelsPicker labelsPicker()
    {
        return labelsPicker;
    }

}
