package com.atlassian.jira.util;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.notification.type.TypeForTesting;
import com.atlassian.jira.notification.type.TypeForTesting2;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.Lists;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 *
 */
public class TestJiraUtils extends LegacyJiraMockTestCase
{
    private ApplicationProperties oldApplicationProperties;

    protected void setUp() throws Exception
    {
        super.setUp();
        oldApplicationProperties = ComponentAccessor.getApplicationProperties();
    }

    protected void tearDown() throws Exception
    {
        //return the application properties to the ManagerFactory.
        ManagerFactory.addService(ApplicationProperties.class, oldApplicationProperties);
        super.tearDown();
    }

    public void testGet24HourTime()
    {
        assertEquals(0, JiraUtils.get24HourTime("AM", 12));
        assertEquals(0, JiraUtils.get24HourTime("am", 12));
        assertEquals(12, JiraUtils.get24HourTime("PM", 12));
        assertEquals(12, JiraUtils.get24HourTime("pm", 12));
        assertEquals(11, JiraUtils.get24HourTime("AM", 11));
        assertEquals(1, JiraUtils.get24HourTime("AM", 1));
        assertEquals(17, JiraUtils.get24HourTime("PM", 5));
        assertEquals(23, JiraUtils.get24HourTime("PM", 11));
        assertEquals(23, JiraUtils.get24HourTime("pm", 11));
    }

    public void testLoadTypes()
    {
        Map types = JiraTypeUtils.loadTypes("test-notification-event-types.xml", this.getClass());
        assertEquals(2, types.size());
        assertTrue(types.get("TEST_TYPE_1") instanceof TypeForTesting);
        assertTrue(types.get("TEST_TYPE_2") instanceof TypeForTesting2);
    }

    public void testGetProject()
    {
        MockProjectManager projectManager = new MockProjectManager();
        MockIssueManager issueManager = new MockIssueManager();
        ManagerFactory.addService(ProjectManager.class, projectManager);
        ManagerFactory.addService(IssueManager.class, issueManager);

        assertNull(JiraEntityUtils.getProject(null));

        GenericValue uhuh = new MockGenericValue("Priority", EasyMap.build("id", "foo"));
        assertNull(JiraEntityUtils.getProject(uhuh));

        GenericValue project = new MockGenericValue("Project", EasyMap.build("id", new Long(1)));
        GenericValue project2 = new MockGenericValue("Project", EasyMap.build("id", new Long(2)));
        projectManager.addProject(project);
        projectManager.addProject(project2);
        assertEquals(project, JiraEntityUtils.getProject(project));

        GenericValue issue = new MockGenericValue("Issue", EasyMap.build("id", new Long(2), "project", new Long(1)));
        issueManager.addIssue(issue);
        assertEquals(project, JiraEntityUtils.getProject(issue));

        GenericValue action = new MockGenericValue("Action", EasyMap.build("id", new Long(3), "issue", new Long(2)));
        assertEquals(project, JiraEntityUtils.getProject(action));
    }


    public void testIsPublicMode()
    {
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(mockComponentWorker);
        MockApplicationProperties ap = new MockApplicationProperties();
        mockComponentWorker.registerMock(ApplicationProperties.class, ap);
        final MockUserManager mockUserManager = new MockUserManager();
        mockComponentWorker.registerMock(UserManager.class, mockUserManager);

        assertTrue(JiraUtils.isPublicMode());

        ap.setString(APKeys.JIRA_MODE, "public");
        assertTrue(JiraUtils.isPublicMode());

        ap.setString(APKeys.JIRA_MODE, "private");
        assertFalse(JiraUtils.isPublicMode());
    }


    public void testCreateEntityMap()
    {
        GenericValue gv = new MockGenericValue("Project", EasyMap.build("id", new Long(1), "name", "foo"));
        GenericValue gv2 = new MockGenericValue("Project", EasyMap.build("id", new Long(2), "name", "bar"));

        Map result = JiraEntityUtils.createEntityMap(Lists.newArrayList(gv, gv2), "id", "name");
        Map expectedResult = new ListOrderedMap();
        expectedResult.put(new Long(1), "foo");
        expectedResult.put(new Long(2), "bar");

        assertEquals(expectedResult, result);
    }
}
