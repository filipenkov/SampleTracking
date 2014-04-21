package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.admin.IntroductionProperty;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.util.I18nHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
public class TestEditApplicationProperties extends ListeningTestCase
{
    @Mock private UserPickerSearchService userPickerSearchService;
    @Mock private ReindexMessageManager reindexMessageManager;
    @Mock private LocaleManager localeManager;
    @Mock private TimeZoneService timeZoneService;
    @Mock private ProjectManager projectManager;
    @Mock private PermissionManager permissionManager;
    @Mock private JiraAuthenticationContext authenticationContext;
    @Mock private I18nHelper helper;
    @Mock private RendererManager rendererManager;
    @Mock private IssueManager issueManager;
    @Mock private FeatureManager featureManager;
    @Mock private IntroductionProperty introduction;
    @Mock private ApplicationProperties applicationProperties;

    @Rule public final MockComponentContainer mockContainer = new MockComponentContainer();
    @Rule public final InitMockitoMocks initMockito = new InitMockitoMocks(this);

    @BeforeClass
    public static void setUpMultiTenantContext()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Before
    public void setUp()
    {
        when(authenticationContext.getI18nHelper()).thenReturn(helper);
        when(helper.getText(anyString())).thenReturn("owned", "paranoid", "workaround");
        mockContainer.addMockComponent(ApplicationProperties.class, applicationProperties)
            .addMockComponent(JiraAuthenticationContext.class, authenticationContext);
    }

    @Test
    public void testBaseUrlNormalised()
    {
        final EditApplicationProperties editApplicationProperties = createAction();

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
    }

    private EditApplicationProperties createAction()
    {
        return new EditApplicationProperties(userPickerSearchService, reindexMessageManager, localeManager,
                timeZoneService, rendererManager, null, null, null, null, issueManager, featureManager, introduction);
    }
}
