/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.imports.AbstractProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzDataBean;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.PriorityValueMapper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.ResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.StatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDataBean;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

public class FogBugzHostedDataBean extends AbstractDataBean<FogBugzHostedConfigBean> {

	private Map<String, ExternalUser> usersByExternalId;
	private Set<ExternalUser> users;

	public FogBugzHostedDataBean(final FogBugzHostedConfigBean configBean) {
		super(configBean);
	}

	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
		return getAllUsers(importLogger);
	}

	Map<String, ExternalUser> getUsersById(ImportLogger log) {
		if (usersByExternalId == null) {
			usersByExternalId = Maps.newHashMap();
			for(ExternalUser user : getAllUsers(log)) {
				usersByExternalId.put(user.getId(), user);
			}
		}
		return usersByExternalId;
	}

	public Set<ExternalUser> getAllUsers(ImportLogger importLogger) {
		if (users == null) {
			try {
				users = Sets.newHashSet(configBean.getClient().getAllUsers(importLogger));
			} catch (FogBugzRemoteException e) {
				throw new RuntimeException(e);
			}
		}
		return users;
	}

	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		try {
			return Sets.newHashSet(Iterables.transform(configBean.getClient().getAllProjects(log), new Function<ExternalProject, ExternalProject>() {
                @Override
                public ExternalProject apply(@Nullable ExternalProject input) {
                    return input != null ? AbstractProjectTransformer.map(configBean, input) : null;
                }
            }));
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<ExternalVersion> getVersions(final ExternalProject externalProject, ImportLogger importLogger) {
		try {
			final Collection<ExternalVersion> versions = configBean.getClient().getFixFors(externalProject,
					importLogger);

			for(ExternalVersion version : versions) {
				if(version.getReleaseDate() != null) {
					version.setReleased(version.getReleaseDate().isBeforeNow());
				}
			}

			return versions;
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<ExternalComponent> getComponents(final ExternalProject externalProject, final ImportLogger importLogger) {
		try {
			return Collections2.transform(configBean.getClient().getAreas(externalProject, importLogger),
					new Function<ExternalComponent, ExternalComponent>() {
						@Override
						public ExternalComponent apply(ExternalComponent input) {
							return new ExternalComponent(input.getName(), input.getId(),
									getUsernameForId(input.getLead(), importLogger),
									input.getDescription());
						}
					});
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	String getUsernameForId(@Nullable String id, ImportLogger log) {
		if (id == null) {
			return null;
		}

		final Map<String, ExternalUser> usersById = getUsersById(log);
		final ExternalUser user = usersById.get(id);
		return user != null ? user.getName() : null;
	}

	public Iterator<ExternalIssue> getIssuesIterator(final ExternalProject externalProject, final ImportLogger importLogger) {
		try {
				return Iterables.transform(configBean.getClient().getCases(externalProject.getId(), importLogger),
						new Function<ExternalIssue, ExternalIssue>() {
					@Override
					public ExternalIssue apply(@Nullable ExternalIssue input) {
						if (input == null) {
							return null;
						}

						if (StringUtils.isNotEmpty(input.getIssueType())) {
							input.setIssueType(configBean.getValueMappingHelper().getValueMappingForImport(
								"sCategory", input.getIssueType()));
						}

						if (StringUtils.isNotEmpty(input.getPriority())) {
							input.setPriority(configBean.getValueMappingHelper()
								.getValueMappingForImport(PriorityValueMapper.PRIORITY_FIELD, input.getPriority()));
						}

						if (StringUtils.isNotEmpty(input.getStatus())) {
							input.setStatus(configBean.getValueMappingHelper().getValueMappingForImport(
								StatusValueMapper.FIELD, input.getStatus()));
						}

						if (StringUtils.isNotEmpty(input.getResolution())) {
							input.setResolution(configBean.getValueMappingHelper().getValueMappingForImport(
								ResolutionValueMapper.FIELD, input.getResolution()));
						}

						input.setReporter(getUsernameForId(input.getReporter(), importLogger));
						input.setAssignee(getUsernameForId(input.getAssignee(), importLogger));

						if (input.getComments() != null) {
							List<ExternalComment> comments = Lists.newArrayList();
							for (ExternalComment comment : input.getComments()) {
								comments.add(new ExternalComment(comment.getBody(),
										getUsernameForId(comment.getAuthor(), importLogger), comment.getCreated()));
							}
							input.setComments(comments);
						}

						if (input.getAttachments() != null) {
							for (ExternalAttachment attachment : input.getAttachments()) {
								attachment.setAttacher(getUsernameForId(attachment.getAttacher(), importLogger));
							}
						}

						return input;
					}
				}).iterator();
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<ExternalLink> getLinks(ImportLogger log) {
		try {
			return Collections2.transform(configBean.getClient().getSubcases(getSelectedProjects(log), log),
					new Function<ExternalLink, ExternalLink>() {
						@Override
						public ExternalLink apply(@Nullable ExternalLink input) {
							return input != null ? new ExternalLink(configBean.getLinkMapping(FogBugzConfigBean.SUBCASE_LINK_NAME),
									input.getSourceId(), input.getDestinationId()) : null;
						}
					});
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public long getTotalIssues(final Set<ExternalProject> selectedProjects, ImportLogger log) {
		try {
			return configBean.getClient().getTotalCases(selectedProjects, log);
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUnusedUsersGroup() {
		return configBean.getUnusedUsersGroup();
	}

	public void cleanUp() {
		configBean.getClient().logout();
	}

	public Collection<ExternalAttachment> getAttachmentsForIssue(final ExternalIssue externalIssue, ImportLogger log) {
		return configBean.getClient().getAttachmentsForIssue(externalIssue, log);
	}

	public String getIssueKeyRegex() {
		return FogBugzDataBean.ISSUE_KEY_REGEX;
	}

	@Override
	public String getExternalSystemUrl() {
		return configBean.getClient().getRootUri().toString();
	}
}
