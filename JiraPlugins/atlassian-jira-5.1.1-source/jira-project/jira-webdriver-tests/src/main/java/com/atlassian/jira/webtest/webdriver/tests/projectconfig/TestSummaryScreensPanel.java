package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.screens.ScreensPanel;
import com.atlassian.jira.pageobjects.project.summary.screens.ScreensPanel.ScreenSchemeListItem;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Web test for the project configuration summary page's Screens panel.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@Restore("xml/TestProjectConfigSummaryScreensPanel.xml")
public class TestSummaryScreensPanel extends BaseJiraWebTest
{

    private static final String MKY_KEY = "MKY";
    private static final String HSP_KEY = "HSP";
    private static final String BLUK_KEY = "BLUK";
    private static final String XSS_KEY = "XSS";

    private List<ScreenSchemeListItem> HSP_SCREEN_SCHEMES;
    private List<ScreenSchemeListItem> MKY_SCREEN_SCHEMES;
    private List<ScreenSchemeListItem> XSS_SCREEN_SCHEMES;
    private List<ScreenSchemeListItem> BLUK_SCREEN_SCHEMES;
    private List<ScreenSchemeListItem> HSP_SCREEN_SCHEMES_FOR_PROJECT_ADMIN;

    private static final String MKY_ISSUE_TYPE_SCREEN_SCHEME_NAME = "<script>alert(\"hello\");</script>";
    private static final String XSS_SCREEN_SCHEME_NAME = "All Issue Types Assigned Screen Scheme";
    private static final String HSP_ISSUE_TYPE_SCREEN_SCHEME_NAME = "Default Issue Type Screen Scheme";
    private static final String BLUK_ISSUE_TYPE_SCREEN_SCHEME_NAME = "Duplicate Screen Scheme Scheme";

    private static final String PROJECT_ADMIN = "project_admin";

    private static String baseUrl;


    @Before
    public void setUp()
    {
        baseUrl = jira.getProductInstance().getBaseUrl();

        HSP_SCREEN_SCHEMES = Lists.newArrayList(
                createListItem("Default Screen Scheme", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=1", true)
        );
        HSP_SCREEN_SCHEMES_FOR_PROJECT_ADMIN = Lists.newArrayList(
                createListItem("Default Screen Scheme", null, true)
        );
        MKY_SCREEN_SCHEMES = Lists.newArrayList(
                createListItem("<script>alert(\"wtf\");</script>", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=10002", true),
                createListItem("Scheme A", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=10000", false),
                createListItem("Scheme B", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=10001", false)
        );
        BLUK_SCREEN_SCHEMES = Lists.newArrayList(
                createListItem("<script>alert(\"wtf\");</script>", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=10002", true),
                createListItem("Scheme A", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=10000", false)
        );
        XSS_SCREEN_SCHEMES = Lists.newArrayList(
                createListItem("Default Screen Scheme", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=1", false),
                createListItem("Scheme A", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=10000", false),
                createListItem("Scheme B", "/secure/admin/ConfigureFieldScreenScheme.jspa?id=10001", false)
        );
    }

    @Test
    public void testCanViewScreensPanel()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ScreensPanel screensPanel = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(ScreensPanel.class);
        assertEquals(HSP_SCREEN_SCHEMES, screensPanel.getScreenSchemes());
        assertEquals(HSP_ISSUE_TYPE_SCREEN_SCHEME_NAME, screensPanel.getIssueTypeScreenSchemeEditLinkText());
        assertEquals(baseUrl + "/plugins/servlet/project-config/" + HSP_KEY + "/screens", screensPanel.getIssueTypeScreenSchemeEditLinkUrl());
    }

    @Test
    public void testCanViewScreenSchemesAsProjectAdmin()
    {
        jira.gotoLoginPage().login(PROJECT_ADMIN, PROJECT_ADMIN, DashboardPage.class);

        final ScreensPanel screensPanel = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(ScreensPanel.class);
        assertEquals(HSP_SCREEN_SCHEMES_FOR_PROJECT_ADMIN, screensPanel.getScreenSchemes());
        assertEquals(HSP_ISSUE_TYPE_SCREEN_SCHEME_NAME, screensPanel.getIssueTypeScreenSchemeEditLinkText());
        assertEquals(baseUrl + "/plugins/servlet/project-config/" + HSP_KEY + "/screens", screensPanel.getIssueTypeScreenSchemeEditLinkUrl());
    }

    @Test
    public void testXSS()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ScreensPanel screensPanel = navigateToSummaryPageFor(MKY_KEY)
                .openPanel(ScreensPanel.class);
        assertEquals(MKY_SCREEN_SCHEMES, screensPanel.getScreenSchemes());
        assertEquals(MKY_ISSUE_TYPE_SCREEN_SCHEME_NAME, screensPanel.getIssueTypeScreenSchemeEditLinkText());
    }

    @Test
    public void testSameScreenSchemeOnlyAppearsOnce()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ScreensPanel screensPanel = navigateToSummaryPageFor(BLUK_KEY)
                .openPanel(ScreensPanel.class);
        assertEquals(BLUK_SCREEN_SCHEMES, screensPanel.getScreenSchemes());
        assertEquals(BLUK_ISSUE_TYPE_SCREEN_SCHEME_NAME, screensPanel.getIssueTypeScreenSchemeEditLinkText());
    }

    @Test
    public void testDefaultScreenDoesNotAppearWhenAllIssueTypesAreAssigned()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ScreensPanel screensPanel = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(ScreensPanel.class);
        assertEquals(XSS_SCREEN_SCHEMES, screensPanel.getScreenSchemes());
        assertEquals(XSS_SCREEN_SCHEME_NAME, screensPanel.getIssueTypeScreenSchemeEditLinkText());
    }


    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

    private ScreenSchemeListItem createListItem(final String schemeName, final String schemeUrl, boolean isDefault)
    {
        final String url = (schemeUrl == null) ?
                null : baseUrl + schemeUrl;
        return new ScreenSchemeListItem(schemeName, url, isDefault);
    }

}
