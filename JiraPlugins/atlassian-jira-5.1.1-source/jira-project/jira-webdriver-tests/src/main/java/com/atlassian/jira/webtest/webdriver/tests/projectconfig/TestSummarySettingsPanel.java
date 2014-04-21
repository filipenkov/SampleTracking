package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.SettingsPanel;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/SummarySettingsPanel.xml")
public class TestSummarySettingsPanel extends BaseJiraWebTest
{
    private static final String PROJECT_NO_CVS = "HSP";
    private static final String PROJECT_CVS = "MKY";

    @Test
    public void testNoCvs() throws Exception
    {
        ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_NO_CVS);
        SettingsPanel settingsPanel = summaryPage.openPanel(SettingsPanel.class);
        assertTrue(settingsPanel.getModules().isEmpty());

        assertTrue(settingsPanel.hasUalLink());
        summaryPage = settingsPanel.gotoUalConfigure().back(ProjectSummaryPageTab.class, PROJECT_CVS);
        settingsPanel = summaryPage.openPanel(SettingsPanel.class);

        assertTrue(settingsPanel.hasCvsChangeLink());
        assertEquals(Collections.<String>emptyList(), settingsPanel.gotoCvsChangeLink().getSelectedNames());
    }

    @Test
    public void testCvs() throws Exception
    {
        ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_CVS);
        SettingsPanel settingsPanel = summaryPage.openPanel(SettingsPanel.class);

        List<SettingsPanel.CvsModule> modules = settingsPanel.getModules();
        assertEquals(2, modules.size());
        assertCvsModule("<b>xss</b>", true, modules.get(0));
        assertCvsModule("Two", true, modules.get(1));

        assertTrue(settingsPanel.hasUalLink());
        summaryPage = settingsPanel.gotoUalConfigure().back(ProjectSummaryPageTab.class, PROJECT_CVS);
        settingsPanel = summaryPage.openPanel(SettingsPanel.class);

        assertTrue(settingsPanel.hasCvsChangeLink());
        assertEquals(Arrays.asList("<b>xss</b>", "Two"), settingsPanel.gotoCvsChangeLink().getSelectedNames());
    }

    @Test
    public void testNoCvsProjectAdmin() throws Exception
    {
        ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .login("fred", "fred", ProjectSummaryPageTab.class, PROJECT_NO_CVS);
        SettingsPanel settingsPanel = summaryPage.openPanel(SettingsPanel.class);
        assertTrue(settingsPanel.getModules().isEmpty());

        assertFalse(settingsPanel.hasUalLink());
        assertFalse(settingsPanel.hasCvsChangeLink());
    }

    @Test
    public void testCvsProjectAdmin() throws Exception
    {
        ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .login("fred", "fred", ProjectSummaryPageTab.class, PROJECT_CVS);
        SettingsPanel settingsPanel = summaryPage.openPanel(SettingsPanel.class);

        List<SettingsPanel.CvsModule> modules = settingsPanel.getModules();
        assertEquals(2, modules.size());
        assertCvsModule("<b>xss</b>", false, modules.get(0));
        assertCvsModule("Two", false, modules.get(1));

        assertFalse(settingsPanel.hasUalLink());
        assertFalse(settingsPanel.hasCvsChangeLink());
    }

    private void assertCvsModule(String name, boolean url, SettingsPanel.CvsModule module)
    {
        assertEquals("Module name is incorrect.", name, module.getName());
        assertEquals("Module should have link.", url, module.hasLink());
    }
}
