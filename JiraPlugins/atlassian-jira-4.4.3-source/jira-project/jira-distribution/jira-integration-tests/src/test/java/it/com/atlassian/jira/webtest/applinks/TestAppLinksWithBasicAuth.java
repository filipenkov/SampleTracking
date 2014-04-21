package it.com.atlassian.jira.webtest.applinks;

import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.ApplicationLink;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;
import com.atlassian.jira.webtest.selenium.Quarantine;
import it.com.atlassian.jira.webtest.IntegrationTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.by;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.webtest.ui.keys.Sequences.chars;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Tests around using AppLinks with basic authentication.
 *
 * @since v4.3
 */
@Quarantine
public class TestAppLinksWithBasicAuth extends IntegrationTest
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

    public void testAddingAndRemovingAOneWayLink() throws Exception
    {
        restoreBlankInstance();
        AppLinksAdminPage adminPage = globalPages().goToAdministration().goToPage(AppLinksAdminPage.class);
        NewAppLinkWizard wizard = adminPage.clickAddApplicationLink();

        // insert the RefApp URL
        assertThat((wizard.isOpen()), byDefaultTimeout());
        assertThat(wizard.step1().isReady(), byDefaultTimeout());
        assertThat(wizard.step1().serverURL().isPresent(), by(10, SECONDS));
        wizard.step1().serverURL().type(chars(refAppUrl));
        wizard.clickNext();

        assertThat(wizard.step2().isReady(), byDefaultTimeout());
//        assertEquals("Link to refapp", wizard.title());
        assertEquals(getEnvironmentData().getBaseUrl().toString(), wizard.step2().getReciprocalURL().value().now());
        assertThat(wizard.step2().createReciprocalLink().isReady(), byDefaultTimeout());

        // uncheck the reciprocal link checkbox
        assertThat(wizard.step2().createReciprocalLink().checked(), byDefaultTimeout());
        wizard.step2().createReciprocalLink().toggle();

        // create the link
        wizard.clickNext();
        assertThat(wizard.isClosed(), by(20, SECONDS));

        // check that it was created
        ApplicationLink link = adminPage.applicationLink(refAppUrl);
        assertThat(link.isReady(), byDefaultTimeout());
        assertEquals(refAppUrl, link.baseURL().byDefaultTimeout());
        assertTrue(link.checkForConfiguredAuthenticationType(ApplicationLink.AuthType.NONE));

        /*
        // now go and delete it
        DeleteApplicationLink deleteDialog = link.clickDelete();
        assertThat(deleteDialog.isReady()).byDefaultTimeout();
        deleteDialog.clickConfirm();

        // make sure it's gone
        assertThat(deleteDialog.isClosed()).by(20, SECONDS);
        assertThat(link.isNotPresent()).byDefaultTimeout();
        */
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        refAppUrl = getRefappBaseUrl();
        initRefApp();
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
