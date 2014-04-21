/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalRemoteException;
import com.atlassian.jira.util.AttachmentUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
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
import java.util.List;
import java.util.Set;

public class FogBugzClient {
	private String token;
	private HttpContext httpContext;

	private final URI rootUri;
	private final String email;
	private final String password;

	private static URI toUri(String url) {
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public URI getRootUri() {
		return rootUri;
	}

	public FogBugzClient(String rootUri, String email, String password) {
		this(toUri(rootUri), email, password);
	}

	public FogBugzClient(URI rootUri, String email, String password) {
		this.email = email;
		this.password = password;

		if (rootUri.getHost().endsWith("fogbugz.com")) {
			this.rootUri = UriBuilder.fromUri(rootUri).path("/api.asp").build();
		} else {
			this.rootUri = UriBuilder.fromUri(rootUri).path("/fogbugz/api.asp").build();
		}
	}

	public void login() throws FogBugzRemoteException {
		token = null;
		httpContext = new BasicHttpContext();

		final HttpPost post = new HttpPost(UriBuilder.fromUri(rootUri).queryParam("cmd", "logon").build());
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Lists.<NameValuePair>newArrayList(
					new BasicNameValuePair("email", email),
					new BasicNameValuePair("password", password)
			));

			post.setEntity(entity);

			final HttpResponse response = getHttpClient().execute(post, httpContext);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
				final org.apache.http.Header location = response.getFirstHeader("Location");
				if (location != null && location.getValue() != null) {
					final URI targetUri = URI.create(location.getValue());
					final String targetHost = targetUri.getHost();
					if (targetHost.equals(rootUri.getHost()) && !targetUri.getScheme().equals(rootUri.getScheme())) {
						throw new FogBugzRemoteException("The server requested redirection. Please consider using \""
						+ targetUri.getScheme() + "\" instead of \"" + rootUri.getScheme() + "\".");
					}
				}
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new FogBugzRemoteException("HTTP Error code " + response + " returned by the server");
			}
			final InputStream is = response.getEntity().getContent();
			if (is == null) {
				throw new FogBugzRemoteException("Connection failed. Empty response returned by the server");
			}
			try {
				final SAXBuilder builder = XmlUtil.getSAXBuilder();
				final Document document = builder.build(is);
				token = document.getRootElement().getChildText("token");
				if (token == null) {
					throw new FogBugzRemoteException("Login failed. Authentication token has not been served by the server");
				}
			} finally {
				IOUtils.closeQuietly(is);
			}
		} catch (FogBugzRemoteException e) {
			throw e;
		} catch (Exception e) {
			throw new FogBugzRemoteException(e);
		}
	}

	public void logout() {
		try {
			getWithToken(UriBuilder.fromUri(rootUri).queryParam("cmd", "logoff"));
		} catch (FogBugzRemoteException e) {
			// don't care
		}

		token = null;
		httpContext = null;
	}

	private InputStream getWithToken0(UriBuilder url) throws FogBugzRemoteException {
		final HttpGet getMethod = new HttpGet(url.queryParam("token", token).build());
		try {
			final HttpResponse response = getHttpClient().execute(getMethod, httpContext);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new FogBugzRemoteException("HTTP Error code " + response + " returned by the server");
			}
			if (response.getEntity().getContentLength() == 0) {
				throw new FogBugzRemoteException("Server returned an empty response");
			}
			final InputStream is = response.getEntity().getContent();
			if (is == null) {
				throw new FogBugzRemoteException("Connection failed. Empty response returned by the server");
			}
			return is;
		} catch (FogBugzRemoteException e) {
			throw e;
		} catch (IOException e) {
			throw new FogBugzRemoteException(e);
		}
	}

	protected void ensureLoggedInToWeb() throws FogBugzRemoteException {
		if (token == null) {
			login();
		}
	}

	private Element getWithToken(UriBuilder uri) throws FogBugzRemoteException {
		InputStream is = null;
		try {
			is = getWithToken0(uri);
			if (is == null) {
				throw new FogBugzRemoteException("Login failed. Empty response returned by the server");
			}
			final Document document = XmlUtil.getSAXBuilder().build(is);
			return document.getRootElement();
		} catch (FogBugzRemoteException e) {
			throw e;
		} catch (IOException e) {
			throw new FogBugzRemoteException(e);
		} catch (JDOMException e) {
			throw new FogBugzRemoteException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private HttpClient getHttpClient() {
		final DefaultHttpClient client = new DefaultHttpClient();
		HttpDownloader.setDefaultProxy(client);
		return client;
	}

	public Collection<String> getAllProjectNames() throws FogBugzRemoteException {
		final Collection<ExternalProject> allProjects = getAllProjects(ConsoleImportLogger.INSTANCE);
		return Collections2.transform(allProjects, new Function<ExternalProject, String>() {
			@Override
			public String apply(ExternalProject input) {
				return input.getName();
			}
		});
	}

	public Collection<ExternalProject> getAllProjects(ImportLogger log) throws FogBugzRemoteException {
		ensureLoggedInToWeb();
		final Element projects = getWithToken(UriBuilder.fromUri(rootUri).queryParam("cmd", "listProjects"));
		return new ProjectParser().getProjects(projects);
	}

	public Collection<ExternalVersion> getFixFors(ExternalProject project, ImportLogger log) throws FogBugzRemoteException {
		ensureLoggedInToWeb();
		final UriBuilder uri = UriBuilder.fromUri(rootUri).queryParam("cmd", "listFixFors")
				.queryParam("ixProject", project.getId());
		final Element xmlResult = getWithToken(uri);
		return new FixForParser().getFixFors(xmlResult);
	}

	public Collection<ExternalComponent> getAreas(ExternalProject project, ImportLogger log) throws FogBugzRemoteException {
		ensureLoggedInToWeb();
		final Element xmlResult = getWithToken(UriBuilder.fromUri(rootUri).queryParam("cmd", "listAreas")
				.queryParam("ixProject", project.getId()));
		return new AreaParser().getAreas(xmlResult);
	}

	public List<ExternalIssue> getCases(String projectId, ImportLogger importLogger) throws FogBugzRemoteException {
		final UriBuilder uri = UriBuilder.fromUri(rootUri)
				.queryParam("cmd", "search")
				.queryParam("q", String.format("project:=%s", projectId))
				.queryParam("cols", "ixBug,tags,fOpen,sTitle,sArea,sComputer,"
						+ "ixPersonAssignedTo,ixPersonOpenedBy,sStatus,ixPriority,sPriority,sFixFor,sVersion,sCustomerEmail,"
						+ "dtOpened,dtClosed,dtResolved,hrsOrigEst,hrsElapsed,hrsCurrEst,sCategory,events");
		final Element response = getWithToken(uri);
		return Ordering.natural().onResultOf(new Function<ExternalIssue, Comparable>() {
			@Override
			public Comparable apply(ExternalIssue input) {
				return input.getExternalId();
			}
		}).sortedCopy(
				Collections2.transform(new CaseParser().parseCases(response.getChild("cases"), importLogger),
						new Function<ExternalIssue, ExternalIssue>() {
							@Override
							public ExternalIssue apply(@Nullable ExternalIssue input) {
								if (input != null) {
									List<ExternalCustomFieldValue> customFields = Lists.newArrayList(
											input.getExternalCustomFieldValues());
									customFields.add(new ExternalCustomFieldValue(DefaultJiraDataImporter.EXTERNAL_ISSUE_URL,
											CustomFieldConstants.URL_FIELD_TYPE, CustomFieldConstants.EXACT_TEXT_SEARCHER,
											UriBuilder.fromUri(rootUri).replacePath("default.asp")
													.queryParam(input.getExternalId(), "").build().toString()));
									input.setExternalCustomFieldValues(customFields);
								}
								return input;
							}
						}));
	}

	public Collection<ExternalUser> getAllUsers(ImportLogger importLogger) throws
			FogBugzRemoteException {
		ensureLoggedInToWeb();
		final Element memberships = getWithToken(UriBuilder.fromUri(rootUri).queryParam("cmd", "listPeople"));
		return new PeopleParser().getPeople(memberships);
	}

	public Collection<ExternalAttachment> getAttachmentsForIssue(ExternalIssue externalIssue, ImportLogger log) {
		final List<ExternalAttachment> externalAttachments = externalIssue.getAttachments();

		final List<ExternalAttachment> result = Lists.newArrayListWithCapacity(externalAttachments.size());
		for (ExternalAttachment attachment : externalAttachments) {
			final PivotalExternalAttachment pivotalAttachment = (PivotalExternalAttachment) attachment;
			try {
				final File file = File.createTempFile("fogBugzAttachment-", ".tmp", getTempDir());
				download(pivotalAttachment.getUrl(), file);
				pivotalAttachment.setResolvedFile(file);
				result.add(pivotalAttachment);
			} catch (IOException e) {
				log.fail(e, "Error retrieving attachment");
			}
		}
		return result;
	}

	protected File getTempDir() {
		return AttachmentUtils.getTemporaryAttachmentDirectory();
	}

	public long getTotalCases(Set<ExternalProject> selectedProjects, ImportLogger log) throws FogBugzRemoteException {
		final UriBuilder uri = UriBuilder.fromUri(rootUri)
				.queryParam("cmd", "search")
				.queryParam("q", getProjectsQuery(selectedProjects));

		final Element response = getWithToken(uri);
		if (response != null) {
			final Element cases = response.getChild("cases");
			final Attribute count = cases != null ? cases.getAttribute("count") : null;
			if (count != null) {
				try {
					return count.getLongValue();
				} catch (DataConversionException e) {
					log.warn(e, "Unable to parse case count number");
				}
			}
		}
		return 0;
	}

	private Object getProjectsQuery(Set<ExternalProject> selectedProjects) {
		return StringUtils.join(
				Collections2.transform(selectedProjects, new Function<ExternalProject, String>() {
					@Override
					public String apply(@Nullable ExternalProject input) {
						return String.format("project:=%s", input.getId());
					}
				}), " OR ");
	}

	private void download(String url, File destination) throws IOException {
		final URI downloadUri = UriBuilder.fromUri(url).build();
		OutputStream outputStream = null;
		ensureLoggedInToWeb();
		final InputStream inputStream = getWithToken0(UriBuilder.fromUri(rootUri)
				.replacePath(downloadUri.getPath()).replaceQuery(downloadUri.getQuery()));
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

	public Set<String> getStatuses() throws FogBugzRemoteException {
		final UriBuilder uri = UriBuilder.fromUri(rootUri).queryParam("cmd", "listStatuses");
		return new StatusParser().getStatuses(getWithToken(uri));
	}

	public Collection<ExternalLink> getSubcases(Set<ExternalProject> selectedProjects, ImportLogger importLogger)
			throws FogBugzRemoteException {
		final UriBuilder uri = UriBuilder.fromUri(rootUri)
				.queryParam("cmd", "search")
				.queryParam("q", getProjectsQuery(selectedProjects))
				.queryParam("cols", "ixBug,ixBugParent");
		final Element response = getWithToken(uri);
		return new CaseParser().parseSubcases(response.getChild("cases"), importLogger);
	}
}
