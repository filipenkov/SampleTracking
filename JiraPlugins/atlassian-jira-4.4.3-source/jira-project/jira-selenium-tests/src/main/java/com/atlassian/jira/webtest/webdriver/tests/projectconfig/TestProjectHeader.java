package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigHeader;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestProjectConfigSummary.xml")
public class TestProjectHeader extends BaseJiraWebTest
{
    public static final String PKEY_HSP = "HSP";
    public static final String PKEY_MKY = "MKY";

    private static String baseUrl;
    private static String context;

    @Before
    public void setUp()
    {
        baseUrl = jira.getProductInstance().getBaseUrl();
        context = jira.getProductInstance().getContextPath();
    }

    @Test
    public void testCorrectDefaultProjectDetails()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectConfigHeader header = navigateToSummaryPageFor(PKEY_HSP).getProjectHeader();

        assertEquals(baseUrl + "/secure/projectavatar?pid=10000&avatarId=10011", header.getProjectAvatarIconSrc());
        assertEquals("homosapien", header.getProjectName());

        assertEquals(header.getProjectKey(), PKEY_HSP);
        assertEquals(header.getProjectUrl(), "No URL");
        assertEquals(header.getProjectCategory(), "None");
        assertEquals(header.getProjectCategoryLink(), context + "/secure/project/SelectProjectCategory!default.jspa?pid=10000");
    }

    @Test
    public void testCorrectCustomisedProjectDetails()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectConfigHeader header = navigateToSummaryPageFor(PKEY_MKY).getProjectHeader();

        assertEquals(header.getProjectAvatarIconSrc(), baseUrl + "/secure/projectavatar?pid=10001&avatarId=10000");
        assertEquals(header.getProjectName(), "Monkey");

        assertEquals(header.getProjectKey(), PKEY_MKY);
        assertEquals(header.getProjectUrl(), "http://example.com");
        assertEquals(header.getProjectUrlLink(), "http://example.com");
        assertEquals(header.getProjectCategory(), "Random Category");
        assertEquals(header.getProjectCategoryLink(), context + "/secure/project/SelectProjectCategory!default.jspa?pid=10001");
        assertTrue(header.getDescriptionHtml().contains("<b>in bold</b>"));
    }

    @Test
    public void testNoXSSInProjectName()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectConfigHeader header = navigateToSummaryPageFor("XSS").getProjectHeader();
        assertEquals(header.getProjectName(), "<script>alert(\"wtf\");</script>");
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }
}
