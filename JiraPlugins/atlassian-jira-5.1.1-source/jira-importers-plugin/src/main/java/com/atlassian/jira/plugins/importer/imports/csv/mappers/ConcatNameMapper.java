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
 * This name mapper is used for usernames that are in the format JSmith. Fullname will be "J. Smith" and username will be "jsmith".
 *
 * This class can be used by reflection.
 */
@SuppressWarnings("unused")
public class ConcatNameMapper implements ExternalUserMapper {
	private String concattedNameField;
	private String emailSuffix;

	public ConcatNameMapper(String concattedNameField) {
		this.concattedNameField = concattedNameField;
		this.emailSuffix = DEFAULT_EMAIL_SUFFIX;
	}

	public ConcatNameMapper(String concattedNameField, String emailSuffix) {
		if (emailSuffix != null) {
			this.emailSuffix = emailSuffix;
		} else {
			this.emailSuffix = DEFAULT_EMAIL_SUFFIX;
		}
		this.concattedNameField = concattedNameField;
	}

	public ExternalUser buildFromMultiMap(Multimap<String, String> bean) {
		ExternalUser externaluser = null;

		if (bean.get(concattedNameField) != null) {
			final String concattedName = (String) ((Collection) bean.get(concattedNameField)).iterator().next();
			final String username = extractUserName(concattedName);

			// Reset the field
			bean.removeAll(concattedNameField);
			if (StringUtils.isNotEmpty(username)) {
				externaluser = new ExternalUser();

				// Change to full name
				String fullName =
						concattedName.substring(0, 1) + ". " + concattedName.substring(1, concattedName.length());
				externaluser.setFullname(fullName);
				externaluser.setName(username.toLowerCase());
				externaluser.setEmail(username.toLowerCase() + emailSuffix);

				bean.put(concattedNameField, username.toLowerCase());
			}
		}

		return externaluser;

	}

	@Override
	public String extractUserName(String concattedName) {
		return StringUtils.replaceChars(concattedName, " '", "");
	}
}
