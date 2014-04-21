package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestPermissionsSummaryPanelContextProvider
{
    private static final String PERMISSION_SCHEME_URL = "something in the way she moves";
    private static final String ISSUE_SECURITY_SCHEME_URL = "forIssueSecurity";

    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    private ContextProviderUtils providerUtils;
    private IMocksControl control;
    private GenericValue issueSecuritySchemeGV;
    private GenericValue projectPermissionsSchemeGV;
    private PermissionsSummaryPanelContextProvider.SimpleIssueSecurityScheme simpleIssueSecurityScheme;
    private TabUrlFactory tabUrlFactory;
    private PermissionsSummaryPanelContextProvider.SimpleProjectPermissionsScheme simpleProjectPermissionsScheme;
    private Project project;

    @Before
    public void setUp() throws Exception
    {
        project = new MockProject(678L, "ABC");
        control = createControl();
        providerUtils = control.createMock(ContextProviderUtils.class);
        issueSecuritySchemeManager = control.createMock(IssueSecuritySchemeManager.class);
        issueSecuritySchemeGV = control.createMock(GenericValue.class);
        projectPermissionsSchemeGV = control.createMock(GenericValue.class);
        simpleIssueSecurityScheme = control.createMock(PermissionsSummaryPanelContextProvider.SimpleIssueSecurityScheme.class);
        simpleProjectPermissionsScheme = control.createMock(PermissionsSummaryPanelContextProvider.SimpleProjectPermissionsScheme.class);
        tabUrlFactory = control.createMock(TabUrlFactory.class);
    }

    @Test
    public void testAdminNoSchemes()
    {
        EasyMock.expect(providerUtils.getProject()).andReturn(project).anyTimes();
        EasyMock.expect(providerUtils.hasAdminPermission()).andReturn(true).anyTimes();

        EasyMock.expect(tabUrlFactory.forIssueSecurity()).andStubReturn(ISSUE_SECURITY_SCHEME_URL);
        EasyMock.expect(tabUrlFactory.forPermissions()).andStubReturn(PERMISSION_SCHEME_URL);

        control.replay();

        PermissionsSummaryPanelContextProvider provider = getProvider(null, null);

        Map<String, Object> paramMap = MapBuilder.<String, Object>build("param", true);
        Map<String, Object> actualMap = provider.getContextMap(paramMap);

        MapBuilder<String, Object> expectedMap = MapBuilder.newBuilder(paramMap)
                .add("issueSecuritySchemeUrl", ISSUE_SECURITY_SCHEME_URL)
                .add("projectPermissionUrl", PERMISSION_SCHEME_URL);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    @Test
    public void testAdminWithProjectPermissionScheme()
    {

        PermissionsSummaryPanelContextProvider provider = getProvider(null, projectPermissionsSchemeGV);

        EasyMock.expect(providerUtils.getProject()).andReturn(project).anyTimes();
        EasyMock.expect(providerUtils.hasAdminPermission()).andReturn(true).anyTimes();
        EasyMock.expect(simpleProjectPermissionsScheme.getId()).andReturn("12312").anyTimes();

        EasyMock.expect(tabUrlFactory.forIssueSecurity()).andStubReturn(ISSUE_SECURITY_SCHEME_URL);
        EasyMock.expect(tabUrlFactory.forPermissions()).andReturn(PERMISSION_SCHEME_URL);

        control.replay();

        Map<String, Object> paramMap = MapBuilder.<String, Object>build("param", true);
        Map<String, Object> actualMap = provider.getContextMap(paramMap);

        MapBuilder<String, Object> expectedMap = MapBuilder.newBuilder(paramMap)
                .add("issueSecuritySchemeUrl", ISSUE_SECURITY_SCHEME_URL)
                .add("projectPermissionsScheme", simpleProjectPermissionsScheme)
                .add("projectPermissionUrl", PERMISSION_SCHEME_URL);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }


    @Test
    public void testAdminWithProjectPermissionAndIssueScheme()
    {

        PermissionsSummaryPanelContextProvider provider = getProvider(issueSecuritySchemeGV, projectPermissionsSchemeGV);

        EasyMock.expect(providerUtils.getProject()).andReturn(project).anyTimes();
        EasyMock.expect(providerUtils.hasAdminPermission()).andReturn(true).anyTimes();
        EasyMock.expect(simpleProjectPermissionsScheme.getId()).andReturn("12312").anyTimes();
        EasyMock.expect(simpleIssueSecurityScheme.getId()).andReturn("5555").anyTimes();
        EasyMock.expect(tabUrlFactory.forIssueSecurity()).andStubReturn(ISSUE_SECURITY_SCHEME_URL);
        EasyMock.expect(tabUrlFactory.forPermissions()).andReturn(PERMISSION_SCHEME_URL);

        control.replay();

        Map<String, Object> paramMap = MapBuilder.<String, Object>build("param", true);
        Map<String, Object> actualMap = provider.getContextMap(paramMap);

        MapBuilder<String, Object> expectedMap = MapBuilder.newBuilder(paramMap)
                .add("issueSecurityScheme", simpleIssueSecurityScheme)
                .add("issueSecuritySchemeUrl", ISSUE_SECURITY_SCHEME_URL)
                .add("projectPermissionsScheme", simpleProjectPermissionsScheme)
                .add("projectPermissionUrl", PERMISSION_SCHEME_URL);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }


    private PermissionsSummaryPanelContextProvider getProvider(final GenericValue issueSecurityScheme, final GenericValue projectPermissionsScheme)
    {
        return new PermissionsSummaryPanelContextProvider(null, issueSecuritySchemeManager, providerUtils, tabUrlFactory)
        {

            @Override
            SimpleIssueSecurityScheme gvToIssueSecurityScheme(GenericValue issueSecurityGV)
            {
                return simpleIssueSecurityScheme;
            }

            @Override
            SimpleProjectPermissionsScheme gvToProjectPermissionsScheme(GenericValue projectPermssionsGV)
            {
                return simpleProjectPermissionsScheme;
            }

            @Override
            GenericValue getProjectPermissionsScheme(GenericValue projectGV)
            {
                return projectPermissionsScheme;
            }

            @Override
            GenericValue getIssueSecuritySchemes(GenericValue projectGV)
            {
                return issueSecurityScheme;
            }

        };
    }
}
