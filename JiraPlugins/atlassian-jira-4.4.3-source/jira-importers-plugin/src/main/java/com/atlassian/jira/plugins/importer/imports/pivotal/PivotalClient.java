/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.util.AttachmentUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PivotalClient {
	public static final String PIVOTAL_HOST = "www.pivotaltracker.com";
	public static final String PIVOTAL_ROOT_URL = "https://" + PIVOTAL_HOST + "/services/v3/";
	public static final String PIVOTAL_SIGNIN_URL = "https://" + PIVOTAL_HOST + "/signin";
	public static final String PIVOTAL_TIMESHIFTS_URL = "https://" + PIVOTAL_HOST + "/time_shifts";
	public static final String PIVOTAL_NOT_ALLOWED_URL = "https://" + PIVOTAL_HOST + "/not_allowed";
	public static final String PIVOTAL_AUTH_COOKIE = "auth_token";

	private String token;
	private final URI rootUri;
	private DefaultHttpClient authenticatedHttpClient;
	private HttpContext httpContext;

	public PivotalClient() {
		this(toUri(PIVOTAL_ROOT_URL));
	}

	private static URI toUri(String url) {
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	PivotalClient(URI rootUri) {
		this.rootUri = rootUri;
	}

	public void login(String username, String password) throws PivotalRemoteException {
		token = null;
		httpContext = new BasicHttpContext();
		authenticatedHttpClient = new DefaultHttpClient();
		authenticatedHttpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(username, password));
		authenticatedHttpClient.getParams().setBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, true);

		final InputStream is = openGet(UriBuilder.fromUri(rootUri).path("tokens/active").build().toString());
		try {
			final SAXBuilder builder = XmlUtil.getSAXBuilder();
			final Document document = builder.build(is);
			token = document.getRootElement().getChildText("guid");
			if (token == null) {
				throw new PivotalRemoteException("Login failed. Authentication token has not been served by Pivotal Tracker");
			}
		} catch (Exception e) {
			throw new PivotalRemoteException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public void logout() {
		token = null;
		httpContext = null;

		if (authenticatedHttpClient != null) {
			authenticatedHttpClient.getConnectionManager().shutdown();
			authenticatedHttpClient = null;
		}
	}

	private void download(String url, File destination) throws IOException {
		OutputStream outputStream = null;
		ensureLoggedInToWeb();
		final InputStream inputStream = openGet(url);
		try {
			outputStream = FileUtils.openOutputStream(destination);
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			throw new PivotalRemoteException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

	private InputStream openGet(String url) throws PivotalRemoteException {
		final HttpGet getMethod = new HttpGet(url);
		try {
			final HttpResponse response = authenticatedHttpClient.execute(getMethod, httpContext);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new PivotalHttpException("HTTP Error code " + response + " returned by Pivotal Tracker",
						response.getStatusLine());
			}
			final InputStream is = response.getEntity().getContent();
			if (is == null) {
				throw new PivotalRemoteException("Connection failed. Empty response returned by Pivotal Tracker");
			}
			return is;
		} catch (PivotalRemoteException e) {
			throw e;
		} catch (IOException e) {
			throw new PivotalRemoteException(e);
		}
	}

	protected void ensureLoggedInToWeb() throws IOException {
		final BrowserCompatSpec matcher = new BrowserCompatSpec();
		final CookieStore store = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		final List<Cookie> cookies = store != null ? store.getCookies() : Collections.<Cookie>emptyList();

		for (Cookie cookie : cookies) {
			if (PIVOTAL_AUTH_COOKIE.equals(cookie.getName()) &&
					matcher.match(cookie, new CookieOrigin(PIVOTAL_HOST, 80, "/", false))) {
				return;
			}
		}

		final HttpPost signInPost = new HttpPost(PIVOTAL_SIGNIN_URL);
		final UsernamePasswordCredentials credentials =
				(UsernamePasswordCredentials) authenticatedHttpClient.getCredentialsProvider().getCredentials(
						AuthScope.ANY);

		final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Lists.<NameValuePair>newArrayList(
				new BasicNameValuePair("credentials[username]", credentials.getUserName()),
				new BasicNameValuePair("credentials[password]", credentials.getPassword()),
				new BasicNameValuePair("time_zone_offset", "0")
		));

		signInPost.setEntity(entity);

		final HttpResponse response = authenticatedHttpClient.execute(signInPost, httpContext);
		// no redirects by default for POST - see org.apache.http.impl.client.DefaultRequestDirector
		try {
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY
					|| PIVOTAL_SIGNIN_URL.equals(getRedirectLocation(response))) {
				throw new PivotalRemoteException("Failed to authenticate Pivotal connection");
			}
		} finally {
			EntityUtils.consume(response.getEntity());
		}
	}


	private Element getWithToken(URI uri) throws PivotalRemoteException {
		final HttpClient httpClient = getHttpClient();
		final HttpGet getMethod = new HttpGet(uri.toString());
		getMethod.addHeader("X-TrackerToken", token);
		final HttpResponse response;
		InputStream is = null;
		try {
			response = httpClient.execute(getMethod, httpContext);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new PivotalRemoteException("HTTP Error code " + response + " returned by Pivotal Tracker");

			}
			is = response.getEntity().getContent();
			if (is == null) {
				throw new PivotalRemoteException("Login failed. Empty response returned by Pivotal Tracker");
			}
			final Document document = XmlUtil.getSAXBuilder().build(is);
			return document.getRootElement();
		} catch (PivotalRemoteException e) {
			throw e;
		} catch (IOException e) {
			throw new PivotalRemoteException(e);
		} catch (JDOMException e) {
			throw new PivotalRemoteException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private HttpClient getHttpClient() {
		final DefaultHttpClient client = new DefaultHttpClient();
		HttpDownloader.setDefaultProxy(client);
		return client;
	}

	public Collection<String> getAllProjectNames() throws PivotalRemoteException {
		final Collection<ExternalProject> allProjects = getAllProjects(ConsoleImportLogger.INSTANCE);
		return Collections2.transform(allProjects, new Function<ExternalProject, String>() {
			@Override
			public String apply(ExternalProject input) {
				return input.getName();
			}
		});
	}

	public Collection<ExternalProject> getAllProjects(ImportLogger log) throws PivotalRemoteException {
		final URI uri = UriBuilder.fromUri(rootUri).path("projects").build();
		final Element projects = getWithToken(uri);
		return new ProjectParser().getProjects(projects);
	}

	public List<ExternalIssue> getStories(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		final URI uri = UriBuilder.fromUri(rootUri).path("projects").path(projectId).path("stories").build();
		final Element projects = getWithToken(uri);
		return new StoryParser().parseStories(projects);
	}

	public Collection<PivotalIteration> getIterations(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		final URI uri = UriBuilder.fromUri(rootUri).path("projects").path(projectId).path("iterations").build();
		final Element projects = getWithToken(uri);
		return new IterationParser().parseIterations(projects);
	}

	public Collection<ExternalUser> getMembers(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		final URI uri = UriBuilder.fromUri(rootUri).path("projects").path(projectId).path("memberships").build();
		final Element memberships = getWithToken(uri);
		return new ProjectMembershipParser().parseUsers(projectId, memberships);
	}

	public List<ExternalWorklog> getWorklog(long projectId, ImportLogger importLogger) throws IOException {
		ensureLoggedInToWeb();
		final HttpGet timeShiftsGet = new HttpGet(PIVOTAL_TIMESHIFTS_URL + "?"
			+ URLEncodedUtils.format(Lists.<NameValuePair>newArrayList(
				new BasicNameValuePair("date_period[start]", "1/1/1990"),
				new BasicNameValuePair("date_period[finish]", "1/1/2100"),
				new BasicNameValuePair("project", String.valueOf(projectId)),
				new BasicNameValuePair("person", "none"),
				new BasicNameValuePair("grouped_by", "none"),
				new BasicNameValuePair("commit", "Export to CSV")), "UTF-8"));
		timeShiftsGet.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
		final HttpResponse response = authenticatedHttpClient.execute(timeShiftsGet, httpContext);
		try {
			if (PIVOTAL_NOT_ALLOWED_URL.equals(getRedirectLocation(response))) {
				importLogger.log("Time tracking is not enabled for this account in Pivotal Tracker, skipping.");
				return Collections.emptyList();
			}

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				importLogger.fail(null, "Error downloading time tracking information from Pivotal Tracker: "
						+ response.getStatusLine().toString());
				return Collections.emptyList();
			}
			final Header header = response.getEntity().getContentType();
			final String contentType = header != null ? header.getValue() : null;
			if (!StringUtils.startsWithIgnoreCase(contentType, "text/csv")) {
				importLogger.fail(null, "Error downloading time tracking information from Pivotal Tracker: expected text/csv content type; got "
								+ contentType);
				return Collections.emptyList();
			}
			return new PivotalTimeShiftsParser(EntityUtils.toString(response.getEntity(), "UTF-8")).parseWorklog();
		} finally {
			EntityUtils.consume(response.getEntity());
		}
	}

	public boolean isLoggedIn() {
		return token != null;
	}

	public Collection<ExternalAttachment> getAttachmentsForIssue(ExternalIssue externalIssue, ImportLogger log) {
		final List<ExternalAttachment> externalAttachments = externalIssue.getAttachments();

		final List<ExternalAttachment> result = Lists.newArrayListWithCapacity(externalAttachments.size());
		for (ExternalAttachment attachment : externalAttachments) {
			final PivotalExternalAttachment pivotalAttachment = (PivotalExternalAttachment) attachment;
			try {
				final File file = File.createTempFile("pivotalAttachment-", ".tmp", getTempDir());
				download(pivotalAttachment.getUrl(), file);
				pivotalAttachment.setResolvedFile(file);
				result.add(pivotalAttachment);
			} catch (IOException e) {
				log.fail(e, "Error retrieving Pivotal attachment");
			}
		}
		return result;
	}

	protected File getTempDir() {
		return AttachmentUtils.getTemporaryAttachmentDirectory();
	}

	@Nullable
	private String getRedirectLocation(HttpResponse response) {
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
			return null;
		}
		final Header location = response.getFirstHeader("location");
		return location == null ? null : location.getValue();
	}
}
