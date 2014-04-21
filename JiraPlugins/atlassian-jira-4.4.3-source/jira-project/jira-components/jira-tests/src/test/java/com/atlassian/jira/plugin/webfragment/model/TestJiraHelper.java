package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;

import javax.servlet.http.HttpServletRequest;

public class TestJiraHelper extends ListeningTestCase
{

    @Test
    public void testDefaultConstructor() throws Exception
    {
        final JiraHelper helper = new JiraHelper();

        assertNull(helper.getRequest());
        assertNull(helper.getProjectObject());
        assertNull(helper.getProject());
    }

    @Test
    public void testConstructorWithRequest() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        final JiraHelper helper = new JiraHelper(request);

        assertEquals(request, helper.getRequest());
        assertNull(helper.getProjectObject());
        assertNull(helper.getProject());
    }

    @Test
    public void testConstructorWithRequestAndProject() throws Exception
    {
        final MockGenericValue projectGV = new MockGenericValue("project", EasyMap.build("id", new Long(123)));
        final MockProject project = new MockProject(new Long(123), "JAVA", "Java Rulez", projectGV);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final JiraHelper helper = new JiraHelper(request, project);

        assertEquals(request, helper.getRequest());
        assertEquals(project, helper.getProjectObject());
        assertEquals(projectGV, helper.getProject());
    }

    @Test
    public void testConstructorWithRequestAndProjectWithGV() throws Exception
    {
        MockGenericValue projectGV = new MockGenericValue("project");
        MockProject project = new MockProject(new Long(123), "JAVA", "Java Rulez", projectGV);
        MockHttpServletRequest request = new MockHttpServletRequest();
        final JiraHelper helper = new JiraHelper(request, project);

        assertEquals(request, helper.getRequest());
        assertEquals(project, helper.getProjectObject());
        assertEquals(projectGV, helper.getProject());
    }

    @Test
    public void testConstructorWithRequestAndProjectGV() throws Exception
    {
        MockGenericValue projectGV = new MockGenericValue("project", EasyMap.build("id", new Long(123)));
        final MockProject project = new MockProject(new Long(123), "JAVA", "Java Rulez", projectGV);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        class MyHelper extends JiraHelper
        {
            public MyHelper(HttpServletRequest request, GenericValue project)
            {
                super(request, project);
            }

            ProjectManager getProjectManager()
            {
                return new MockProjectManager()
                {
                    public Project getProjectObj(Long id)
                    {
                        assertEquals(new Long(123), id);
                        return project;
                    }
                };
            }
        }
        JiraHelper helper = new MyHelper(request, projectGV);

        assertEquals(request, helper.getRequest());
        assertEquals(project, helper.getProjectObject());
        assertEquals(projectGV, helper.getProject());
    }

    @Test
    public void testConstructorWithRequestAndNullGV() throws Exception
    {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final JiraHelper helper = new JiraHelper(request, (GenericValue) null);

        assertEquals(request, helper.getRequest());
        assertNull(helper.getProjectObject());
        assertNull(helper.getProject());
    }

}
