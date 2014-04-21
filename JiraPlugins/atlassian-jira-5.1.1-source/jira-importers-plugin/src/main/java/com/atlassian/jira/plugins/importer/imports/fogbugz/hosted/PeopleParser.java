/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.jdom.Element;

import java.util.Collection;

public class PeopleParser {

	public Collection<ExternalUser> getPeople(Element response) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(response.getChild("people"), "person"), new Function<Element, ExternalUser>() {
					public ExternalUser apply(Element from) {
						return getPerson(from);
					}
				}));
	}

	public ExternalUser getPerson(Element element) {
		final String id = element.getChildText("ixPerson");
		final String name = element.getChildText("sFullName");
		final String email = element.getChildText("sEmail");
		final ExternalUser user = new ExternalUser(email, name, email);
		user.setId(id);
		return user;
	}

}
