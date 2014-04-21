/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDataBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build175;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.DefaultedMap;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PivotalDataBean extends AbstractDataBean<PivotalConfigBean> {
	private static final String UNUSED_USERS_GROUP = "pivotal-import-unused-users";

	private final PivotalSchemeManager pivotalSchemeManager;
	private final boolean importTimeTracking;
	private final Map<String, String> issueTypeMapping;
	private final Map<String, String> statusMapping;
	private final Set<String> statusNeedsResolution = ImmutableSet.of(
			PivotalSchemeManager.ACCEPTED,
			IssueFieldConstants.CLOSED_STATUS);
	@SuppressWarnings({"unchecked"})
	private final Map<String, String> roleMapping = DefaultedMap.decorate(ImmutableMap.of(
			"Owner", UpgradeTask_Build175.ROLE_ADMINISTRATORS,
			"Member", UpgradeTask_Build175.ROLE_DEVELOPERS,
			"Viewer", UpgradeTask_Build175.ROLE_USERS),
			/* default role */ UpgradeTask_Build175.ROLE_DEVELOPERS);

	public PivotalDataBean(final PivotalConfigBean configBean,
			PivotalSchemeManager pivotalSchemeManager, boolean importTimeTracking) {
		super(configBean);
		this.pivotalSchemeManager = pivotalSchemeManager;
		this.importTimeTracking = importTimeTracking;

		//noinspection unchecked
		issueTypeMapping = DefaultedMap.decorate(new CaseInsensitiveMap(new ImmutableMap.Builder<String, String>()
				.put("bug", PivotalSchemeManager.BUG)
				.put("chore", PivotalSchemeManager.CHORE)
				.put("feature", PivotalSchemeManager.FEATURE)
				.put("release", PivotalSchemeManager.RELEASE)
				.put("subtask", PivotalSchemeManager.SUBTASK)
				.build()), PivotalSchemeManager.BUG);
		//noinspection unchecked
		statusMapping = DefaultedMap.decorate(new CaseInsensitiveMap(new ImmutableMap.Builder<String, String>()
				.put("delivered", PivotalSchemeManager.DELIVERED)
				.put("finished", PivotalSchemeManager.FINISHED)
				.put("started", PivotalSchemeManager.STARTED)
				.put("rejected", PivotalSchemeManager.REJECTED)
				.put("accepted", PivotalSchemeManager.ACCEPTED)
				.put(StoryParser.SUBTASK_STATUS_OPEN, IssueFieldConstants.OPEN_STATUS)
				.put(StoryParser.SUBTASK_STATUS_FINISHED, IssueFieldConstants.CLOSED_STATUS)
				.build()), PivotalSchemeManager.NOT_YET_STARTED);
	}

	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
		final HashSet<ExternalUser> users = Sets.newHashSet();
		for (ExternalProject project : projects) {
			try {
				final Collection<ExternalUser> members = configBean.getPivotalClient().getMembers(project.getId(), importLogger);
				translateProjectRoles(members);
				users.addAll(members);
			} catch (PivotalRemoteException e) {
				throw new RuntimeException(e);
			}
		}
		return users;
	}

	public Set<ExternalUser> getAllUsers(ImportLogger log) {
		return getRequiredUsers(getSelectedProjects(log), log);
	}

	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		try {
			final HashSet<ExternalProject> externalProjects = Sets.newHashSet(configBean.getPivotalClient().getAllProjects(log));
			for (ExternalProject externalProject : externalProjects) {
				final String externalProjectName = externalProject.getExternalName();

				externalProject.setKey(getProjectKey(configBean, externalProjectName));
				externalProject.setName(configBean.getProjectName(externalProjectName));
			}
			return externalProjects;
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e);
		}


	}

	protected String getProjectKey(AbstractConfigBean2 configBean, final String projectName) {
		final String projectKey = configBean.getProjectKey(projectName);
		if (projectKey != null) {
			return projectKey;
		} else {
			return null;
		}
	}

	public Collection<ExternalVersion> getVersions(final ExternalProject externalProject, ImportLogger importLogger) {
		try {
			final Collection<PivotalIteration> iterations = configBean.getPivotalClient().getIterations(externalProject.getId(),
					importLogger);

			return Collections2.transform(iterations, new Function<PivotalIteration, ExternalVersion>() {
				@Override
				public ExternalVersion apply(PivotalIteration input) {
					final ExternalVersion version = new ExternalVersion();
					version.setId(input.getId());
					version.setName(input.getName());
					final DateTime finish = input.getFinish();
					version.setReleaseDate(finish.toDate());
					version.setReleased(finish.isBeforeNow());
					return version;
				}
			});
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<ExternalComponent> getComponents(final ExternalProject externalProject, ImportLogger importLogger) {
		return Collections.emptyList();
	}

	// @todo wseliga that should be mapped better!!!
	public Iterable<ExternalIssue> getIssues(final ExternalProject externalProject, ImportLogger importLogger) {
		try {
			final Collection<ExternalIssue> stories = configBean.getPivotalClient().getStories(externalProject.getId(), importLogger);

			final Map<String, String> issuesPerIteration = indexIssuesToIterations(externalProject, stories.size(), importLogger);
			final Map<String, String> name2id = ImmutableMap.<String, String>builder()
								.putAll(pivotalSchemeManager.getPTStatusNameToIdMapping())
								.put(IssueFieldConstants.OPEN_STATUS, String.valueOf(IssueFieldConstants.OPEN_STATUS_ID))
								.put(IssueFieldConstants.CLOSED_STATUS, String.valueOf(IssueFieldConstants.CLOSED_STATUS_ID))
								.build();

			final CustomField rankingField = configBean.getCustomFieldNameForRanking();
			final String searcherType =
					rankingField != null && rankingField.getCustomFieldSearcher() != null ?
					rankingField.getCustomFieldSearcher().getDescriptor().getCompleteKey() : null;

			for (ExternalIssue story : stories) {
				// if Rank field is present set the field to null so the issue is auto-ranked 'last'
				// custom field value here should be the ranking or key of the issue that this
				// one should be ranked before - no absolute values!
				if (rankingField != null) {
					final ExternalCustomFieldValue ranking = new ExternalCustomFieldValue(
							rankingField.getName(),
							rankingField.getCustomFieldType().getKey(),
							searcherType,
							null);
					story.addExternalCustomFieldValue(ranking);
				}
				translateSingle(story, name2id);
				if (issuesPerIteration.containsKey(story.getExternalId())) {
					story.setFixedVersions(Collections.singletonList(issuesPerIteration.get(story.getExternalId())));
				}
				
				for (ExternalIssue subtask : story.getSubtasks()) {
					translateSingle(subtask, name2id);
					subtask.setFixedVersions(story.getFixedVersions());
				}
			}

			return importTimeTracking ? Iterables.concat(stories, createIssueForTimeTracking(externalProject, importLogger))
					: stories;
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	private Collection<ExternalIssue> createIssueForTimeTracking(ExternalProject externalProject, ImportLogger importLogger)
			throws PivotalRemoteException {
		final List<ExternalWorklog> worklog;
		try {
			worklog = configBean.getPivotalClient().getWorklog(Long.parseLong(externalProject.getId()), importLogger);
		} catch (IOException e) {
			importLogger.fail(e, "Error downloading time tracking information");
			return Collections.emptyList();
		}

		if (worklog.isEmpty()) {
			importLogger.log("No time tracking information found for project " + externalProject.getName());
			return Collections.emptyList();
		}

		final ExternalIssue timeTrackingHolder = new ExternalIssue();
		timeTrackingHolder.setSummary("Placeholder for imported time tracking data");
		timeTrackingHolder.setDescription("Pivotal Tracker provides time tracking information on the project level.\nJIRA stores time tracking information on issue level, so this issue has been created to store imported time tracking information.");
		timeTrackingHolder.setIssueType(PivotalSchemeManager.CHORE);
		timeTrackingHolder.setWorklog(worklog);

		return Collections.singletonList(timeTrackingHolder);
	}

	private Map<String, String> indexIssuesToIterations(ExternalProject externalProject, final int expectedSize, ImportLogger importLogger)
			throws PivotalRemoteException {
		final Map<String, String> issuesPerIteration = Maps.newHashMapWithExpectedSize(expectedSize);
		for(PivotalIteration pi : configBean.getPivotalClient().getIterations(externalProject.getId(), importLogger)) {
			for(ExternalIssue story : pi.getStories()) {
				issuesPerIteration.put(story.getExternalId(), pi.getName());
			}
		}
		return issuesPerIteration;
	}

	private void translateSingle(ExternalIssue story, Map<String, String> name2id) {
		final String status = statusMapping.get(story.getStatus());
		if (statusNeedsResolution.contains(status)) {
			story.setResolution(configBean.getDefaultResolutionId());
		}
		story.setStatus(name2id.get(status));
		story.setIssueType(issueTypeMapping.get(story.getIssueType()));
	}

	private void translateProjectRoles(Collection<ExternalUser> externalUsers) {
		for (ExternalUser externalUser : externalUsers) {
			final Multimap<String, String> transformedProjectRoles = HashMultimap.create(
					Multimaps.transformValues(externalUser.getProjectRoles(), new Function<String, String>() {
						@Override
						public String apply(String input) {
							return roleMapping.get(input);
						}
					}));
			externalUser.setProjectRoles(transformedProjectRoles);
		}
	}

	public Iterator<ExternalIssue> getIssuesIterator(final ExternalProject externalProject, ImportLogger importLogger) {
		return getIssues(externalProject, importLogger).iterator();
	}

	public Collection<ExternalLink> getLinks(ImportLogger log) {
		return Collections.emptyList();
	}

	public long getTotalIssues(Set<ExternalProject> selectedProjects, ImportLogger log) {
		int total = 0;
		for (ExternalProject selectedProject : selectedProjects) {
			// todo: we should exploit the pagination total attribute instead - see
			// https://www.pivotaltracker.com/help/api?version=v3#get_stories_with_pagination
			total += Iterables.size(getIssues(selectedProject, log));
		}
		return total;
	}

	public String getUnusedUsersGroup() {
		return UNUSED_USERS_GROUP;
	}

	public void cleanUp() {
		try {
			configBean.getPivotalClient().logout();
		} catch (PivotalRemoteException e) {
			// ignore
		}
	}

	public Collection<ExternalAttachment> getAttachmentsForIssue(final ExternalIssue externalIssue, ImportLogger log) {
		try {
			return configBean.getPivotalClient().getAttachmentsForIssue(externalIssue, log);
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public String getIssueKeyRegex() {
		return null;
	}

	@Override
	public void afterProjectCreated(ExternalProject externalProject, Project project, ImportLogger importLogger) {
		try {
			pivotalSchemeManager.setPTSchemesForProject(project);
		} catch (Exception e) {
			throw new RuntimeException("Error setting up Pivotal scheme for project " + project.getKey(), e);
		}
	}

	@Override
	public String getExternalSystemUrl() {
		return "https://" + PivotalClient.PIVOTAL_HOST;
	}
}
