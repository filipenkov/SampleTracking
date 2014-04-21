/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.trac;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBeanTest;
import com.atlassian.jira.plugins.importer.po.common.ImporterCustomFieldsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterLogsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterValueMappingsPage;
import com.atlassian.jira.plugins.importer.po.trac.TracSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.BasicWatchers;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.Watchers;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestTracImport extends ScreenshotFuncTestCase {

	private JiraRestClient restClient;

	@Override
	public void setUpTest() {
		administration.restoreBlankInstance();
		administration.attachments().enable();
		ITUtils.doWebSudoCrap(navigation, tester);

        product = TestedProductFactory.create(JiraTestedProduct.class);

		restClient = ITUtils.createRestClient(environmentData);
	}

	public void testImportFromSqlite() throws IOException {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final TracSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(TracSetupPage.class)
				.webSudo();

		setupPage.setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-sqlite.zip");

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage
				.next()
				.createProject("My example project", "My example project", "MYE");

		assertTrue(projectsMappingsPage.next().next().next().nextToLogs().waitUntilFinished().isSuccess());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, search.getTotal());

		fixPriorityBug("MYE-1");
		Issue issue = restClient.getIssueClient().getIssue("MYE-1", new NullProgressMonitor());
		for(Field f : issue.getFields()) {
			if ("Milestone".equals(f.getName())) {
				assertEquals("{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10002\",\"value\":\"milestone2\"}",
						f.getValue());
			}
		}

		issue = restClient.getIssueClient().getIssue("MYE-4", new NullProgressMonitor());
		final ImmutableList<Attachment> attachments = ImmutableList.copyOf(issue.getAttachments());
		assertEquals(5, attachments.size());
		assertEquals(ImmutableList.of("' and %22.jpg", "quote ' and %22.jpg", "strange(1)-_.~*!.jpg",
				"test with spaces lol.jpg", "test_bracket[1]"),
				Immutables.transformThenCopyToList(attachments, new Function<Attachment, String>() {
					@Override
					public String apply(Attachment input) {
						return input.getFilename();
					}
				}));

		issue = restClient.getIssueClient().getIssue("MYE-2", new NullProgressMonitor());
		BasicWatchers basicWatchers = issue.getWatchers();
		assertEquals(2, basicWatchers.getNumWatchers());
		Watchers watchers = restClient.getIssueClient().getWatchers(
				basicWatchers.getSelf(), new NullProgressMonitor());
		assertEquals(ImmutableList.<String>of("olaf@wp.pl", "pniewiadomski@atlassian.com"),
				Immutables.transformThenCopyToList(watchers.getUsers(), new Function<BasicUser, String>() {
					@Override
					@Nullable
					public String apply(@Nullable BasicUser input) {
						return input != null ? input.getName() : null;
					}
				}));
	}

	public void testImportFromPostgreSQL() throws Exception {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final TracSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(TracSetupPage.class)
				.webSudo();

		setupPage.setEnvironmentFile(getTestResourcesPath() + "trac-pg.zip");

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage
				.next()
				.createProject("My example project", "My example project", "MYE");

		assertTrue(projectsMappingsPage.next().next().next().nextToLogs().waitUntilFinished().isSuccess());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("MYE-1", new NullProgressMonitor());
        final Map<String, String> expected = MapBuilder.<String, String>newBuilder()
                .add("Milestone", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10024\",\"value\":\"milestone1\"}")
                .add("Priority", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10023\",\"value\":\"major\"}")
                .add("Surname", "Nawzdf")
                .add("Request status", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10002\",\"value\":\"SOLVED\"}")
                .add("External issue ID", "1")
                .add("Originator's department", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10007\",\"value\":\"OPERATIONS\"}")
				.add("Profile Required", "ASD234")
                .add("System Name", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10005\",\"value\":\"OTHER (not on the list)\"}")
				.add("Email", "@alcatel-lucent.com")
                .add("labels", "[]")
                .add("Country", "Polska")
				.add("sub-tasks", "[]")
                .add("ALU Line Manager", "zxsdfkj")
				.add("Type of service requested", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10017\",\"value\":\"Exit procedure\"}")
                .add("Testing?", "[{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10025\",\"value\":\"Testing?\"}]")
				.add("Custom radio", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10012\",\"value\":\"c\"}")
                .add("UIN", "2134234234").toMap();

        for(Field field : issue.getFields()) {
            assertEquals(field.getName(), expected.get(field.getName()), field.getValue() != null ? field.getValue().toString() : null);
        }

		issue = restClient.getIssueClient().getIssue("MYE-2", new NullProgressMonitor());
        Field field = issue.getField("customfield_10013");
        assertNotNull(field);
		assertNull(field.getValue()); // make sure Testing? is not set in MYE-2

		verifyUnmappedCustomRadioAndSelectHaveUnusedValues();
	}

	private void verifyUnmappedCustomRadioAndSelectHaveUnusedValues() throws IOException {
		final ImmutableMap<String, Field> fields = retrieveImportedFields();

		final String typeOfServiceId = fields.get("Type of service requested").getId();
		Assert.assertEquals(TracConfigBeanTest.EXPECTED_TYPE_OF_SERVICE, getAvailableOptionsForCustomField(typeOfServiceId));

		final String radioId = fields.get("Custom radio").getId();
		Assert.assertEquals(TracConfigBeanTest.EXPECTED_RADIO, getAvailableOptionsForCustomField(radioId));
	}

	public void testMappedRadioFieldValues() throws Exception {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final ImmutableMap<String, String> mappedValues = ImmutableMap.of("Systems Access", "remapped Systems Access",
				"Exit procedure", "remapped Exit procedure");

		final TracSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(TracSetupPage.class)
				.webSudo();

		setupPage.setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-pg.zip");

		final ImporterCustomFieldsPage customFieldMappingsPage = setupPage.next().createProject("My example project", "My example project", "MYE").next();
		final ImporterFieldMappingsPage fieldValueMappings = customFieldMappingsPage
				.selectOtherFieldMapping("radio", "remapped_radio")
				.selectOtherFieldMapping("type_of_service_requested", "remapped_tos")
				.next();
		final ImporterValueMappingsPage valueMappingsPage = fieldValueMappings
				.setMappingEnabled("type_of_service_requested", true)
				.next();
		for (Map.Entry<String, String> mapping : mappedValues.entrySet()) {
			valueMappingsPage.setMapping("type_of_service_requested", mapping.getKey(), mapping.getValue());
		}
		final ImporterLogsPage logsPage = valueMappingsPage.nextToLogs();
		assertTrue(logsPage.waitUntilFinished().isSuccess());

		final ImmutableMap<String, Field> fields = retrieveImportedFields();

		final String radioId = fields.get("remapped_radio").getId();
		Assert.assertEquals(TracConfigBeanTest.EXPECTED_RADIO, getAvailableOptionsForCustomField(radioId));

		final String typeOfServiceId = fields.get("remapped_tos").getId();
		final List<String> expectedValues = Lists
				.transform(TracConfigBeanTest.EXPECTED_TYPE_OF_SERVICE, new Function<String, String>() {
					@Override
					public String apply(String input) {
						return StringUtils.defaultString(mappedValues.get(input), input);
					}
				});
		Assert.assertEquals(expectedValues, getAvailableOptionsForCustomField(typeOfServiceId));
	}

	private ImmutableMap<String, Field> retrieveImportedFields() throws IOException {
		fixPriorityBug("MYE-1");
		final Issue issue = restClient.getIssueClient().getIssue("MYE-1", new NullProgressMonitor());
		return Maps.uniqueIndex(
				issue.getFields(),
				new Function<Field, String>() {
					@Override
					public String apply(@Nullable Field input) {
						return input != null ? input.getName() : null;
					}
				});
	}


	private List<String> getAvailableOptionsForCustomField(String fieldId) {
		tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + StringUtils
				.removeStart(fieldId, "customfield_"));
		navigation.webSudoAuthenticateUsingLastPassword();
		final XPathLocator locator = new XPathLocator(tester, "//ul[@class=\"optionslist\"]/li");
		return Lists.transform(ImmutableList.copyOf(locator.getNodes()), new Function<Node, String>() {
			@Override
			public String apply(Node input) {
				return input.getTextContent();
			}
		});
	}

	// workaround for REST failing when Priority field is not set
	// TODO: remove once http://jira.atlassian.com/browse/JRA-24054 is fixed (JIRA 4.4)
	private void fixPriorityBug(String issueKey) throws IOException {
		final String jelly = IOUtils.toString(getClass().getResourceAsStream("/pivotal/fixPriority.jelly"))
				.replace("${issueKey}", issueKey);
		administration.runJellyScript(jelly);
	}

	/**
	 * Smoke test for Trac
	 */
	public void testImportFromMySQL() throws Exception {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final TracSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(TracSetupPage.class)
				.webSudo();

		setupPage.setEnvironmentFile(getTestResourcesPath() + "trac-my.zip");

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage
				.next()
				.createProject("My example project", "My example project", "MYE");

		assertTrue(projectsMappingsPage.next().next().next().nextToLogs().waitUntilFinished().isSuccess());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(0, search.getTotal());
	}

	private static String getTestResourcesPath() throws Exception {
		return ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac" + (ITUtils.isPortForwardEnv() ? "/portforward/" : "/");
	}

	/**
	 * Test for https://studio.atlassian.com/browse/JIM-460
	 */
	public void testNoTracIni() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final TracSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(TracSetupPage.class)
				.webSudo();

		setupPage.setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-no-ini.zip");

		setupPage.nextWithError();

		assertEquals(1, setupPage.getGlobalErrors().size());
		assertTrue(setupPage.getGlobalErrors().get(0).contains("No conf/trac.ini"));
	}

		/**
	 * Test for https://studio.atlassian.com/browse/JIM-460
	 */
	public void testInvalidIni() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final TracSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(TracSetupPage.class)
				.webSudo();

		setupPage.setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-invalid-ini.zip");

		setupPage.nextWithError();

		assertEquals(1, setupPage.getGlobalErrors().size());
		assertTrue(setupPage.getGlobalErrors().get(0).contains("You database configuration stored in trac.ini is empty"));
	}

}
