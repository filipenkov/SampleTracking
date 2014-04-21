package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.core.PageObject;
import junit.framework.Assert;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.JQUERY;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.removeLocatorPrefix;

/**
 * Represents AJS.Dropdown component.
 *
 * @since v4.2
 */
public final class AjsDropdown extends AbstractSeleniumPageObject implements PageObject
{
    public class DropdownOpenMode
    {
        private DropdownOpenMode() {}

        public AjsDropdown byClick()
        {
            client.click(triggerId);
            return AjsDropdown.this;
        }

        public AjsDropdown byShortcut()
        {
            throw new UnsupportedOperationException("No shortcut for me");
        }
    }

    private final String triggerId;

    public AjsDropdown(SeleniumContext ctx, String triggerId)
    {
        super(ctx);
        this.triggerId = Assertions.notNull("triggerId", triggerId);
    }

    public String triggerLocator()
    {
        return triggerId;
    }

    public String inlineLayerLocator()
    {
        return JQUERY.create("div.ajs-layer.active#" + triggerId + "_drop");
    }

    public String inDropdown(String locator)
    {
        return inlineLayerLocator() + " " + removeLocatorPrefix(locator);   
    }


    public DropdownOpenMode open()
    {
        return new DropdownOpenMode();
    }

    public boolean isOpenBy(final long timeout)
    {
        return IsPresentCondition.forContext(context).locator(inlineLayerLocator()).defaultTimeout(timeout).build().byDefaultTimeout();
    }

    public boolean canOpenBy(final long timeout)
    {
        return !isOpenBy(timeout) && IsPresentCondition.forContext(context).locator(triggerLocator())
                .defaultTimeout(timeout).build().byDefaultTimeout();
    }



    public AjsDropdown assertIsOpen(final long timout)
    {
        Assert.assertTrue(asString(this," not open by ",timout, " ms"), isOpenBy(timout));
        return this;
    }

    public void assertReady(final long timeout)
    {
        assertIsOpen(context.timeouts().components());
    }
}
