/*
 * Copyright (C) 2002-2012 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.*;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CsvDataBean extends ImportDataBean {
	private static final String EXTERNAL_ISSUE_AUTO = "autoid-";
	protected static final String UNUSED_USERS_GROUP = "csv-import-unused-users";

	private CsvProvider provider;
	private LinkedHashSet<ExternalProject> projectsCache;
	private Set<ExternalUser> usersCache;
	private ListMultimap<ExternalProject, ExternalVersion> versionCache;
	private ListMultimap<ExternalProject, ExternalComponent> componentCache;
	private Multimap<ExternalProject, ExternalIssue> issueCache; // actually ListMultimap
	private ListMultimap<ExternalIssue, String> attachmentsCache;
	private LinkedListMultimap<ExternalIssue, String> parentIssuesCache;
    private final IssueManager issueManager;
    private final AttachmentManager attachmentManager;

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
	private final ExternalWorklogMapper externalWorklogMapper;
	private final CsvConfigBean configBean;
    private final JiraHome jiraHome;

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
                          final ExternalLabelsMapper labelsMapper,
                          ExternalWorklogMapper externalWorklogMapper,
                          JiraHome jiraHome, IssueManager issueManager, AttachmentManager attachmentManager) throws FileNotFoundException {
		this.configBean = configBean;
		this.customFieldVersionMapper = customFieldVersionMapper;
		this.externalWorklogMapper = externalWorklogMapper;
		this.jiraHome = jiraHome;
        this.issueManager = issueManager;
        this.attachmentManager = attachmentManager;
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

	public CsvDataBean(CsvConfigBean configBean, CustomFieldManager customFieldManager, JiraHome jiraHome, IssueManager issueManager, AttachmentManager attachmentManager)
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
				new SimpleExternalLabelsMapper(), new SimpleExternalWorklogMapper(configBean),
				jiraHome, issueManager, attachmentManager);
		CsvMapper mapper = new PropertiesCsvMapper(configBean);
		provider = new MindProdCsvProvider(configBean.getImportLocation(), configBean.getEncoding(), mapper,
				configBean.getDelimiter());
	}

	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		refreshCache(log);

		return Sets.newHashSet(projectsCache);
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
		projectsCache = Sets.newLinkedHashSet();
		usersCache = Collections.newSetFromMap(new ConcurrentHashMap<ExternalUser, Boolean>());
		versionCache = ArrayListMultimap.create();
		componentCache = ArrayListMultimap.create();
		issueCache = Multimaps.synchronizedMultimap(ArrayListMultimap.<ExternalProject, ExternalIssue>create());
		attachmentsCache = ArrayListMultimap.create();
		parentIssuesCache = LinkedListMultimap.create();
	}

	private void mergeCaches(ExternalProject from, ExternalProject to) {
		versionCache.putAll(to, versionCache.removeAll(from));
		componentCache.putAll(to, componentCache.removeAll(from));
		issueCache.putAll(to, issueCache.removeAll(from));
		projectsCache.remove(from);
	}

	private synchronized void populateCache(ImportLogger log) {
		try {
			provider.startSession();

			ListMultimap<String, String> issueMap;

			final Map<String, ExternalProject> projectsByName = Maps.newHashMap();
			final Map<String, ExternalProject> projectsByKey = Maps.newHashMap();
			while ((issueMap = provider.getNextLine()) != null) {
				for (ExternalUserMapper externalUserMapper : userMappers) {
					// TODO: We need to change the ExternalUserMapper so that it can handle multiple Users for multi-select User Custom Fields.
					// we translate user names here so issueMapper.buildFromMultiMap() below sees the translated names already.
					// comment mapper (and other non-flat fields) have to do that themselves - syntax is too complex for externalUserMapper
					ExternalUser externalUser = externalUserMapper.buildFromMultiMap(issueMap);
					if (externalUser != null) {
						usersCache.add(externalUser);
					}
				}

				// all mappers are configured with the same class.
				final ExternalUserNameMapper userMapper = userMappers.isEmpty() ? ExternalUserMapper.NOOP : userMappers.iterator().next();

				final ExternalProject curProject = projectMapper.buildFromMultiMap(issueMap);
				if (curProject == null) {
					log.warn("CSV row cannot be assosciated with any project: %s", issueMap);
					continue;
				}
				final ExternalProject externalProject = collapseProjectIfPossible(projectsByName, projectsByKey, curProject, log);
				if (externalProject != null) {
					projectsCache.add(externalProject);

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
					final List<ExternalComponent> externalComponents = componentMapper.buildFromMultiMap(issueMap);
					if (externalComponents != null) {
						componentCache.putAll(externalProject, externalComponents);
					}

					// Labels
					final Set<String> externalLabels = labelsMapper.buildFromMultiMap(issueMap);

					// Comments
					final List<ExternalComment> externalComments = commentMapper.buildFromMultiMap(issueMap, userMapper, log);

					final List<ExternalWorklog> externalWorklogs = externalWorklogMapper.buildFromMultiMap(issueMap, userMapper, log);

					// Custom Fields
					final List<ExternalCustomFieldValue> externalCustomFieldValues =
							customFieldValueMapper.buildFromMultiMap(issueMap, log);

					// Issues
					final ExternalIssue issue = issueMapper.buildFromMultiMap(issueMap, log);

//					if (StringUtils.isNotBlank(issue.getSummary())) {
						if (affectedVersions != null) {
							issue.setAffectedVersions(Lists.transform(affectedVersions, NamedExternalObject.NAME_FUNCTION));
						}
						if (fixedVersions != null) {
							issue.setFixedVersions(Lists.transform(fixedVersions, NamedExternalObject.NAME_FUNCTION));
						}
						if (externalComponents != null) {
							issue.setComponents(Lists.transform(externalComponents, NamedExternalObject.NAME_FUNCTION));
						}
						if (externalComments != null) {
							issue.setComments(externalComments);
						}
						if (externalWorklogs != null) {
							issue.setWorklogs(externalWorklogs);
						}
						issue.setExternalCustomFieldValues(externalCustomFieldValues);
						issue.setLabels(externalLabels);

						issueCache.put(externalProject, issue);
						// @todo attachment cache should be removed completely and instead ExternalAttachment refactored
						// to use Supplier for getting the content should be used
						attachmentsCache.putAll(issue, issueMap.get(IssueFieldConstants.ATTACHMENT));
						if (issue.getExternalId() == null) {
							issue.setExternalId(EXTERNAL_ISSUE_AUTO + RandomUtils.nextLong());
							issue.setAutoExternalId(true);
						}
						parentIssuesCache.putAll(issue, issueMap.get(DefaultExternalIssueMapper.SUBTASK_PARENT_ID));
//					} else {
//						log.warn("Issue %s has a blank summary. This is being ignored.", issue);
//					}
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
	private ExternalProject collapseProjectIfPossible(Map<String, ExternalProject> projectsByName, Map<String, ExternalProject> projectsByKey,
			ExternalProject curProject, ImportLogger logger) {
		ExternalProject externalProject = curProject;
		if (curProject.getName() != null) {
			final ExternalProject prevProj = projectsByName.get(curProject.getName());
			if (prevProj != null) {
				// names are the same
				externalProject = chooseProject(prevProj, curProject, prevProj.getKey(), curProject.getKey());
				if (externalProject == null) {
					logger.warn("Two projects have the same name '%s' but different key '%s' and '%s'. This issue will be skipped",
							curProject.getName(), prevProj.getKey(), curProject.getKey());
					return null;
				}
			}
			projectsByName.put(externalProject.getName(), externalProject);
		}
		if (curProject.getKey() != null) {
			final ExternalProject prevProj = projectsByKey.get(curProject.getKey());
			if (prevProj != null) {
				externalProject = chooseProject(prevProj, externalProject, prevProj.getName(), externalProject.getName());
				if (externalProject == null) {
					logger.warn("Two projects have the same key '%s' but different name '%s' and '%s'. This issue will be skipped",
							curProject.getKey(), prevProj.getName(), curProject.getName());
					return null;
				}
			}
			projectsByKey.put(externalProject.getKey(), externalProject);
		}
		return externalProject;
	}

	private ExternalProject chooseProject(ExternalProject prevProject, ExternalProject curProject, final String prevId, final String curId) {
		if (Objects.equal(prevId, curId)) {
			return prevProject;
		} else if (prevId != null && curId != null ){
			// error
			return null;
		} else if (curId != null) {
			mergeCaches(prevProject, curProject);
			return curProject;
		} else {
			return prevProject;
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

	@Override
	public synchronized Collection<ExternalAttachment> getAttachmentsForIssue(final ExternalIssue externalIssue, final ImportLogger log) {
		final List<String> attachmentUrls = attachmentsCache.get(externalIssue);
        final Collection<ExternalAttachment> attachmentsForIssue = getAttachmentsForIssue(externalIssue, attachmentUrls, log);
        if (externalIssue.getKey() == null) {
            return attachmentsForIssue;
        }
        final MutableIssue issue = issueManager.getIssueObject(externalIssue.getKey());
        final List<Attachment> attachments = attachmentManager.getAttachments(issue);
        final Collection<String> existingAttachments = Collections2.transform(attachments, new Function<Attachment, String>() {
            @Override
            public String apply(Attachment input) {
                return input.getFilename();
            }
        });
        return Collections2.filter(attachmentsForIssue, new Predicate<ExternalAttachment>() {
            @Override
            public boolean apply(ExternalAttachment input) {
                final boolean alreadyExists = existingAttachments.contains(input.getName());
                if (alreadyExists) {
                    log.log("Attachment '%s' of issue '%s' already exists. Skipping", input.getName(), externalIssue.getKey());
                }
                return !alreadyExists;
            }
        });
	}

	Collection<ExternalAttachment> getAttachmentsForIssue(ExternalIssue externalIssue, List<String> attachmentUrls, ImportLogger log) {
		final List<ExternalAttachment> attachments = Lists.newArrayListWithCapacity(attachmentUrls.size());
		final HttpDownloader downloader = new HttpDownloader();

		for (String url : attachmentUrls) {
			try {
				final ExternalAttachmentInfo info = new ExternalAttachmentInfoMapper(configBean).parse(url);
				final File file;
				final String guessedFileName;
				if ("file".equals(info.uri.getScheme())) {
					// we could use getPath() only if we insisted on proper file:///always/3/slashes
					final String filePart = info.uri.getSchemeSpecificPart();
					final File importAttachmentsDirectory = jiraHome.getImportAttachmentsDirectory();
					final File sourceFile = new File(importAttachmentsDirectory, filePart).getCanonicalFile();
					if (!StringUtils.startsWith(sourceFile.getAbsolutePath(), importAttachmentsDirectory.getCanonicalPath())) {
						log.warn("Imported attachment file is outside of permitted base directory, skipping: %s", filePart);
						continue;
					}
					if (!sourceFile.exists() || !sourceFile.canRead()) {
						log.warn("Attachment file not found or not readable, skipping: %s", sourceFile.getAbsolutePath());
						continue;
					}
					file = File.createTempFile("temporary-jira-importer-attachment-copy", ".tmp");
					FileUtils.copyFile(sourceFile, file);
					guessedFileName = sourceFile.getName();
				} else {
					file = downloader.getAttachmentFromUrl(null, externalIssue.getExternalId(), info.uri.toString());
					guessedFileName = new File(StringUtils.defaultString(info.uri.getPath())).getName();
				}
				final String filename = StringUtils.defaultIfEmpty(info.filename, guessedFileName);
				final Date timestamp = info.timestamp != null ? info.timestamp.toDate() : new Date();
				final ExternalAttachment attachment = new ExternalAttachment(filename, file, timestamp);
				attachment.setAttacher(info.author);
//				attachment.setDescription(info.uri.toString());
				attachments.add(attachment);
			} catch (ParseException e) {
				log.warn(e, "Cannot parse '%s' as attachment info: %s", url, e.getMessage());
			} catch (IOException e) {
				log.warn(e, "An exception occurred dealing with attachment '%s'.", url);
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

    /**
     * Get HTML with a return link
     */
    public String getReturnLinks() {
        return String.format("<div id=\"importAgain\"><a href=\"ExternalImport1.jspa\">%s</a></div>",
                configBean.getI18n().getText("jira-importer-plugin.ImporterLogsPage.import.another"));
    }
}
