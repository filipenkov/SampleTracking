package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.driver.admin.plugins.PluginsManagement;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManageExistingPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;

/**
 * @since v4.00
 */
@WebTest({Category.SELENIUM_TEST })
public class TestTextGadget extends GadgetTest
{
    private static final long TIMEOUT = 30000;

    private static final String GADGET_DIRECTORY = "add-gadget";
    private static final String FINISH_ADDING_GADGET = "css=button.finish";
    private static final String ENABLE_TEXT_GADGET = "enable-com.atlassian.jira.gadgets:text-gadget";
    private static final String DISABLE_TEXT_GADGET = "disable-com.atlassian.jira.gadgets:text-gadget";
    private static final String TEXT_GADGET = "//li[@id='macro-Text']//h3/a";
    private static final String TEXT_TITLE_FIELD = "title";
    private static final String TEXT_GADGET_SAVE_BUTTON = "//input[@class='button save']";
    private static final String HTML_TEXT_FIELD = "html";
    private static final String TEXT_GADGET_TITLE = "//h3[@id='gadget-10060-title']";
    private static final String TEXT_GADGET_FRAME = "//iframe[@id='gadget-10060']";
    private static final String TEXT_GADGET_MESSAGE = "css=div.view";
    private static final String CHANGED_MESSAGE = "MessageChanged";
    private static final String CHANGED_TITLE = "TitleChanged";
    private static final String CURRENT_TITLE = "//input[@id='title' and @value='" + CHANGED_TITLE + "']";
    private static final String TEXT_GADGET_RENDERBOX = "gadget-10060-renderbox";
    private static final String TEXT_GADGET_DESCRIPTION = "Display any text, formatted as HTML";
    private static final String DEFAULT_TITLE = "Text";
    private static final String HTML_TEXTAREA = "//textarea[@id='html']";

    private PluginsManagement pluginsManagement;
    private Plugins plugins;
    
    public void onSetUp()
    {
        super.onSetUp();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        pluginsManagement = new PluginsManagement(globalPages());
        pluginsManagement.goToPlugins();
        plugins = pluginsManagement.plugins();
        getNavigator().gotoHome();
    }

    @Override
    protected void restoreGadgetData()
    {
        restoreData("TestTextGadget.xml");
    }

    public void testTextGadget()
    {
        _testDefaultDisabled();
        _testEnable();
        _testTitleChanged();
        _testMessageChanged();
        _testHTMLtags();

    }

    private void _testDefaultDisabled()
    {
        //Check that the gadget is not in the Gadget Directory initially
        client.click(GADGET_DIRECTORY);
        // This can occasionally take > 30s, so the normal TIMEOUT will not suffice
        assertThat.elementPresentByTimeout("category-all", GADGET_DIRECTORY_TIMEOUT);
        assertThat.textNotPresentByTimeout(TEXT_GADGET_DESCRIPTION, TIMEOUT);
        client.click(FINISH_ADDING_GADGET);
        waitFor(5000);

        //Check that the gadget is disabled by default
        pluginsManagement.goToPlugins().goToTab(ManageExistingPlugins.class);
        pluginsManagement.checkSystemPluginComponentExistsButDisabled("com.atlassian.jira.gadgets");
    }

    private void _testEnable()
    {
        //Enable the gadget
        getAdministration().enablePluginModule("2507175054", "text-gadget");
        getNavigator().gotoHome();

        //Check the gadget is now visible in the gadget directory
        client.click(GADGET_DIRECTORY);
        assertThat.elementPresentByTimeout("category-all", GADGET_DIRECTORY_TIMEOUT);
        assertThat.textPresentByTimeout(TEXT_GADGET_DESCRIPTION, TIMEOUT);

        //Add the gadget to the dashboard and check it is added
        client.click(TEXT_GADGET);
        client.click(FINISH_ADDING_GADGET);
        waitFor(5000);
        assertThat.visibleByTimeout(TEXT_GADGET_RENDERBOX, TIMEOUT);
    }

    private void _testTitleChanged()
    {
        //Check the default title is correct
        assertThat.elementContainsText(TEXT_GADGET_TITLE, DEFAULT_TITLE);

        //Change the title of the gadget and save
        client.typeInElementWithName(TEXT_TITLE_FIELD, CHANGED_TITLE);
        client.click(TEXT_GADGET_SAVE_BUTTON);
        waitFor(1000);
        assertThat.visibleByTimeout(TEXT_GADGET_FRAME, TIMEOUT);

        //Check that the gadget title is changed
        assertThat.elementContainsText(TEXT_GADGET_TITLE, CHANGED_TITLE);

        //Check that the current title is preloaded in the title field
        client.selectFrame("gadget-10060");
        clickConfigButton();
        waitForGadgetConfiguration();
        assertThat.visibleByTimeout(CURRENT_TITLE, TIMEOUT);
    }

    private void _testMessageChanged()
    {
        //Check that the default message is empty
        assertThat.elementContainsText(HTML_TEXTAREA, "");

        //Change the message of the gadget and save
        client.typeInElementWithName(HTML_TEXT_FIELD, CHANGED_MESSAGE);
        client.click(TEXT_GADGET_SAVE_BUTTON);
        waitFor(1000);
        client.selectWindow(null);
        assertThat.visibleByTimeout(TEXT_GADGET_FRAME, TIMEOUT);

        //Check that the gadget message is changed and title remaions the same
        assertThat.elementContainsText(TEXT_GADGET_TITLE, CHANGED_TITLE);
        assertThat.elementContainsText(TEXT_GADGET_MESSAGE, CHANGED_MESSAGE);

        //Check that the current title & message is preloaded in the correct fields
        client.selectFrame("gadget-10060");
        clickConfigButton();
        waitForGadgetConfiguration();
        assertThat.visibleByTimeout(CURRENT_TITLE, TIMEOUT);
        assertThat.elementContainsText(HTML_TEXTAREA, CHANGED_MESSAGE);

    }

    private void _testHTMLtags()
    {
        //Make sure that html tags are not escaped
        client.typeInElementWithName(HTML_TEXT_FIELD, "<div class=\"htmlText\"><strong>test</strong></div>");
        client.click(TEXT_GADGET_SAVE_BUTTON);
        waitFor(1000);
        client.selectWindow(null);
        assertThat.visibleByTimeout(TEXT_GADGET_FRAME, TIMEOUT);
        assertThat.elementContainsText("css=div.view div.htmlText", "test");
    }

}