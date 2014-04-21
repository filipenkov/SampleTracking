/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class MantisClient extends HttpDownloader {

	private final SiteConfiguration urlBean;

	private boolean logged;

	private HttpContext httpContext;

	public MantisClient(SiteConfiguration urlBean) {
		super();
		this.urlBean = urlBean;
	}

	public Map<String,String> validateUrl() {
		DefaultHttpClient client = createHttpClient();
		HttpHead get = new HttpHead(urlBean.getUrl() + "/main_page.php");
		try {
			final HttpResponse status = client.execute(get);
			try {
				if (status.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (status.getFirstHeader("refresh") != null) {
						return MapBuilder.build(getI18nBean().getText("jira-importer-plugin.external.mantis.uses.iis.mode"),
								getI18nBean().getText("jira-importer-plugin.external.mantis.uses.iis.mode.hint"));
					} else {
						return Collections.emptyMap();
					}
				} else {
					return MapBuilder.build(getI18nBean().getText("jira-importer-plugin.external.mantis.site.url.notdetected"),
							getI18nBean().getText("jira-importer-plugin.importer.site.url.httperrorcode",
									Integer.toString(status.getStatusLine().getStatusCode()), get.getURI().toString()));
				}
			} finally {
				EntityUtils.consume(status.getEntity());
			}
		} catch (Exception e) {
			return MapBuilder.build(getI18nBean().getText("jira-importer-plugin.importer.site.connection.failed",
                    urlBean.getUrl(), e.getMessage()), null);
		}
	}

	public File getAttachment(String ixBug, String attachId) throws IOException {
		final String attachUrl = urlBean.getUrl() + "/file_download.php?type=bug&file_id=" + attachId;
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
	private String doLogin(DefaultHttpClient client, HttpContext httpContext) {
		HttpPost post = new HttpPost(urlBean.getUrl() + "/login.php");
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Lists.<NameValuePair>newArrayList(
					new BasicNameValuePair("username", urlBean.getUsername()),
					new BasicNameValuePair("password", urlBean.getPassword()),
					new BasicNameValuePair("secure_session", "on"),
					new BasicNameValuePair("return", "index.php")
			));

			post.setEntity(entity);

			final HttpResponse status = client.execute(post, httpContext);
			try {
				if (status.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
					if (status.getFirstHeader("Location") != null
							&& status.getFirstHeader("Location").getValue().contains("login_cookie_test.php?return=")) {
						return null;
					} else {
						return getI18nBean().getText("jira-importer-plugin.importer.site.credentials.invalid");
					}
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
		return doLogin(createHttpClient(), createHttpContext());
	}

	protected I18nHelper getI18nBean() {
		return ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper();
	}

	public Map<String, String> validateConnection() {
        final Map<String, String> errors = Maps.newHashMap();

		errors.putAll(validateUrl());

		if(errors.isEmpty()) {
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
