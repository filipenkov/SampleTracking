package com.atlassian.jira.webtest.framework.impl.selenium.page.admin;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.page.admin.CustomFields;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumCustomFields  extends AbstractSeleniumPage implements CustomFields
{
    private final Locator linkLocator;
    private final Locator detector;
    private final Locator addCustomFieldsLink;
    private final Locator cascadingSelectRadioButton;

    public SeleniumCustomFields(SeleniumContext ctx)
    {
        super(ctx);
        detector = css("table#custom-fields");
        linkLocator = id("view_custom_fields");
        addCustomFieldsLink = id("add_custom_fields");
        cascadingSelectRadioButton = id("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect_id");
    }

    @Override
    public TimedCondition canAddCustomFields()
    {
        return addCustomFieldsLink.element().isPresent();
    }

    @Override
    public CustomFields openAddCustomFields()
    {
        addCustomFieldsLink.element().click();
        return this;
    }

    @Override
    public TimedCondition cascadingSelectAvailable()
    {
        return cascadingSelectRadioButton.element().isPresent();
    }

    @Override
    public Locator adminLinkLocator()
    {
        return linkLocator;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return (SeleniumLocator) detector;
    }
}
