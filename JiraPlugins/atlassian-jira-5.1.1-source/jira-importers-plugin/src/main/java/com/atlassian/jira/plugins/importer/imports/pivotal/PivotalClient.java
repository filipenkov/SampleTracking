/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.external.beans.NamedExternalObject;
import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.util.AttachmentUtils;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.impl.client.DefaultHttpClient;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private final UserNameMapper userNameMapper;
	private static final int DEFAULT_STORY_PAGINATION_LIMIT = 2000;
	private int storyPaginationLimit = DEFAULT_STORY_PAGINATION_LIMIT;

	private static final Pattern AUTHENTICITY_PATTERN = Pattern.compile("input +name=\"authenticity_token\" +type=\"hidden\" +value=\"([^\"]+)\"");

	public PivotalClient(UserNameMapper userNameMapper) {
		this(toUri(PIVOTAL_ROOT_URL), userNameMapper);
	}

	private static URI toUri(String url) {
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	PivotalClient(URI rootUri, UserNameMapper userNameMapper) {
		this.rootUri = rootUri;
		this.userNameMapper = userNameMapper;
	}

	public void login(String username, String password) throws PivotalRemoteException {
		token = null;
		httpContext = new BasicHttpContext();
		authenticatedHttpClient = getHttpClient();
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

	protected String fixDownloadUrl(String url) {
		return url.startsWith("http://") ? "https" + StringUtils.removeStart(url, "http") : url;
	}

	private void download(String url, File destination) throws IOException {
		OutputStream outputStream = null;
		final InputStream inputStream = openGet(fixDownloadUrl(url));
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
			final HttpResponse response = executeWithLogin(getMethod);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				EntityUtils.consume(response.getEntity());
				throw new PivotalHttpException("HTTP Error code " + response.getStatusLine() + " returned by Pivotal Tracker",
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

	private HttpResponse executeWithLogin(HttpGet get) throws IOException {
		get.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
		final HttpResponse response = authenticatedHttpClient.execute(get, httpContext);

		if (PIVOTAL_SIGNIN_URL.equals(getRedirectLocation(response))) {
			EntityUtils.consume(response.getEntity());
			ensureLoggedInToWeb();
			return authenticatedHttpClient.execute(get, httpContext);
		} else {
			return response;
		}
	}

	protected void ensureLoggedInToWeb() throws IOException {
		final String authenticityToken = fetchAuthenticityToken();

		final HttpPost signInPost = new HttpPost(PIVOTAL_SIGNIN_URL);
		final UsernamePasswordCredentials credentials =
				(UsernamePasswordCredentials) authenticatedHttpClient.getCredentialsProvider().getCredentials(
						AuthScope.ANY);

		final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Lists.<NameValuePair>newArrayList(
				new BasicNameValuePair("authenticity_token", authenticityToken),
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

	private	String fetchAuthenticityToken() throws IOException {
		final HttpGet signInGet = new HttpGet(PIVOTAL_SIGNIN_URL);
		final HttpResponse response = authenticatedHttpClient.execute(signInGet, httpContext);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new PivotalRemoteException("Failed to retrieve Pivotal sign-in form, authentication failed");
		}
		final Matcher matcher = AUTHENTICITY_PATTERN.matcher(EntityUtils.toString(response.getEntity()));
		if (!matcher.find()) {
			throw new PivotalRemoteException("Authenticity token not found in Pivotal sign-in form, authentication failed");
		}

		return matcher.group(1);
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

	private DefaultHttpClient getHttpClient() {
		final DefaultHttpClient client = new DefaultHttpClient();
		HttpDownloader.setDefaultProxy(client);
		return client;
	}

	public Collection<String> getAllProjectNames() throws PivotalRemoteException {
		final Collection<ExternalProject> allProjects = getAllProjects(UserNameMapper.NO_MAPPING);
		return Immutables.transformThenCopyToList(allProjects, NamedExternalObject.NAME_FUNCTION);
	}

	public Collection<ExternalProject> getAllProjects(ImportLogger log) throws PivotalRemoteException {
		return getAllProjects(userNameMapper);
	}

	private Collection<ExternalProject> getAllProjects(UserNameMapper userNameMapper) throws PivotalRemoteException {
		final URI uri = UriBuilder.fromUri(rootUri).path("projects").build();
		final Element projects = getWithToken(uri);
		return new ProjectParser(userNameMapper).getProjects(projects);
	}

	public List<ExternalIssue> getStories(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		final int paginationLimit = this.storyPaginationLimit; // let's copy it to behave well if another thread changes it
		final UriBuilder uriBuilder = UriBuilder.fromUri(rootUri).path("projects").path(projectId).path("stories").queryParam("limit", paginationLimit);
		final StoryParser storyParser = new StoryParser(userNameMapper);
		final List<ExternalIssue> stories = Lists.newArrayList();
		for (int offset = 0;;offset += paginationLimit) {
			importLogger.log("Retrieving Pivotal stories (%d to %d).", offset, (offset + paginationLimit - 1));
			final URI uri = uriBuilder.replaceQueryParam("offset", offset).build();
			final List<ExternalIssue> storiesPage = storyParser.parseStories(getWithToken(uri));
			stories.addAll(storiesPage);
			if (storiesPage.size() != paginationLimit) {
				break;
			}
		}
		importLogger.log("Retrieved in total %d Pivotal stories.", stories.size());
		return stories;
	}

	public Collection<PivotalIteration> getIterations(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		final URI uri = UriBuilder.fromUri(rootUri).path("projects").path(projectId).path("iterations").build();
		final Element projects = getWithToken(uri);
		return new IterationParser(userNameMapper).parseIterations(projects);
	}

	public Collection<ExternalUser> getMembers(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		final URI uri = UriBuilder.fromUri(rootUri).path("projects").path(projectId).path("memberships").build();
		final Element memberships = getWithToken(uri);
		return new ProjectMembershipParser(userNameMapper).parseUsers(projectId, memberships);
	}

	public List<ExternalWorklog> getWorklog(long projectId, ImportLogger importLogger) throws IOException {
		final HttpGet timeShiftsGet = new HttpGet(PIVOTAL_TIMESHIFTS_URL + "?"
			+ URLEncodedUtils.format(Lists.<NameValuePair>newArrayList(
				new BasicNameValuePair("date_period[start]", "1/1/1990"),
				new BasicNameValuePair("date_period[finish]", "1/1/2100"),
				new BasicNameValuePair("project", String.valueOf(projectId)),
				new BasicNameValuePair("person", "none"),
				new BasicNameValuePair("grouped_by", "none"),
				new BasicNameValuePair("commit", "Export to CSV")), "UTF-8"));
		final HttpResponse response = executeWithLogin(timeShiftsGet);
		try {
			if (PIVOTAL_NOT_ALLOWED_URL.equals(getRedirectLocation(response))) {
				importLogger.log("Time tracking is not enabled for this account in Pivotal Tracker, skipping.");
				return Collections.emptyList();
			}

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				importLogger.fail(null, "Error downloading time tracking information from Pivotal Tracker: %s",
						response.getStatusLine().toString());
				return Collections.emptyList();
			}
			final Header header = response.getEntity().getContentType();
			final String contentType = header != null ? header.getValue() : null;
			if (!StringUtils.startsWithIgnoreCase(contentType, "text/csv")) {
				importLogger.fail(null, "Error downloading time tracking information from Pivotal Tracker: expected text/csv content type; got %s",
								contentType);
				return Collections.emptyList();
			}
			return new PivotalTimeShiftsParser(EntityUtils.toString(response.getEntity(), "UTF-8"), userNameMapper).parseWorklog();
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
			if (StringUtils.isBlank(pivotalAttachment.getUrl())) {
				log.warn("Attachment URL missing for attachment '%s' in Pivotal Story '%s'; skipping.",
						attachment.getName(),
						externalIssue.getExternalId());
				continue;
			}
			try {
				final File file = File.createTempFile("pivotalAttachment-", ".tmp", getTempDir());
				download(pivotalAttachment.getUrl(), file);
				pivotalAttachment.setAttachment(file);
				result.add(pivotalAttachment);
			} catch (Exception e) {
				log.fail(e, "Error retrieving attachment '%s' from '%s' for Pivotal Story '%s'",
						attachment.getName(),
						pivotalAttachment.getUrl(),
						externalIssue.getExternalId());
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

	public void setStoryPaginationLimit(int storyPaginationLimit) {
		if (storyPaginationLimit < 1 || storyPaginationLimit > 3000) {
			throw new IllegalArgumentException("Pagination limit must be between 1 and 3000");
		}
		this.storyPaginationLimit = storyPaginationLimit;
	}
}
