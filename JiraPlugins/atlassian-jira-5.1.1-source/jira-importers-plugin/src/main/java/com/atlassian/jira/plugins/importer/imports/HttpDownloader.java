/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.google.common.collect.Sets;
import org.apache.commons.httpclient.URI;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ProxySelector;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class HttpDownloader {

	protected final DefaultHttpClient client;

	private final static String PREFIX = "jira-importers-plugin-downloader-";
	private final static String TMP = ".tmp";

	public HttpDownloader() {
		client = createHttpClient();
	}

	public HttpContext createHttpContext() {
		final HttpContext context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
		return context;
	}

	protected DefaultHttpClient createHttpClient() {
		final DefaultHttpClient res = new DefaultHttpClient();
		res.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		setDefaultProxy(res);
		return res;
	}

	public static void setDefaultProxy(DefaultHttpClient client) {
		ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
        	client.getConnectionManager().getSchemeRegistry(), ProxySelector.getDefault());

		client.setRoutePlanner(routePlanner);
	}

	public File getAttachmentFromUrl(@Nullable HttpContext context, String ixBug, String attachUrl) throws IOException {
		if ("file".equals(new URI(attachUrl, false).getScheme())) {
			throw new IOException("For security reasons you are not allowed to use file protocol.");
		}

		final HttpGet get = new HttpGet(attachUrl);
		final HttpResponse response = client.execute(get, context);
		try {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final File file = File.createTempFile(PREFIX, TMP);
				final OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
				try {
					IOUtils.copy(response.getEntity().getContent(), os);
				} finally {
					IOUtils.closeQuietly(os);
				}

				validateAttachment(ixBug, response, file);

				return file;
			} else {
				throw new IOException(
						String.format("Error downloading attachment for %s from %s. Error: HTTP status code: %d", ixBug,
								attachUrl, response.getStatusLine().getStatusCode()));
			}
		} finally {
			EntityUtils.consume(response.getEntity());
		}
	}

	/**
	 * Override if you need some special check before attachment can be returned. Bugzilla needs it.
	 * @param get
	 * @param file
	 */
	protected void validateAttachment(String ixBug, HttpResponse get, File file) throws IOException {
		// do nothing
	}

	protected Collection<String> getMissingCookies(HttpContext context, Collection<String> requiredCookies) {
		CookieStore store = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
		if (store == null) {
			return requiredCookies;
		}

		List<Cookie> cookies = store.getCookies();
		if (cookies == null) {
			return requiredCookies;
		}

		final Set<String> receivedCookies = Sets.newHashSetWithExpectedSize(cookies.size());
		for(Cookie cookie : cookies) {
			receivedCookies.add(cookie.getName());
		}

		Set<String> result = Sets.newHashSet(requiredCookies);
		result.removeAll(receivedCookies);
		return result;
	}

}
