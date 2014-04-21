package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Test;

import java.util.HashSet;
import java.util.Set;


/**
 * Test of the sharing options driven by JavaScript on the Add Dashboard page
 *
 * @since v3.13
 */
@WebTest({Category.SELENIUM_TEST })
public class TestAddPortalPage extends PortalPageTestCase
{
    public static Test suite()
    {
        return suiteFor(TestAddPortalPage.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        getNavigator().login(ADMIN_USERNAME);
    }

    public void testAddFavourite()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddFavourite", null, true, PRIVATE_PERMISSIONS);
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }
    
    public void testAddNonFavourite()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddNonFavourite", null, false, PRIVATE_PERMISSIONS);
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }

    public void testAddPrivatelySharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddPrivatelySharedPage", "Desc of testAddPrivatelySharedPage", false, PRIVATE_PERMISSIONS);
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }

    public void testAddGloballySharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddGloballySharedPage", "Desc of testAddGloballySharedPage", true, PUBLIC_PERMISSIONS);
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }

    public void testAddGroupSharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddGroupSharedPage", "Desc of testAddGroupSharedPage", true, createGroupShare(GROUP_JIRA_DEVELOPERS));
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }

    public void testAddProjectSharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddProjectSharedPage", "", false, HOMOSAPIEN_PERMISSIONS);
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }

    public void testAddProjectAndRoleSharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddProjectSharedPage", "", false, HIDDENFROMFRED_ROLE_USERS_PERMISSIONS);
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }

    public void testAddComplexSharePage()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testAddProjectSharedPage", "", true, null);
        Set permissions = new HashSet();
        permissions.addAll(HIDDENFROMFRED_ROLE_ADMINISTRATORS_PERMISSIONS);
        permissions.addAll(HIDDENFROMFRED_ROLE_DEVELOPERS_PERMISSIONS);
        permissions.addAll(HIDDENFROMFRED_ROLE_USERS_PERMISSIONS);
        permissions.addAll(GROUP_JIRA_ADMINS_PERMISSIONS);
        permissions.addAll(GROUP_JIRA_DEVELOPERS_PERMISSIONS);

        page.setSharingPermissions(permissions);
        addPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, page));
    }
}
