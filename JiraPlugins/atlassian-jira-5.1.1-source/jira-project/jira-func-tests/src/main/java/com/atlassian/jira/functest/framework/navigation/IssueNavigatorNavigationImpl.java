package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.AbstractNavigationUtil;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigator.NavigatorCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebResponse;
import junit.framework.Assert;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Navigate Issue Navigation functionality.
 *
 * @since v3.13
 */
public class IssueNavigatorNavigationImpl extends AbstractNavigationUtil implements IssueNavigatorNavigation
{
    private static final String ID_LINK_SWITCHNAVTYPE = "switchnavtype";
    private static final String ID_LINK_VIEWFILTER = "viewfilter";
    private static final String ID_LINK_NEW_FILTER = "new_filter";

    private static final String ID_FILTER_FORM_HEADER = "filterFormHeader";

    private static final String ID_JQL_FORM = "jqlform";
    private static final String ID_ISSUE_FILTER = "issue-filter";
    private static final String NAME_FILTER_FORM = "issue-filter";

    private static final Pattern CREATE_URL_PATTERN = Pattern.compile("requestId=(\\d+)");
    private static final Pattern SAVE_URL_PATTERN = CREATE_URL_PATTERN;
    private static final String ID_LINK_EDITFILTER = "editfilter";

    public IssueNavigatorNavigationImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData);
    }

    public NavigatorMode getCurrentMode()
    {
        if (!isCurrentlyOnNavigator())
        {
            return null;
        }

        //This will find the text of the currently selected tab.
        final String selectedTab = StringUtils.trimToNull(new XPathLocator(tester, "//ul[@id='" + ID_FILTER_FORM_HEADER + "']/li[@class='active']").getText());
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

        //Unable to find TAB that matches, very strange.
        final HttpUnitDialog dialog = tester.getDialog();
        try
        {
            if (dialog.isLinkPresent(ID_LINK_SWITCHNAVTYPE))
            {
                return NavigatorMode.EDIT;
            }
            else if (dialog.getResponse().getFormWithID(ID_JQL_FORM) != null)
            {
                return NavigatorMode.EDIT;
            }
            else if (dialog.getResponse().getFormWithName(NAME_FILTER_FORM) != null)
            {
                return NavigatorMode.EDIT;
            }
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    public NavigatorEditMode getCurrentEditMode()
    {
        final WebResponse webResponse = tester.getDialog().getResponse();
        final NavigatorMode mode = getCurrentMode();
        if (mode == NavigatorMode.EDIT || mode == NavigatorMode.NEW)
        {
            try
            {
                if (webResponse.getFormWithID(ID_JQL_FORM) != null)
                {
                    return NavigatorEditMode.ADVANCED;
                }
                else if (webResponse.getFormWithID(ID_ISSUE_FILTER) != null)
                {
                    return NavigatorEditMode.SIMPLE;
                }
            }
            catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Goes to the navigation section, or, if already in the section, does nothing.
     */
    public void gotoNavigator()
    {
        if (!isCurrentlyOnNavigator())
        {
            log("going to Navigator page");
            if (new IdLocator(tester, "find_link").hasNodes())
            {
                tester.clickLink("find_link");
            }
            else
            {
                tester.clickLink("leave_admin");
                tester.clickLink("find_link");
            }
            tester.assertTextPresent("Issue Navigator");
        }
    }

    /**
     * Executes quicksearch with no search string to return all issues
     */
    public void displayAllIssues()
    {
        if (new IdLocator(tester, "quicksearch").hasNodes())
        {
            tester.setWorkingForm("quicksearch");
            tester.setFormElement("searchString", "");
            tester.submit();
            tester.assertTextPresent("Issue Navigator");
        }
        else
        {
            tester.clickLink("leave_admin");
            displayAllIssues();
        }
    }

    public void sortIssues(String field, String direction)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?sorter/field=" + field + "&sorter/order=" + direction);
    }

    public void addColumnToIssueNavigator(String[] fieldNames)
    {
        tester.clickLink("view_profile");
        tester.clickLink("view_nav_columns");

        for (String fieldName : fieldNames)
        {
            try
            {
                tester.selectOption("fieldId", fieldName);
                tester.submit("add");
            }
            catch (Throwable t)
            {
                log("Field already added to Issue Navigator");
            }
        }
    }

    public void restoreColumnDefaults()
    {
        tester.clickLink("view_profile");
        tester.clickLink("view_nav_columns");
        tester.clickLinkWithText("Restore Defaults");
    }

    public void runSearch()
    {
        tester.submit("show");
    }

    public void expandAllNavigatorSections()
    {
        //do nothing as we don't care in the func tests.
    }

    public void expandNavigatorSection(final String sectionId)
    {
        //do nothing as we don't care in the func tests.
    }

    public BulkChangeWizard bulkChange(final BulkChangeOption bulkChangeOption)
    {
        tester.clickLink(bulkChangeOption.getLinkId());
        return new BulkChangeWizardImpl(tester, getEnvironmentData());
    }

    public void loadFilter(final long id)
    {
        loadFilter(id, null);
    }

    public void loadFilter(final long id, final NavigatorEditMode mode)
    {
        final StringBuilder builder = new StringBuilder("secure/IssueNavigator.jspa?mode=");
        if (mode != null)
        {
            builder.append("show&navType=");
            if (mode == NavigatorEditMode.SIMPLE)
            {
                builder.append("simple");
            }
            else
            {
                builder.append("advanced");
            }
        }
        else
        {
            builder.append("hide");
        }
        builder.append("&requestId=").append(id);

        tester.gotoPage(builder.toString());
        tester.assertTextNotPresent("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.");
    }

    public void gotoEditMode(final NavigatorEditMode editMode)
    {
        gotoNavigator();

        //Need to be in edit mode.
        gotoNavigatorMode(NavigatorMode.EDIT, ID_LINK_EDITFILTER);

        //Switch modes if the edit mode does not match up.
        switchIntoEditMode(editMode);
    }

    public void clickEditModeFlipLink()
    {
        tester.clickLink(ID_LINK_SWITCHNAVTYPE);
    }

    public void gotoViewMode()
    {
        gotoNavigator();
        gotoNavigatorMode(NavigatorMode.SUMMARY, ID_LINK_VIEWFILTER);
    }

    public IssueNavigatorNavigation createSearch(final String jqlQuery)
    {
        gotoNewMode(NavigatorEditMode.ADVANCED);

        tester.setWorkingForm(ID_JQL_FORM);
        tester.setFormElement("jqlQuery", jqlQuery);
        tester.submit();
        return this;
    }

    public void createSearch(final NavigatorSearch search)
    {
        log("Creating search: " + search);

        gotoNewMode(NavigatorEditMode.SIMPLE);

        editSearch(search);
    }

    public void modifySearch(final NavigatorSearch search)
    {
        log("Modifying search to: " + search);

        gotoEditMode(NavigatorEditMode.SIMPLE);

        editSearch(search);
    }

    public long createNewAndSaveAsFilter(final SharedEntityInfo info, final NavigatorSearch search)
    {
        createSearch(search);
        return saveCurrentAsNewFilter(info);
    }

    public long saveCurrentAsNewFilter(final SharedEntityInfo info)
    {
        return saveCurrentAsNewFilter(info.getName(), info.getDescription(), info.isFavourite(), info.getSharingPermissions());
    }

    public long saveCurrentAsNewFilter(final String name, final String description, final boolean favourite,
            final Set<? extends TestSharingPermission> permissions)
    {
        final HttpUnitDialog dialog = tester.getDialog();

        if (dialog.isLinkPresent("filtersavenew"))
        {
            tester.clickLink("filtersavenew");
        }
        else if (dialog.isLinkPresent("filtersaveas"))
        {
            tester.clickLink("filtersaveas");
        }
        else
        {
            Assert.fail("Unable to find 'filtersavenew' or 'filtersaveas' link on page to save as new filter.");
        }

        if (favourite && (permissions == null || permissions.isEmpty()))
        {
            tester.setFormElement("filterName", name);
            if (!StringUtils.isBlank(description))
            {
                tester.setFormElement("filterDescription", description);
            }
            tester.submit("saveasfilter_submit");
        }
        else
        {
            //This is a hack to get around the fact that JWebUnit does not support
            //setting hidden fields. Ahhhh.....

            saveUsingPut(name, description, favourite, permissions);
        }

        URL url = dialog.getResponse().getURL();
        if (StringUtils.isBlank(url.getQuery()))
        {
            Assert.fail("Unable to save filter.");
        }
        else
        {
            Matcher matcher = CREATE_URL_PATTERN.matcher(url.getQuery());
            if (matcher.find())
            {
                final long id = Long.parseLong(matcher.group(1));
                log("Saved new filter (" + id + ")");
                return id;
            }
            else
            {
                Assert.fail("Unable to save filter.");
            }
        }
        return Long.MIN_VALUE;
    }

    public long saveCurrentFilter()
    {
        final HttpUnitDialog dialog = tester.getDialog();

        if (dialog.isLinkPresent("filtersave"))
        {
            tester.clickLink("filtersave");
        }
        else
        {
            Assert.fail("Unable to find 'filtersave' link on the page to save current filter.");
        }

        tester.submit("Save");

        URL url = dialog.getResponse().getURL();
        if (StringUtils.isBlank(url.getQuery()))
        {
            Assert.fail("Unable to save filter.");
        }
        else
        {
            Matcher matcher = SAVE_URL_PATTERN.matcher(url.getQuery());
            if (matcher.find())
            {
                final long id = Long.parseLong(matcher.group(1));
                log("Saved filter (" + id + ")");
                return id;
            }
            else
            {
                Assert.fail("Unable to save filter.");
            }
        }
        return Long.MIN_VALUE;
    }

    public void deleteFilter(final long id)
    {
        tester.gotoPage("secure/DeleteFilter.jspa?filterId=" + id);
    }

    public void hideActionsColumn()
    {
        tester.gotoPage("secure/ViewUserIssueColumns!hideActionsColumn.jspa");
    }

    public void showActionsColumn()
    {
        tester.gotoPage("secure/ViewUserIssueColumns!showActionsColumn.jspa");
    }

    /**
     * Save the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden fields.
     *
     * @param name the name of the search.
     * @param description the description of the search.
     * @param favourite should the filter be saved as a favourite.
     * @param permissions the permissions to save.
     */
    private void saveUsingPut(final String name, final String description, final boolean favourite,
            final Set<? extends TestSharingPermission> permissions)
    {
        tester.gotoPage(createSaveUrl(name, description, favourite, permissions));
    }

    /**
     * Create the URL that can be used to perform a filter save.
     *
     * @param name the name of the search.
     * @param description the description of the search.
     * @param favourite should the filter be saved as a favourite.
     * @param permissions the permissions to save.
     * @return the URL for the filter save.
     */
    private String createSaveUrl(String name, String description, boolean favourite,
            final Set<? extends TestSharingPermission> permissions)
    {
        StringBuilder buffer = new StringBuilder("secure/SaveAsFilter.jspa?submit=Save");
        if (!StringUtils.isBlank(name))
        {
            buffer.append("&filterName=").append(encode(name));
        }
        if (!StringUtils.isBlank(description))
        {
            buffer.append("&filterDescription=").append(encode(description));
        }
        if (permissions != null)
        {
            buffer.append("&shareValues=").append(encode(TestSharingPermissionUtils.createJsonString(permissions)));
        }
        buffer.append("&favourite=").append(String.valueOf(favourite));

        return buffer.toString();
    }

    /**
     * HTML encode the argument.
     *
     * @param data string to encode.
     * @return the encoded string.
     */
    private String encode(String data)
    {
        try
        {
            return URLEncoder.encode(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean isCurrentlyOnNavigator()
    {
        //Look for the filter tabs...then this must be a navigator page.
        return new IdLocator(tester, ID_FILTER_FORM_HEADER).hasNodes();
    }

    private void switchIntoEditMode(final NavigatorEditMode mode)
    {
        final NavigatorEditMode currentEditMode = getCurrentEditMode();
        if (currentEditMode != mode)
        {
            tester.clickLink(ID_LINK_SWITCHNAVTYPE);
            final NavigatorEditMode newEditMode = getCurrentEditMode();
            if (newEditMode != mode)
            {
                Assert.fail("Unable to transition into " + mode + " mode from " + currentEditMode + " mode. Current edit mode " + newEditMode + ".");
            }
        }
    }

    public void gotoNewMode(final NavigatorEditMode navigatorEditMode)
    {
        gotoNavigator();
        gotoNavigatorMode(NavigatorMode.NEW, ID_LINK_NEW_FILTER);
        if (navigatorEditMode != null)
        {
            switchIntoEditMode(navigatorEditMode);
        }
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
        else if (tester.getDialog().isLinkPresent(ID_LINK_EDITFILTER))
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

    @Override
    public void goToConfigureColumns()
    {
        tester.clickLink("configure-cols");
    }

    private void editSearch(final NavigatorSearch search)
    {
        tester.setWorkingForm(ID_ISSUE_FILTER);
        for (NavigatorCondition condition : search.getConditions())
        {
            condition.setForm(tester);
        }
        tester.submit("show");
        tester.clickLink("viewfilter");

    }

    private void gotoNavigatorMode(final NavigatorMode mode, final String linkId)
    {
        final NavigatorMode currentMode = getCurrentMode();
        if (currentMode != mode)
        {
            tester.clickLink(linkId);
            final NavigatorMode newMode = getCurrentMode();
            if (newMode != mode)
            {
                Assert.fail("Unable to transition into " + mode + " mode from " + currentMode + " mode. Current mode " + newMode + ".");
            }
        }
    }


}
