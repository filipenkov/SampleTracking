/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.jdom.Element;

import java.util.Collection;

public class ProjectParser {

	public String getName(Element element) {
		return element.getChildText("sProject");
	}

	public Collection<String> getProjectNames(Element response) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(response.getChild("projects"), "project"), new Function<Element, String>() {
					public String apply(Element from) {
						return getName(from);
					}
				}));
	}

	public Collection<ExternalProject> getProjects(Element response) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(response.getChild("projects"), "project"), new Function<Element, ExternalProject>() {
					public ExternalProject apply(Element from) {
						return getProject(from);
					}
				}));
	}

	public ExternalProject getProject(Element element) {
		final String id = element.getChildText("ixProject");
		final String name = getName(element);
		final ExternalProject project = new ExternalProject(name, null);
		project.setLead(element.getChildText("sEmail"));
		project.setId(id);
		return project;
	}


}
