package it.com.atlassian.jira.webtest.applinks;

import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.ApplicationLink;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.selenium.Quarantine;
import it.com.atlassian.jira.webtest.IntegrationTest;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.by;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.webtest.ui.keys.Sequences.chars;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @since v4.3
 */
@Quarantine
public class TestAppLinksWithTrustedApps extends IntegrationTest
{
    /**
     * The key for the Charlie used in testing.
     */
    public static final String CHARLEY_KEY = "CLY";

    /**
     * The running RefApp.
     */
    private static RefAppClient REF_APP;

    /**
     * Base URL for the RefApp.
     */
    private String refAppUrl;

    public void testCreateApplicationLinkWithTrustedApps() throws Exception
    {
        AppLinksAdminPage adminPage = globalPages().goToAdministration().goTo().goToPage(AppLinksAdminPage.class);
        NewAppLinkWizard wizard = adminPage.clickAddApplicationLink();

        // insert the RefApp URL
        assertThat((wizard.isOpen()), byDefaultTimeout());
        assertThat(wizard.step1().isReady(), byDefaultTimeout());
        wizard.step1().serverURL().type(chars(refAppUrl));
        wizard.clickNext();

        assertThat(wizard.step2().isReady(), byDefaultTimeout());
//        assertEquals("Link to refapp", wizard.title());
        assertEquals(getEnvironmentData().getBaseUrl().toString(), wizard.step2().getReciprocalURL().value().now());
        wizard.step2().enterUsername("admin");
        wizard.step2().enterPassword("admin");
        wizard.clickNext();
        assertThat(wizard.step3().isReady(), byDefaultTimeout());
        wizard.clickSubmit();
        assertThat(wizard.isClosed(), by(20, SECONDS));

        // navigate off this page and back. this seems to fix the problems with this test...
        globalPages().goToAdministration().goTo().goToPage(Plugins.class).isReady().byDefaultTimeout();
        adminPage = globalPages().goToAdministration().goTo().goToPage(AppLinksAdminPage.class);
        assertThat(adminPage.isReady(), byDefaultTimeout());
        ApplicationLink link = adminPage.applicationLink(refAppUrl);
        assertThat(link.isReady(), by(30, SECONDS));
        assertEquals(refAppUrl, link.baseURL().byDefaultTimeout());
        assertTrue(link.checkForConfiguredAuthenticationType(ApplicationLink.AuthType.TRUSTED_APPS));
    }

     @Override
    public void onSetUp()
    {
        super.onSetUp();
        refAppUrl = getRefappBaseUrl();
        initRefApp();
        restoreBlankInstance();
    }

    /**
     * Initialises the RefApp (once per test case).
     */
    private void initRefApp()
    {
        if (REF_APP == null)
        {
            REF_APP = new RefAppClient(refAppUrl).loginAs("admin").createCharlie(CHARLEY_KEY, "Charlie");
        }
    }
}
