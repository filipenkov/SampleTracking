package com.atlassian.jira.bc.project;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.TestJiraKeyUtils;

import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class TestDefaultProjectServiceJiraMock extends LegacyJiraMockTestCase
{
    JiraAuthenticationContext jiraAuthenticationContext;
    ProjectManager projectManager;
    ApplicationProperties applicationProperties;

    private static final String TEST_USER = "test user";
    private static final Long ASSIGNEETYPE_PROJECTLEAD = AssigneeTypes.PROJECT_LEAD;

    private static String ORIGINAL_PROJECTKEY_PATTERN;

    protected void setUp() throws Exception
    {
        super.setUp();
        if (jiraAuthenticationContext == null)
        {
            jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
            ORIGINAL_PROJECTKEY_PATTERN = ComponentAccessor.getApplicationProperties()
                    .getDefaultBackedString(APKeys.JIRA_PROJECTKEY_PATTERN);
        }

        projectManager = new MockProjectManager();
        applicationProperties = new MockApplicationProperties();

        //create our test user if she's not here already
        if (!UserUtils.existsUser(TEST_USER))
        {
            UtilsForTests.getTestUser(TEST_USER);
        }
    }

    protected void tearDown() throws Exception
    {
        restoreProjectKeyRegex();
    }

    public void testIsValidRequiredProjectDataHappyPath()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        assertTrue(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "PRJ", TEST_USER));
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testIsValidRequiredProjectDataNoName()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        //empty String
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "", "PRJ", TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectName", errorMapping.getKey());
            assertEquals("You must specify a valid project name.", errorMapping.getValue());
        }

        //null String
        errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), null, "PRJ", TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectName", errorMapping.getKey());
            assertEquals("You must specify a valid project name.", errorMapping.getValue());
        }
    }

    public void testIsValidRequiredProjectDataDuplicateName()
    {
        //mock out a ProjectManager that will return a duplicate project
        ProjectManager projectManager = new MockProjectManager()
        {
            public Project getProjectObjByName(String projectName)
            {
                if ("project".equals(projectName))
                {
                    return new MockProject();
                }
                return null;
            }
        };

        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "PRJ", TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectName", errorMapping.getKey());
            assertEquals("A project with that name already exists.", errorMapping.getValue());
        }
    }

    public void testIsValidRequiredProjectDataNoKey()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        //empty String
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "", TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectKey", errorMapping.getKey());
            assertEquals("You must specify a unique project key, at least 2 characters long, containing only uppercase letters.", errorMapping.getValue());
        }

        //null String
        errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", null, TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectKey", errorMapping.getKey());
            assertEquals("You must specify a unique project key, at least 2 characters long, containing only uppercase letters.", errorMapping.getValue());
        }
    }

    public void testIsValidRequiredProjectDataInvalidKeyDefaultPattern()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "!NV4L!D K3Y", TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectKey", errorMapping.getKey());
            assertEquals("You must specify a unique project key, at least 2 characters long, containing only uppercase letters.", errorMapping.getValue());
        }
    }

    public void testIsValidRequiredProjectDataInvalidKeyCustomPattern()
    {
        //mock out an ApplicationProperties that returns a String representation of a JIRA_PROJECTKEY_WARNING message
        ApplicationProperties applicationProperties = new MockApplicationProperties()
        {
            public String getDefaultBackedString(String name)
            {
                if (APKeys.JIRA_PROJECTKEY_WARNING.equals(name))
                {
                    return "Test Project Key Regex Warning Message";
                }
                return null;
            }
        };

        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            boolean isProjectKeyValid(String key)
            {
                return false;
            }
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "AA", TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("Test Project Key Regex Warning Message", errorCollection.getErrors().get("projectKey"));
    }

    public void testIsValidRequiredProjectDataInvalidKeyReservedWord()
    {
        //JiraKeyUtils.isReservedKeyword(String) will only ever return true on a Windows system, so we should mimic one
        String originalOsNameProperty = System.getProperty("os.name");
        System.setProperty("os.name", "windows");

        try {
            ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

            //CON is a reserved word (not allowed as a project key since you cannot use it as a directory name)
            ErrorCollection errorCollection = new SimpleErrorCollection();
            assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "CON", TEST_USER));
            assertTrue(errorCollection.hasAnyErrors());
            assertEquals(1, errorCollection.getErrors().size());
            assertEquals("This keyword is invalid as it is a reserved word on this operating system.", errorCollection.getErrors().get("projectKey"));
        }
        finally
        {
            System.setProperty("os.name", originalOsNameProperty);
        }
    }

    public void testIsValidRequiredProjectDataDuplicateKey()
    {
        //mock out a ProjectManager that will return a duplicate project
        ProjectManager projectManager = new MockProjectManager()
        {
            public Project getProjectObjByKey(String projectKey)
            {
                if ("PRJ".equals(projectKey))
                {
                    return new MockProject();
                }
                return null;
            }
        };

        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "PRJ", TEST_USER));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectKey", errorMapping.getKey());
            assertEquals("A project with that project key already exists.", errorMapping.getValue());
        }
    }

    public void testIsValidRequiredProjectDataNoLead()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        //empty String
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "PRJ", ""));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectLead", errorMapping.getKey());
            assertEquals("You must specify a project lead.", errorMapping.getValue());
        }

        //null String
        errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "PRJ", null));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectLead", errorMapping.getKey());
            assertEquals("You must specify a project lead.", errorMapping.getValue());
        }
    }

    public void testIsValidRequiredProjectDataNonExistentLead()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidRequiredProjectData(constructServiceContext(errorCollection), "project", "PRJ", "stranger"));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectLead", errorMapping.getKey());
            assertEquals("The user you have specified as project lead does not exist.", errorMapping.getValue());
        }
    }

    public void testIsValidAllProjectDataHappyPath()
    {
        //mock out project manager to make 'required data' automatically pass (to unit test optional fields)
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            public boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String lead)
            {
                return true;
            }
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertTrue(projectService.isValidAllProjectData(constructServiceContext(errorCollection), null, null, null, "http://www.example.com", ASSIGNEETYPE_PROJECTLEAD));
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testIsValidAllProjectDataHappyPathNonRequiredFieldsLeftBlank()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            public boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String lead)
            {
                return true;
            }
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertTrue(projectService.isValidAllProjectData(constructServiceContext(errorCollection), null, null, null, null, null));
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testIsValidAllProjectDataInvalidURL()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            public boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String lead)
            {
                return true;
            }
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidAllProjectData(constructServiceContext(errorCollection), null, null, null, "not - a - valid - url", null));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        for (Iterator iterator = errorCollection.getErrors().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry errorMapping = (Map.Entry) iterator.next();
            assertEquals("projectUrl", errorMapping.getKey());
            assertEquals("The URL specified is not valid - it must start with http://", errorMapping.getValue());
        }
    }

    public void testIsValidAllProjectDataInvalidAssigneeType()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            public boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String lead)
            {
                return true;
            }
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidAllProjectData(constructServiceContext(errorCollection), null, null, null, null, new Long(500)));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        for (Iterator iterator = errorCollection.getErrorMessages().iterator(); iterator.hasNext();)
        {
            String msg = (String) iterator.next();
            assertEquals("Invalid default Assignee.", msg);
        }
    }

    public void testIsValidAllProjectDataInvalidRequiredData()
    {
        ProjectService projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            public boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String lead)
            {
                serviceContext.getErrorCollection().addErrorMessage("Test Error Message");
                return false;
            }
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(projectService.isValidAllProjectData(constructServiceContext(errorCollection), null, null, null, null, null));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        for (Iterator iterator = errorCollection.getErrorMessages().iterator(); iterator.hasNext();)
        {
            String msg = (String) iterator.next();
            assertEquals("Test Error Message", msg);
        }
    }

    private JiraServiceContext constructServiceContext(ErrorCollection errorCollection)
    {
        return new JiraServiceContextImpl(null, errorCollection);
    }

    /**
     * Sets the project key pattern application property (necessary as we can't override JiraKeyUtils' static methods)
     *
     * @param regex
     */
    private void setProjectKeyRegex(String regex)
    {
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_PROJECTKEY_PATTERN, regex);
        TestJiraKeyUtils.refreshKeyMatcherOnUtilClass();
    }

    /**
     * Cleanup method that restores the project key pattern application property that was in place when this test was instantiated
     */
    private void restoreProjectKeyRegex()
    {
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_PROJECTKEY_PATTERN, ORIGINAL_PROJECTKEY_PATTERN);
        TestJiraKeyUtils.refreshKeyMatcherOnUtilClass();
    }
}
