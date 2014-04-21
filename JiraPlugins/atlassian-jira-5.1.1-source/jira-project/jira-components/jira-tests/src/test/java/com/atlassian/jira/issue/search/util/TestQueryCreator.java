/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.web.util.OutlookDate;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;

public class TestQueryCreator extends LegacyJiraMockTestCase
{
    private static final String DEFAULT_SEARCH_STRING = "IssueNavigator.jspa?reset=true&mode=show&summary=true&description=true&body=true";

    public void testNullQuery()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());
        assertEquals("IssueNavigator.jspa?mode=show", queryCreator.createQuery(null));  //JRA-6084
    }

    public void testStandardQuery()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());

        assertStartsWith(DEFAULT_SEARCH_STRING, queryCreator.createQuery(""));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc", queryCreator.createQuery("abc"));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc%26def", queryCreator.createQuery("abc&def"));
    }

    private static void assertStartsWith(String startingString, String wholeString)
    {
        assertTrue(wholeString.startsWith(startingString));
    }

    public void testInternationalQuery()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());

        assertStartsWith(DEFAULT_SEARCH_STRING, queryCreator.createQuery(""));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=h%EF%BF%BDr", queryCreator.createQuery("h" + '\ufffd' + "r"));
    }

    public void testProjectKeyQuery()
    {
        ProjectManager projectManager = getProjectByKeyProjectManager();

        QueryCreator queryCreator = new DefaultQueryCreator(projectManager, getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());

        assertStartsWith(DEFAULT_SEARCH_STRING, queryCreator.createQuery(""));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&pid=77", queryCreator.createQuery("PRJ"));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc&pid=77", queryCreator.createQuery("PRJ abc"));
    }

    private ProjectManager getProjectByKeyProjectManager()
    {
        ProjectManager projectManager = new MockProjectManager()
        {

            public Project getProjectObjByKey(String key)
            {
                if ("PRJ".equals(key))
                    return new MockProject(new MockGenericValue("Project", EasyMap.build("id", new Long(77))));
                else
                    return null;
            }

            public Project getProjectObjByName(String name)
            {
                return null;
            }
            
        };
        return projectManager;
    }

    public void testProjectNameQuery()
    {
        ProjectManager projectManager = new MockProjectManager()
        {
            public Project getProjectObjByKey(String key)
            {
                return null;
            }

            @Override
            public Project getProjectObjByName(String name)
            {
                if ("project-name".equals(name))
                    return new MockProject(new MockGenericValue("Project", EasyMap.build("id", new Long(77))));
                else
                    return null;
            }
        };

        QueryCreator queryCreator = new DefaultQueryCreator(projectManager, getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());

        assertStartsWith(DEFAULT_SEARCH_STRING, queryCreator.createQuery(""));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc&pid=77", queryCreator.createQuery("project-name abc"));
    }

    public void testStatusQuery()
    {
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addStatus(new MockGenericValue("Status", EasyMap.build("id", new Long(77), "name", "open")));

        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), mockConstantsManager, getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());

        assertStartsWith(DEFAULT_SEARCH_STRING + "&status=77", queryCreator.createQuery("open"));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc&status=77", queryCreator.createQuery("open abc"));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc&status=77", queryCreator.createQuery("Open abc")); // case sensitivity
    }

    public void testTypeQuery()
    {
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", new Long(89), "name", "bug")));

        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), mockConstantsManager, getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());

        assertStartsWith(DEFAULT_SEARCH_STRING + "&" + DocumentConstants.ISSUE_TYPE + "=89", queryCreator.createQuery("bug"));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc&" + DocumentConstants.ISSUE_TYPE + "=89", queryCreator.createQuery("bug abc"));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc&" + DocumentConstants.ISSUE_TYPE + "=89", queryCreator.createQuery("Bugs abc")); // plurals
    }

    public void testMyIssues()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());
        assertStartsWith(DEFAULT_SEARCH_STRING + "&assigneeSelect=issue_current_user", queryCreator.createQuery("my"));
        assertStartsWith(DEFAULT_SEARCH_STRING + "&query=abc&assigneeSelect=issue_current_user", queryCreator.createQuery("my abc"));
    }

    public void testProjectAndStatusAndTypeQuery()
    {
        ProjectManager projectManager = getProjectByKeyProjectManager();

        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addStatus(new MockGenericValue("Status", EasyMap.build("id", new Long(77), "name", "open")));
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", new Long(89), "name", "bug")));

        QueryCreator queryCreator = new DefaultQueryCreator(projectManager, mockConstantsManager, getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager());

        String query = queryCreator.createQuery("PRJ open bug abc");
        assertStartsWith(DEFAULT_SEARCH_STRING, query);
        // The query's parameters order depends on the JDK that is used. So test their presence as order does not really matter.
        assertTrue(query.indexOf("query=abc") > -1);
        assertTrue(query.indexOf(DocumentConstants.ISSUE_TYPE + "=89") > -1);
        assertTrue(query.indexOf("pid=77") > -1);
        assertTrue(query.indexOf("status=77") > -1);
    }

    private DateTimeFormatterFactory getDateTimeFormatterFactory()
    {
        Mock dateTimeFormatterFactory = new Mock(DateTimeFormatterFactory.class);
        return (DateTimeFormatterFactory) dateTimeFormatterFactory.proxy();
    }
    
    private TimeZoneManager getTimeZoneManager()
    {
        Mock timeZoneManager = new Mock(TimeZoneManager.class);
        return (TimeZoneManager) timeZoneManager.proxy();
    }

    private ConstantsManager getNullConstantsManager()
    {
        return new MockConstantsManager();
    }

    private VersionManager getNullVersionManager()
    {
        Mock versionManagerMock = new Mock(VersionManager.class);
        versionManagerMock.expectAndReturn("getVersions", P.ANY_ARGS, Collections.EMPTY_LIST);
        return (VersionManager) versionManagerMock.proxy();
    }

    private ProjectManager getNullProjectManager()
    {
        Mock projectManagerMock = new Mock(ProjectManager.class);
        projectManagerMock.expectAndReturn("getProjectByKey", P.ANY_ARGS, null);
        projectManagerMock.expectAndReturn("getProjectByName", P.ANY_ARGS, null);

        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();
        return projectManager;
    }

    private ProjectComponentManager getNullProjectComponentManager()
    {
        Mock projectComponentManagerMock = new Mock(ProjectComponentManager.class);
        projectComponentManagerMock.expectAndReturn("findAllForProject", P.ANY_ARGS, null);

        return (ProjectComponentManager) projectComponentManagerMock.proxy();
    }

    private ApplicationProperties getApplicationProperties()
    {
        MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setEncoding("UTF-8");
        return applicationProperties;
    }

    private PermissionManager getNullPermissionsManager()
    {
        PermissionManager permissionManager = new MockPermissionManager()
        {
            public boolean hasPermission(int permissionsId, Project entity, com.atlassian.crowd.embedded.api.User u)
            {
                return true;
            }
        };
        return permissionManager;
    }

    private JiraAuthenticationContext getAuthenticationContext()
    {
        return new MockSimpleAuthenticationContext(null) {
            @Override
            public OutlookDate getOutlookDate()
            {
                return null;
            }
        };
    }
}
