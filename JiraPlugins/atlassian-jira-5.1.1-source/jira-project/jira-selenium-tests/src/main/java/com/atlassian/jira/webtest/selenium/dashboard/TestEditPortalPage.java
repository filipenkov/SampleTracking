package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Test;

import java.util.HashSet;
import java.util.Set;


/**
 * Test of the sharing options driven by JavaScript on the Edit Dashboard page
 *
 * @since v3.13
 */
@WebTest({Category.SELENIUM_TEST })
public class TestEditPortalPage extends PortalPageTestCase
{

    private static final Long existsPageId = PAGE_EXISTS.getId();
    public static Test suite()
    {
        return suiteFor(TestEditPortalPage.class);
    }
    public void onSetUp()
    {
        super.onSetUp();
        getNavigator().login(ADMIN_USERNAME);
    }

    public void testAddFavourite()
    {
        SharedEntityInfo page = new SharedEntityInfo(10020L, "testGlobalShareCreation", "Desc of testAddFavourite", true, PUBLIC_PERMISSIONS);
        addPage(page);
        assertPageList(TABLE_ID_PP_FAVOURITE, EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, page));
    }

    public void testEditFavourite()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditNonFavourite", null, true, PRIVATE_PERMISSIONS);
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE,  page));
    }

    public void testEditNonFavourite()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditNonFavourite", null, false, PRIVATE_PERMISSIONS);
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE,  page));
    }

    public void testEditPrivatelySharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditPrivatelySharedPage", "Desc of testEditPrivatelySharedPage", false, PRIVATE_PERMISSIONS);
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE,  page));
    }

    public void testEditGloballySharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditGloballySharedPage", "Desc of testEditGloballySharedPage", true, PUBLIC_PERMISSIONS);
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE,  page));
    }

    public void testEditGroupSharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditGroupSharedPage", "Desc of testEditGroupSharedPage", true, createGroupShare(GROUP_JIRA_DEVELOPERS));
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE,  page));
    }

    public void testEditProjectSharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditProjectSharedPage", "", false, HOMOSAPIEN_PERMISSIONS);
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE,  page));
    }

    public void testEditProjectAndRoleSharedPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditProjectSharedPage", "", false, HIDDENFROMFRED_ROLE_USERS_PERMISSIONS);
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE,  page));
    }

    public void testEditComplexSharePage()
    {
        SharedEntityInfo page = new SharedEntityInfo(existsPageId,"testEditProjectSharedPage", "", true, null);
        Set permissions = new HashSet();
        permissions.addAll(HIDDENFROMFRED_ROLE_ADMINISTRATORS_PERMISSIONS);
        permissions.addAll(HIDDENFROMFRED_ROLE_DEVELOPERS_PERMISSIONS);
        permissions.addAll(HIDDENFROMFRED_ROLE_USERS_PERMISSIONS);
        permissions.addAll(GROUP_JIRA_ADMINS_PERMISSIONS);
        permissions.addAll(GROUP_JIRA_DEVELOPERS_PERMISSIONS);

        page.setSharingPermissions(permissions);
        editPage(page);
        assertPageList(TABLE_ID_PP_MY, EasyList.build(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, page));
    }

}