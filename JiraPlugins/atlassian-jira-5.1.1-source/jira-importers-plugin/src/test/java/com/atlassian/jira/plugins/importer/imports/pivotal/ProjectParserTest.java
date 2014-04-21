/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.IterableMatcher;
import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ProjectParserTest {
	private final ProjectParser projectParser = new ProjectParser(new MockUserNameMapper("wseliga", "Translated wseliga"));
	@Test
	public void testGetProjectNames() throws Exception {
		final Element rootElement = getRootElement("/pivotal/projects.xml");
		final Collection<String> projectNames = projectParser.getProjectNames(rootElement);
		Assert.assertThat(projectNames, IterableMatcher.hasOnlyElements("My Sample Project", "My Test Project"));
	}

	public static Element getRootElement(String resourcePath) throws JDOMException, IOException {
		final SAXBuilder builder = XmlUtil.getSAXBuilder();
		return builder.build(ProjectParserTest.class.getResourceAsStream(resourcePath)).getRootElement();
	}

	@Test
	public void testGetProject() throws JDOMException, IOException {
		final Element projectElement = getRootElement("/pivotal/projects.xml").getChild("project");

		final ExternalProject project = projectParser.getProject(projectElement);
		assertEquals("232465", project.getId());
		assertEquals("My Sample Project", project.getName());
		assertEquals("Translated wseliga", project.getLead());
	}
}
