/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BugzillaClient extends HttpDownloader {

	private static final int MAX_LINE_READ = 50;
	private final SiteConfiguration urlBean;

	private HttpContext httpContext;

	private boolean logged;

	public BugzillaClient(SiteConfiguration urlBean) {
		super();
		this.urlBean = urlBean;
	}

	public Map<String,String> validateBugzillaUrl() {
		DefaultHttpClient client = createHttpClient();
		try {
			final HttpGet get = new HttpGet(urlBean.getUrl());
			final HttpResponse status = client.execute(get);
			try {
				if (status.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					final String encoding = StringUtils.defaultIfEmpty(
							EntityUtils.getContentCharSet(status.getEntity()), HTTP.DEFAULT_CONTENT_CHARSET);
					if (hasBugzillaTitle(status.getEntity().getContent(), encoding, MAX_LINE_READ)) {
						return Collections.emptyMap();
					} else {
						return MapBuilder.build(
								getI18nBean().getText("jira-importer-plugin.external.bugzilla.site.url.notdetected"),
								null);
					}
				} else {
					return MapBuilder.build(getI18nBean().getText("jira-importer-plugin.external.bugzilla.site.url.notdetected"),
							getI18nBean().getText("jira-importer-plugin.importer.site.url.httperrorcode",
								Integer.toString(status.getStatusLine().getStatusCode()), get.getURI().toString()));
				}
			} finally {
				EntityUtils.consume(status.getEntity());
			}
		} catch (Exception e) {
			return MapBuilder.build(getI18nBean().getText("jira-importer-plugin.importer.site.connection.failed",
					urlBean.getUrl(), e.getMessage() == null ? e.getCause().getMessage() : e.getMessage()), null);
		}
	}

	public boolean containsLoginForm(File input, String encoding) throws IOException {
		InputStream is = new FileInputStream(input);
		try {
			return containsLoginForm(is, encoding);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private boolean containsLoginForm(InputStream is, String encoding) throws IOException {
		LineIterator iter = IOUtils.lineIterator(is, StringUtils.isNotBlank(encoding)
				? encoding : client.getParams().getParameter(AllClientPNames.HTTP_CONTENT_CHARSET).toString());
		boolean login = false, password = false, go = false;
		while(iter.hasNext()) {
			String line = iter.nextLine();
			login |= line.contains("name=\"Bugzilla_login\"");
			password |= line.contains("name=\"Bugzilla_password\"");
			go |= line.contains("name=\"GoAheadAndLogIn\"");
		}
		return login && password && go;
	}

	@Override
	protected void validateAttachment(String ixBug, HttpResponse get, File file) throws IOException {
		if (get.getEntity().getContentType().getValue().startsWith("text/html")) {
			if (containsLoginForm(file, StringUtils.defaultIfEmpty(
					EntityUtils.getContentCharSet(get.getEntity()), HTTP.DEFAULT_CONTENT_CHARSET))) {
				throw new IOException(
					String.format("Authentication required to access attachment %s.", ixBug));
			}
		}
	}

	public File getAttachment(String ixBug, String attachId) throws IOException {
		final String attachUrl = urlBean.getUrl() + "/attachment.cgi?id=" + attachId;
		return getAttachmentFromUrl(httpContext, ixBug, attachUrl);
	}

	public void login() throws IOException {
		if (! logged) {
			String error;
			httpContext = createHttpContext();
			if ((error = doLogin(client, httpContext)) != null) {
				throw new IOException(error);
			} else {
				logged = true;
			}
		}
	}

	public void logout() throws IOException {
		logged = false;
		httpContext = null;
		client.getConnectionManager().shutdown();
	}

	@Nullable
	private String doLogin(HttpClient client, HttpContext httpContext) {
		HttpPost post = new HttpPost(urlBean.getUrl() + "/index.cgi");

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Lists.<NameValuePair>newArrayList(
					new BasicNameValuePair("Bugzilla_login", urlBean.getUsername()),
					new BasicNameValuePair("Bugzilla_password", urlBean.getPassword()),
					new BasicNameValuePair("Bugzilla_restrictlogin", "checked"),
					new BasicNameValuePair("GoAheadAndLogIn", "Log In")
			));

			post.setEntity(entity);

			final HttpResponse status = client.execute(post, httpContext);
			try {
				if (status.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					final InputStream is = status.getEntity().getContent();
					if (is != null) {
						try {
							if (containsError(is, StringUtils.defaultIfEmpty(
									EntityUtils.getContentCharSet(post.getEntity()), HTTP.DEFAULT_CONTENT_CHARSET))) {
								return getI18nBean().getText("jira-importer-plugin.importer.site.credentials.invalid");
							}
						} finally {
							IOUtils.closeQuietly(is);
						}
					}

					Collection<String> missingCookies = getMissingCookies(httpContext, ImmutableSet.of("Bugzilla_logincookie"));
					if (!missingCookies.isEmpty()) {
						return getI18nBean().getText("jira-importer-plugin.importer.missing.cookies",
								StringUtils.join(missingCookies, ","));
					}
					return null;
				} else {
					return getI18nBean().getText("jira-importer-plugin.importer.site.url.httperrorcode") + " " + status;
				}
			} finally {
				EntityUtils.consume(status.getEntity());
			}
		} catch(Exception e) {
			return getI18nBean().getText("jira-importer-plugin.importer.site.connection.failed", urlBean.getUrl(), e.getMessage());
		}
	}

	public String validateCredentials() {
		HttpClient client = createHttpClient();
		return doLogin(client, createHttpContext());
	}

	private boolean containsError(InputStream is, String encoding) throws IOException {
		LineIterator iter = IOUtils.lineIterator(is, encoding);
		while(iter.hasNext()) {
			String line = iter.nextLine();
			if(line.contains("id=\"error_msg\"") || line.contains("class=\"throw_error\"")) {
				return true;
			}
		}
		return false;
	}

	private static final java.util.regex.Pattern pattern = java.util.regex.Pattern
			.compile("<title>.*</title>");

	public static boolean hasBugzillaTitle(InputStream in, String encoding, final int maxLineRead) throws IOException {
		BufferedReader isr = new BufferedReader(new InputStreamReader(in, encoding));
		try {
			int counter = 0;
			for (String line = isr.readLine(); line != null; line = isr.readLine(), counter++) {
				if (pattern.matcher(line).find()) {
					return true;
				}
				if (counter == maxLineRead) {
					return false;
				}
			}
			return false;
		} finally {
			IOUtils.closeQuietly(isr);
		}
	}

	protected I18nHelper getI18nBean() {
		return ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper();
	}

	public Map<String, String> validateConnection() {
		final Map<String, String> errors = Maps.newHashMap();

		errors.putAll(validateBugzillaUrl());

		if (errors.isEmpty()) {
			if (urlBean.isUseCredentials()) {
				String error = validateCredentials();
				if (error != null) {
					errors.put(error, null);
				}
			}
		}

		return errors;
	}

	public SiteConfiguration getUrlBean() {
		return urlBean;
	}

	public boolean isAuthenticated() {
		return logged;
	}
}
