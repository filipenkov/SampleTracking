package com.atlassian.jira.webtest.framework.impl.selenium.page.admin;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.model.admin.Screen;
import com.atlassian.jira.webtest.framework.page.admin.AddFieldSection;
import com.atlassian.jira.webtest.framework.page.admin.ConfigureScreen;
import com.atlassian.jira.webtest.framework.page.admin.ViewScreens;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the 'Configure Screen' page that is a child page of the 'View screens' administration page.
 *
 * @since v4.2
 */
public class SeleniumConfigureScreen extends AbstractSeleniumPage implements ConfigureScreen
{
    private static final String DETECTOR = "td.jiraformheader h3.formtitle:contains(Configure Screen)";

    private final ViewScreens parent;
    private final Screen screen;

    private final AddFieldSection addField;
    private final SeleniumLocator main;


    public SeleniumConfigureScreen(ViewScreens parent, Screen screen, SeleniumContext ctx)
    {
        super(ctx);
        this.parent = notNull("parent", parent);
        this.screen = notNull("screen", screen);
        this.addField = new SeleniumAddFieldSection(this, context);
        this.main = jQuery(DETECTOR);
    }

    @Override
    public Screen screen()
    {
        return screen;
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected SeleniumLocator detector()
    {
        return main;
    }

    public Locator fieldTableLocator()
    {
        return jQuery("#field_table");
    }

    private Locator viewScreensLink()
    {
        return id("view_screens");
    }

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    public AddFieldSection addFieldSection()
    {
        return addField;
    }

     /* ---------------------------------------------- ACTIONS ------------------------------------------------------ */

    @Override
    public ViewScreens back()
    {
        viewScreensLink().element().click();
        waitFor().pageLoad();
        return parent;
    }

}
