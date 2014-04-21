/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web;

import org.apache.commons.lang.StringUtils;

public class RemoteSiteValidator {
	public boolean validateConnection(RemoteSiteImporterSetupPage page) {
		boolean res = true;
		SiteConfiguration siteConfiguration = new SiteConfiguration(page.getSiteUrl(),
				page.getSiteCredentials(), page.getSiteUsername(), page.getSitePassword());

		if (!siteConfiguration.isValidUrl()) {
			page.addError(RemoteSiteImporterSetupPage.CONNECTION_BEAN_URL,
					page.getText("jira-importer-plugin.importer.site.url.invalid"));
			res = false;
		}

		if (siteConfiguration.isUseCredentials()) {
			if (StringUtils.isBlank(siteConfiguration.getUsername())) {
				page.addError(RemoteSiteImporterSetupPage.CONNECTION_BEAN_USERNAME, page.getText("jira-importer-plugin.username.missing"));
				res = false;
			}

			if (StringUtils.isBlank(siteConfiguration.getPassword())) {
				page.addError(RemoteSiteImporterSetupPage.CONNECTION_BEAN_PASSWORD, page.getText("jira-importer-plugin.password.missing"));
				res = false;
			}
		}
		return res;
	}

}
