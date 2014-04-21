/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import net.jcip.annotations.NotThreadSafe;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;

@NotThreadSafe
public class SiteConfiguration {

	private final String url;
	private final String username;
	private final String password;
	private final boolean useCredentials;

	/* used only by tests */
	public SiteConfiguration(final String url) {
		this(url, false, null, null);
	}

	public SiteConfiguration(final String url, boolean useCredentials,
			@Nullable final String username, @Nullable final String password) {
		this.useCredentials = useCredentials;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public boolean isValidUrl() {
		try {
			final URL u = new URL(url);
			if (!"http".equals(u.getProtocol()) && !"https".equals(u.getProtocol())) {
				return false;
			}
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isUseCredentials() {
		return useCredentials;
	}
}
