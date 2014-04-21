package com.atlassian.jira.webtest.framework.impl.selenium.page.admin;

import com.atlassian.jira.webtest.framework.core.component.MultiSelect;
import com.atlassian.jira.webtest.framework.core.component.Option;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumMultiSelect;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPageSection;
import com.atlassian.jira.webtest.framework.page.admin.AddFieldSection;
import com.atlassian.jira.webtest.framework.page.admin.ConfigureScreen;

import java.util.List;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

/**
 * AddFieldSection in the 'Configure Screens' administration page. It is used to
 * add set of fields to a particular tab of the currently configured screen.
 *
 * @since v4.2
 */
public class SeleniumAddFieldSection extends AbstractSeleniumPageSection<ConfigureScreen> implements AddFieldSection
{
    private final MultiSelect fieldSelect;

    public SeleniumAddFieldSection(ConfigureScreen parentPage, SeleniumContext ctx)
    {
        super(parentPage, ctx);
        this.fieldSelect = new SeleniumMultiSelect(addFieldSelectLocator(), context);
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected SeleniumLocator detector()
    {
        return addFieldSubmitLocator();
    }

    @Override
    public SeleniumLocator addFieldSelectLocator()
    {
        return css("select[name=fieldId]");
    }

    @Override
    public SeleniumLocator addFieldSubmitLocator()
    {
        return css("#add_field_submit");
    }

    /* ----------------------------------------------- QUERIES ------------------------------------------------------ */

    // TODO

    /* --------------------------------------------- COMPONENTS ----------------------------------------------------- */

    @Override
    public MultiSelect selectFields()
    {
        return fieldSelect;
    }

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    @Override
    public SeleniumAddFieldSection submitAdd()
    {
        List<Option> selected = fieldSelect.selected();
        addFieldSubmitLocator().element().click();
        waitFor().pageLoad();
        assertThat("Fields multi-select not found", fieldSelect.isReady(), byDefaultTimeout());
        for (Option option : selected)
        {
            assertThat(missingFieldOptionMsg(option), parentContainsLabelOf(option), byDefaultTimeout());
        }
        return this;
    }

    private String missingFieldOptionMsg(Option option)
    {
        return asString("Select field <",option.label(),"> not added to the table");
    }

    private TimedCondition parentContainsLabelOf(Option option)
    {
        return page().fieldTableLocator().element().containsText(option.label());
    }

}
