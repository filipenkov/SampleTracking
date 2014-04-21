/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web;

public interface RemoteSiteImporterSetupPage {

	final String CONNECTION_BEAN_URL = "siteUrl";
	final String CONNECTION_BEAN_USERNAME = "siteUsername";
	final String CONNECTION_BEAN_PASSWORD = "sitePassword";

	boolean isSiteUrlRequired();

	String getSiteUrl();

	void setSiteUrl(String siteUrl);

	String getSiteUsername();

	void setSiteUsername(String siteUsername);

	String getSitePassword();

	void setSitePassword(String sitePassword);

	void addErrorMessage(String errorMessage);

	void addError(String fieldName, String errorMessage);

	boolean getSiteCredentials();

  	public String getText(String key);

	String getLoginLabel();

	String getPasswordLabel();
}
