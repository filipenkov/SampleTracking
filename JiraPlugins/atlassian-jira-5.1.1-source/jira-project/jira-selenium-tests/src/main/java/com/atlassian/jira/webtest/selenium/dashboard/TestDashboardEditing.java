package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.jira.webtest.selenium.harness.util.Dashboard;

/**
 * @since v4.00
 */
@WebTest({Category.SELENIUM_TEST })
public class TestDashboardEditing extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final int GADGET_DIRECTORY_TIMEOUT = 60000;

    private static final String GADGET_COLOUR = "//div[@id='gadget-10018-renderbox' and @class='gadget <colour>']";
    private static final String LAYOUT_A = "//div[@class='layout layout-a']";
    private static final String GADGET_XPATH = "//div[@id='gadget-<gadgetNo>-chrome']/div[@class='dashboard-item-header']/div[@class='gadget-menu']/ul/";
    private static final String GADGET_10019 = "gadget-10019-renderbox";
    private static final String GADGET_10050 = "gadget-10050-renderbox";
    //Firefox 2 and Firefox 3 renders the class slightly differently so we use contains here. FF3 has a trailing space
    private static final String GADGET_DROPDOWN = "li[@class='aui-dd-parent dd-allocated']/a[contains(@class,'aui-dd-trigger standard')]";
    private static final String GADGET_DELETE_LINK = "li[@class='aui-dd-parent dd-allocated active']/ul[@class='aui-dropdown standard aui-dropdown-right aui-box-shadow']//li/a[@class='item-link delete']";
    private static final String FINISH_ADD_GADGET = "css=button.finish";
    private static final String GADGET_COLOUR_OPTION = "li[@class='aui-dd-parent dd-allocated active']/ul[@class='aui-dropdown standard aui-dropdown-right aui-box-shadow']/li[@class='item-link gadget-colors";
    private static final String GADGET_COLOUR_SELECTION = GADGET_COLOUR_OPTION + " active']/ul/li[@class='<colour>']/a";
    private static final String GADGET_DROPDOWN_ACTIVE = "//div[@id='gadget-<gadgetNo>-renderbox' and @class='gadget color1 dropdown-active']";
    private static final String GADGET_LAYOUT = "//div[@class='layout layout-a']/ul[@class='column first sortable ui-sortable']/li[<order>]";


    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDashboardEditing.xml");
    }

    public void testDashboardEditing()
    {
        _testAddGadget();
        _testDeleteGadget();
        _testEditGadgetColour();
        _testEditGadgetLayout();
        _testEditGadgetPosition();
        _testEditGadgetDashboard();

    }

    private void _testAddGadget()
    {
        //Make sure gadget isn't there already
        assertThat.elementNotPresentByTimeout(GADGET_10050, TIMEOUT);
        //Adding the gadget
        client.click("add-gadget");
        assertThat.elementPresentByTimeout("category-all", GADGET_DIRECTORY_TIMEOUT);
        client.click("//li[@id='macro-PieChart']/div[@class='add-button']/input[@class='macro-button-add']");
        client.click(FINISH_ADD_GADGET);
        //Checking for added gadget and checking again after refresh
        assertThat.visibleByTimeout(GADGET_10050, TIMEOUT);
        getNavigator().gotoHome();
        assertThat.visibleByTimeout(GADGET_10050, TIMEOUT);

    }

    private void _testDeleteGadget()
    {
        //Make sure gadget is there
        visibleByTimeoutWithDelay(GADGET_10019, TIMEOUT);
        //Clicking the gadget dropdown
        Mouse.mouseover(client, "gadget-10019-title");
        visibleByTimeoutWithDelay(GADGET_XPATH.replace("<gadgetNo>", "10019") + GADGET_DROPDOWN, TIMEOUT);
        client.click(GADGET_XPATH.replace("<gadgetNo>", "10019") + GADGET_DROPDOWN);
        visibleByTimeoutWithDelay(GADGET_DROPDOWN_ACTIVE.replace("<gadgetNo>", "10019"), TIMEOUT);
        //Clicking the delete link
        client.click(GADGET_XPATH.replace("<gadgetNo>", "10019") + GADGET_DELETE_LINK);
        client.getConfirmation();
        //Checking for deleted gadget and checking again after refresh
        getNavigator().gotoHome();
        assertThat.elementNotPresentByTimeout(GADGET_10019, TIMEOUT);
    }


    private void _testEditGadgetColour()
    {
        //Make sure gadget is color1
        assertThat.visibleByTimeout(GADGET_COLOUR.replace("<colour>", "color1"), TIMEOUT);
        //Clicking the gadget dropdown
        Mouse.mouseover(client, "gadget-10018-title");
        assertThat.elementPresentByTimeout(GADGET_XPATH.replace("<gadgetNo>", "10018") + GADGET_DROPDOWN, TIMEOUT);
        client.click(GADGET_XPATH.replace("<gadgetNo>", "10018") + GADGET_DROPDOWN);
        assertThat.visibleByTimeout(GADGET_DROPDOWN_ACTIVE.replace("<gadgetNo>", "10018"), TIMEOUT);
        //Choosing the colour
        Mouse.mouseover(client, GADGET_XPATH.replace("<gadgetNo>", "10018") + GADGET_COLOUR_OPTION + "']");
        assertThat.elementPresentByTimeout(GADGET_XPATH.replace("<gadgetNo>", "10018") + GADGET_COLOUR_OPTION + " active']", TIMEOUT);
        client.click(GADGET_XPATH.replace("<gadgetNo>", "10018") + GADGET_COLOUR_SELECTION.replace("<colour>", "color3"));
        //Checking for colour change and checking again after refresh
        assertThat.visibleByTimeout(GADGET_COLOUR.replace("<colour>", "color3"), TIMEOUT);
        waitFor(1000);
        getNavigator().gotoHome();
        assertThat.visibleByTimeout(GADGET_COLOUR.replace("<colour>", "color3"), TIMEOUT);
    }

    private void _testEditGadgetLayout()
    {
        //Make sure this layout is not selected
        assertThat.elementNotPresentByTimeout(LAYOUT_A);
        //Change the layout
        client.click("layout-changer");
        assertThat.textPresentByTimeout("Edit Layout ", TIMEOUT);
        client.click("layout-a");
        //Checking for changed layout and checking again after refresh
        assertThat.elementPresentByTimeout(LAYOUT_A);
        waitFor(1000);
        getNavigator().gotoHome();
        assertThat.elementPresentByTimeout(LAYOUT_A);

    }

    private void _testEditGadgetPosition()
    {
        //Confirm gadget current position
        assertThat.visibleByTimeout(GADGET_LAYOUT.replace("<order>", "3"), TIMEOUT);
        //Drag gadget to new position
        client.dragAndDropToObject("gadget-10018-title", "gadget-10017-renderbox");
        //Checking that gadget position is changed and checking again after refresh
        assertThat.visibleByTimeout(GADGET_LAYOUT.replace("<order>", "2"), TIMEOUT);
        waitFor(1000);
        getNavigator().gotoHome();
        assertThat.visibleByTimeout(GADGET_LAYOUT.replace("<order>", "2"), TIMEOUT);
    }

    private void _testEditGadgetDashboard()
    {
        //Make sure the gadget is visible
        assertThat.visibleByTimeout(GADGET_10050, TIMEOUT);
        //Dragging the gadget to a different tab
        final Dashboard dashboard = getNavigator().dashboard("10011");
        dashboard.dragGadgetToTab("10050", "My Dashboard", 2);
        //Checking that the gadget is no longer on this dashboard and checking again after refresh
        assertThat.elementNotPresentByTimeout(GADGET_10050, TIMEOUT);
        waitFor(5000);
        getNavigator().gotoHome();
        assertThat.elementNotPresentByTimeout(GADGET_10050, TIMEOUT);
        //Checking that the gadget is moved to the other dashboad and checking again after refresh
        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10012", true);
        assertThat.visibleByTimeout(GADGET_10050, TIMEOUT);
        waitFor(1000);
        getNavigator().gotoHome();
        assertThat.visibleByTimeout(GADGET_10050, TIMEOUT);
    }
}
