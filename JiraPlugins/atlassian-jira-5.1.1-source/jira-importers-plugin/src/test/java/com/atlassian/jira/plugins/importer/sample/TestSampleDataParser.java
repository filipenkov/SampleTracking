package com.atlassian.jira.plugins.importer.sample;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.plugins.importer.DateTimeMatcher;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalHistoryGroup;
import com.atlassian.jira.plugins.importer.external.beans.ExternalHistoryItem;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.external.beans.NamedExternalObject;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporterFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TestSampleDataParser {

    @Mock
    JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    JiraDataImporterFactory jiraDataImporterFactory;
    @Mock
    ConstantsManager constantsManager;
    @Mock
    TemplateRenderer templateRenderer;

    private SampleDataImporterImpl importer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        importer = new SampleDataImporterImpl(jiraAuthenticationContext, jiraDataImporterFactory, constantsManager, templateRenderer);
    }

    private String getJson(@Nonnull String filename) {
        InputStream is = this.getClass().getResourceAsStream("/sample/" + filename);
        Assert.assertNotNull(is);
        try {
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Test
    public void checkPeriodMapping() {
        final String json = getJson("simple.json");
        SampleData sd = importer.parseSampleData(json);

		final ExternalProject project = Iterables.getOnlyElement(sd.getProjects());
		final ExternalIssue issue = Iterables.getOnlyElement(project.getIssues());
		Assert.assertEquals(ImmutableList.of("2.3"), issue.getFixedVersions());
		Assert.assertEquals(ImmutableList.of("1.9"), issue.getAffectedVersions());

		Assert.assertThat(issue.getCreated(), DateTimeMatcher.ago(Period.days(3)));
		Assert.assertThat(issue.getUpdated(), DateTimeMatcher.ago(Period.days(1)));

		Assert.assertEquals(ImmutableList.of("Core", "HTTP"), issue.getComponents());
	}

	@Test
	public void testDualFormOfComponents() throws Exception {
		SampleData sd = importer.parseSampleData(getJson("dual_form_components.json"));

		final ExternalProject project = Iterables.getOnlyElement(sd.getProjects());
		final ImmutableSet<ExternalComponent> expectedComponents = ImmutableSet.of(
				new ExternalComponent("Raw Name"),
				new ExternalComponent("Just Name"),
				new ExternalComponent("With Desc", null, null, "The description"),
				new ExternalComponent("Sophisticated", null, "admin", "This is a component with provided description.")
		);
		Assert.assertEquals(expectedComponents, project.getComponents());

	}

	@Test
	public void testDualFormOfVersions() throws Exception {
		SampleData sd = importer.parseSampleData(getJson("dual_form_components.json"));

		final ExternalProject project = Iterables.getOnlyElement(sd.getProjects());
		final Map<String, ExternalVersion> versions = Maps.uniqueIndex(project.getVersions(), NamedExternalObject.NAME_FUNCTION);

		Assert.assertEquals(4, versions.size());

		final ExternalVersion v10 = versions.get("1.0");
		Assert.assertEquals("1.0", v10.getName());
		Assert.assertEquals("Version 1.0", v10.getDescription());
		Assert.assertTrue(v10.isReleased());
		Assert.assertTrue(v10.isArchived());
		Assert.assertThat(v10.getReleaseDate(), DateTimeMatcher.ago(Period.weeks(3)));

		testAlmostEmptyVersion(versions.get("1.1"), "1.1", "Version 1.1");
		testAlmostEmptyVersion(versions.get("1.9"), "1.9", "Version 1.9");
		testAlmostEmptyVersion(versions.get("2.3"), "2.3", null);
	}

	private void testAlmostEmptyVersion(ExternalVersion version, String name, String description) {
		Assert.assertNotNull(version);
		Assert.assertEquals(name, version.getName());
		Assert.assertEquals(description, version.getDescription());
		Assert.assertFalse(version.isReleased());
		Assert.assertFalse(version.isArchived());
		Assert.assertNull(version.getReleaseDate());
	}

	@Test
	public void testParsingChangeHistory() throws Exception {
		SampleData sd = importer.parseSampleData(getJson("history.json"));
		final ExternalProject project = Iterables.getOnlyElement(sd.getProjects());
		final ExternalIssue issue = Iterables.getOnlyElement(project.getIssues());
		final List<ExternalHistoryGroup> history = issue.getHistory();
		Assert.assertNotNull(history);
		
		final ExternalHistoryGroup group = Iterables.getOnlyElement(history);
		Assert.assertNotNull(group);
		Assert.assertEquals("wseliga", group.getAuthor());
		Assert.assertThat(group.getCreated(), DateTimeMatcher.ago(Period.days(1)));

		final ExternalHistoryItem item = Iterables.getOnlyElement(group.getItems());
		final ExternalHistoryItem expected = new ExternalHistoryItem("jira", "status", "1", "Open", "5", "Resolved");
		Assert.assertEquals(expected, item);


	}
}

