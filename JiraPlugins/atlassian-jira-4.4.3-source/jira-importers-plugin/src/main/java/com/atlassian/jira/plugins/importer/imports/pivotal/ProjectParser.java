/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.XmlUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.jdom.Element;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ProjectParser {

	public String getName(Element element) {
		return element.getChildText("name");
	}

	@Nullable
	public String getChildText(Element element, String ... childrenNames) {
		for (String childrenName : childrenNames) {
			element = element.getChild(childrenName);
			if (element == null) {
				return null;
			}
		}
		return element.getText();
	}

	public Collection<String> getProjectNames(Element projects) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(projects, "project"), new Function<Element, String>() {
					public String apply(Element from) {
						return getName(from);
					}
				}));
	}

	public Collection<ExternalProject> getProjects(Element projects) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(projects, "project"), new Function<Element, ExternalProject>() {
					public ExternalProject apply(Element from) {
						return getProject(from);
					}
				}));
	}

	public ExternalProject getProject(Element element) {
		final String id = element.getChildText("id");
		final String name = getName(element);
		final ExternalProject project = new ExternalProject(name, null);
		project.setLead(getProjectOwner(element));
		project.setId(id);
		return project;
		/*
  <project>
    <id>232465</id>
    <name>My Sample Project</name>
    <iteration_length type="integer">1</iteration_length>
    <week_start_day>Monday</week_start_day>
    <point_scale>0,1,2,3</point_scale>
    <account>wseliga</account>
    <velocity_scheme>Average of 3 iterations</velocity_scheme>
    <current_velocity>8</current_velocity>
    <initial_velocity>10</initial_velocity>
    <number_of_done_iterations_to_show>12</number_of_done_iterations_to_show>
    <labels>admin,blog,cart,checkout,deployment,design,epic,featured products,ie6,needs discussion,orders,reporting,search,shopper accounts,shopping,signup / signin,usability,user generated content</labels>
    <allow_attachments>true</allow_attachments>
    <public>false</public>
    <use_https>false</use_https>
    <bugs_and_chores_are_estimatable>false</bugs_and_chores_are_estimatable>
    <commit_mode>false</commit_mode>
    <memberships>
      <membership>
        <id>697209</id>
        <person>
          <name>wseliga</name>
          <initials>WS</initials>
        </person>
        <role>Owner</role>
      </membership>
      <membership>
        <id>697259</id>
        <person>
          <name>Test Member</name>
          <initials>TM</initials>
        </person>
        <role>Viewer</role>
      </membership>
    </memberships>
    <integrations>
    </integrations>
  </project>

		 */
	}

	@Nullable
	private String getProjectOwner(Element element) {
		final List<Element> membershipElements = XmlUtil.getChildren(element.getChild("memberships"), "membership");
		for (Element membershipElement : membershipElements) {
			if ("Owner".equals(membershipElement.getChildText("role"))) {
				final String ownerName = getChildText(membershipElement, "person", "name");
				if (ownerName != null) {
					return ownerName;
				}
			}
		}
		return null;
	}
}
