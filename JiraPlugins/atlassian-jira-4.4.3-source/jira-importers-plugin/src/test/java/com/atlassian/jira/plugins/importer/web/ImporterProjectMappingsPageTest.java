package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.web.model.ProjectModel;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.google.common.collect.ImmutableList;
import junit.framework.Assert;
import mock.user.MockOSUser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

public class ImporterProjectMappingsPageTest {

	@Test
	public void testGeneratingModel() throws Exception {
		ImporterProjectMappingsPage page = Mockito.mock(ImporterProjectMappingsPage.class);
		final ProjectModel p1 = new ProjectModel("Existing Project", "EP", "mock user", false);
		final ProjectModel p2 = new ProjectModel("External Mapping", "SUGGESTED", "suggested lead", true);
		final MockProject existingProject = new MockProject(1, p1.key, p1.name);
		existingProject.setLead(new MockOSUser(p1.lead, "Mock User Name", "mock@user.test"));

		Mockito.when(page.getApplicableProjects()).thenReturn(ImmutableList.<Project>of(existingProject));
		Mockito.when(page.getSuggestedNewProjects()).thenReturn(ImmutableList.of(new ExternalProject(p2.name, p2.key, p2.lead)));
		Mockito.when(page.getProjectSuggestionsModel()).thenCallRealMethod();

		String model = page.getProjectSuggestionsModel();

		final Object o = new ObjectMapper().readValue(model, new TypeReference<List<ProjectModel>>() {});
		Assert.assertEquals(o, ImmutableList.of(p1, p2));
	}

}
