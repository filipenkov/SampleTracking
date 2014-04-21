package it.com.atlassian.jira.collector.plugin;


import com.atlassian.jira.collector.plugin.page.AddCollectorPage;
import com.atlassian.jira.collector.plugin.page.CongratulationsPage;
import com.atlassian.jira.collector.plugin.page.CustomTemplateDialog;
import com.atlassian.jira.collector.plugin.page.GlobalViewCollectorsPage;
import com.atlassian.jira.collector.plugin.page.PageWithCollector;
import com.atlassian.jira.collector.plugin.page.ViewCollectorPage;
import com.atlassian.jira.collector.plugin.page.ViewCollectorsPage;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TestIssueCollector extends BaseJiraWebTest
{
    private static final String PROJECT_MKY = "MKY";
    private static final String SAMPLE_MSG = "This is a test message!";
	@Inject
	protected JiraTestedProduct product;
	@Inject
	protected JIRAEnvironmentData environmentData;


	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Before
	public void restoreData() {
		backdoor.dataImport().restoreData("collector-data.zip");
	}

    @Test
    public void testCreateCollector() throws IOException, InterruptedException {
        ViewCollectorsPage collectorsPage = product.gotoLoginPage().loginAsSysAdmin(ViewCollectorsPage.class, PROJECT_MKY);
        assertEquals(5, collectorsPage.getCollectorCount());

        final CongratulationsPage congratsPage = collectorsPage.addCollector().useRaiseBugTemplate().name("Test Collector").reporter("admin").submit();
        final String collectorId = congratsPage.getCollectorId();
        final String scriptSource = congratsPage.getScriptSource();

        collectorsPage = product.goTo(ViewCollectorsPage.class, PROJECT_MKY);
        assertEquals(6, collectorsPage.getCollectorCount());

        final ViewCollectorsPage.Collector collector = collectorsPage.getCollectorById(collectorId);
        assertEquals("Test Collector", collector.getName());
        assertEquals("Administrator", collector.getCreator());
        assertEquals("Bug", collector.getIssueType());
        assertEquals("", collector.getDescription().trim());

        final File outFile = generateSamplePageWithCollector(scriptSource);

        final PageWithCollector pageWithCollector = product.getPageBinder().bind(PageWithCollector.class, "file://" + outFile.getAbsolutePath());
        final String issueKey = pageWithCollector.openCollector().description(SAMPLE_MSG).submitFeedback();

		IssueClient ic = new IssueClient(environmentData);
		final Issue.Fields fields = ic.get(issueKey).fields;
		assertEquals(SAMPLE_MSG, fields.summary);
		assertEquals(SAMPLE_MSG, fields.description);

		collectorsPage = product.goTo(ViewCollectorsPage.class, PROJECT_MKY);
        collectorsPage = collectorsPage.deleteCollector(collectorId);
        assertEquals(5, collectorsPage.getCollectorCount());
    }

    @Test
    public void testCreateCollectorValidationErrors() throws IOException, InterruptedException
    {
        ViewCollectorsPage collectorsPage = product.gotoLoginPage().loginAsSysAdmin(ViewCollectorsPage.class, PROJECT_MKY);
        assertEquals(5, collectorsPage.getCollectorCount());

        final AddCollectorPage newCollector = collectorsPage.addCollector();

        newCollector.useRaiseBugTemplate();
        newCollector.submitExpectingError();
        assertThat(newCollector.getCollectorNameError(), containsString("enter a name"));
        assertThat(newCollector.getIssueReporterError(), containsString("enter a default reporter"));

        // JRADEV-12511 - First, set just the reporter field, leaving the name blank.
        newCollector.reporter("admin");
        newCollector.submitExpectingError();
        assertThat(newCollector.getCollectorNameError(), containsString("enter a name"));
        assertThat(newCollector.getIssueReporterValue(), is(equalTo("Administrator")));

        newCollector.name("Bla");
        newCollector.submit();

        collectorsPage = product.goTo(ViewCollectorsPage.class, PROJECT_MKY);
        assertEquals(6, collectorsPage.getCollectorCount());
    }

    @Test
    public void testCollectorWithJavaScriptEmbed() throws IOException
    {
        ViewCollectorsPage collectorsPage = product.gotoLoginPage().loginAsSysAdmin(ViewCollectorsPage.class, PROJECT_MKY);
        assertEquals(5, collectorsPage.getCollectorCount());

        final CongratulationsPage congratsPage = collectorsPage.addCollector().useRaiseBugTemplate().name("Test Collector").reporter("admin").submit();
        final String scriptSource = congratsPage.getScriptSourceJavascript();

        final File outFile = generateSamplePage("/webdriver-test/test-js-embed.html", ImmutableMap.of("JS_SCRIPT_SOURCE", scriptSource));

        final PageWithCollector pageWithCollector = product.getPageBinder().bind(PageWithCollector.class, "file://" + outFile.getAbsolutePath());
        final String issueKey = pageWithCollector.openCollector().description(SAMPLE_MSG).submitFeedback();

		product.gotoLoginPage(); // workaround for DirtyWarningTerminator

        //this would blow up if the issue doesn't exist. Good enough.
		IssueClient ic = new IssueClient(environmentData);
		final Issue.Fields fields = ic.get(issueKey).fields;
		assertEquals(SAMPLE_MSG, fields.summary);
		assertEquals(SAMPLE_MSG, fields.description);
	}

    @Test
    public void testDisabledCollector() throws IOException
    {
        ViewCollectorsPage collectorsPage = product.gotoLoginPage().loginAsSysAdmin(ViewCollectorsPage.class, PROJECT_MKY);
        ViewCollectorsPage.Collector collector = collectorsPage.getCollectors().get(0);
        collector.disable();

        ViewCollectorPage viewCollector = product.goTo(ViewCollectorPage.class, PROJECT_MKY, collector);
        final String scriptSource = viewCollector.getScriptSource();

        final File outFile = generateSamplePageWithCollector(scriptSource);

        final PageWithCollector pageWithCollector = product.getPageBinder().bind(PageWithCollector.class, "file://" + outFile.getAbsolutePath());
        assertTrue(pageWithCollector.openCollector().isDisabled());

        collectorsPage = product.goTo(ViewCollectorsPage.class, PROJECT_MKY);
        List<String> errors = collectorsPage.getErrors();
        assertTrue(!errors.isEmpty());
        assertTrue(errors.get(0).contains("Disabled collector '" + collector.getName() + "' was invoked"));
    }

    @Test
    public void testCustomCollector() throws IOException
    {
        ViewCollectorsPage collectorsPage = product.gotoLoginPage().loginAsSysAdmin(ViewCollectorsPage.class, PROJECT_MKY);
        AddCollectorPage addCollectorPage = collectorsPage.addCollector().name("Test Collector").reporter("admin").trigger(AddCollectorPage.TriggerStyle.SUBTLE, "Subtle Trigger");
        CustomTemplateDialog customTemplateDialog = addCollectorPage.template(AddCollectorPage.Template.CUSTOM);
        customTemplateDialog.addField("priority").addField("description").title("My Custom Feedback Dialog!");

        addCollectorPage.customMessage("Let the pigs fly!!");
        final CongratulationsPage congratsPage = addCollectorPage.submit();
        final String scriptSource = congratsPage.getScriptSource();

        final File outFile = generateSamplePageWithCollector(scriptSource);

        final PageWithCollector pageWithCollector = product.getPageBinder().bind(PageWithCollector.class, "file://" + outFile.getAbsolutePath());
        assertEquals("Subtle Trigger", pageWithCollector.getTriggerText());
        pageWithCollector.openCollector();
        assertEquals("Let the pigs fly!!", pageWithCollector.getCustomMessage());
        final String issueKey = pageWithCollector.summary("My test summary").description(SAMPLE_MSG).submitFeedback();

		product.gotoLoginPage(); // workaround for DirtyWarningTerminator

        //this would blow up if the issue doesn't exist. Good enough.
		IssueClient ic = new IssueClient(environmentData);
		final Issue.Fields fields = ic.get(issueKey).fields;
		assertEquals("My test summary", fields.summary);
		assertEquals(SAMPLE_MSG, fields.description);
    }

    @Test
    public void testBackwardsCompatibility() throws IOException
    {
        ViewCollectorsPage collectorsPage = product.gotoLoginPage().loginAsSysAdmin(ViewCollectorsPage.class, PROJECT_MKY);
        final CongratulationsPage congratsPage = collectorsPage.addCollector().useRaiseBugTemplate().name("Backwards Compat Collector").reporter("admin").useCredentials().submit();
        final String collectorId = congratsPage.getCollectorId();

		final File outFile = generateSamplePage("/webdriver-test/test-backwards-compat.html", ImmutableMap.of("BASEURL", product .getProductInstance().getBaseUrl(),
                "COLLECTOR_ID", collectorId));

        final PageWithCollector pageWithCollector = product.getPageBinder().bind(PageWithCollector.class, "file://" + outFile.getAbsolutePath());
        assertEquals("Raise a Bug", pageWithCollector.getTriggerText());
        final String issueKey = pageWithCollector.openCollector().description("Legacy collector feedback!").submitFeedback();

		product.gotoLoginPage(); // workaround for DirtyWarningTerminator

        //this would blow up if the issue doesn't exist. Good enough.
		IssueClient ic = new IssueClient(environmentData);
		final Issue.Fields fields = ic.get(issueKey).fields;
		assertEquals("Legacy collector feedback!", fields.summary);
    }

	@Test
    public void testGlobalAdminArea() throws IOException
    {
        GlobalViewCollectorsPage globalAdmin = product.gotoLoginPage().loginAsSysAdmin(GlobalViewCollectorsPage.class);
        List<String> projectNames = globalAdmin.getProjectNames();
        assertTrue(projectNames.contains("Homosapien"));
        assertTrue(projectNames.contains("Public"));
        assertTrue(projectNames.contains("Monkey"));

        List<String> collectorNames = globalAdmin.getCollectorNames();
        assertEquals(10, collectorNames.size());
        assertTrue(collectorNames.contains("Custom Collector"));
    }

    private File generateSamplePageWithCollector(final String scriptSource) throws IOException
    {
        return generateSamplePage("/webdriver-test/test.html", ImmutableMap.of("SCRIPT_PLACEHOLDER", scriptSource));
    }

    private File generateSamplePage(final String file, final Map<String, String> params) throws IOException
    {
        String sampleHtml = IOUtils.toString(getClass().getResourceAsStream(file));
        for (Map.Entry<String, String> param : params.entrySet())
        {
            sampleHtml = sampleHtml.replace("@@" + param.getKey() + "@@", param.getValue());
        }
		final File outFile = tmpFolder.newFile("transformed-page.html");
        FileUtils.writeStringToFile(outFile, sampleHtml);
		return outFile;
    }
}
