/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class XmlUtil {
	@SuppressWarnings("unchecked")
	public static List<Element> getChildren(@Nullable Element parent, String name) {
		if (parent == null) {
			return Collections.emptyList();
		}
		return parent.getChildren(name);
	}

	public static SAXBuilder getSAXBuilder() {
		final SAXBuilder builder = new SAXBuilder(false);
		builder.setFeature("http://xml.org/sax/features/validation", false);
    	builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		return builder;
	}
}
