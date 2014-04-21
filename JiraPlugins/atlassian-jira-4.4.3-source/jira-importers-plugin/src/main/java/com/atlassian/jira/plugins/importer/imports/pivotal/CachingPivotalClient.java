/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CachingPivotalClient extends PivotalClient {
	private final Map<String, Collection<ExternalIssue>> storiesCache = Maps.newHashMap();
	private final Map<String, Collection<PivotalIteration>> iterationsCache = Maps.newHashMap();
	private final Map<String, Collection<ExternalUser>> membersCache = Maps.newHashMap();
	private Collection<ExternalProject> externalProjects;

	public CachingPivotalClient() {
	}

	CachingPivotalClient(URI rootUri) {
		super(rootUri);
	}

	@Override
	public Collection<ExternalProject> getAllProjects(ImportLogger log) throws PivotalRemoteException {
		if (externalProjects == null) {
			externalProjects = super.getAllProjects(log);
		}

		return Immutables.transformThenCopyToList(externalProjects, new Function<ExternalProject, ExternalProject>() {
			@Override
			public ExternalProject apply(ExternalProject input) {
				return input.getClone();
			}
		});
	}

	@Override
	public List<ExternalIssue> getStories(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		Collection<ExternalIssue> result = storiesCache.get(projectId);
		if (result == null) {
			result = super.getStories(projectId, importLogger);
			storiesCache.put(projectId, result);
		}
		return Immutables.transformThenCopyToList(result, new Function<ExternalIssue, ExternalIssue>() {
			@Override
			public ExternalIssue apply(ExternalIssue input) {
				return new ExternalIssue(input);
			}
		});
	}

	@Override
	public Collection<PivotalIteration> getIterations(String projectId, ImportLogger importLogger)
			throws PivotalRemoteException {
		Collection<PivotalIteration> result = iterationsCache.get(projectId);
		if (result == null) {
			result = super.getIterations(projectId, importLogger);
			iterationsCache.put(projectId, result);
		}
		return Immutables.transformThenCopyToList(result, new Function<PivotalIteration, PivotalIteration>() {
			@Override
			public PivotalIteration apply(PivotalIteration input) {
				return new PivotalIteration(input);
			}
		});
	}

	@Override
	public Collection<ExternalUser> getMembers(String projectId, ImportLogger importLogger) throws PivotalRemoteException {
		Collection<ExternalUser> result = membersCache.get(projectId);
		if (result == null) {
			result = super.getMembers(projectId, importLogger);
			membersCache.put(projectId, result);
		}
		return Immutables.transformThenCopyToList(result, new Function<ExternalUser, ExternalUser>() {
			@Override
			public ExternalUser apply(ExternalUser input) {
				return new ExternalUser(input);
			}
		});
	}

	@Override
	public void logout() {
		super.logout();
		externalProjects = null;
		membersCache.clear();
		storiesCache.clear();
	}
}
