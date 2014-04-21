package com.atlassian.jira.webtest.selenium.menu;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.webtest.ui.keys.KeyEventType;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import junit.framework.Test;

/**
 * @since v4.0
 */
@Quarantine
@WebTest({Category.SELENIUM_TEST })
public class TestProjectMenu extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";
    private final String CONTEXT = getEnvironmentData().getContext();
    private static final String MAIN_BODY = "jira";

    private static final KeySequence ENTER = SpecialKeys.ENTER.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYPRESS);

    public static Test suite()
    {
        return suiteFor(TestProjectMenu.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestProjectMenuAtoZ.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

    }

    public void testProjectMenu()
    {


        _testNotLoggedInCanSeeNoHistory();
        _testNotLoggedInNoHistoryGoesToViewAll();
        _testNotLoggedInCanSeeOneHistory();
        _testNotLoggedInGoesToCurrentProject();
        _testNotLoggedInCanSeeManyHistory();
        _testNotLoggedInChangingHistory();
        _testNotLoggedInMaxHistory();


        _testLoggedInCanSeeNoHistory();
        //This test is not working
        //_testCtrlBWithoutProjects();
        _testLoggedInNoHistoryGoesToViewAll();
        _testLoggedInCanSeeOneHistory();
        //This test is not working
        //_testCtrlBWithProjects();
        _testLoggedInGoesToCurrentProject();
        _testLoggedInCanSeeManyHistory();
        _testArrowKeys();
        _testLoggedInChangingHistory();
        _testLoggedInMaxHistory();
        _testLoggedInMaxHistoryOverload();
        //_testEnterKeyNoItemSelected();
        _testEnterKeyItemSelected();

//        _testCtrlP();
//        _testEsc();

        _testPermissionsRemoved();
        _testProjectDeleted();
        _testNotLoggedInCantSee();


    }


    private void _testProjectDeleted()
    {
        getNavigator().gotoAdmin();
        client.click("delete_project_10013",true);
        client.click("Delete", true);
        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.elementNotPresent("proj_lnk_10013");
        client.click("browse_link_drop");

    }

    private void _testEsc()
    {
        assertThat.visibleByTimeout(MAIN_BODY, TIMEOUT);
        client.keyPress(MAIN_BODY, "\\27");
        assertThat.elementNotPresentByTimeout("jquery=li.aui-dd-parent.lazy.selected.dd-allocated active a#browse_link", TIMEOUT);
    }

    //TODO: NOT CROSS-PLATFORM
//    private void _testCtrlP()
//    {
//
//        client.controlKeyDown();
//        assertThat.visibleByTimeout(MAIN_BODY, TIMEOUT);
//        client.keyPress(MAIN_BODY, "p");
//        assertThat.visibleByTimeout("//li[@class='aui-dd-parent lazy selected dd-allocated active']//a[@id='browse_link']", TIMEOUT);
//        client.controlKeyUp();
//
//    }
//
//    private void _testCtrlBWithProjects()
//    {
//        client.controlKeyDown();
//        assertThat.visibleByTimeout(MAIN_BODY, TIMEOUT);
//        client.keyPress(MAIN_BODY, "b");
//        client.controlKeyUp();
//
//        assertThat.textPresentByTimeout("TPA", TIMEOUT);
//    }
//
//    public void _testCtrlBWithoutProjects()
//    {
//        client.controlKeyDown();
//        assertThat.visibleByTimeout(MAIN_BODY, TIMEOUT);
//        client.keyPress(MAIN_BODY, "b");
//        client.controlKeyUp();
//
//        assertThat.textPresentByTimeout("Browse Projects", TIMEOUT);
//    }

    private void _testLoggedInGoesToCurrentProject()
    {
        _testNotLoggedInGoesToCurrentProject();
    }

    private void _testNotLoggedInGoesToCurrentProject()
    {
        client.click("browse_link", true);
        assertThat.textPresentByTimeout("TPA", TIMEOUT);
    }

    private void _testLoggedInNoHistoryGoesToViewAll()
    {
        _testNotLoggedInNoHistoryGoesToViewAll();
    }

    private void _testNotLoggedInNoHistoryGoesToViewAll()
    {
        client.click("browse_link", true);
        assertThat.textPresentByTimeout("Browse Projects", TIMEOUT);
    }


    private void _testLoggedInMaxHistoryOverload()
    {
        getNavigator().browseProject("TPS");
        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPS']", TIMEOUT);
        ensureHistoryInOrder(
                "TPR",
                "TPQ",
                "TPP",
                "TPO",
                "TPN",
                "TPM",
                "TPL",
                "TPK",
                "TPJ"
        );
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10003");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10004");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10005");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10006");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10007");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10008");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10009");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10010");
        assertThat.elementNotPresent("jquery=ul#project_history_main'> li#proj_lnk_10011");

        client.click("browse_link_drop");

    }

    private void browseToProjects(String... projects)
    {
        for (String project : projects)
        {
            getNavigator().browseProject(project);
        }
    }

    private void ensureHistoryInOrder(String... projects)
    {
        for (int i = 0; i < projects.length; ++i)
        {
            assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(" + (i + 1) + ")", projects[i]);
        }
    }

    private void _testLoggedInMaxHistory()
    {
        browseToProjects("TPE",
                "TPF",
                "TPG",
                "TPH",
                "TPI",
                "TPJ",
                "TPK",
                "TPL",
                "TPM",
                "TPN",
                "TPO",
                "TPP",
                "TPQ",
                "TPR"
        );
        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPR']", TIMEOUT);
        ensureHistoryInOrder(
                "TPQ",
                "TPP",
                "TPO",
                "TPN",
                "TPM",
                "TPL",
                "TPK",
                "TPJ",
                "TPI",
                "TPH",
                "TPG",
                "TPF",
                "TPE",
                "TPC",
                "TPD",
                "TPA",
                "TPB"
        );
        client.click("browse_link_drop");
    }

    private void _testLoggedInChangingHistory()
    {
        _testNotLoggedInChangingHistory();
    }

    private void _testLoggedInCanSeeManyHistory()
    {
        _testNotLoggedInCanSeeManyHistory();
    }


    private void _testNotLoggedInMaxHistory()
    {
        browseToProjects("TPE",
                "TPF",
                "TPG",
                "TPH",
                "TPI",
                "TPJ",
                "TPK"
        );
        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPK']", TIMEOUT);
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(1)", "TPJ");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(2)", "TPI");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(3)", "TPH");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(4)", "TPG");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(5)", "TPF");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(6)", "TPE");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(7)", "TPC");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(8)", "TPD");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(9)", "TPA");
        assertThat.elementNotPresent("jquery=ul#project_history_main > li#proj_lnk_10003");
        client.click("browse_link_drop");

    }

    private void _testNotLoggedInChangingHistory()
    {
        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPC']", TIMEOUT);
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(1)", "TPB");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(2)", "TPA");

        getNavigator().browseProject("TPA");
        assertThat.textPresent("TPA");
        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPA']", TIMEOUT);
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(1)", "TPC");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(2)", "TPB");


        getNavigator().browseProject("TPD");
        assertThat.textPresent("TPD");
        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPD']", TIMEOUT);
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(1)", "TPA");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(2)", "TPC");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(3)", "TPB");


        getNavigator().browseProject("TPC");
        assertThat.textPresent("TPC");
        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPC']", TIMEOUT);
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(1)", "TPD");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(2)", "TPA");
        assertThat.elementVisibleContainsText("jquery=ul#project_history_main > li:nth-child(3)", "TPB");
        client.click("browse_link_drop");

    }

    private void _testNotLoggedInCanSeeManyHistory()
    {
        getNavigator().browseProject("TPB");
        assertThat.textPresent("TPB");

        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("admin_main_proj_link", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPB']", TIMEOUT);
        assertThat.textPresentByTimeout("Recent Projects", TIMEOUT);
        assertThat.visibleByTimeout("proj_lnk_10004", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#proj_lnk_10004 a[href='" + CONTEXT + "/browse/TPA']", TIMEOUT);
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.elementNotPresent("proj_lnk_10003");
        assertThat.elementNotPresent("proj_lnk_10005");
        assertThat.elementNotPresent("proj_lnk_10006");
        assertThat.elementNotPresent("proj_lnk_10007");
        assertThat.elementNotPresent("proj_lnk_10008");
        assertThat.elementNotPresent("proj_lnk_10009");
        assertThat.elementNotPresent("proj_lnk_10010");
        assertThat.elementNotPresent("proj_lnk_10011");
        assertThat.elementNotPresent("proj_lnk_10012");
        assertThat.elementNotPresent("proj_lnk_10013");

        getNavigator().browseProject("TPC");
        assertThat.textPresent("TPC");

        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("admin_main_proj_link", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPC']", TIMEOUT);
        assertThat.textPresentByTimeout("Recent Projects", TIMEOUT);
        assertThat.visibleByTimeout("proj_lnk_10004", TIMEOUT);
        assertThat.visibleByTimeout("proj_lnk_10003", TIMEOUT);
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);

        assertThat.elementNotPresent("proj_lnk_10005");
        assertThat.elementNotPresent("proj_lnk_10006");
        assertThat.elementNotPresent("proj_lnk_10007");
        assertThat.elementNotPresent("proj_lnk_10008");
        assertThat.elementNotPresent("proj_lnk_10009");
        assertThat.elementNotPresent("proj_lnk_10010");
        assertThat.elementNotPresent("proj_lnk_10011");
        assertThat.elementNotPresent("proj_lnk_10012");
        assertThat.elementNotPresent("proj_lnk_10013");
        client.click("browse_link_drop");


    }

    public void _testNotLoggedInCantSee()
    {
        getNavigator().gotoAdmin();
        client.click("permission_schemes", true);
        client.click("0_edit", true);
        client.click("del_perm_10_", true);
        client.click("Delete", true);

        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        assertThat.elementNotPresentByTimeout("browse_link", TIMEOUT);
        assertThat.elementNotPresent("browse_link_drop");
    }

    public void _testNotLoggedInCanSeeNoHistory()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        assertThat.elementPresentByTimeout("browse_link", TIMEOUT);
        assertThat.elementPresentByTimeout("browse_link_drop", TIMEOUT);
        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.elementNotPresent("proj_lnk_10003");
        assertThat.elementNotPresent("proj_lnk_10004");
        assertThat.elementNotPresent("proj_lnk_10005");
        assertThat.elementNotPresent("proj_lnk_10006");
        assertThat.elementNotPresent("proj_lnk_10007");
        assertThat.elementNotPresent("proj_lnk_10008");
        assertThat.elementNotPresent("proj_lnk_10009");
        assertThat.elementNotPresent("proj_lnk_10010");
        assertThat.elementNotPresent("proj_lnk_10011");
        assertThat.elementNotPresent("proj_lnk_10012");
        assertThat.elementNotPresent("proj_lnk_10013");
        client.click("browse_link_drop");
    }

    public void _testLoggedInCanSeeNoHistory()
    {
        getNavigator().login(TEST_USERNAME, TEST_PASSWORD);
        getNavigator().gotoHome();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertThat.elementPresentByTimeout("browse_link", TIMEOUT);
        assertThat.elementPresent("browse_link_drop");
        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.elementNotPresent("proj_lnk_10003");
        assertThat.elementNotPresent("proj_lnk_10004");
        assertThat.elementNotPresent("proj_lnk_10005");
        assertThat.elementNotPresent("proj_lnk_10006");
        assertThat.elementNotPresent("proj_lnk_10007");
        assertThat.elementNotPresent("proj_lnk_10008");
        assertThat.elementNotPresent("proj_lnk_10009");
        assertThat.elementNotPresent("proj_lnk_10010");
        assertThat.elementNotPresent("proj_lnk_10011");
        assertThat.elementNotPresent("proj_lnk_10012");
        assertThat.elementNotPresent("proj_lnk_10013");
        client.click("browse_link_drop");


    }

    public void _testLoggedInCanSeeOneHistory()
    {
        getNavigator().browseProject("TPA");
        assertThat.textPresentByTimeout("TPA", TIMEOUT);

        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("admin_main_proj_link", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPA']", TIMEOUT);
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.elementNotPresent("proj_lnk_10003");
        assertThat.elementNotPresent("proj_lnk_10004");
        assertThat.elementNotPresent("proj_lnk_10005");
        assertThat.elementNotPresent("proj_lnk_10006");
        assertThat.elementNotPresent("proj_lnk_10007");
        assertThat.elementNotPresent("proj_lnk_10008");
        assertThat.elementNotPresent("proj_lnk_10009");
        assertThat.elementNotPresent("proj_lnk_10010");
        assertThat.elementNotPresent("proj_lnk_10011");
        assertThat.elementNotPresent("proj_lnk_10012");
        assertThat.elementNotPresent("proj_lnk_10013");
        client.click("browse_link_drop");
    }


    public void _testNotLoggedInCanSeeOneHistory()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().browseProject("TPA");
        assertThat.textPresent("TPA");

        client.click("browse_link_drop");
        assertThat.textPresentByTimeout("Current Project", TIMEOUT);
        assertThat.visibleByTimeout("admin_main_proj_link", TIMEOUT);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link a[href='" + CONTEXT + "/browse/TPA']", TIMEOUT);
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.elementNotPresent("proj_lnk_10003");
        assertThat.elementNotPresent("proj_lnk_10004");
        assertThat.elementNotPresent("proj_lnk_10005");
        assertThat.elementNotPresent("proj_lnk_10006");
        assertThat.elementNotPresent("proj_lnk_10007");
        assertThat.elementNotPresent("proj_lnk_10008");
        assertThat.elementNotPresent("proj_lnk_10009");
        assertThat.elementNotPresent("proj_lnk_10010");
        assertThat.elementNotPresent("proj_lnk_10011");
        assertThat.elementNotPresent("proj_lnk_10012");
        assertThat.elementNotPresent("proj_lnk_10013");
        client.click("browse_link_drop");
    }

    public void _testArrowKeys()
    {
        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.visibleByTimeout(MAIN_BODY, TIMEOUT);

        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
        assertThat.visibleByTimeout("jquery=li#proj_lnk_10003.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
        assertThat.visibleByTimeout("jquery=li#proj_lnk_10004.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
        assertThat.visibleByTimeout("jquery=li#project_view_all_link.active", TIMEOUT);

        client.simulateKeyPressForSpecialKey("css=body", 38);
        assertThat.visibleByTimeout("jquery=li#proj_lnk_10004.active", TIMEOUT);
        client.simulateKeyPressForSpecialKey("css=body", 38);
        assertThat.visibleByTimeout("jquery=li#proj_lnk_10003.active", TIMEOUT);
        client.simulateKeyPressForSpecialKey("css=body", 38);
        assertThat.visibleByTimeout("jquery=li#admin_main_proj_link.active", TIMEOUT);
        client.click("browse_link_drop");

    }

    public void _testEnterKeyItemSelected()
    {
        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        Mouse.mouseover(client, "project_view_all_link");
        assertThat.visibleByTimeout(MAIN_BODY, TIMEOUT);
        context().ui().pressInBody(ENTER);
        client.waitForPageToLoad();
        context().ui().pressInBody(ENTER);
        assertThat.textPresent("Browse Projects");

        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        Mouse.mouseover(client, "admin_main_proj_link");
        context().ui().pressInBody(ENTER);
        client.waitForPageToLoad();
        assertThat.textPresent("TPS");
        context().ui().pressInBody(ENTER);

        client.click("browse_link_drop");
        assertThat.visibleByTimeout("proj_lnk_10020", TIMEOUT);
        Mouse.mouseover(client, "proj_lnk_10020");
        assertThat.visibleByTimeout(MAIN_BODY, TIMEOUT);
        context().ui().pressInBody(ENTER);
        client.waitForPageToLoad();
        assertThat.textPresent("TPL");
        context().ui().pressInBody(ENTER);
        client.click("browse_link_drop");


    }

    public void _testPermissionsRemoved()
    {
        getNavigator().gotoPage("/plugins/servlet/project-config/" + "TPS" + "/permissions", true);
        client.click("project-config-permissions-scheme-change", true);
        client.select("schemeIds_select", "index=1");
        client.click("Associate", true);
        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        assertThat.elementNotPresent("proj_lnk_10023");
        client.click("browse_link_drop");

    }


    /*public void _testEnterKeyNoItemSelected()
    {
        getNavigator().gotoHome();


        client.click("browse_link_drop");
        assertThat.visibleByTimeout("project_view_all_link", TIMEOUT);
        client.keyPress("jira", "\\13");
        client.waitForPageToLoad();
        assertThat.textPresent("Browse Projects");
    }*/
}
