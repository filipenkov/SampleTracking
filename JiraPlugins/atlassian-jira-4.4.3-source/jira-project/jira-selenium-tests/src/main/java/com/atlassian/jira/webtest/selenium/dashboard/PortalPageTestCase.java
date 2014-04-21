package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.sharing.*;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A base class for PortalPage tests
 *
 * @since v3.13
 */
public abstract class PortalPageTestCase extends JiraSeleniumTest
{
    protected static final String GROUP_JIRA_ADMINISTRATORS = "jira-administrators";
    protected static final String GROUP_JIRA_DEVELOPERS = "jira-developers";

    protected static final Set PUBLIC_PERMISSIONS = TestSharingPermissionUtils.createPublicPermissions();
    protected static final Set PRIVATE_PERMISSIONS = TestSharingPermissionUtils.createPrivatePermissions();

    protected static final Set HOMOSAPIEN_PERMISSIONS = Collections.singleton(new ProjectTestSharingPermission(10000, "homosapien"));
    protected static final Set HIDDENFROMFRED_ROLE_USERS_PERMISSIONS = Collections.singleton(new ProjectTestSharingPermission(10010, 10000, "hidden_from_fred", "Users"));
    protected static final Set HIDDENFROMFRED_ROLE_DEVELOPERS_PERMISSIONS = Collections.singleton(new ProjectTestSharingPermission(10010, 10001, "hidden_from_fred", "Developers"));
    protected static final Set HIDDENFROMFRED_ROLE_ADMINISTRATORS_PERMISSIONS = Collections.singleton(new ProjectTestSharingPermission(10010, 10002, "hidden_from_fred", "Administrators"));
    protected static final Set GROUP_JIRA_ADMINS_PERMISSIONS = Collections.singleton(new GroupTestSharingPermission(GROUP_JIRA_ADMINISTRATORS));
    protected static final Set GROUP_JIRA_DEVELOPERS_PERMISSIONS = Collections.singleton(new GroupTestSharingPermission(GROUP_JIRA_DEVELOPERS));

    protected static final SharedEntityInfo PAGE_EXISTS = new SharedEntityInfo(new Long(10012), "Exists", null, true, PRIVATE_PERMISSIONS);
    protected static final SharedEntityInfo PAGE_ADMINNOTFAVOURITE = new SharedEntityInfo(new Long(10013), "AdminNotFavourite", null, false, PUBLIC_PERMISSIONS);
    protected static final SharedEntityInfo PAGE_ADMINFAVOURITE = new SharedEntityInfo(new Long(10014), "AdminFavourite", null, true, PUBLIC_PERMISSIONS);
    protected static final String SHARE_TYPE_SELECTOR = "id=share_type_selector";
    protected static final String LABEL_PROJECT = "label=Project";
    protected static final String PROJECT_SHARE_PROJECT = "id=projectShare-project";
    protected static final String PROJECT_SHARE_ROLE = "id=projectShare-role";
    protected static final String LABEL_GROUP = "label=Group";
    protected static final String GROUP_SHARE = "groupShare";
    protected static final String LABEL_EVERYONE = "label=Everyone";
    protected static final String TABLE_ID_PP_FAVOURITE = "pp_favourite";
    protected static final String TABLE_ID_PP_MY = "pp_owned";
    protected static final String PRIVATE_SHARE = "Private Dashboard";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("dashboard/BaseProfessionalPortalPage.xml");
    }

    protected void addPage(final SharedEntityInfo page)
    {
        getNavigator().gotoPage("/secure/AddPortalPage!default.jspa", true);
        client.type("portalPageName", page.getName());
        if (!StringUtils.isBlank(page.getDescription()))
        {
            client.type("portalPageDescription", page.getDescription());
        }
        configFavouriteStar(page);
        togglePermissions(page);
        getNavigator().clickAndWaitForPageLoad("Add");
    }

     protected void editPage(final SharedEntityInfo page)
    {
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=" + page.getId(), true);
        client.type("portalPageName", page.getName());
        if (!StringUtils.isBlank(page.getDescription()))
        {
            client.type("portalPageDescription", page.getDescription());
        }
        configFavouriteStar(page);
        togglePermissions(page);
        getNavigator().clickAndWaitForPageLoad("Update");
    }

    private void togglePermissions(final SharedEntityInfo page)
    {
        while (client.isVisible("//img[@alt='Delete Share']")) {
            getNavigator().click("//img[@alt='Delete Share']");
        }
        Set permissions = page.getSharingPermissions();
        if (permissions != null && ! permissions.isEmpty()) {
            for (Iterator iterator = permissions.iterator(); iterator.hasNext();)
            {
                TestSharingPermission testSharingPermission = (TestSharingPermission) iterator.next();
                togglePermissionSelect(testSharingPermission);
            }
        }
    }

    private void configFavouriteStar(final SharedEntityInfo page)
    {    	
        if (page.isFavourite())
        {
            if(!client.isElementPresent("css=a#fav_a_favourite.enabled"))
            {
                client.click("fav_a_favourite");
            }
        }
        else
        {
            if(!client.isElementPresent("css=a#fav_a_favourite.disabled"))
            {
                client.click("fav_a_favourite");
            }          
        }
    }

    private void togglePermissionSelect(final TestSharingPermission permission)
    {
        if (permission instanceof ProjectTestSharingPermission)
        {
            ProjectTestSharingPermission projectTestSharingPermission = (ProjectTestSharingPermission) permission;
            client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
            client.select(PROJECT_SHARE_PROJECT, "label=" + projectTestSharingPermission.getProjectName());
            String roleName = projectTestSharingPermission.getRoleName();
            if (!StringUtils.isBlank(roleName))
            {
                client.select(PROJECT_SHARE_ROLE, "label=" + roleName);
            }
            getNavigator().click("share_add_project");
        }
        else if (permission instanceof GroupTestSharingPermission)
        {
            GroupTestSharingPermission groupTestSharingPermission = (GroupTestSharingPermission) permission;
            client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
            client.select(GROUP_SHARE, "label=" + groupTestSharingPermission.getGroup());
            getNavigator().click("share_add_group");
        }
        else if (permission instanceof GlobalTestSharingPermission)
        {
            client.select(SHARE_TYPE_SELECTOR, LABEL_EVERYONE);
            getNavigator().click("share_add_global");
        }
        else
        {
            fail("What the hell is this share type?" + permission);
        }
    }

    protected void assertPageList(String tableId, final List pageList)
    {
        final int sharingCol;
        if (TABLE_ID_PP_MY.equals(tableId))
        {
            sharingCol = 2;
            getNavigator().gotoPage("secure/ConfigurePortalPages!default.jspa?view=my", true);
        }
        else
        {
            sharingCol = 3;
            getNavigator().gotoPage("secure/ConfigurePortalPages!default.jspa?view=favourite", false);
        }
        // selenium xpath is 1's based.  Boooooo!
        int row = 0;
        for (Iterator iterator = pageList.iterator(); iterator.hasNext();)
        {

            SharedEntityInfo page = (SharedEntityInfo) iterator.next();

            if (page.getId() != null)
            {
                assertEquals("Expected this page id in order : " + page.getId(), client.getAttribute("jquery=#" + tableId + " tbody tr:eq(" + row + ")@id"), "pp_" + page.getId());
            }

            String nameAndFavCell = client.getText("jquery=#pp_" + page.getId() + " td:eq(0)");
            assertTrue("The expected page name was not present in order : " + page.getName(), nameAndFavCell.indexOf(page.getName()) >= 0);

            // we can infer this from the generated Java
            if (page.isFavourite())
            {
            	
                assertTrue("The page is expected to be a favourite : " + page.getName(), client.isElementPresent("jquery=#pp_" + page.getId() + " td:eq(0) a.fav-link.enabled"));
            }
            else
            {
                assertTrue("The page is expected to be a non favourite : " + page.getName(), client.isElementPresent("jquery=#pp_" + page.getId() + " td:eq(0) a.fav-link.disabled"));
            }
            String sharing;
            if (client.isElementPresent("jquery=#share_list_summary_" + page.getId()))
            {
            	client.click("jquery=#share_list_summary_" + page.getId() +" span.switch");
                sharing = client.getText("jquery=#share_list_complete_" + page.getId());
            }
            else
            {
                final String expr = "jquery=#pp_" + page.getId() + " ul.shareList";
                sharing = client.getText(expr);
            }
            validateSharing(page, removeNewLines(sharing));

            row++;
        }
        String xpath = "jquery=#" + tableId + " tbody tr:eq(" + (row + 1) + ")";
        if (client.isElementPresent(xpath))
        {
            fail("There are too many rows in the page table.  Expected : " + pageList.size());
        }
    }

    private void validateSharing(final SharedEntityInfo page, final String sharing)
    {
        String pageName = page.getName();
        Set permissions = page.getSharingPermissions();
        if (permissions == null || permissions.isEmpty())
        {
            assertTrue(pageName + " does not have expected private permission :", sharing.indexOf(PRIVATE_SHARE) >= 0);
        }
        else
        {
            for (Iterator iterator = permissions.iterator(); iterator.hasNext();)
            {
                TestSharingPermission permission = (TestSharingPermission) iterator.next();
                final String displayStr = permission.toDisplayFormat();
                assertTrue(pageName + " does not have expected permission :" + displayStr, sharing.indexOf(displayStr) >= 0);
            }
        }
    }

    private String removeNewLines(final String displayStr)
    {
        return StringUtils.replace(displayStr, "\n", "");
    }

    protected Set createGroupShare(final String groupName)
    {
        return Collections.singleton(new GroupTestSharingPermission(groupName));
    }
}
