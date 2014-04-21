package com.atlassian.jira.webtest.selenium.filters;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.gadgets.GadgetTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestFilterOrProjectPicker extends GadgetTest
{
    private static final long TIMEOUT = 30000;

    private static final Map<String, String> PROJECT_TO_KEY = new HashMap<String, String>();
    private static final String SUGGESTIONS_BOX = "//div[@class='suggestions aui-list dropdown-ready']";
    private static final String INPUT_BOX = "//input[@id='quickfind']";
    private static final String FILTERS_TITLE = "Filters";
    private static final String PROJECTS_TITLE = "Projects";
    private static final String NONE_SELECTED = "No Filter/Project selected";
    private static final String SAVE_BUTTON = "//input[@class='button save']";
    private static final String POPUP_TITLE = "Filter or Project Picker - Your Company JIRA";
    private static final String POPUP_HEADING = "Filter or Project Picker";
    private static final String POPUP_LINK = "filter_projectOrFilterId_advance";
    private static final String CONFIGURE_BUTTON = "//button[@class='configure']";
    private static final String ASCII_DOWN = "\\40";
    private static final String ACSII_ENTER = "\\13";
    private static final String GADGET_TITLE = "//h3[@id='gadget-10010-title']";

    public static Test suite()
    {
        return suiteFor(TestFilterOrProjectPicker.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestFilterOrProjectPicker.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        PROJECT_TO_KEY.put("abc", "ABC");
        PROJECT_TO_KEY.put("cba", "CBA");
        PROJECT_TO_KEY.put("cde", "CDE");
        PROJECT_TO_KEY.put("cds", "CDS");
        PROJECT_TO_KEY.put("b1", "BB");
        PROJECT_TO_KEY.put("b2", "BBB");
        PROJECT_TO_KEY.put("b3", "BBBB");

    }

    public void testFilterOrProjectPicker()
    {
        _testOnlyProjectSingle();
        _testOnlyFilterSingle();
        _testOnlyProjectMany();
        _testOnlyFilterMany();
        _testBoth();
        _testBothChanging();
        _testBoldedSearchString();
        _testPopupFilter();
        _testPopupProject();
        _testSpaceGetsAll();
    }

    // JRA-18847: pressing space should show everything: projects and filter
    private void _testSpaceGetsAll()
    {
        //Configure the gadget again, checking the current filter is still abc
        client.selectFrame("gadget-10010");
        clickConfigButton();
        waitForGadgetConfiguration();
        checkCurrentFilterOrProject("abc");
        client.keyPress(INPUT_BOX, " ");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);

        checkProjectAutoComplete("abc", "b1", "b2", "b3", "cba", "cde", "cds");
        checkFilterAutoComplete(true, "cba", "cbb", "cdf", "d1", "d2", "d3", "xyz");
        checkSuggestionActive("abc");

        // this should cancel the suggestions box
        client.keyPress(INPUT_BOX, VK_ESC);
    }

    private void _testOnlyProjectSingle()
    {
        //Wait for gadget to load and check that no filter/project is selected
        waitFor(3000);
        checkCurrentFilterOrProject(NONE_SELECTED);
        //Check that suggestion box does not appear at first
        assertThat.elementNotPresentByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        //Type into the input box and make sure that the suggestion box appears but without any filters
        client.keyPress(INPUT_BOX, "a");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        assertThat.elementDoesNotContainText(SUGGESTIONS_BOX, FILTERS_TITLE);
        //Check only project abc is displayed and that it is selected
        checkProjectAutoComplete("abc");
        checkSuggestionActive("abc");
    }

    private void _testOnlyFilterSingle()
    {
        //Wait for gadget to load
        getNavigator().gotoHome();
        waitFor(3000);
        //Type into the input box and make sure that the suggestion box appears but without any projects
        client.keyPress(INPUT_BOX, "x");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        assertThat.elementDoesNotContainText(SUGGESTIONS_BOX, PROJECTS_TITLE);
        //Check only filter xyz is displayed and that it is selected        
        checkFilterAutoComplete(false, "xyz");
        checkSuggestionActive("xyz");
    }

    private void _testOnlyProjectMany()
    {
        //Wait for gadget to load
        getNavigator().gotoHome();
        waitFor(3000);
        //Type into the input box and make sure that the suggestion box appears but without any filters
        client.keyPress(INPUT_BOX, "b");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        assertThat.elementDoesNotContainText(SUGGESTIONS_BOX, FILTERS_TITLE);
        //Check project list is displayed and that b1 is selected as default
        checkProjectAutoComplete("b1",
                "b2",
                "b3");
        checkSuggestionActive("b1");
    }

    private void _testOnlyFilterMany()
    {
        //Wait for gadget to load
        getNavigator().gotoHome();
        waitFor(3000);
        //Type into the input box and make sure that the suggestion box appears but without any projects        
        client.keyPress(INPUT_BOX, "d");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        assertThat.elementDoesNotContainText(SUGGESTIONS_BOX, PROJECTS_TITLE);
        //Check filter list is displayed and that d1 is selected as default                        
        checkFilterAutoComplete(false, "d1",
                "d2",
                "d3");
        checkSuggestionActive("d1");
    }

    private void _testBoth()
    {
        //Wait for gadget to load
        getNavigator().gotoHome();
        waitFor(3000);
        //Type into the input box and make sure that the suggestion box appears                
        client.keyPress(INPUT_BOX, "c");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        //Check that both filter list and project list are displayed and that cba is selected as default                        
        checkProjectAutoComplete("cba",
                "cde",
                "cds");
        checkFilterAutoComplete(true, "cba",
                "cbb",
                "cdf");
        checkSuggestionActive("cba");
    }

    private void _testBothChanging()
    {
        //Check that suggestion box is still open and type into the input box
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        client.keyPress(INPUT_BOX, "d");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        waitFor(3000);
        //Check that the suggest has been updated to reflect the additional input
        checkProjectAutoComplete("cde",
                "cds");
        checkFilterAutoComplete(true, "cdf");
        checkSuggestionActive("cde");
        //Arrow key down to the last choice and choose it by pressing enter
        client.keyPress(INPUT_BOX, ASCII_DOWN);
        client.keyPress(INPUT_BOX, ASCII_DOWN);
        client.keyPress(INPUT_BOX, ACSII_ENTER);
        waitFor(3000);
        //Check that the current filter is cdf and save it
        checkCurrentFilterOrProject("cdf");
        client.click(SAVE_BUTTON);
        waitFor(3000);
        //Check that the saved gadget has a changed title
        assertThat.elementContainsText(GADGET_TITLE, "Pie Chart: cdf");
    }

    private void _testBoldedSearchString()
    {
        //Configure the gadget again, checking the current filter is still cdf
        client.selectFrame("gadget-10010");
        clickConfigButton();
        waitForGadgetConfiguration();
        checkCurrentFilterOrProject("cdf");
        //Check that suggestion does not appear at first, type into the input box and check that the suggestion appears
        assertThat.elementNotPresentByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        client.keyPress(INPUT_BOX, "a");
        client.keyPress(INPUT_BOX, "b");
        assertThat.visibleByTimeout(SUGGESTIONS_BOX, TIMEOUT);
        assertThat.elementDoesNotContainText(SUGGESTIONS_BOX, FILTERS_TITLE);
        //Check that the search string is bolded
        assertThat.elementContainsText(SUGGESTIONS_BOX + "/ul[1]/li[1]/a/strong[1]", "ab");
        assertThat.elementContainsText(SUGGESTIONS_BOX + "/ul[1]/li[1]/a/strong[2]", "AB");
    }
    
    private void _testPopupFilter()
    {
        //Use the advanced popup
        client.click(POPUP_LINK);
        waitFor(5000);
        client.selectWindow(POPUP_TITLE);
        assertThat.textPresentByTimeout(POPUP_HEADING, TIMEOUT);
        //Choose cba filter using the popup
        client.click("filterlink_10000");
        waitFor(5000);
        client.selectWindow(null);
        //Check that cba is now  the current filter
        checkCurrentFilterOrProject("cba");
    }

    private void _testPopupProject()
    {
        //Choose using the advanced popup again, this time choosing project abc
        client.click(POPUP_LINK);
        waitFor(1000);
        client.selectWindow(POPUP_TITLE);
        waitFor(5000);
        assertThat.textPresentByTimeout(POPUP_HEADING);
        client.clickLinkWithText("Projects", true);
        client.click("filterlink_10000");
        waitFor(5000);
        client.selectWindow(null);
        //Check current project is abc
        checkCurrentFilterOrProject("abc");
        client.click(SAVE_BUTTON);
        waitFor(3000);
        //Check that the saved gadget has a changed title        
        assertThat.elementContainsText(GADGET_TITLE, "Pie Chart: abc");
    }

    private void checkFilterAutoComplete(boolean ProjectsPresent, String... list)
    {
        assertThat.elementContainsText(SUGGESTIONS_BOX, FILTERS_TITLE);
        //Changing the ul number if list does not contain projects
        int listNumber = 2;
        if(!ProjectsPresent)
        {
            listNumber = 1;
        }

        for(int i = 0; i < list.length; i++)
        {
            assertThat.elementContainsText(SUGGESTIONS_BOX + "/ul[" + listNumber + "]/li[" + (i+1) + "]/a", list[i]);
        }

    }

    private void checkProjectAutoComplete(String... list)
    {
        assertThat.elementContainsText(SUGGESTIONS_BOX, PROJECTS_TITLE);
        for(int i = 0; i < list.length; i++)
        {
            assertThat.elementContainsText(SUGGESTIONS_BOX + "/ul[1]/li[" + (i+1) + "]/a", list[i] + " (" + PROJECT_TO_KEY.get(list[i]) + ")");
        }
    }

    private void checkSuggestionActive(String filterOrProjectName)
    {
        assertThat.elementContainsText(SUGGESTIONS_BOX + "//ul/li[@class='aui-list-item active']/a", filterOrProjectName);
    }

    private void checkCurrentFilterOrProject(String current){
        waitFor(3000);
        assertThat.elementContainsText("//span[@id='filter_projectOrFilterId_name']", current);
    }
}
