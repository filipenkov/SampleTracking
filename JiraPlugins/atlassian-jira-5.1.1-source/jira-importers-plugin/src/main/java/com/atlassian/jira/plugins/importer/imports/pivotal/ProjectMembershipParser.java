/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import java.util.List;
import java.util.Locale;

public class ProjectMembershipParser {
	private final UserNameMapper userNameMapper;

	public ProjectMembershipParser(UserNameMapper userNameMapper) {
		this.userNameMapper = userNameMapper;
	}

	public ExternalUser parseUser(String projectId, Element element) {
		final Element person = element.getChild("person");
		final String email = person.getChildText("email");
		final String name = userNameMapper.getUsernameForLoginName(person.getChildText("name"));
		final ExternalUser externalUser = new ExternalUser(name.toLowerCase(Locale.ENGLISH), name, email);
		final String role = element.getChildText("role");
		if (StringUtils.isNotBlank(role)) {
			externalUser.addRole(projectId, role);
		}
		return externalUser;
	}

	public List<ExternalUser> parseUsers(final String projectId, Element memberships) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(memberships, "membership"), new Function<Element, ExternalUser>() {
					public ExternalUser apply(Element from) {
						return parseUser(projectId, from);
					}
				}));
	}

}
