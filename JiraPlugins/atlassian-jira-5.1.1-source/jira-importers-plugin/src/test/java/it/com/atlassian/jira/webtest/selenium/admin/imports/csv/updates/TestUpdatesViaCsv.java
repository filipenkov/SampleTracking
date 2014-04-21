/*
 * Copyright (C) 2002-2012 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.csv.updates;

import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;


public class TestUpdatesViaCsv extends ScreenshotFuncTestCase {
	private static final ImmutableSet<String> EMPTY = ImmutableSet.<String>of();
	private JiraRestClient restClient;
	private NullProgressMonitor np = new NullProgressMonitor();

	public void setUpTest() {
		super.setUpTest();
		administration.restoreBlankInstance();
		restClient = ITUtils.createRestClient(environmentData);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	public void testUpdateSummary() throws Exception {
		final ImporterFinishedPage importerFinishedPage = performImport("csv_update/update-simple.csv", "csv_update/update-simple.json");
		Assert.assertTrue(importerFinishedPage.isSuccess());

		final Issue tt1 = restClient.getIssueClient().getIssue("TT-1", np);
		final Issue tt2 = restClient.getIssueClient().getIssue("TT-2", np);

		Assert.assertEquals("Changed summary", tt1.getSummary());
		Assert.assertEquals(7, tt1.getVotes().getVotes());
		Assert.assertEquals(ImmutableSet.of("label-1", "label-2"), tt1.getLabels());

		Assert.assertEquals("Original summary 2", tt2.getSummary());
		Assert.assertEquals(0, tt2.getVotes().getVotes());
		Assert.assertTrue(tt2.getLabels().isEmpty());
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	public void testUpdateCustomFields() throws Exception {
		final ImporterFinishedPage importerFinishedPage = performImport("csv_update/update-custom-fields.csv", "csv_update/update-custom-fields.json");
		Assert.assertTrue(importerFinishedPage.isSuccess());

		final Issue tt1 = restClient.getIssueClient().getIssue("TT-1", np);
		final Issue tt2 = restClient.getIssueClient().getIssue("TT-2", np);

		Assert.assertEquals("Issue summary", tt1.getSummary());
		Assert.assertEquals("Changed Field One Again", tt1.getFieldByName("Free Text Field").getValue());
		Assert.assertEquals("Changed Field Two", tt1.getFieldByName("Text Field").getValue());
		Assert.assertEquals("[\"Changed\",\"Field\",\"Three\"]", tt1.getFieldByName("Labels Field").getValue().toString());
		Assert.assertEquals(2d, tt1.getFieldByName("Number Field").getValue());

		final Object value = tt1.getFieldByName("Date Time Field").getValue();
		Assert.assertTrue(value instanceof String);
		DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime((String) value);

		Assert.assertEquals(new DateTime(2012, 01, 02, 14, 20, 0, 0), dateTime);
		Assert.assertEquals("2012-01-01", tt1.getFieldByName("Date Picker Field").getValue());

		Assert.assertEquals("Issue summary", tt2.getSummary());
		Assert.assertNull(tt2.getFieldByName("Free Text Field").getValue());
		Assert.assertNull(tt2.getFieldByName("Text Field").getValue());
		Assert.assertNull(tt2.getFieldByName("Labels Field").getValue());
		Assert.assertNull(tt2.getFieldByName("Number Field").getValue());
		Assert.assertNull(tt2.getFieldByName("Date Time Field").getValue());
		Assert.assertNull(tt2.getFieldByName("Date Picker Field").getValue());

	}

	@Test
	public void testUpdateWithKeyMapping() throws Exception {
		final ImporterFinishedPage importerFinishedPage = performImport("csv_update/update-with-name-mapping.csv", "csv_update/update-with-name-mapping.json");
		Assert.assertTrue(importerFinishedPage.isSuccess());

		Assert.assertEquals(1, restClient.getSearchClient().searchJql("", np).getTotal());;
		final Issue ss1 = restClient.getIssueClient().getIssue("SS-1", np);
		Assert.assertEquals("Changed summary", ss1.getSummary());
	}

	@Test
	public void testUpdateMiscFields() throws Exception {
		final ImporterFinishedPage importerFinishedPage = performImport("misc/clearing-fix-versions.csv", "misc/clearing-fix-versions.json");
		Assert.assertTrue(importerFinishedPage.isSuccess());

		final Issue issue1 = restClient.getIssueClient().getIssue("DMISTWOOH-1", np);
		final Issue issue2 = restClient.getIssueClient().getIssue("DMISTWOOH-2", np);
		final Issue canary1 = restClient.getIssueClient().getIssue("DMISTWOOH-3", np);
		final Issue canary2 = restClient.getIssueClient().getIssue("DMISTWOOH-4", np);

		// checking preconditions
		verifyIssue(canary1, "abc", EMPTY, ImmutableSet.of("v1.1"), ImmutableSet.of("Component A", "Component B"));
		verifyIssue(canary2, "second issue", ImmutableSet.of("v1.2", "v1.3"), EMPTY, ImmutableSet.of("Component X"));

		// checking clears
		verifyIssue(issue1, "abc", EMPTY, EMPTY, EMPTY);
		verifyIssue(issue2, "second issue", EMPTY, EMPTY, ImmutableSet.of("Component X", "Component Z"));

		final Project dmistwooh = restClient.getProjectClient().getProject("DMISTWOOH", np);
		Assert.assertEquals(ImmutableSet
				.of("Component A", "Component B", "Component X", "Component Z"), toComponentNames(dmistwooh.getComponents()));
		Assert.assertEquals(ImmutableSet.of("v1.1", "v1.2", "v1.3"), toVersionNames(dmistwooh.getVersions()));

	}

	private void verifyIssue(Issue issue, String summary, ImmutableSet<String> affectVersions, ImmutableSet<String> fixVersions, ImmutableSet<String> components) {
		Assert.assertEquals(summary, issue.getSummary());
		Assert.assertEquals(fixVersions, toVersionNames(issue.getFixVersions()));
		Assert.assertEquals(components, toComponentNames(issue.getComponents()));
		Assert.assertEquals(affectVersions, toVersionNames(issue.getAffectedVersions()));
	}

	private Iterable<String> toVersionNames(Iterable<Version> fixVersions) {
		return ImmutableSet.copyOf(Iterables.transform(fixVersions, new Function<Version, String>() {
			@Override
			public String apply(Version input) {
				return input.getName();
			}
		}));
	}

	private Iterable<String> toComponentNames(Iterable<BasicComponent> fixVersions) {
		return ImmutableSet.copyOf(Iterables.transform(fixVersions, new Function<BasicComponent, String>() {
			@Override
			public String apply(BasicComponent input) {
				return input.getName();
			}
		}));
	}

	private ImporterFinishedPage performImport(String csv, String config) {
		CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		setupPage.setCsvFile(ITUtils.getCsvResource(csv));
		setupPage.setConfigurationFile(ITUtils.getCsvResource(config));

		CsvFieldMappingsPage fieldMappingsPage = setupPage.next().next();
		return fieldMappingsPage.next().next().waitUntilFinished();
	}
}
