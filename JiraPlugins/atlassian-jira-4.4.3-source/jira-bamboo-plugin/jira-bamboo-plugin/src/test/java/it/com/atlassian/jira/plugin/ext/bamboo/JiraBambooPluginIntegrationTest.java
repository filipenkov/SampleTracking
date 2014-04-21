package it.com.atlassian.jira.plugin.ext.bamboo;

import com.atlassian.jira.functest.framework.FuncTestCase;

import org.junit.Test;

import static it.com.atlassian.jira.plugin.ext.bamboo.RelatedBuildAssertions.AA_SLAP_3;
import static it.com.atlassian.jira.plugin.ext.bamboo.RelatedBuildAssertions.AA_SLAP_4;
import static it.com.atlassian.jira.plugin.ext.bamboo.RelatedBuildAssertions.AA_SLAP_57;

/**
 * Although no application links exist in the restore file, legacy JBAM Bamboo servers do.
 * If the upgrade tasks complete successfully, these will be converted into Bamboo application links
 * and thus the Builds tabs will appear as expected.
 */
public class JiraBambooPluginIntegrationTest extends FuncTestCase
{
    private static final String BAMBOO_SERVER_NAME = "Bamboo test server";
    private static final String ISSUE_PANEL_LINK = "bamboo-build-results-tabpanel";
    private static final String PROJECT_PANEL_LINK = "bamboo-project-tabpanel-panel";

    @Test
    public void testIssueBuildsTabAppears()
    {
        goToIssue("ONE-1");
        getTester().assertLinkPresent(ISSUE_PANEL_LINK);
    }

    @Test
    public void testProjectBuildsTabAppears()
    {
        goToProject("ONE");
        getTester().assertLinkPresent(PROJECT_PANEL_LINK);
    }

    @Test
    public void testIssueBuildsTabRenders()
    {
        goToIssueBuildsPanel("ONE-1");
        getTester().assertTextPresent(BAMBOO_SERVER_NAME);
    }

    @Test
    public void testProjectBuildsTabRenders()
    {
        goToProjectBuildsPanel("ONE");
        getTester().assertTextPresent(BAMBOO_SERVER_NAME);
    }

    @Test
    public void testIssueBuildResultsAppearWhenViewingTab()
    {
        goToIssueBuildsPanelByDate("ONE-1");
        assertRelatedBuilds(AA_SLAP_3, AA_SLAP_4);
    }

    @Test
    public void testNoAssociatedIssueBuildsWhenViewingTab()
    {
        goToIssueBuildsPanelByDate("ONE-2");
        getTester().assertTextPresent("No associated builds found.");
    }

    @Test
    public void testProjectBuildResultsAppearWhenViewingTabByDate()
    {
        goToProjectBuildsPanelByDate("ONE");
        assertRelatedBuilds(AA_SLAP_3, AA_SLAP_4);
    }

    @Test
    public void testProjectBuildResultsAppearWhenViewingTabByPlanStatus()
    {
        goToProjectBuildsPanelByPlanStatus("ONE");
        assertRelatedBuilds(AA_SLAP_57);
    }

    private void goToIssue(String issueKey)
    {
        getTester().gotoPage("browse/" + issueKey);
    }

    private void goToIssueBuildsPanel(String issueKey)
    {
        goToIssue(issueKey);
        getTester().clickLink(ISSUE_PANEL_LINK);
    }

    private void goToProject(String projectKey)
    {
        getTester().gotoPage("browse/" + projectKey);
    }

    private void goToProjectBuildsPanel(String projectKey)
    {
        goToProject(projectKey);
        getTester().clickLink(PROJECT_PANEL_LINK);
    }

    private void goToIssueBuildsPanelByDate(String issueKey)
    {
        getTester().gotoPage("/secure/ViewBambooPanelContent.jspa?issueKey=" + issueKey + "&selectedSubTab=buildByDate");
    }

    private void goToProjectBuildsPanelByDate(String projectKey)
    {
        getTester().gotoPage("/secure/ViewBambooPanelContent.jspa?projectKey=" + projectKey + "&selectedSubTab=buildByDate");
    }

    private void goToProjectBuildsPanelByPlanStatus(String projectKey)
    {
        getTester().gotoPage("/secure/ViewBambooPanelContent.jspa?projectKey=" + projectKey + "&selectedSubTab=planStatus");
    }

    private void assertRelatedBuilds(RelatedBuildAssertion... assertions)
    {
        assertEquals("Unexpected number of related builds", assertions.length, xpath("//ol[@class='build_result_list']/li").getNodes().length);

        for (RelatedBuildAssertion assertion : assertions)
        {
            String buildKey = assertion.getProjectKey() + "-" + assertion.getPlanKey() + "-" + assertion.getBuildNumber();
            String buildsResultId = "buildResult_" + buildKey;

            // Validate if the correct Bamboo project/plan is being referenced
            assertEquals("Plan link text is incorrect for " + buildKey, assertion.getProjectName() + " - " + assertion.getPlanName(),
                xpath("//li[@id='" + buildsResultId + "']//a[@class='build-project']").getText());

            // Validate correct build result key is referenced
            assertEquals("Build result link text is incorrect for " + buildKey,
                         "#" + assertion.getBuildNumber(), xpath("//li[@id='" + buildsResultId + "']//a[@class='build-issue-key']").getText());

            // Validate related issues
            for (String relatedIssue : assertion.getRelatedIssues())
            {
                assertTrue("Related issues are not as expected for " + buildKey,
                    xpath("//li[@id='" + buildsResultId + "']//span[@class='related_issues']").getText().contains(relatedIssue));
            }

            // Validate build reason
            assertEquals("Build reason is incorrect for " + buildKey, assertion.getReason(),
                         xpath("//li[@id='" + buildsResultId + "']//span[@class='reason']").getText());

            // Validate duration summary exists
            assertNotEmptyString("Build duration does not exist for " + buildKey, xpath("//li[@id='" + buildsResultId + "']//span[@class='duration']").getText());

            // Validate test summary
            if (assertion.getTestsFailed() > 0)
            {
                assertEquals("Expected failed tests for " + buildKey, assertion.getTestsFailed() + " out of " + assertion.getTotalTests() + " failed",
                             xpath("//li[@id='" + buildsResultId + "']//span[@class='test_results']").getText());
            }
            else
            {
                if (assertion.getTotalTests() > 0)
                {
                    assertEquals("Expected passed tests for " + buildKey, assertion.getTotalTests() + " passed",
                                 xpath("//li[@id='" + buildsResultId + "']//span[@class='test_results']").getText());
                }
                else
                {
                    assertEquals("Expected no tests for " + buildKey, "No tests found",
                                 xpath("//li[@id='" + buildsResultId + "']//span[@class='test_results']").getText());
                }
            }

            // Validate build result
            assertEquals("Build result is incorrect for " + buildKey, assertion.isSuccess() ? "Build Successful:" : "Build Failed:",
                         xpath("//li[@id='" + buildsResultId + "']//span[@class='build_result']").getText());
        }
    }

    private void assertNotEmptyString(String message, String value)
    {
        assertFalse(message, "".equals(value));
    }
}
