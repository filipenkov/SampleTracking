package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.MapBuilder;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link PeopleSummaryPanelContextProvider}
 *
 * @since v4.4
 */
public class TestPeopleSummaryPanelContextProvider
{
    private static final String LEAD_USERNAME = "username";
    private static final String ASSIGNEE_PRETTY_STRING = "assigneePrettyString";
    private static final String FORMATTED_PROJECT_LEAD = "formattedProjectLead";
    private static final String MOCK_URI_STRING = "http://www.example.com";

    private PermissionManager permissionManager;
    private IMocksControl mockControl;
    private Map<String,Object> testContext;
    private Project project;
    private User user;
    private UserFormatManager userFormatManager;
    private UserManager userManager;
    private AvatarService avatarService;
    private URI uri;


    @Before
    public void setUp() throws URISyntaxException
    {
        mockControl = createControl();
        permissionManager = mockControl.createMock(PermissionManager.class);
        userFormatManager = mockControl.createMock(UserFormatManager.class);
        userManager = mockControl.createMock(UserManager.class);
        project = mockControl.createMock(Project.class);
        user = mockControl.createMock(User.class);
        avatarService = mockControl.createMock(AvatarService.class);
        uri = new URI(MOCK_URI_STRING);
        testContext = MapBuilder.<String, Object>build(
                ContextProviderUtils.CONTEXT_PROJECT_KEY, project
        );
        MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
        componentAccessorWorker.registerMock(ApplicationProperties.class, mockApplicationProperties);
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
    }

    @After
    public void tearDown()
    {
        mockControl = null;
        permissionManager = null;
        userFormatManager = null;
        userManager = null;
        project = null;
        user = null;
        avatarService = null;
        uri = null;
        testContext = null;
    }

    @Test
    public void testGetContextMapWhereNoLeadExists() throws Exception
    {

        final PeopleSummaryPanelContextProvider contextProviderUnderTest =
                new PeopleSummaryPanelContextProvider(permissionManager,
                userFormatManager, userManager, avatarService)
        {
            String getPrettyAssigneeTypeString(Long assigneeType)
            {
                assertEquals(assigneeType, Long.valueOf(ProjectAssigneeTypes.PROJECT_LEAD));
                return ASSIGNEE_PRETTY_STRING;
            }
        };
        expect(userManager.getUserObject(eq(LEAD_USERNAME))).andReturn(null).atLeastOnce();
        expect(project.getAssigneeType()).andReturn(ProjectAssigneeTypes.PROJECT_LEAD).atLeastOnce();
        expect(project.getLeadUserName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(avatarService.isUserAvatarsEnabled()).andReturn(false);

        mockControl.replay();

        final Map<String, Object> contextMap = contextProviderUnderTest.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_EXISTS_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_KEY, LEAD_USERNAME)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_KEY, ASSIGNEE_PRETTY_STRING)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_USER_AVATARS_ENABLED_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_LEAD_USER_AVATAR_URL_KEY, null)
                .toMap();
        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWhereLeadExistsAndLeadIsNotAssignable() throws Exception
    {

        final PeopleSummaryPanelContextProvider contextProviderUnderTest =
                new PeopleSummaryPanelContextProvider(permissionManager, userFormatManager, userManager, avatarService)
        {
            @Override
            String getPrettyAssigneeTypeString(Long assigneeType)
            {
                assertEquals(assigneeType, Long.valueOf(ProjectAssigneeTypes.PROJECT_LEAD));
                return ASSIGNEE_PRETTY_STRING;
            }
        };
        expect(project.getLeadUserName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(userManager.getUserObject(eq(LEAD_USERNAME))).andReturn(user).atLeastOnce();
        expect(user.getName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(project.getAssigneeType()).andReturn(ProjectAssigneeTypes.PROJECT_LEAD).atLeastOnce();
        expect(userFormatManager.formatUser(eq(LEAD_USERNAME), eq("profileLink"), eq("projectLead")))
                .andReturn(FORMATTED_PROJECT_LEAD);
        expect(permissionManager.hasPermission(eq(Permissions.ASSIGNABLE_USER), eq(project), eq(user))).andReturn(false);
        expect(avatarService.isUserAvatarsEnabled()).andReturn(false);

        mockControl.replay();

        final Map<String, Object> contextMap = contextProviderUnderTest.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_EXISTS_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_KEY, FORMATTED_PROJECT_LEAD)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_KEY, ASSIGNEE_PRETTY_STRING)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_USER_AVATARS_ENABLED_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_LEAD_USER_AVATAR_URL_KEY, null)
                .toMap();
        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWhereLeadExistsAndLeadIsAssignable() throws Exception
    {

        final PeopleSummaryPanelContextProvider contextProviderUnderTest =
                new PeopleSummaryPanelContextProvider(permissionManager, userFormatManager, userManager, avatarService)
        {
            @Override
            String getPrettyAssigneeTypeString(Long assigneeType)
            {
                assertEquals(assigneeType, Long.valueOf(ProjectAssigneeTypes.PROJECT_LEAD));
                return ASSIGNEE_PRETTY_STRING;
            }
        };
        expect(project.getLeadUserName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(userManager.getUserObject(eq(LEAD_USERNAME))).andReturn(user).atLeastOnce();
        expect(user.getName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(project.getAssigneeType()).andReturn(ProjectAssigneeTypes.PROJECT_LEAD).atLeastOnce();
        expect(userFormatManager.formatUser(eq(LEAD_USERNAME), eq("profileLink"), eq("projectLead")))
                .andReturn(FORMATTED_PROJECT_LEAD);
        expect(permissionManager.hasPermission(eq(Permissions.ASSIGNABLE_USER), eq(project), eq(user))).andReturn(true);
        expect(avatarService.isUserAvatarsEnabled()).andReturn(false);

        mockControl.replay();

        final Map<String, Object> contextMap = contextProviderUnderTest.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_EXISTS_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_KEY, FORMATTED_PROJECT_LEAD)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_KEY, ASSIGNEE_PRETTY_STRING)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_USER_AVATARS_ENABLED_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_LEAD_USER_AVATAR_URL_KEY, null)
                .toMap();
        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWhereAssigneeTypeIsNull() throws Exception
    {

        final PeopleSummaryPanelContextProvider contextProviderUnderTest =
                new PeopleSummaryPanelContextProvider(permissionManager, userFormatManager, userManager, avatarService)
        {
            @Override
            String getPrettyAssigneeTypeString(Long assigneeType)
            {
                assertEquals(assigneeType, null);
                return ASSIGNEE_PRETTY_STRING;
            }
        };
        expect(project.getLeadUserName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(userManager.getUserObject(eq(LEAD_USERNAME))).andReturn(user).atLeastOnce();
        expect(user.getName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(project.getAssigneeType()).andReturn(null).atLeastOnce();
        expect(userFormatManager.formatUser(eq(LEAD_USERNAME), eq("profileLink"), eq("projectLead")))
                .andReturn(FORMATTED_PROJECT_LEAD);
        expect(avatarService.isUserAvatarsEnabled()).andReturn(false);

        mockControl.replay();

        final Map<String, Object> contextMap = contextProviderUnderTest.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_EXISTS_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_KEY, FORMATTED_PROJECT_LEAD)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_KEY, ASSIGNEE_PRETTY_STRING)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_USER_AVATARS_ENABLED_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_LEAD_USER_AVATAR_URL_KEY, null)
                .toMap();
        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWhereUserIsDeletedButAssignableDueToLDAP() throws Exception
    {

        final PeopleSummaryPanelContextProvider contextProviderUnderTest =
                new PeopleSummaryPanelContextProvider(permissionManager, userFormatManager, userManager, avatarService)
        {
            @Override
            String getPrettyAssigneeTypeString(Long assigneeType)
            {
                assertEquals(assigneeType, null);
                return ASSIGNEE_PRETTY_STRING;
            }
        };
        expect(project.getLeadUserName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(userManager.getUserObject(eq(LEAD_USERNAME))).andReturn(null).atLeastOnce();
        expect(project.getAssigneeType()).andReturn(null).atLeastOnce();
        expect(avatarService.isUserAvatarsEnabled()).andReturn(false);

        mockControl.replay();

        final Map<String, Object> contextMap = contextProviderUnderTest.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_EXISTS_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_KEY, LEAD_USERNAME)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_KEY, ASSIGNEE_PRETTY_STRING)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_USER_AVATARS_ENABLED_KEY, false)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_LEAD_USER_AVATAR_URL_KEY, null)
                .toMap();
        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWhereUserAvatarIsEnabled() throws Exception
    {

        final PeopleSummaryPanelContextProvider contextProviderUnderTest =
                new PeopleSummaryPanelContextProvider(permissionManager, userFormatManager, userManager, avatarService)
        {
            @Override
            String getPrettyAssigneeTypeString(Long assigneeType)
            {
                assertEquals(assigneeType, null);
                return ASSIGNEE_PRETTY_STRING;
            }
        };
        expect(project.getLeadUserName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(userManager.getUserObject(eq(LEAD_USERNAME))).andReturn(user).atLeastOnce();
        expect(user.getName()).andReturn(LEAD_USERNAME).atLeastOnce();
        expect(project.getAssigneeType()).andReturn(null).atLeastOnce();
        expect(userFormatManager.formatUser(eq(LEAD_USERNAME), eq("profileLink"), eq("projectLead")))
                .andReturn(FORMATTED_PROJECT_LEAD);
        expect(avatarService.isUserAvatarsEnabled()).andReturn(true);
        expect(avatarService.getAvatarURL(eq(user), eq(LEAD_USERNAME), eq(Avatar.Size.SMALL)))
                .andReturn(uri);

        mockControl.replay();

        final Map<String, Object> contextMap = contextProviderUnderTest.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_EXISTS_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_PROJECT_LEAD_KEY, FORMATTED_PROJECT_LEAD)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_KEY, ASSIGNEE_PRETTY_STRING)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_IS_USER_AVATARS_ENABLED_KEY, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, true)
                .add(PeopleSummaryPanelContextProvider.CONTEXT_LEAD_USER_AVATAR_URL_KEY, MOCK_URI_STRING)
                .toMap();
        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();

    }

}
