/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class TextsUtil {

	public static ResourceBundle getTexts(JiraWebActionSupport as, String bundleName) {
		try {
			return as.getTexts(bundleName);
		} catch (MissingResourceException e) {
			if (bundleName.startsWith("com.atlassian.jira.plugins.importer")) {
				// we have all messages in one file
				return ResourceBundle
						.getBundle("com.atlassian.jira.plugins.importer.web.action.util.messages", as.getLocale(),
								as.getClass().getClassLoader());
			}
			throw e;
		}
	}

	public static String convertToNiceHtmlString(String str) {
		final StringBuilder res = new StringBuilder();
		final Iterable<String> splitStr = Splitter.on("\n").split(str);
		if (Iterables.size(splitStr) <= 1) {
			return str;
		}
		for (String line : splitStr) {
			res.append("<p>").append(line).append("</p>");
		}
		return res.toString();
	}

	public static String buildErrorMessage(final ErrorCollection errorCollection) {
		final String msg1 = StringUtils.join(errorCollection.getErrorMessages(), "\n");
		final ArrayList<String> sortedKeys = Lists.newArrayList(errorCollection.getErrors().keySet());
		Collections.sort(sortedKeys);

		final List<String> sortedValues = Lists.newArrayListWithCapacity(sortedKeys.size());
		for (String key : sortedKeys) {
			sortedValues.add(errorCollection.getErrors().get(key));
		}
		final String msg2 = StringUtils.join(sortedValues, "\n");
		return msg1 + (!StringUtils.isEmpty(msg1) && !StringUtils.isEmpty(msg2) ? "\n" : "") + msg2;
	}
}
