/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

/**
 * CSV user mapper that assumes the name in the format "John Smith". "John Smith" is used as full name and "jsmith" as username.
 *
 * This class can be used by reflection.
 */
@SuppressWarnings("unused")
public class FirstInitialFirstNameUserMapper implements ExternalUserMapper {
	private String nameField;
	private String emailSuffix;

	public FirstInitialFirstNameUserMapper(String nameField) {
		this.nameField = nameField;
		this.emailSuffix = DEFAULT_EMAIL_SUFFIX;
	}

	public FirstInitialFirstNameUserMapper(String nameField, String emailSuffix) {
		if (emailSuffix != null) {
			this.emailSuffix = emailSuffix;
		} else {
			this.emailSuffix = DEFAULT_EMAIL_SUFFIX;
		}
		this.nameField = nameField;
	}


	public ExternalUser buildFromMultiMap(Multimap<String, String> bean) {
		ExternalUser externaluser = null;

		if (!bean.get(nameField).isEmpty()) {
			String fullName = (String) ((Collection) bean.get(nameField)).iterator().next();
			String username = extractUsername(fullName);

			// Reset the field
			bean.removeAll(nameField);
			if (StringUtils.isNotEmpty(username)) {
				final String name = username.toLowerCase();

				externaluser = new ExternalUser();
				externaluser.setFullname(fullName);
				externaluser.setName(name);
				externaluser.setEmail(name + emailSuffix);

				bean.put(nameField, name);
			}
		}

		return externaluser;

	}

	public static String extractUsername(String fullName) {
		String username;
		if (StringUtils.contains(fullName, " ")) {
			username = StringUtils.replaceChars(fullName, "-'()", "");
			username = StringUtils.substring(username, 0, 1) + StringUtils.substringAfter(username, " ");
			username = StringUtils.replaceChars(username, " ", "");
		} else {
			username = fullName;
		}
		return StringUtils.lowerCase(username);
	}

}
