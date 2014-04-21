package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.webtest.ui.keys.SpecialKeys;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;
import static com.atlassian.webtest.ui.keys.Sequences.keys;

/**
 * Selenium implementation of the {@link com.atlassian.jira.webtest.framework.component.AjsDropdown.Item} interface.
 *
 * @since v4.3
 */
public class SeleniumDDItem<P extends PageObject> extends AbstractSeleniumComponent<AjsDropdown.Section<P>>
        implements AjsDropdown.Item<P>
{
    static final String LIST_ITEM_LOCATOR = "a.aui-list-item-link";
    static final String ITEM_WITH_TEXT_LOCATOR_TEMPLATE = LIST_ITEM_LOCATOR + ":contains('%s')";
    static final String ACTIVE_LIST_ITEM_LOCATOR = "a.aui-list-item-link.active";
    private static final String SELECTED_LOCATOR_TEMPLATE = ACTIVE_LIST_ITEM_LOCATOR + ":contains('%s')";

    private final SeleniumLocator locator;
    private final SeleniumLocator selectedLocator;
    private final String name;

    public SeleniumDDItem(SeleniumContext ctx, SeleniumDDSection<P> parentSection, String name)
    {
        super(parentSection, ctx);
        this.name = notNull("name", name);
        this.locator = parentSection.locator().combine(jQuery(String.format(ITEM_WITH_TEXT_LOCATOR_TEMPLATE, name)));
        this.selectedLocator = parentSection.locator().combine(jQuery(String.format(SELECTED_LOCATOR_TEMPLATE, name)))
                .withDefaultTimeout(Timeouts.UI_ACTION);
    }

    @Override
    protected SeleniumLocator detector()
    {
        return locator;
    }


    @Override
    public SeleniumLocator locator()
    {
        return detector();
    }

    @Override
    public AjsDropdown<P> dropDown()
    {
        return parent().parent();
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public TimedCondition isSelected()
    {
        return selectedLocator.element().isPresent();
    }

    @Override
    public TimedCondition isNotSelected()
    {
        return not(isSelected());
    }

    @Override
    public AjsDropdown.Item<P> select()
    {
        if (isSelected().now())
        {
            return this;
        }
        int positionCount = dropDown().itemCount().byDefaultTimeout();
        for (int i=0; i<=positionCount; i++)
        {
            dropDown().locator().element().type(keys(SpecialKeys.ARROW_DOWN));
            if (isSelected().byDefaultTimeout())
            {
                return this;
            }
        }
        throw new IllegalStateException("Unable to select");
    }

    @Override
    public AjsDropdown.Item<P> down()
    {
        assertIsSelected();
        dropDown().locator().element().type(keys(SpecialKeys.ARROW_DOWN));
        // TODO is it in breach with the 'no-post-condition-validation' rule? otherwise we're not sure if the press has
        // TODO changed anything... so we would return this instance, which is also bad...
        assertNotSelected();
        return dropDown().selectedItem().byDefaultTimeout();
    }

    @Override
    public AjsDropdown.Item<P> up()
    {
        assertIsSelected();
        dropDown().locator().element().type(keys(SpecialKeys.ARROW_UP));
        // TODO is it in breach with the 'no-post-condition-validation' rule? otherwise we're not sure if the press has
        // TODO changed anything... so we would return this instance, which is also bad...
        assertNotSelected();
        return dropDown().selectedItem().byDefaultTimeout();
    }


    private void assertIsSelected()
    {
        if (!isSelected().byDefaultTimeout())
        {
            throw new IllegalStateException("Not selected: " + this);
        }
    }

    private void assertNotSelected()
    {
        // no, it's not the same as isSelected(); it will return as soon as this element loses selection
        if (!isNotSelected().byDefaultTimeout())
        {
            throw new IllegalStateException("Still selected: " + this);
        }
    }


    @Override
    public String toString()
    {
        return asString("SeleniumDDPosition[locator=", locator.toString(),"]");
    }
}
