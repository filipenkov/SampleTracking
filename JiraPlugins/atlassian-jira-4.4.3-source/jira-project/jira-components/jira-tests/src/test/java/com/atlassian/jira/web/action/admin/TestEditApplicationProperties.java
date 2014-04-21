package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.local.runner.ListeningPowerMockRunner;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ComponentManagerMocker;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.util.I18nHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v4.4
 */
@PrepareForTest( {ComponentManager.class} )
@PowerMockIgnore("org.apache.log4j.*")
@RunWith (ListeningPowerMockRunner.class)
public class TestEditApplicationProperties extends MockControllerTestCase
{
    UserPickerSearchService userPickerSearchService;
    ReindexMessageManager reindexMessageManager;
    LocaleManager localeManager;
    TimeZoneService timeZoneService;
    ComponentManagerMocker mocker;
    ProjectManager projectManager;
    PermissionManager permissionManager;
    JiraAuthenticationContext authenticationContext;
    I18nHelper helper;

    EditApplicationProperties editApplicationProperties;
    RendererManager rendererManager;

    @BeforeClass
    public static void setUpMultiTenantContext()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Before
    public void setUp()
    {
        mocker = new ComponentManagerMocker();
        mocker.doMock();

        projectManager = mockController.getMock(ProjectManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        authenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        helper = mockController.getMock(I18nHelper.class);
        mocker.addComponent(ProjectManager.class, projectManager);
        mocker.addComponent(PermissionManager.class, permissionManager);
        mocker.addComponent(JiraAuthenticationContext.class, authenticationContext);
        expect(authenticationContext.getI18nHelper()).andReturn(helper);
        expect(helper.getText(this.<String>anyObject()))
                .andReturn("owned")
                .andReturn("paranoid")
                .andReturn("workaround");

        userPickerSearchService = mockController.getMock(UserPickerSearchService.class);
        reindexMessageManager = mockController.getMock(ReindexMessageManager.class);
        localeManager = mockController.getMock(LocaleManager.class);
        timeZoneService = mockController.getMock(TimeZoneService.class);
        rendererManager = mockController.getMock(RendererManager.class);
    }

    @Test
    public void testBaseUrlNormalised()
    {
        mockController.replay();

        editApplicationProperties = new EditApplicationProperties(userPickerSearchService, reindexMessageManager, localeManager, timeZoneService, rendererManager);

        String url = "http://example.com/context";

        // A URL which needs no modification
        editApplicationProperties.setBaseURL(url);
        assertThat(editApplicationProperties.getBaseURL(), is(url));

        // Trailing slashes stripped?
        editApplicationProperties.setBaseURL(url + "/");
        assertThat(editApplicationProperties.getBaseURL(), is(url));

        // Leading and trailing whitespace stripped?
        editApplicationProperties.setBaseURL("\u2029" + url + " ");
        assertThat(editApplicationProperties.getBaseURL(), is(url));

        // When both a trailing slash and whitespace are present, are both stripped?
        editApplicationProperties.setBaseURL("\t" + url + "/\u200b");
        assertThat(editApplicationProperties.getBaseURL(), is(url));

        mockController.verify();
    }
}
