/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import java.util.Collection;

public class FixForParser {

	public Collection<ExternalVersion> getFixFors(Element response) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(response.getChild("fixfors"), "fixfor"), new Function<Element, ExternalVersion>() {
					public ExternalVersion apply(Element from) {
						return getFixFor(from);
					}
				}));
	}

	public ExternalVersion getFixFor(Element element) {
		final ExternalVersion version = new ExternalVersion();
		version.setName(element.getChildText("sFixFor"));
		version.setId(element.getChildText("ixFixFor"));

		String dt = element.getChildText("dt");
		if (StringUtils.isNotEmpty(dt)) {
			version.setReleaseDate(CaseParser.parseDateTime(dt).toDate());
		}
		return version;
	}
}
