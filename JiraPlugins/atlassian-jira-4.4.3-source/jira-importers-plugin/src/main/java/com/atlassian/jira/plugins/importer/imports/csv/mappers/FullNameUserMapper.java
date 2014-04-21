/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.google.common.collect.Multimap;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * CSV user mapper that assumes the name in the format "John Smith". "John Smith" is used as full name and "johnsmith" as
 * username. Inexplicably, the e-mail address is "jsmith@example.com"
 */
public class FullNameUserMapper implements ExternalUserMapper {
	private String fullNameField;
	private String emailSuffix;

	public FullNameUserMapper(String fullNameField) {
		this.fullNameField = fullNameField;
		this.emailSuffix = DEFAULT_EMAIL_SUFFIX;
	}

	public FullNameUserMapper(String fullNameField, String emailSuffix) {
		if (emailSuffix != null) {
			this.emailSuffix = emailSuffix;
		} else {
			this.emailSuffix = DEFAULT_EMAIL_SUFFIX;
		}
		this.fullNameField = fullNameField;
	}


	@Nullable
	public ExternalUser buildFromMultiMap(Multimap<String, String> bean) {
		ExternalUser externaluser = null;

		if (!bean.get(fullNameField).isEmpty()) {
			String fullName = (String) ((Collection) bean.get(fullNameField)).iterator().next();
			// Remove illegal chars for the username
			String username = StringUtils.replaceChars(fullName, " '()", "");

			// Reset the field
			// TODO: Why are we removing 'fullNameField'
			bean.removeAll(fullNameField);
			if (StringUtils.isNotEmpty(username)) {
				externaluser = new ExternalUser();
				externaluser.setFullname(fullName);
				externaluser.setName(username.toLowerCase());

				externaluser.setEmail(extractEmail(fullName));

				// TODO: Why are we putting a username into 'fullNameField'?
				bean.put(fullNameField, username.toLowerCase());
			}
		}

		return externaluser;
	}

	private String extractEmail(String fullName) {
		String s;
		// JRA-15322: if the user's name looks like an email address, use that
		if (TextUtils.verifyEmail(fullName)) {
			s = fullName;
		} else if (StringUtils.contains(fullName, " ")) {
			s = fullName.substring(0, 1) + StringUtils.substringAfterLast(fullName, " ") + emailSuffix;
		} else {
			s = fullName + emailSuffix;
		}

		return s.toLowerCase();
	}
}
