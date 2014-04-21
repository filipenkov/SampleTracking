/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.trac;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBeanTest;
import com.atlassian.jira.plugins.importer.po.common.*;
import com.atlassian.jira.plugins.importer.po.trac.TracSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TestTracImport extends BaseJiraWebTest {

	private JiraRestClient restClient;
    private TracSetupPage setupPage;

    @Before
	public void setUpTest() {
        backdoor.restoreData("blankprojects.xml");
		backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        restClient = ITUtils.createRestClient(jira.environmentData());

        setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(TracSetupPage.class);
	}

	@Test
	public void testImportFromSqlite() throws IOException, JSONException, org.codehaus.jettison.json.JSONException {


		setupPage.setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-sqlite.zip");

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage
				.next()
				.createProject("My example project", "My example project", "MYE");

		assertTrue(projectsMappingsPage.next().next().next().nextToLogs().waitUntilFinished().isSuccess());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("MYE-1", new NullProgressMonitor());
		final Field milestone = issue.getFieldByName("Milestone");
		assertNotNull(milestone);
		assertEquals("milestone2", ((JSONObject) milestone.getValue()).get("value"));

		issue = restClient.getIssueClient().getIssue("MYE-4", new NullProgressMonitor());
		final ImmutableList<Attachment> attachments = ImmutableList.copyOf(issue.getAttachments());
		assertEquals(5, attachments.size());
		assertEquals(ImmutableList.of("' and %22.jpg", "quote ' and %22.jpg", "strange(1)-_.~*!.jpg",
				"test_bracket[1]", "test with spaces lol.jpg"),
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

	public static String customField(String id, String name) {
		return String.format("{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/2\\/customFieldOption\\/%s\",\"value\":\"%s\",\"id\":\"%s\"}", id, name, id);
	};

	@Test
	public void testImportFromPostgreSQL() throws Exception {
		setupPage.setEnvironmentFile(getTestResourcesPath() + "trac-pg.zip");

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage
				.next()
				.createProject("My example project", "My example project", "MYE");

		assertTrue(projectsMappingsPage.next().next().next().nextToLogs().waitUntilFinished().isSuccess());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("MYE-1", new NullProgressMonitor());

		for (Map.Entry<String, String> expected : MapBuilder.newBuilder("Milestone", "milestone1", "Priority", "major", "Request status", "SOLVED", "Originator's department", "OPERATIONS")
				.add("System Name", "OTHER (not on the list)").add("Type of service requested", "Exit procedure").add(
						"Custom radio", "c").toMap().entrySet()) {
			final Field field = issue.getFieldByName(expected.getKey());
			assertNotNull(expected.getKey(), field);
			assertEquals(expected.getValue(), ((JSONObject) field.getValue()).get("value"));
		}

		for (Map.Entry<String, String> expected : MapBuilder.newBuilder("Surname", "Nawzdf", "External issue ID", "1", "Profile Required", "ASD234", "Email", "@alcatel-lucent.com")
				.add("Country", "Polska").add("ALU Line Manager", "zxsdfkj").add("UIN", "2134234234").toMap().entrySet()) {
			assertEquals(expected.getValue(), issue.getFieldByName(expected.getKey()).getValue());
		}

		Field testing = issue.getFieldByName("Testing?");
		assertNotNull(testing);
		assertTrue(testing.getValue() instanceof JSONArray);
		JSONArray testingValues = (JSONArray)testing.getValue();
		assertEquals(1, testingValues.length());
		assertEquals("Testing?", ((JSONObject)testingValues.get(0)).get("value"));

        final Map<String, String> expected = MapBuilder.<String, String>newBuilder()
                .add("Labels", "[]")
                .add("Watchers", "{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/2\\/issue\\/MYE-1\\/watchers\",\"watchCount\":0,\"isWatching\":false}")
                .toMap();

        for(Field field : issue.getFields()) {
			if (expected.containsKey(field.getName())) {
            	assertEquals(field.getName(), expected.get(field.getName()), field.getValue() != null ? field.getValue().toString() : null);
			}
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

	@Test
	public void testMappedRadioFieldValues() throws Exception {
		final ImmutableMap<String, String> mappedValues = ImmutableMap.of("Systems Access", "remapped Systems Access",
				"Exit procedure", "remapped Exit procedure");

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
		jira.getTester().gotoUrl(jira.getProductInstance().getBaseUrl() + "/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + StringUtils
				.removeStart(fieldId, "customfield_"));

		return Lists.transform(jira.getTester().getDriver().findElements(By.xpath("//ul[@class=\"optionslist\"]/li")), new Function<WebElement, String>() {
			@Override
			public String apply(WebElement input) {
				return input.getText();
			}
		});
	}

	/**
	 * Smoke test for Trac
	 */
	@Test
	public void testImportFromMySQL() throws Exception {
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
	@Test
	public void testNoTracIni() {
		setupPage.setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-no-ini.zip");

		setupPage.nextWithError();

		assertEquals(1, setupPage.getGlobalErrors().size());
		assertTrue(setupPage.getGlobalErrors().get(0).contains("No conf/trac.ini"));
	}

	/**
	 * Test for https://studio.atlassian.com/browse/JIM-460
	 */
	@Test
	public void testInvalidIni() {
		setupPage.setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-invalid-ini.zip");

		setupPage.nextWithError();

		assertEquals(1, setupPage.getGlobalErrors().size());
		assertTrue(setupPage.getGlobalErrors().get(0).contains("You database configuration stored in trac.ini is empty"));
	}

}
