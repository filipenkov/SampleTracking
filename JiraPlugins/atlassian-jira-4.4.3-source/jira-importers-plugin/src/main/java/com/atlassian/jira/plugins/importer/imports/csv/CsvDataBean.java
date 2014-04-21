/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.external.beans.NamedExternalObject;
import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.DefaultExternalCustomFieldValueMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.DefaultExternalIssueMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.DefaultExternalProjectMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalCommentMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalComponentMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalCustomFieldValueMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalIssueMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalLabelsMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalProjectMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalUserMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalVersionMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.FullNameUserMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleCommentMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleCustomFieldVersionMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleExternalComponentMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleExternalLabelsMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleExternalVersionMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleTimeEstimateConverter;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.TimeEstimateConverter;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.project.Project;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CsvDataBean implements ImportDataBean {
	private static final String EXTERNAL_ISSUE_AUTO = "autoid-";
	protected static final String UNUSED_USERS_GROUP = "csv-import-unused-users";

	private CsvProvider provider;
	private LinkedHashMap<ExternalProject, ExternalProject> projectsCache;
	private Set<ExternalUser> usersCache;
	private ListMultimap<ExternalProject, ExternalVersion> versionCache;
	private ListMultimap<ExternalProject, ExternalComponent> componentCache;
	private Multimap<ExternalProject, ExternalIssue> issueCache; // actually ListMultimap
	private ListMultimap<ExternalIssue, String> attachmentsCache;
	private ListMultimap<ExternalIssue, String> parentIssuesCache;

	// Registering some Mappers
	private ExternalVersionMapper affectedVersionMapper;
	private ExternalVersionMapper fixedVersionMapper;
	private List<ExternalUserMapper> userMappers;
	private final ExternalComponentMapper componentMapper;
	private ExternalProjectMapper projectMapper;
	private ExternalIssueMapper issueMapper;
	private ExternalCommentMapper commentMapper;
	private final ExternalCustomFieldValueMapper customFieldValueMapper;
	private ExternalLabelsMapper labelsMapper;

	private final ExternalVersionMapper customFieldVersionMapper;

	protected CsvDataBean(CsvConfigBean configBean,
			ExternalVersionMapper affectedVersionMapper,
			ExternalVersionMapper fixedVersionMapper,
			ExternalVersionMapper customFieldVersionMapper,
			List<ExternalUserMapper> userMappers,
			ExternalComponentMapper componentMapper,
			ExternalProjectMapper projectMapper,
			ExternalIssueMapper issueMapper,
			ExternalCommentMapper commentMapper,
			ExternalCustomFieldValueMapper customFieldValueMapper,
			final ExternalLabelsMapper labelsMapper
	) throws FileNotFoundException {
		this.customFieldVersionMapper = customFieldVersionMapper;
		provider = new MindProdCsvProvider(configBean.getImportLocation(), configBean.getEncoding(),
				new HeaderRowCsvMapper(), configBean.getDelimiter());

		this.affectedVersionMapper = affectedVersionMapper;
		this.fixedVersionMapper = fixedVersionMapper;
		this.userMappers = userMappers;
		this.componentMapper = componentMapper;
		this.projectMapper = projectMapper;
		this.issueMapper = issueMapper;
		this.commentMapper = commentMapper;
		this.customFieldValueMapper = customFieldValueMapper;
		this.labelsMapper = labelsMapper;
	}

	public CsvDataBean(CsvConfigBean configBean, CustomFieldManager customFieldManager)
			throws FileNotFoundException, ConfigurationException {
		this(configBean,
				new SimpleExternalVersionMapper(ExternalVersion.AFFECTED_VERSION_PREFIX),
				new SimpleExternalVersionMapper(ExternalVersion.FIXED_VERSION_PREFIX),
				new SimpleCustomFieldVersionMapper(customFieldManager),
				Lists.<ExternalUserMapper>newArrayList(new FullNameUserMapper(CsvConfiguration.REPORTER_FIELD),
						new FullNameUserMapper(CsvConfiguration.ASSIGNEE_FIELD)),
				new SimpleExternalComponentMapper(),
				new DefaultExternalProjectMapper(),
				new DefaultExternalIssueMapper(new SimpleTimeEstimateConverter(), configBean),
				new SimpleCommentMapper(configBean),
				new DefaultExternalCustomFieldValueMapper(configBean, customFieldManager),
				new SimpleExternalLabelsMapper()
		);

		CsvMapper mapper = new PropertiesCsvMapper(configBean);
		provider = new MindProdCsvProvider(configBean.getImportLocation(), configBean.getEncoding(), mapper,
				configBean.getDelimiter());
	}

	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		refreshCache(log);

		return Sets.newHashSet(projectsCache.values());
	}

	public Set<ExternalProject> getSelectedProjects(ImportLogger log) {
		return getAllProjects(log);
	}

	private synchronized void refreshCache(ImportLogger log) {
		if (projectsCache == null) {
			clearCache();
			populateCache(log);
		}
	}

	private synchronized void clearCache() {
		projectsCache = Maps.newLinkedHashMap();
		usersCache = Collections.newSetFromMap(new ConcurrentHashMap<ExternalUser, Boolean>());
		versionCache = ArrayListMultimap.create();
		componentCache = ArrayListMultimap.create();
		issueCache = Multimaps.synchronizedMultimap(ArrayListMultimap.<ExternalProject, ExternalIssue>create());
		attachmentsCache = ArrayListMultimap.create();
		parentIssuesCache = ArrayListMultimap.create();
	}

	private synchronized void populateCache(ImportLogger log) {
		try {
			provider.startSession();

			ListMultimap<String, String> issueMap;
			while ((issueMap = provider.getNextLine()) != null) {
				for (ExternalUserMapper externalUserMapper : userMappers) {
					// TODO: We need to change the ExternalUserMapper so that it can handle multiple Users for multi-select User Custom Fields.
					ExternalUser externalUser = externalUserMapper.buildFromMultiMap(issueMap);
					if (externalUser != null) {
						usersCache.add(externalUser);
					}
				}

				ExternalProject externalProject = projectMapper.buildFromMultiMap(issueMap);
				if (externalProject != null) {
					if (!projectsCache.containsKey(externalProject)) {
						projectsCache.put(externalProject, externalProject);
					} else {
						externalProject = projectsCache.get(externalProject);
					}

					// Versions
					List<ExternalVersion> affectedVersions = affectedVersionMapper.buildFromMultiMap(issueMap);
					if (affectedVersions != null) {
						versionCache.putAll(externalProject, affectedVersions);
					}

					List<ExternalVersion> fixedVersions = fixedVersionMapper.buildFromMultiMap(issueMap);
					if (fixedVersions != null) {
						versionCache.putAll(externalProject, fixedVersions);
					}

					List<ExternalVersion> customFieldVersions = customFieldVersionMapper.buildFromMultiMap(issueMap);
					if (customFieldVersions != null) {
						versionCache.putAll(externalProject, customFieldVersions);
					}

					// Components
					List<ExternalComponent> externalComponents = componentMapper.buildFromMultiMap(issueMap);
					if (externalComponents != null) {
						componentCache.putAll(externalProject, externalComponents);
					}

					// Labels
					final Set<Label> externalLabels = labelsMapper.buildFromMultiMap(issueMap);

					// Comments
					final List<ExternalComment> externalComments = commentMapper.buildFromMultiMap(issueMap, log);

					// Custom Fields
					final List<ExternalCustomFieldValue> externalCustomFieldValues =
							customFieldValueMapper.buildFromMultiMap(issueMap, log);

					// Issues
					final ExternalIssue issue = issueMapper.buildFromMultiMap(issueMap, log);

					if (StringUtils.isNotBlank(issue.getSummary())) {
						if (affectedVersions != null) {
							issue.setAffectedVersions(Lists.transform(affectedVersions, NamedExternalObject.NAME_FUNCTION));
						}
						if (fixedVersions != null) {
							issue.setFixedVersions(Lists.transform(fixedVersions, NamedExternalObject.NAME_FUNCTION));
						}
						if (externalComponents != null) {
							issue.setExternalComponents(Lists.transform(externalComponents, NamedExternalObject.NAME_FUNCTION));
						}
						if (externalComments != null) {
							issue.setExternalComments(externalComments);
						}
						issue.setExternalCustomFieldValues(externalCustomFieldValues);
						issue.setLabels(externalLabels);

						issueCache.put(externalProject, issue);
						attachmentsCache.putAll(issue, issueMap.get("attachment"));
						if (issue.getExternalId() == null) {
							issue.setExternalId(EXTERNAL_ISSUE_AUTO + RandomUtils.nextLong());
						}
						parentIssuesCache.putAll(issue, issueMap.get(DefaultExternalIssueMapper.SUBTASK_PARENT_ID));
					} else {
						log.warn("Issue " + issue + " has a blank summary. This is being ignored.");
					}
				}
			}

		} catch (ImportException e) {
			log.fail(e, "Can't populate cache");
			projectsCache = null;
			throw new DataAccessException(e);
		} finally {
			try {
				provider.stopSession();
			} catch (ImportException e) {
				throw new DataAccessException(e);
			}

		}
	}

	@Nullable
	public Collection<ExternalVersion> getVersions(ExternalProject externalProject, ImportLogger importLogger) {
		refreshCache(importLogger);

		Collection<ExternalVersion> versions = versionCache.get(externalProject);
		if (versions != null) {
			ArrayList<ExternalVersion> versionsList = new ArrayList<ExternalVersion>(versions);
			Collections.sort(versionsList);
			return versionsList;
		} else {
			return null;
		}
	}

	public Collection<ExternalComponent> getComponents(ExternalProject externalProject, ImportLogger importLogger) {
		refreshCache(importLogger);

		Collection<ExternalComponent> components = componentCache.get(externalProject);
		if (components != null) {
			return Lists.newArrayList(components);
		} else {
			return Lists.newArrayList();
		}
	}

	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
		return getAllUsers(importLogger);
	}

	public Set<ExternalUser> getAllUsers(ImportLogger importLogger) {
		refreshCache(importLogger);
		return usersCache;
	}

	public Collection<ExternalIssue> getIssues(ExternalProject externalProject, ImportLogger importLogger) {
		refreshCache(importLogger);
		return Lists.newArrayList(issueCache.get(externalProject));
	}

	public Iterator<ExternalIssue> getIssuesIterator(ExternalProject externalProject, ImportLogger importLogger) {
		refreshCache(importLogger);

		Collection<ExternalIssue> issuesForProject = issueCache.get(externalProject);
		if (issuesForProject != null) {
			return issuesForProject.iterator();
		} else {
			return Lists.<ExternalIssue>newArrayList().iterator();
		}
	}

	public void cleanUp() {
		// Do nudda
	}

	public synchronized Collection<ExternalAttachment> getAttachmentsForIssue(ExternalIssue externalIssue, ImportLogger log) {
		List<ExternalAttachment> attachments = Lists.newArrayList();
		HttpDownloader downloader = new HttpDownloader();

		for(String url : attachmentsCache.get(externalIssue)) {
			try {
				File file = downloader.getAttachmentFromUrl(null, externalIssue.getExternalId(), url);
				ExternalAttachment attachment = new ExternalAttachment(
						new File(new URI(url, false).getPath()).getName(), file, new Date());
				attachment.setDescription(url);
				attachments.add(attachment);
			} catch (IOException e) {
				throw new DataAccessException("Exception occurred dealing with attachment.", e);
			}
		}
		return attachments;
	}

	public String getIssueKeyRegex() {
		return null;
	}

	public Collection<ExternalLink> getLinks(ImportLogger log) {
		final ArrayList<ExternalLink> res = Lists.newArrayList();
		for (Map.Entry<ExternalIssue, String> entry : parentIssuesCache.entries()) {
			res.add(new ExternalLink(ExternalLink.SUB_TASK_LINK, entry.getKey().getExternalId(), entry.getValue()));
		}
		return res;
	}

	public long getTotalIssues(Set<ExternalProject> selectedProjects, ImportLogger log) {
		refreshCache(log);
		return issueCache == null ? 0l : issueCache.values().size();
	}

	public String getUnusedUsersGroup() {
		return UNUSED_USERS_GROUP;
	}

	@Override
	public void afterProjectCreated(ExternalProject externalProject, Project project, ImportLogger importLogger) {
		// do nothing
	}

	public synchronized void setUserMappers(List<ExternalUserMapper> userMappers) {
		this.userMappers = userMappers;
	}

	public synchronized void setProjectMapper(ExternalProjectMapper projectMapper) {
		this.projectMapper = projectMapper;
	}

	public void setTimeEstimateConverter(TimeEstimateConverter timeEstimateConverter) {
		this.issueMapper.setTimeTrackingConverter(timeEstimateConverter);
	}

	public synchronized void setCommentMapper(ExternalCommentMapper commentMapper) {
		this.commentMapper = commentMapper;
	}

	@Override
	public Collection<ExternalCustomFieldValue> getGlobalCustomFields() {
		return Collections.emptyList();
	}

	@Override
	@Nullable
	public String getExternalSystemUrl() {
		return null;
	}
}
