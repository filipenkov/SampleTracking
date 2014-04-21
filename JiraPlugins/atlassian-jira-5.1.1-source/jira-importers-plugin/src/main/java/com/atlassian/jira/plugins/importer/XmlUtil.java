/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
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
		// it looks like XInclude is disabled by default. I am not switching it to false, as we would risk UnsupportedOperationExceptions
		final SAXBuilder builder = new SAXBuilder(false);
		builder.setExpandEntities(false);
		builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		builder.setFeature("http://xml.org/sax/features/validation", false);
    	builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		return builder;
	}
}
