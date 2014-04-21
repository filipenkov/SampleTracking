/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.jdom.Element;

import java.util.Set;

public class StatusParser {

	public Set<String> getStatuses(Element response) {
		return Sets.newHashSet(
				Collections2.transform(XmlUtil.getChildren(response.getChild("statuses"), "status"),
						new Function<Element, String>() {
							@Override
							public String apply(Element from) {
								return getStatusName(from);
							}
						}));
	}

	public String getStatusName(Element element) {
		return element.getChildText("sStatus");
	}
}
