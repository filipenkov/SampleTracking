/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import java.util.Collection;

public class AreaParser {

	public Collection<ExternalComponent> getAreas(Element response) {
		return Lists.newArrayList(
				Collections2.transform(XmlUtil.getChildren(response.getChild("areas"), "area"), new Function<Element, ExternalComponent>() {
					public ExternalComponent apply(Element from) {
						return getArea(from);
					}
				}));
	}

	public ExternalComponent getArea(Element element) {
		return new ExternalComponent(element.getChildText("sArea"),
				StringUtils.defaultIfEmpty(element.getChildText("ixArea"), null),
				StringUtils.defaultIfEmpty(element.getChildText("ixPersonOwner"), null), null);
	}

}
