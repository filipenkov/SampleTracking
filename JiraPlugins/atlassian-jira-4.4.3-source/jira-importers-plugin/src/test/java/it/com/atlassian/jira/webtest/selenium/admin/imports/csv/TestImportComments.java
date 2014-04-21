/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvProjectMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.atlassian.jira.issue.IssueFieldConstants.*;
import static com.google.common.collect.Lists.newArrayList;

public class TestImportComments extends ScreenshotFuncTestCase {
	private final String CSV_FILE = getCurrentWorkingDirectory() + "/src/test/resources/csv/comments.csv";
	private final String CFG_FILE = getCurrentWorkingDirectory() + "/src/test/resources/csv/comments.cfg";
	private final File emptyFile;
	private JiraRestClient restClient;
	private final NullProgressMonitor pm = new NullProgressMonitor();
	private static final String PROJECT_KEY = "TCSV";

	public TestImportComments() {
		try {
			emptyFile = File.createTempFile("jim-selenium", "test");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setUpTest() {
		super.setUpTest();
		URI jiraServerUri;
		try {
			jiraServerUri = environmentData.getBaseUrl().toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "admin", "admin");
//				FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
		product = TestedProductFactory.create(JiraTestedProduct.class);
	}

	@Test
	public void testMissingFile() {
		final CsvSetupPage csvSetupPage = gotoCsvSetupPage();
		csvSetupPage.setCsvFile(emptyFile.getAbsolutePath()).next();
		assertEquals(ImmutableList.of(emptyFile.getName() + " does not exist or is zero bytes in size."), csvSetupPage.getFieldErrors());
	}

	private CsvSetupPage gotoCsvSetupPage() {
		return product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();
	}

	@Test
	public void testInvalidFileEncoding() {
		final CsvSetupPage csvSetupPage = gotoCsvSetupPage();
		csvSetupPage.setCsvFile(CSV_FILE).setEncoding("somefakeencoding").next();
		assertEquals(ImmutableList.of("Invalid file encoding"), csvSetupPage.getFieldErrors());
	}

	@Test
	public void testMissingConfigurationFile() {
		final CsvSetupPage csvSetupPage = gotoCsvSetupPage();
		final String fakeFile = "fakefilewhichdoesnotexist";
		csvSetupPage.setConfigurationFile("/tmp/" + fakeFile).setCsvFile(CSV_FILE).next();
		assertEquals(ImmutableList.of(fakeFile + " does not exist or is zero bytes in size."),
				csvSetupPage.getFieldErrors());
	}


	@Test
	public void testMappingSeveralColumnsIntoComments() throws URISyntaxException {
		importFromCsvFile(CSV_FILE);
		assertIssueHasComments(PROJECT_KEY, "Z", "my first comment", "my second comment");
		assertIssueHasComments(PROJECT_KEY, "D", "Comment 2", "bye bye", "third comment");
	}

	private void importFromCsvFile(String csvFile) {
		ITUtils.doWebSudoCrap(navigation, tester);
		administration.restoreBlankInstance();
		final CsvSetupPage csvSetupPage = gotoCsvSetupPage();
		final CsvProjectMappingsPage projectMappingsPage = csvSetupPage.setCsvFile(csvFile).setConfigurationFile(CFG_FILE).next();

		assertEquals(PROJECT_KEY, projectMappingsPage.getProjectKey());
		assertEquals("Testing multi-comments", projectMappingsPage.getProjectName());

		final CsvFieldMappingsPage fieldMappingsPage = projectMappingsPage.next();

		final ArrayList<String> expectedFields = newArrayList(COMMENT, COMMENT, COMMENT, PRIORITY, STATUS, SUMMARY, FIX_FOR_VERSIONS);
		assertEquals(expectedFields, newArrayList(
				Collections2.transform(fieldMappingsPage.getMappingFields(), new Function<WebElement, String>() {
					public String apply(WebElement from) {
						return from.getValue();
					}
				})));

		// only Status is checked
		assertEquals(newArrayList(false, false, false, false, true, false, false), newArrayList(
				Collections2.transform(fieldMappingsPage.getMappingCheckboxes(), new Function<WebElement, Boolean>() {
					public Boolean apply(WebElement from) {
						return from.isSelected();
					}
				})));

		fieldMappingsPage.next().next().waitUntilFinished();
	}


	@Test
	public void testMappingMoreCommentsThanHeaders() {
		importFromCsvFile(getCurrentWorkingDirectory() + "/src/test/resources/csv/more-comments-than-headers.csv");

		assertIssueHasComments(PROJECT_KEY, "Z", "my first comment", "my second comment", "xxxxx", "zzzz");
		assertIssueHasComments(PROJECT_KEY, "B", "another comment", "hello");
		assertIssueHasComments(PROJECT_KEY, "D", "Comment 2", "bye bye", "third comment", "third comment", "my fourth comment", "and the last comment");
	}

	private static String getCurrentWorkingDirectory() {
		return new File(".").getAbsolutePath();
	}

	private void assertIssueHasComments(String projectKey, String summary, String... comments) {
		// I am sick of using screen-scraping (via jira-func-tests) to actually assert if the issues have been correctly created
		// I am using here JRJC (and JIRA REST API) to make it far faster and more elegant.
		final String jql = buildJql(projectKey, summary, comments);
		final SearchResult searchResult = restClient.getSearchClient().searchJql(jql, pm);
		assertEquals(1, Iterables.size(searchResult.getIssues()));
		final BasicIssue basicIssue = searchResult.getIssues().iterator().next();
		final Issue issue = restClient.getIssueClient().getIssue(basicIssue.getKey(), pm);
		assertEquals(Sets.newHashSet(comments), Sets.newHashSet(Iterables.transform(issue.getComments(), new Function<Comment, String>() {
			public String apply(@Nullable Comment from) {
				return from.getBody();
			}
		})));
	}

	private String buildJql(String projectKey, String summary, String[] comments) {
		final StringBuilder commentSearchTerm = new StringBuilder();
		for (String comment : comments) {
			commentSearchTerm.append(" and comment ~ \"\\\"").append(comment).append("\\\"\"");
		}
		return "project = " + projectKey + " and summary ~ \"\\\"" + summary + "\\\"\"" + commentSearchTerm + " order by key";
	}
}
