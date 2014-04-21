/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDataBean;
import com.atlassian.jira.plugins.importer.sample.Callbacks;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build175;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.DefaultedMap;
import org.joda.time.DateTime;
import webwork.action.ActionContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class PivotalDataBean extends AbstractDataBean<PivotalConfigBean> {
	private static final String UNUSED_USERS_GROUP = "pivotal-import-unused-users";

	private final PivotalClient pivotalClient;
	private final Map<Long, String> rapidBoards = Maps.newHashMap();
	private final PivotalSchemeManager pivotalSchemeManager;
	private final PivotalRapidBoardManager pivotalRapidBoardManager;
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
			PivotalSchemeManager pivotalSchemeManager, boolean importTimeTracking,
			PivotalRapidBoardManager pivotalRapidBoardManager, boolean mapUserNames) {
		super(configBean);
		this.pivotalClient = mapUserNames ? configBean.getPivotalClient() : new CachingPivotalClient(UserNameMapper.NO_MAPPING);
		this.pivotalSchemeManager = pivotalSchemeManager;
		this.importTimeTracking = importTimeTracking;
		this.pivotalRapidBoardManager = pivotalRapidBoardManager;

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
				.build()), PivotalSchemeManager.ICE_BOX);
	}

	public PivotalClient getPivotalClient() throws PivotalRemoteException {
		if (!pivotalClient.isLoggedIn()) {
			pivotalClient.login(configBean.getCredentials().getUsername(), configBean.getCredentials().getPassword());
		}
		return pivotalClient;
	}


	@Override
	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
		final HashSet<ExternalUser> users = Sets.newHashSet();
		for (ExternalProject project : projects) {
			try {
				final Collection<ExternalUser> members = getPivotalClient().getMembers(project.getId(), importLogger);
				translateProjectRoles(members);
				users.addAll(members);
			} catch (PivotalRemoteException e) {
				throw new RuntimeException(e);
			}
		}
		return users;
	}

	@Override
	public Set<ExternalUser> getAllUsers(ImportLogger log) {
		return getRequiredUsers(getSelectedProjects(log), log);
	}

	@Override
	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		try {
			final HashSet<ExternalProject> externalProjects = Sets.newHashSet(getPivotalClient().getAllProjects(log));
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

	@Override
	public Collection<ExternalVersion> getVersions(final ExternalProject externalProject, ImportLogger importLogger) {
		try {
			final Collection<PivotalIteration> iterations = getPivotalClient().getIterations(externalProject.getId(),
					importLogger);

			return Collections2.transform(iterations, new Function<PivotalIteration, ExternalVersion>() {
				@Override
				public ExternalVersion apply(PivotalIteration input) {
					final ExternalVersion version = new ExternalVersion();
					version.setId(input.getId());
					version.setName(input.getName());
					final DateTime finish = input.getFinish();
					version.setReleaseDate(finish);
					// let's mark versions which had release date "yesterday" or earlier as released
					version.setReleased(finish.isBefore(new DateTime().toDateMidnight().toInstant()));
					return version;
				}
			});
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<ExternalComponent> getComponents(final ExternalProject externalProject, ImportLogger importLogger) {
		return Collections.emptyList();
	}

	// @todo wseliga that should be mapped better!!!
	public Iterable<ExternalIssue> getIssues(final ExternalProject externalProject, ImportLogger importLogger) {
		try {
			final Collection<ExternalIssue> stories = getPivotalClient().getStories(externalProject.getId(), importLogger);

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
			worklog = getPivotalClient().getWorklog(Long.parseLong(externalProject.getId()), importLogger);
		} catch (IOException e) {
			importLogger.fail(e, "Error downloading time tracking information");
			return Collections.emptyList();
		}

		if (worklog.isEmpty()) {
			importLogger.log("No time tracking information found for project %s", externalProject.getName());
			return Collections.emptyList();
		}

		final ExternalIssue timeTrackingHolder = new ExternalIssue();
		timeTrackingHolder.setSummary("Placeholder for imported time tracking data");
		timeTrackingHolder.setDescription("Pivotal Tracker provides time tracking information on the project level.\nJIRA stores time tracking information on issue level, so this issue has been created to store imported time tracking information.");
		timeTrackingHolder.setIssueType(PivotalSchemeManager.CHORE);
		timeTrackingHolder.setWorklogs(worklog);

		return Collections.singletonList(timeTrackingHolder);
	}

	private Map<String, String> indexIssuesToIterations(ExternalProject externalProject, final int expectedSize, ImportLogger importLogger)
			throws PivotalRemoteException {
		final Map<String, String> issuesPerIteration = Maps.newHashMapWithExpectedSize(expectedSize);
		for(PivotalIteration pi : getPivotalClient().getIterations(externalProject.getId(), importLogger)) {
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

	@Override
	public Iterator<ExternalIssue> getIssuesIterator(final ExternalProject externalProject, ImportLogger importLogger) {
		return getIssues(externalProject, importLogger).iterator();
	}

	@Override
	public Collection<ExternalLink> getLinks(ImportLogger log) {
		return Collections.emptyList();
	}

	@Override
	public long getTotalIssues(Set<ExternalProject> selectedProjects, ImportLogger log) {
		int total = 0;
		for (ExternalProject selectedProject : selectedProjects) {
			// todo: we should exploit the pagination total attribute instead - see
			// https://www.pivotaltracker.com/help/api?version=v3#get_stories_with_pagination
			total += Iterables.size(getIssues(selectedProject, log));
		}
		return total;
	}

	@Override
	public String getUnusedUsersGroup() {
		return UNUSED_USERS_GROUP;
	}

	@Override
	public void cleanUp() {
		try {
			getPivotalClient().logout();
		} catch (PivotalRemoteException e) {
			// ignore
		}
	}

	@Override
	public Collection<ExternalAttachment> getAttachmentsForIssue(final ExternalIssue externalIssue, ImportLogger log) {
		try {
			return getPivotalClient().getAttachmentsForIssue(externalIssue, log);
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Nullable
	public String getIssueKeyRegex() {
		return null;
	}

    @Override
    public Callbacks getCallbacks() {
        return new Callbacks() {
            @Override
            public void afterProjectCreated(ExternalProject project, Project jiraProject) {
                try {
                    pivotalSchemeManager.setPTSchemesForProject(jiraProject);
                } catch (Exception e) {
                    throw new RuntimeException("Error setting up Pivotal scheme for project " + project.getKey(), e);
                }

                if (pivotalRapidBoardManager.isGreenHooperFeaturesEnabled()) {
                    try {
                        final Pair<Long, String> view = pivotalRapidBoardManager.createRapidBoard(jiraProject);
                        if (view != null) {
                            rapidBoards.put(view.first(), view.second());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error setting up Pivotal Rapid Board for project " + project.getKey(), e);
                    }
                }
            }
        };
    }

	@Override
	public String getExternalSystemUrl() {
		return "https://" + PivotalClient.PIVOTAL_HOST;
	}

	@Override
	public String getReturnLinks() {
		if (rapidBoards.isEmpty()) {
			return super.getReturnLinks();
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("<div id=\"rapidBoardLinks\">")
					.append(configBean.getI18n().getText("jira-importer-plugin.PivotalDataBean.take.me"));
			for(Map.Entry<Long, String> view : rapidBoards.entrySet()) {
				sb.append("<span><a href=\"")
						.append(ActionContext.getContext().getRequestImpl().getContextPath())
						.append("/secure/RapidBoard.jspa?rapidView=")
						.append(view.getKey())
						.append("\">")
						.append(view.getValue())
						.append("</a></span>");
			}
			sb.append("</div>");
			return sb.toString();
		}
	}
}
