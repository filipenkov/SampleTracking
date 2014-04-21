package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.EditWorkflowScheme;
import com.atlassian.jira.pageobjects.pages.admin.workflow.SelectWorkflowScheme;
import com.atlassian.jira.pageobjects.project.workflow.WorkflowsPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * These tests only seem to pass under Mac on FF36+. It seems there is some kind of bug in Webdriver that stops
 * links from being updated correctly. You can set it to use FF36 using -Dwebdriver.browser=firefox-3.6.
 * 
 * @since v4.4
 */

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestWorkflowTab.xml")
public class TestTabWorkflow extends BaseJiraWebTest
{
    private static final String PROJECT_DEFAULT_KEY = "HSP";
    private static final String PROJECT_DEFAULT_NAME = "homosapien";
    private static final long PROJECT_DEFAULT_ID = 10000L;

    private static final String PROJECT_PRIVATE_KEY = "MKY";
    private static final long PROJECT_PRIVATE_ID = 10001L;

    private static final String PROJECT_XSS_NAME = "<strong>Cross Site Scripting</strong>";
    private static final String PROJECT_XSS_KEY = "XSS";
    private static final long PROJECT_XSS_ID= 10010L;

    private static final String WF_SCHEME_MONKEY_NAME = "Monkey Scheme";
    private static final long WF_SCHEME_MONKEY_ID = 10000L;

    private static final String WF_SCHEME_XSS_NAME = "<strong>XSS WFScheme</strong>";
    private static final long WF_SCHEME_XSS_ID = 10001;

    private static final String ISSUE_TYPE_XSS = "<strong>XSS Type</strong>";
    private static final String ISSUE_TYPE_BUG = "Bug";
    private static final String ISSUE_TYPE_NEW_FEATURE = "New Feature";
    private static final String ISSUE_TYPE_TASK = "Task";
    private static final String ISSUE_TYPE_IMPROVEMENT = "Improvement";

    private static final String WF_DEFAULT_NAME = "jira";
    private static final String WF_XSS_NAME = "<strong>XSS Workflow</strong>";
    private static final String WF_ONLY_MONKEY = "Only Monkey";

    @Test
    public void testSystemDefaultWorkflowScheme() throws Exception
    {
        WorkflowsPageTab workflows = jira.gotoLoginPage().loginAsSysAdmin(WorkflowsPageTab.class, PROJECT_DEFAULT_KEY);

        assertEquals("Default Workflow Scheme", workflows.getSchemeName());
        assertFalse(workflows.isSchemeLinked());
        assertTrue(workflows.isSchemeChangeAvailable());

        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        assertEquals(1, panels.size());

        WorkflowsPageTab.WorkflowPanel panel = panels.get(0);
        assertEquals(Lists.newArrayList(ISSUE_TYPE_BUG, ISSUE_TYPE_XSS, ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_NEW_FEATURE, ISSUE_TYPE_TASK), panel.getIssueTypes());
        assertEquals(WF_DEFAULT_NAME, panel.getWorkflowName());
        assertTrue(panel.isDefaultWorkflow());

        List<WorkflowsPageTab.WorkflowTransition> transitions = panel.getTransitions();

        List<TestTransition> actualRows = toTestTranstions(transitions);
        List<TestTransition> expectedRows = createDefaultWorkflow(false);
        assertEquals(expectedRows, actualRows);
        assertScreens(transitions, false);

        //Are the projects correct?
        assertEquals(Arrays.asList(PROJECT_XSS_NAME, PROJECT_DEFAULT_NAME), panel.getProjects());

        //Ca we edit the workflow.
        assertTrue(panel.hasEditWorkflowLink());
        assertLinkForWorkflow(WF_DEFAULT_NAME, panel.getEditWorkflowLink());

        SelectWorkflowScheme selectWorkflowScheme = workflows.gotoSelectScheme();
        assertEquals(PROJECT_DEFAULT_ID, (long)selectWorkflowScheme.getProjectId());
        assertEquals("Default", selectWorkflowScheme.getSelectedScheme());
    }

    @Test
    public void testSystemDefaultWorkflowSchemeProjectAdmin()
    {
        WorkflowsPageTab workflows = jira.gotoLoginPage().login("fred", "fred", WorkflowsPageTab.class, PROJECT_DEFAULT_KEY);

        assertEquals("Default Workflow Scheme", workflows.getSchemeName());

        // Assert the cog actions aren't present
        assertFalse(workflows.isSchemeLinked());
        assertFalse(workflows.isSchemeChangeAvailable());

        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        assertEquals(1, panels.size());

        WorkflowsPageTab.WorkflowPanel panel = panels.get(0);
        assertEquals(Lists.newArrayList(ISSUE_TYPE_BUG, ISSUE_TYPE_XSS, ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_NEW_FEATURE, ISSUE_TYPE_TASK), panel.getIssueTypes());
        assertEquals(WF_DEFAULT_NAME, panel.getWorkflowName());
        assertTrue(panel.isDefaultWorkflow());

        List<WorkflowsPageTab.WorkflowTransition> transitions = panel.getTransitions();
        List<TestTransition> actualRows = toTestTranstions(transitions);
        List<TestTransition> expectedRows = createDefaultWorkflow(true);
        assertEquals(expectedRows, actualRows);
        assertScreens(transitions, true);

        assertTrue(panel.getProjects().isEmpty());
        assertFalse(panel.hasEditWorkflowLink());
    }

    @Test
    public void testProjectWithUniqueWorkflow() throws Exception
    {
        WorkflowsPageTab workflows = jira.gotoLoginPage().loginAsSysAdmin(WorkflowsPageTab.class, PROJECT_PRIVATE_KEY);

        assertEquals(WF_SCHEME_MONKEY_NAME, workflows.getSchemeName());
        assertTrue(workflows.isSchemeLinked());
        assertTrue(workflows.isSchemeChangeAvailable());

        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        assertEquals(1, panels.size());

        WorkflowsPageTab.WorkflowPanel panel = panels.get(0);
        assertEquals(Lists.newArrayList(ISSUE_TYPE_BUG, ISSUE_TYPE_XSS, ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_NEW_FEATURE, ISSUE_TYPE_TASK), panel.getIssueTypes());
        assertEquals(WF_ONLY_MONKEY, panel.getWorkflowName());
        assertFalse(panel.isDefaultWorkflow());

        assertTrue(panel.hasEditWorkflowLink());
        assertLinkForWorkflow(WF_ONLY_MONKEY, panel.getEditWorkflowLink());

        List<WorkflowsPageTab.WorkflowTransition> transitions = panel.getTransitions();
        List<TestTransition> actualRows = toTestTranstions(transitions);
        List<TestTransition> expectedRows = createOnlyMonkeyWorkflow(false);

        assertEquals(expectedRows, actualRows);
        assertTrue(panel.getProjects().isEmpty());
        assertScreens(transitions, false);

        SelectWorkflowScheme selectWorkflowScheme = workflows.gotoSelectScheme();
        assertEquals(PROJECT_PRIVATE_ID, (long)selectWorkflowScheme.getProjectId());
        assertEquals(WF_SCHEME_MONKEY_NAME, selectWorkflowScheme.getSelectedScheme());

        workflows = selectWorkflowScheme.back(WorkflowsPageTab.class, PROJECT_PRIVATE_KEY);
        EditWorkflowScheme editWorkflowScheme = workflows.gotoEditScheme();
        assertEquals(WF_SCHEME_MONKEY_ID, editWorkflowScheme.getWorflowSchemeId());
    }

    @Test
    public void testProjectWithUniqueWorkflowProjectAdmin()
    {
        WorkflowsPageTab workflows = jira.gotoLoginPage().login("fred", "fred", WorkflowsPageTab.class, PROJECT_PRIVATE_KEY);

        assertEquals(WF_SCHEME_MONKEY_NAME, workflows.getSchemeName());

        // Assert the cog actions aren't present
        assertFalse(workflows.isSchemeLinked());
        assertFalse(workflows.isSchemeChangeAvailable());

        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        assertEquals(1, panels.size());

        WorkflowsPageTab.WorkflowPanel panel = panels.get(0);
        assertEquals(Lists.newArrayList(ISSUE_TYPE_BUG, ISSUE_TYPE_XSS, ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_NEW_FEATURE, ISSUE_TYPE_TASK), panel.getIssueTypes());
        assertEquals(WF_ONLY_MONKEY, panel.getWorkflowName());
        assertFalse(panel.isDefaultWorkflow());

        List<WorkflowsPageTab.WorkflowTransition> transitions = panel.getTransitions();
        List<TestTransition> actualRows = toTestTranstions(transitions);
        List<TestTransition> expectedRows = createOnlyMonkeyWorkflow(true);
        assertEquals(expectedRows, actualRows);
        assertTrue(panel.getProjects().isEmpty());
        assertFalse(panel.hasEditWorkflowLink());
        assertScreens(transitions, true);
    }

    @Test
    public void testProjectWithMultipleWorkflows() throws UnsupportedEncodingException
    {
        WorkflowsPageTab workflows = jira.gotoLoginPage().loginAsSysAdmin(WorkflowsPageTab.class, PROJECT_XSS_KEY);

        assertEquals(WF_SCHEME_XSS_NAME, workflows.getSchemeName());
        assertTrue(workflows.isSchemeLinked());
        assertTrue(workflows.isSchemeChangeAvailable());

        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        assertEquals(2, panels.size());

        //Second panel is the standard JIRA workflow.
        WorkflowsPageTab.WorkflowPanel panel = panels.get(0);
        assertEquals(Lists.newArrayList(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_NEW_FEATURE, ISSUE_TYPE_TASK), panel.getIssueTypes());
        assertEquals(WF_DEFAULT_NAME, panel.getWorkflowName());
        assertTrue(panel.isDefaultWorkflow());
        List<WorkflowsPageTab.WorkflowTransition> transitions = panel.getTransitions();
        assertEquals(createDefaultWorkflow(false), toTestTranstions(transitions));
        assertEquals(Arrays.asList(PROJECT_XSS_NAME, PROJECT_DEFAULT_NAME), panel.getProjects());
        assertScreens(transitions, false);

        assertTrue(panel.hasEditWorkflowLink());
        assertLinkForWorkflow(WF_DEFAULT_NAME, panel.getEditWorkflowLink());

        //First panel is a custom workflow.
        panel = panels.get(1);
        assertEquals(Lists.newArrayList(ISSUE_TYPE_XSS), panel.getIssueTypes());
        assertEquals(WF_XSS_NAME, panel.getWorkflowName());
        assertFalse(panel.isDefaultWorkflow());

        transitions = panel.getTransitions();
        List<TestTransition> actualRows = toTestTranstions(transitions);
        List<TestTransition> expectedRows = createXSSWorkflow(false);

        assertEquals(expectedRows, actualRows);
        assertTrue(panel.getProjects().isEmpty());
        assertScreens(transitions, false);

        assertTrue(panel.hasEditWorkflowLink());
        assertLinkForWorkflow(WF_XSS_NAME, panel.getEditWorkflowLink());

        SelectWorkflowScheme selectWorkflowScheme = workflows.gotoSelectScheme();
        assertEquals(PROJECT_XSS_ID, (long)selectWorkflowScheme.getProjectId());
        assertEquals(WF_SCHEME_XSS_NAME, selectWorkflowScheme.getSelectedScheme());

        workflows = selectWorkflowScheme.back(WorkflowsPageTab.class, PROJECT_XSS_KEY);
        EditWorkflowScheme editWorkflowScheme = workflows.gotoEditScheme();
        assertEquals(WF_SCHEME_XSS_ID, editWorkflowScheme.getWorflowSchemeId());
    }

    @Test
    public void testShowHide()
    {
        WorkflowsPageTab workflows = jira.gotoLoginPage().loginAsSysAdmin(WorkflowsPageTab.class, PROJECT_DEFAULT_KEY);

        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        assertEquals(1, panels.size());

        //Collapse the first panel.
        WorkflowsPageTab.WorkflowPanel panel = panels.get(0);
        assertEquals(WF_DEFAULT_NAME, panel.getWorkflowName());
        assertFalse(panel.isCollapsed());
        assertTrue(panel.toggleCollapsed());

        //Visit the second project. We collapse all panels if there is more than 1
        workflows = jira.visit(WorkflowsPageTab.class, PROJECT_XSS_KEY);
        panels = workflows.getWorkflowPanels();
        assertEquals(2, panels.size());

        panel = panels.get(0);
        assertEquals(WF_DEFAULT_NAME, panel.getWorkflowName());
        assertTrue(panel.isCollapsed());
        //Expand the workflow panel.
        assertFalse(panel.toggleCollapsed());

        panel = panels.get(1);
        assertEquals(WF_XSS_NAME, panel.getWorkflowName());
        assertTrue(panel.isCollapsed());


    }

    private List<TestTransition> createOnlyMonkeyWorkflow(boolean projectAdmin)
    {
        String testScreenUrl = projectAdmin ? null: createScreenLinkForId(10000);

        List<TestTransition> expectedRows = new ArrayList<TestTransition>();
        expectedRows.add(new TestTransition("Open", "Kill", null, null, "Closed"));
        expectedRows.add(new TestTransition("Open", "Attach", "Test Screen", testScreenUrl, "Open"));
        expectedRows.add(new TestTransition("Open", "Progress", "Test Screen", testScreenUrl, "In Progress"));
        expectedRows.add(new TestTransition("Closed"));
        expectedRows.add(new TestTransition("In Progress", "Ping", null, null, "In Progress"));
        return expectedRows;
    }

    private List<TestTransition> createXSSWorkflow(boolean projectAdmin)
    {
        String xssScreenUrl = projectAdmin ? null : createScreenLinkForId(10001);

        List<TestTransition> expectedRows = new ArrayList<TestTransition>();
        expectedRows.add(new TestTransition("Open", "<strong>XSS Transition</strong>", "<strong>XSS Screen</strong>", xssScreenUrl,  "<strong>XSS Status</strong>"));
        expectedRows.add(new TestTransition("<strong>XSS Status</strong>"));
        return expectedRows;
    }

    private List<TestTransition> createDefaultWorkflow(boolean projectAdmin)
    {
        String resolveUrl = projectAdmin ? null : createScreenLinkForId(3);
        String workflowUrl = projectAdmin ? null : createScreenLinkForId(2);

        List<TestTransition> expectedRows = new ArrayList<TestTransition>();
        expectedRows.add(new TestTransition("Open", "Start Progress", null, null, "In Progress"));
        expectedRows.add(new TestTransition("Open", "Resolve Issue", "Resolve Issue Screen", resolveUrl, "Resolved"));
        expectedRows.add(new TestTransition("Open", "Close Issue", "Resolve Issue Screen", resolveUrl, "Closed"));

        expectedRows.add(new TestTransition("In Progress", "Stop Progress", null, null, "Open"));
        expectedRows.add(new TestTransition("In Progress", "Resolve Issue", "Resolve Issue Screen", resolveUrl, "Resolved"));
        expectedRows.add(new TestTransition("In Progress", "Close Issue", "Resolve Issue Screen", resolveUrl, "Closed"));

        expectedRows.add(new TestTransition("Resolved", "Close Issue", "Workflow Screen", workflowUrl, "Closed"));
        expectedRows.add(new TestTransition("Resolved", "Reopen Issue", "Workflow Screen", workflowUrl, "Reopened"));

        expectedRows.add(new TestTransition("Reopened", "Resolve Issue", "Resolve Issue Screen", resolveUrl, "Resolved"));
        expectedRows.add(new TestTransition("Reopened", "Close Issue", "Resolve Issue Screen", resolveUrl, "Closed"));
        expectedRows.add(new TestTransition("Reopened", "Start Progress", null, null, "In Progress"));

        expectedRows.add(new TestTransition("Closed", "Reopen Issue", "Workflow Screen", workflowUrl, "Reopened"));
        return expectedRows;
    }

    private List<TestTransition> toTestTranstions(Collection<? extends WorkflowsPageTab.WorkflowTransition> transitions)
    {
        List<TestTransition> testTransitions = new ArrayList<TestTransition>(transitions.size());
        for (WorkflowsPageTab.WorkflowTransition transition : transitions)
        {
            testTransitions.add(new TestTransition(transition));
        }
        return testTransitions;
    }

    private void assertScreens(Collection<? extends WorkflowsPageTab.WorkflowTransition> transitions, boolean projectAdmin)
    {
        for (WorkflowsPageTab.WorkflowTransition transition : transitions)
        {
            assertEquals(!projectAdmin && transition.hasScreen(), transition.hasScreenLink());
        }
    }

    private void assertLinkForWorkflow(String name, String link) throws UnsupportedEncodingException
    {
        StringBuilder builder = new StringBuilder(jira.getProductInstance().getContextPath());
        builder.append("/secure/admin/workflows/WorkflowDesigner.jspa?workflowMode=live&wfName=");
        builder.append(URLEncoder.encode(name, "UTF-8"));

        assertEquals(builder.toString(), link);
    }

    private String createScreenLinkForId(long id)
    {
        StringBuilder builder = new StringBuilder(jira.getProductInstance().getContextPath());
        builder.append("/secure/admin/ConfigureFieldScreen.jspa?id=");
        builder.append(id);

        return builder.toString();
    }

    private static class TestTransition
    {
        private String sourceStatus;
        private String targetStatus;
        private String transitionName;
        private String screenName;
        private String screenUrl;

        private TestTransition(WorkflowsPageTab.WorkflowTransition transition)
        {
            this.sourceStatus = transition.getSourceStatusName();
            this.targetStatus = transition.getTargetStatusName();
            this.transitionName = transition.getTransitionName();
            if (transition.hasScreen())
            {
                this.screenName = transition.getScreenName();
                this.screenUrl = transition.getScreenLink();
            }
        }

        public TestTransition(String sourceStatus, String transitionName, String screenName, String screenUrl, String targetStatus)
        {
            this.sourceStatus = sourceStatus;
            this.targetStatus = targetStatus;
            this.screenName = screenName;
            this.transitionName = transitionName;
            this.screenUrl = screenUrl;
        }

        public TestTransition(String sourceStatus)
        {
            this(sourceStatus, null, null, null, null);
        }

        public String getScreenName()
        {
            return screenName;
        }

        public TestTransition setScreenName(String screenName)
        {
            this.screenName = screenName;
            return this;
        }

        public String getSourceStatus()
        {
            return sourceStatus;
        }

        public TestTransition setSourceStatus(String sourceStatus)
        {
            this.sourceStatus = sourceStatus;
            return this;
        }

        public String getTargetStatus()
        {
            return targetStatus;
        }

        public TestTransition setTargetStatus(String targetStatus)
        {
            this.targetStatus = targetStatus;
            return this;
        }

        public String getTransitionName()
        {
            return transitionName;
        }

        public TestTransition setTransitionName(String transitionName)
        {
            this.transitionName = transitionName;
            return this;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            TestTransition that = (TestTransition) o;

            if (screenName != null ? !screenName.equals(that.screenName) : that.screenName != null) { return false; }
            if (screenUrl != null ? !screenUrl.equals(that.screenUrl) : that.screenUrl != null) { return false; }
            if (sourceStatus != null ? !sourceStatus.equals(that.sourceStatus) : that.sourceStatus != null)
            {
                return false;
            }
            if (targetStatus != null ? !targetStatus.equals(that.targetStatus) : that.targetStatus != null)
            {
                return false;
            }
            if (transitionName != null ? !transitionName.equals(that.transitionName) : that.transitionName != null)
            { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = sourceStatus != null ? sourceStatus.hashCode() : 0;
            result = 31 * result + (targetStatus != null ? targetStatus.hashCode() : 0);
            result = 31 * result + (transitionName != null ? transitionName.hashCode() : 0);
            result = 31 * result + (screenName != null ? screenName.hashCode() : 0);
            result = 31 * result + (screenUrl != null ? screenUrl.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
