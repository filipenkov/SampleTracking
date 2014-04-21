package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.selenium.SeleniumAssertions;
import com.atlassian.selenium.SeleniumClient;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * Implementation of {@link com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation} for Selenium
 * tests.
 *
 * @since v4.2
 */
public class IssueNavigatorNavigationImpl implements IssueNavigatorNavigation
{
    private static final String ID_LINK_SWITCHNAVTYPE = "id=switchnavtype";
    private static final String ID_LINK_VIEWFILTER = "id=viewfilter";
    private static final String ID_LINK_NEW_FILTER = "id=new_filter";
    private static final String ID_FILTER_FORM_HEADER = "id=filterFormHeader";
    private static final String ID_LINK_EDITFILTER = "id=editfilter";

    private static final String JQL_FORM_LOCATOR = "css=form#jqlform";
    private static final String JQL_INPUT_LOCATOR = "css=#jqltext";
    private static final String SIMPLE_FORM_LOCATOR = "css=form#issue-filter";

    private static final String MENU_SEARCH_LOCATOR = "id=find_link";
    private static final String RUN_ADVANCED_LOCATOR = "id=jqlrunquery";
    private static final String RUN_SIMPLE_LOCATOR = "id=issue-filter-submit";
    private static final String SELECTED_TAB_LOCATOR = "css=#filterFormHeader .active";

    private final SeleniumClient client;
    private final SeleniumAssertions assertThat;

    public IssueNavigatorNavigationImpl(final SeleniumClient client, final SeleniumAssertions assertThat)
    {
        this.client = client;
        this.assertThat = assertThat;
    }

    public NavigatorMode getCurrentMode()
    {
        if (!isCurrentlyOnNavigator())
        {
            return null;
        }

        final String selectedTab = client.getText(SELECTED_TAB_LOCATOR);
        if (selectedTab != null)
        {
            for (NavigatorMode navigatorMode : NavigatorMode.values())
            {
                if (navigatorMode.name().equalsIgnoreCase(selectedTab))
                {
                    return navigatorMode;
                }
            }
        }

        if (client.isElementPresent(ID_LINK_SWITCHNAVTYPE))
        {
            return NavigatorMode.EDIT;
        }
        else
        {
            if (client.isElementPresent(JQL_FORM_LOCATOR))
            {
                return NavigatorMode.EDIT;
            }
            else if (client.isElementPresent(SIMPLE_FORM_LOCATOR))
            {
                return NavigatorMode.EDIT;
            }
        }
        return null;
    }

    public NavigatorEditMode getCurrentEditMode()
    {
        final NavigatorMode mode = getCurrentMode();
        if (mode == NavigatorMode.EDIT || mode == NavigatorMode.NEW)
        {
            if (client.isElementPresent("css=form#jqlform"))
            {
                return NavigatorEditMode.ADVANCED;
            }
            else if (client.isElementPresent(SIMPLE_FORM_LOCATOR))
            {
                return NavigatorEditMode.SIMPLE;
            }
        }
        return null;
    }

    public void gotoNavigator()
    {
        if (!isCurrentlyOnNavigator())
        {
            clickAndWaitForPageToLoad(MENU_SEARCH_LOCATOR);
            assertThat.elementPresentByTimeout(ID_FILTER_FORM_HEADER);
        }
    }

    public void displayAllIssues()
    {
        gotoNewMode(NavigatorEditMode.SIMPLE);
        runSimpleSearch();
    }

    public void sortIssues(final String field, final String direction)
    {
        throw new UnsupportedOperationException();
    }

    public void loadFilter(final long id)
    {
        throw new UnsupportedOperationException();
    }

    public void loadFilter(final long id, final NavigatorEditMode mode)
    {
        throw new UnsupportedOperationException();
    }

    public IssueNavigatorNavigation createSearch(final String jqlQuery)
    {
        gotoNewMode(NavigatorEditMode.ADVANCED);
        if (StringUtils.isNotBlank(jqlQuery))
        {
            client.type(JQL_INPUT_LOCATOR, jqlQuery);
        }
        runAdvancedSearch();
        return this;
    }

    public void createSearch(final NavigatorSearch search)
    {
        throw new UnsupportedOperationException();
    }

    public void modifySearch(final NavigatorSearch search)
    {
        throw new UnsupportedOperationException();
    }

    public long createNewAndSaveAsFilter(final SharedEntityInfo info, final NavigatorSearch search)
    {
        throw new UnsupportedOperationException();
    }

    public long saveCurrentAsNewFilter(final SharedEntityInfo info)
    {
        throw new UnsupportedOperationException();
    }

    public long saveCurrentAsNewFilter(final String name, final String description, final boolean favourite, final Set<? extends TestSharingPermission> permissions)
    {
        throw new UnsupportedOperationException();
    }

    public long saveCurrentFilter()
    {
        throw new UnsupportedOperationException();
    }

    public void deleteFilter(final long id)
    {
        throw new UnsupportedOperationException();
    }

    public void hideActionsColumn()
    {
        throw new UnsupportedOperationException();
    }

    public void showActionsColumn()
    {
        throw new UnsupportedOperationException();
    }

    public void addColumnToIssueNavigator(final String[] fieldNames)
    {
        throw new UnsupportedOperationException();
    }

    public void restoreColumnDefaults()
    {
        throw new UnsupportedOperationException();
    }

    public void runSearch()
    {
        final NavigatorMode currentMode = getCurrentMode();
        if (currentMode == NavigatorMode.EDIT || currentMode == NavigatorMode.NEW)
        {
            final NavigatorEditMode currentEditMode = getCurrentEditMode();
            if (currentEditMode == NavigatorEditMode.SIMPLE)
            {
                runSimpleSearch();
            }
            else if (currentEditMode == NavigatorEditMode.ADVANCED)
            {
                runAdvancedSearch();
            }
            else
            {
                assert false : "Have a state we did not consider.";
            }
        }
        else
        {
            Assert.fail("Must be in edit or new mode to run a search. Current mode is " + currentMode);
        }
    }

    public void expandAllNavigatorSections()
    {
        expandNavigatorSection("common-concepts-projectcomponents-group");
        expandNavigatorSection("navigator-filter-subheading-issueattributes-group");
        expandNavigatorSection("navigator-filter-subheading-datesandtimes-group");
        expandNavigatorSection("navigator-filter-subheading-workratio-group");
        expandNavigatorSection("navigator-filter-subheading-customfields-group");
    }

    public void expandNavigatorSection(final String sectionId)
    {
        if (client.isElementPresent("id=" + sectionId))
        {
            final String toggleClass = client.getAttribute("//fieldset[@id='" + sectionId + "']@class");
            if (toggleClass.contains("collapsed"))
            {
                client.click("//fieldset[@id='" + sectionId + "']/legend/span");
            }
        }
    }

    public BulkChangeWizard bulkChange(final BulkChangeOption bulkChangeOption)
    {
        clickAndWaitForPageToLoad(bulkChangeOption.getLinkId());
        return new BulkChangeWizardImpl(client);
    }

    public void gotoEditMode(final NavigatorEditMode mode)
    {
        gotoNavigator();

        //Need to be in edit mode.
        gotoNavigatorMode(NavigatorMode.EDIT, ID_LINK_EDITFILTER);

        //Switch modes if the edit mode does not match up.
        switchIntoEditMode(mode);
    }

    public NavigatorMode gotoEditOrNewMode(final NavigatorEditMode mode)
    {
        gotoNavigator();
        final NavigatorMode navigatorMode = getCurrentMode();
        if (navigatorMode == NavigatorMode.EDIT || navigatorMode == NavigatorMode.NEW)
        {
            switchIntoEditMode(mode);
            return navigatorMode;
        }
        else if (client.isElementPresent(ID_LINK_EDITFILTER))
        {
            gotoEditMode(mode);
            return NavigatorMode.EDIT;
        }
        else
        {
            gotoNewMode(mode);
            return NavigatorMode.NEW;
        }
    }

    public void clickEditModeFlipLink()
    {
        clickAndWaitForPageToLoad(ID_LINK_SWITCHNAVTYPE);
    }

    public void gotoViewMode()
    {
        gotoNavigator();
        gotoNavigatorMode(NavigatorMode.SUMMARY, ID_LINK_VIEWFILTER);
    }

    public void gotoNewMode(final NavigatorEditMode navigatorEditMode)
    {
        gotoNavigator();
        gotoNavigatorMode(IssueNavigatorNavigation.NavigatorMode.NEW, ID_LINK_NEW_FILTER);
        if (navigatorEditMode != null)
        {
            switchIntoEditMode(navigatorEditMode);
        }
    }

    @Override
    public void goToConfigureColumns()
    {
        throw new UnsupportedOperationException("Implement me");
    }

    private boolean isCurrentlyOnNavigator()
    {
        return client.isElementPresent(ID_FILTER_FORM_HEADER);
    }

    private void switchIntoEditMode(final NavigatorEditMode mode)
    {
        final NavigatorEditMode currentEditMode = getCurrentEditMode();
        if (currentEditMode != mode)
        {
            clickAndWaitForPageToLoad(ID_LINK_SWITCHNAVTYPE);
            final NavigatorEditMode newEditMode = getCurrentEditMode();
            if (newEditMode != mode)
            {
                Assert.fail("Unable to transition into " + mode + " mode from " + currentEditMode + " mode. Current edit mode " + newEditMode + ".");
            }
        }
    }

    private void gotoNavigatorMode(final NavigatorMode mode, final String linkLocator)
    {
        final NavigatorMode currentMode = getCurrentMode();
        if (currentMode != mode)
        {
            clickAndWaitForPageToLoad(linkLocator);
            final NavigatorMode newMode = getCurrentMode();
            if (newMode != mode)
            {
                Assert.fail("Unable to transition into " + mode + " mode from " + currentMode + " mode. Current mode " + newMode + ".");
            }
        }
    }

    private void clickAndWaitForPageToLoad(final String locator)
    {
        client.click(locator, true);
    }

    private void runSimpleSearch()
    {
        clickAndWaitForPageToLoad(RUN_SIMPLE_LOCATOR);
    }

    private void runAdvancedSearch()
    {
        clickAndWaitForPageToLoad(RUN_ADVANCED_LOCATOR);
    }
}
