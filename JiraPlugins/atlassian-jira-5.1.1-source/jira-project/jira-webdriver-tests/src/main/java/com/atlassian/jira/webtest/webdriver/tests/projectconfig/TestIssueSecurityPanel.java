package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.jira.pageobjects.project.issuesecurity.IssueSecurity;
import com.atlassian.jira.pageobjects.project.issuesecurity.IssueSecurityPage;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
@RestoreOnce ("xml/TestIssueSecurityTab.xml")
public class TestIssueSecurityPanel extends BaseJiraWebTest
{
    @Test
    public void notAdmin()
    {
        IssueSecurityPage issueSecurityPage = jira.gotoLoginPage().login("fred", "fred", IssueSecurityPage.class, "HSP");

        // No security levels in the table.
        assertEquals(0, issueSecurityPage.getIssueSecurities().size());
        assertTrue(issueSecurityPage.getNoSecurityLevelsMessage().isPresent());

        // Assert the cog actions aren't present
        assertFalse(issueSecurityPage.isSchemeLinked());
        assertFalse(issueSecurityPage.isSchemeChangeAvailable());

        ProjectSharedBy sharedBy = issueSecurityPage.getSharedBy();
        assertFalse(sharedBy.isPresent());

        IssueSecurityPage lalaIssueSecurityPage = pageBinder.navigateToAndBind(IssueSecurityPage.class, "LALA");
        ProjectSharedBy lalaSharedBy = lalaIssueSecurityPage.getSharedBy();
        assertFalse(lalaSharedBy.isPresent());
    }

    @Test
    public void projectWithoutSecurityLevels()
    {
        IssueSecurityPage issueSecurityPage = jira.gotoLoginPage().loginAsSysAdmin(IssueSecurityPage.class, "HSP");

        // No security levels in the table.
        assertEquals(0, issueSecurityPage.getIssueSecurities().size());
        assertTrue(issueSecurityPage.getNoSecurityLevelsMessage().isPresent());

        // No link to edit scheme is present.
        assertFalse(issueSecurityPage.isSchemeLinked());

        // Link to scheme select is present.
        assertTrue(issueSecurityPage.isSchemeChangeAvailable());

        assertFalse(issueSecurityPage.getSharedBy().isPresent());
    }

    @Test
    public void projectWithSecurityLevels()
    {
        IssueSecurityPage issueSecurityPage = jira.gotoLoginPage().loginAsSysAdmin(IssueSecurityPage.class, "MKY");

        // No security levels in the table.
        assertEquals(3, issueSecurityPage.getIssueSecurities().size());
        assertFalse(issueSecurityPage.getNoSecurityLevelsMessage().isPresent());

        final List<IssueSecurity> securityList = CollectionBuilder.<IssueSecurity>newBuilder(
                new IssueSecurity().setName("Classified (Default)").setDescription("most people can see this").setEntities(CollectionBuilder.newBuilder("Current Assignee", "Reporter").asList()),
                new IssueSecurity().setName("Secret").setDescription("<strong>don't even think about telling your wife</strong>").setEntities(CollectionBuilder.newBuilder("Project Lead", "Project Role (Developers)", "Reporter").asList()),
                new IssueSecurity().setName("Top Secret").setDescription("We will kill you.").setEntities(CollectionBuilder.newBuilder("Single User (fred)").asList())
        ).asList();
        Assert.assertEquals(securityList, issueSecurityPage.getIssueSecurities());

        // link to edit scheme is present.
        assertTrue(issueSecurityPage.isSchemeLinked());

        // Link to scheme select is present.
        assertTrue(issueSecurityPage.isSchemeChangeAvailable());

        final ProjectSharedBy sharedBy = issueSecurityPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("3 projects", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("LALA", "XSS", "monkey"), sharedBy.getProjects());
    }
}
