/*
 * Copyright (C) 2002-2012 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.*;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SummarySystemField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.ExternalException;
import com.atlassian.jira.plugins.importer.external.ExternalUserUtils;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.imports.csv.ImportException;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.DefaultExternalIssueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.*;
import com.atlassian.jira.plugins.importer.sample.Callbacks;
import com.atlassian.jira.plugins.importer.managers.CreateConstantsManager;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build175;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ImportUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.joda.time.DateTime;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.plugins.importer.external.CustomFieldConstants.TEXT_FIELD_TYPE;
import static org.apache.commons.lang.Validate.notNull;

@Nonnull
@SuppressWarnings("deprecation")
public class DefaultJiraDataImporter implements JiraDataImporter {
	public static final String EXTERNAL_ISSUE_ID = "External issue ID";
	public static final String EXTERNAL_ISSUE_URL = "External issue URL";

	private static final int MAX_ISSUES_PER_REINDEX = 50;

	private final ExternalUtils utils;
	private final WorklogManager worklogManager;
	private final FieldManager fieldManager;
	private final WatcherManager watcherManager;
	private final VoteManager voteManager;
	private final SubTaskManager subTaskManager;
	private final VersionManager versionManager;
	private final ExternalUserUtils externalUserUtils;
	private final CreateProjectManager createProjectManager;
	private final JiraContextTreeManager jiraContextTreeManager;
	private final CrowdService crowdService;
	private final OptionsManager optionsManager;
	private final SearchProviderFactory searchServiceFactory;
	private final UserUtil userUtil;
	private final JiraLicenseService jiraLicenseService;
	private final ProjectComponentManager componentManager;
	private final CustomFieldManager customFieldManager;
	private final FieldScreenManager fieldScreenManager;
    private final FieldConfigManager fieldConfigManager;
	private final OfBizHistoryImporter historyImporter;

	// Flags
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicBoolean aborted = new AtomicBoolean(false);
	private final IssueIndexManager indexManager;
	private final CreateConstantsManager createConstantsManager;
	private final LabelParser.CreateFromString<Label> labelFactory;

	private final Collection<GenericValue> unindexedIssueGvs = new LinkedList<GenericValue>();

	private Set<ExternalProject> selectedProjects = null;
	private UserProvider userProvider;

	protected ImportLogger log;
	protected ImportStats stats;
	private ImportDataBean dataBean;

	private File logFile;
	private ImportObjectIdMappings mappings;
	private Map<String, String> externalIdToIssueKey;
	private Map<String, CustomField> customFieldsMapping;
	private volatile String abortedBy;
	private int skippedIssues;
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    private Callbacks callbacks;

	public DefaultJiraDataImporter(ExternalUtils utils,
			WorklogManager worklogManager, FieldManager fieldManager,
			WatcherManager watcherManager, VoteManager voteManager, IssueIndexManager indexManager,
			CreateConstantsManager createConstantsManager, SubTaskManager subTaskManager,
			VersionManager versionManager, ExternalUserUtils externalUserUtils,
			JiraContextTreeManager jiraContextTreeManager, CreateProjectManager createProjectManager,
			CrowdService crowdService, OptionsManager optionsManager, SearchProviderFactory searchProviderFactory,
			UserUtil userUtil, JiraLicenseService jiraLicenseService, ProjectComponentManager componentManager,
			CustomFieldManager customFieldManager, FieldScreenManager fieldScreenManager, FieldConfigSchemeManager fieldConfigSchemeManager, FieldConfigManager fieldConfigManager, OfBizHistoryImporter historyImporter) {
		this.utils = utils;
		this.worklogManager = worklogManager;
		this.fieldManager = fieldManager;
		this.watcherManager = watcherManager;
		this.voteManager = voteManager;
		this.indexManager = indexManager;
		this.createConstantsManager = createConstantsManager;
		this.subTaskManager = subTaskManager;
		this.versionManager = versionManager;
		this.externalUserUtils = externalUserUtils;
		this.jiraContextTreeManager = jiraContextTreeManager;
		this.createProjectManager = createProjectManager;
		this.crowdService = crowdService;
		this.optionsManager = optionsManager;
		this.searchServiceFactory = searchProviderFactory;
		this.userUtil = userUtil;
		this.jiraLicenseService = jiraLicenseService;
		this.componentManager = componentManager;
		this.customFieldManager = customFieldManager;
		this.fieldScreenManager = fieldScreenManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.fieldConfigManager = fieldConfigManager;
		this.historyImporter = historyImporter;

		this.labelFactory = new CreateLabelFromString();
	}

	public Set<ExternalUser> getNonExistentAssociatedUsers() {
		final Set<ExternalUser> nonExistentUsers = new HashSet<ExternalUser>();
		for (final ExternalUser externalUser : dataBean.getRequiredUsers(selectedProjects, log)) {
			final User user = userProvider.getUser(externalUser);
			if (user == null) {
				nonExistentUsers.add(externalUser);
			}
		}
		return nonExistentUsers;
	}

	public void doImport() {
		try {
			try {
				log.log("Import started by %s using %s", utils.getAuthenticationContext().getLoggedInUser().getName(), dataBean.getClass().getName());

				preImport();

				stats.beginStep(ImportStats.Stage.VALIDATE);
				try {
					// If they have chosen not to import users or if external user management is enabled we want to check
					// that all users already exist within the system.
					if (isExternalUserManagementEnabled()) {
						Set<ExternalUser> unknownUsers = getNonExistentAssociatedUsers();
						if (!unknownUsers.isEmpty()) {
							throw new UnknownUsersException(getI18nHelper().getText("jira-importer-plugin.external.user.externalusermanagementenabled"), unknownUsers);
						}
					}

					// Check if all
				} finally {
					stats.endStep();
				}

				stats.beginStep(ImportStats.Stage.USERS);
				try {
					// Import the Users
					if (!isExternalUserManagementEnabled()) {
						importUsers();

						log.log("%d users successfully created.", stats.getStage(ImportStats.Stage.USERS).getItemsCreated());
					} else {
						log.skip("Users");
					}
				} finally {
					stats.endStep();
				}

				log.log("Retrieving projects...");

				stats.beginStep(ImportStats.Stage.PROJECTS);
				try {
					stats.setTotalItems(selectedProjects.size());

					for (final ExternalProject project : selectedProjects) {
						if (isShouldStopImport()) {
							break;
						}

						stats.incrementProgress();

						importProject(project);

						// Create Versions
						importVersions(project);

						// Create Components
						importComponents(project);
					}
				} finally {
					stats.endStep();
				}

				stats.beginStep(ImportStats.Stage.CUSTOM_FIELDS);
				try {
                    if (!selectedProjects.isEmpty()) {
                        log.log("Retrieving custom fields...");
                        final List<ExternalCustomField> customFields = Lists.newArrayList(dataBean.getCustomFields());
                        customFields.add(new ExternalCustomField(EXTERNAL_ISSUE_ID, EXTERNAL_ISSUE_ID, CustomFieldConstants.TEXT_FIELD_TYPE, CustomFieldConstants.TEXT_FIELD_SEARCHER));

                        stats.setTotalItems(customFields.size());

                        final List<CustomField> existingCustomFields = customFieldManager.getCustomFieldObjects();

                        for (ExternalCustomField field : customFields) {
                            if (isShouldStopImport()) {
                                break;
                            }

                            stats.incrementProgress();

                            customFieldsMapping.put(field.getName(), verifyCustomFieldOrCreate(existingCustomFields, selectedProjects, field));
                        }
                    }
				} finally {
					stats.endStep();
				}

				log.beginImportSection("Issues");

				stats.beginStep(ImportStats.Stage.ISSUES);
				try {
					stats.setTotalItems(dataBean.getTotalIssues(selectedProjects, log));

					for (final ExternalProject project : selectedProjects) {
						if (isShouldStopImport()) {
							break;
						}
						// Create Issues
						importIssues(project);
					}

					if (skippedIssues > 0) {
						log.warn("%d of %d issues have been skipped because they already exist in destination projects.",
								skippedIssues, stats.getIssuesStage().getItemsToBeImported());
					}

					if (unindexedIssueGvs.size() > 0) {
						reindexIssues();
					}

					final long totalItems = stats.getTotalItems();
					final long importedItems = stats.getIssuesStage().getItemsImported();
					final String fmt = (totalItems == importedItems ? "%d"  : "%d out of %d") + " issues successfully created";
					log.log(fmt, importedItems, totalItems);
				} finally {
					stats.endStep();

					log.endImportSection("Issues");
				}

				stats.beginStep(ImportStats.Stage.LINKS);
				try {
					final Map<String, String> externalIdsToIssueKeys;

					if (dataBean.getExternalSystemUrl() != null) {
						externalIdsToIssueKeys = getExternalIdsToIssueKeysMap(createQueryForExternalSystemUrl(
								dataBean.getExternalSystemUrl()));
					} else {
						externalIdsToIssueKeys = getExternalIdsToIssueKeysMap(getSelectedProjects());
					}

					externalIdsToIssueKeys.putAll(externalIdToIssueKey);

					if (!isShouldStopImport()) {
						// Rewrite comments,
						final String regex = dataBean.getIssueKeyRegex();
						if (regex != null) {
							rewriteOldIssueKeys(externalIdsToIssueKeys, regex);
						}
					}

					if (!isShouldStopImport()) {
						// Link issues if issue linking is on
						importIssueLinks(externalIdsToIssueKeys, utils.areSubtasksEnabled(), utils.isIssueLinkingOn());
					}
				} finally {
					stats.endStep();
				}
			} catch (UnknownUsersException e) {
				log.fail(e, e.getMessage());
			} catch (final Exception e) {
				log.fail(e, "Unexpected failure occurred. Importer will stop immediately. Data maybe in an unstable state");
			} catch (Error e) {
				// JIRA does not log exception/errors for submitted tasks via TaskManager at all!
				log.fail(e, "Unexpected failure occurred. Importer will stop immediately. Data maybe in an unstable state");
			}

			postImport();
		} finally {
			running.set(false);
		}
	}

	private Query createQueryForExternalSystemUrl(String externalSystemUrl) {
		Collection<CustomField> customFields = customFieldManager.getCustomFieldObjectsByName(EXTERNAL_ISSUE_URL);
		BooleanQuery query = new BooleanQuery();
		for (CustomField cf : customFields) {
			query.add(new PrefixQuery(
					new Term(cf.getId(), externalSystemUrl)), BooleanClause.Occur.SHOULD);
		}
		return query;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	protected File getLogFile() {
		if (logFile != null && logFile.exists()) {
			logFile.delete();
			logFile = null;
		}

		try {
			logFile = File.createTempFile("jiraImportersPlugin-", ".log");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return logFile;
	}

	@Override
	public void initializeLog() {
		aborted.set(false);

		stats = new ImportStats(utils.getDateUtils(), utils.getAuthenticationContext().getI18nHelper());

		log = new FileImportLogger(getLogFile()) {
			@Override
			public void fail(@Nullable Throwable e, String fmt, Object... args) {
				super.fail(e, fmt, args);
				incrementFailures(fmt, args);
			}

			private void incrementFailures(String fmt, Object... args) {
				if (stats != null) {
					stats.incrementFailures(String.format(fmt, args));
				}
			}

			private void incrementWarnings(String fmt, Object... args) {
				if (stats != null) {
					stats.incrementWarnings(String.format(fmt, args));
				}
			}

			@Override
			public void warn(@Nullable Throwable e, String s, Object... args) {
				super.warn(e, s, args);
				incrementWarnings(s, args);
			}
		};
	}

	protected void preImport() {
		ImportUtils.setSubvertSecurityScheme(true);
		ImportUtils.setEnableNotifications(false);

		stats.start();

		skippedIssues = 0;
		unindexedIssueGvs.clear();
		mappings = new ImportObjectIdMappings();
		externalIdToIssueKey = Maps.newHashMap();
		customFieldsMapping = Maps.newHashMap();
		selectedProjects = dataBean.getSelectedProjects(log);
        callbacks = dataBean.getCallbacks();
	}

	protected void postImport() {
		if (isAborted()) {
			log.log(StringUtils.repeat("-", 30));
			log.log("IMPORT CANCELLED");
			log.log("The import has been cancelled by %s. Cleaning up import...", abortedBy);
			log.log(StringUtils.repeat("-", 30));
		}

		userProvider = null;

		// Clean up the data source
		dataBean.cleanUp();

		ImportUtils.setSubvertSecurityScheme(false);

		// This is needed since the project category caches don't get cleared properly
		utils.getProjectManager().refresh();

		ImportUtils.setEnableNotifications(true);

		if (unindexedIssueGvs.size() > 0) {
			reindexIssues();
		} else {
			log.log("No issues need to be reindexed.");
		}
		stats.stop();

		mappings = null;
        callbacks = null;
	}

	/**
	 * @return -1 if all can be activated
	 */
	protected int getNumberOfUsersThatCanBeActivated() {
		final LicenseDetails licenseDetails = jiraLicenseService.getLicense();
		if (licenseDetails.isUnlimitedNumberOfUsers()) {
			return -1;
		}

		return licenseDetails.getMaximumNumberOfUsers() - userUtil.getActiveUserCount();
	}

	protected void importUsers() {
		// Import the Users
		log.beginImportSection("Users");
		final Collection<ExternalUser> users = Ordering.natural().onResultOf(NamedExternalObject.NAME_FUNCTION)
				.immutableSortedCopy(dataBean.getAllUsers(log));
		final Set<ExternalUser> requiredUsers = dataBean.getRequiredUsers(getSelectedProjects(), log);

		stats.setTotalItems(users.size());

		final int usersToActivate = getNumberOfUsersThatCanBeActivated();

		if (usersToActivate == -1 || usersToActivate >= users.size()) {
			log.log("%d users associated with import.", users.size());
		} else {
			log.warn("%d users associated with import. %d will be imported as active due to license limits. Check log for details.",
					users.size(), usersToActivate);
		}

		for (final ExternalUser externalUser : users) {
			if (isShouldStopImport()) {
				break;
			}

			stats.incrementProgress();

			User user = userProvider.getUser(externalUser);
			if (user == null) {
				if (!requiredUsers.contains(externalUser)) {
					externalUser.getGroups().add(dataBean.getUnusedUsersGroup());
				}

				user = externalUserUtils.createUser(externalUser, log);
				if (user != null) {
                    callbacks.afterUserCreated(externalUser, user);
					stats.incrementCreated();

					if (!requiredUsers.contains(externalUser)) {
						log.log("Imported user %s (%s) as an inactive user because it was not used in the external system.",
								externalUser.getFullname(), externalUser.getName());
						try {
							externalUserUtils.deactivateUser(user);
						} catch (Exception e) {
							log.fail(e, "Unable to deactivate user %s", externalUser);
						}
					} else {
						if (externalUserUtils.isUserActive(user)) {
							log.log("Imported user %s (%s)", externalUser.getFullname(), externalUser.getName());
						} else {
							log.log("Imported user %s (%s) as an inactive user due to license limits.",
								externalUser.getFullname(), externalUser.getName());
						}
					}
				} else {
					log.fail(null, "Unable to import user %s", externalUser);
				}
			}
		}
		log.endImportSection("Users");
	}

	private Project importProject(final ExternalProject externalProject) throws Exception {
		Project jiraProject = utils.getProject(externalProject);

		if (jiraProject == null) {
			try {
				// JIM-477: match the lead name to the actuall user
				if (externalProject.getLead() != null) {
					externalProject.setLead(userProvider.getUser(externalProject.getLead()).getName());
				}

				jiraProject = createProjectManager.createProject(utils.getAuthenticationContext().getLoggedInUser(), externalProject, log);

				final Set<ExternalUser> users = dataBean.getRequiredUsers(Collections.singleton(externalProject), log);
				final Multimap<String, String> userRoles = HashMultimap.create();
				userRoles.put(UpgradeTask_Build175.ROLE_DEVELOPERS, externalProject.getLead());
				for (ExternalUser user : users) {
					for (String role : user.getProjectRoles().get(externalProject.getId())) {
						userRoles.put(role, userProvider.getUser(user.getName()).getName());
					}
				}
				for (String role : userRoles.keySet()) {
					externalUserUtils.addUsersToProjectRole(utils.getAuthenticationContext().getLoggedInUser(),
							jiraProject, role, userRoles.get(role));
				}

				if (StringUtils.isNotBlank(externalProject.getProjectCategoryName())) {
					ProjectCategory projectCategory = utils.getProjectManager()
							.getProjectCategoryObjectByNameIgnoreCase(externalProject.getProjectCategoryName());
					if (projectCategory == null) {
						projectCategory = utils.getProjectManager()
								.createProjectCategory(externalProject.getProjectCategoryName(), null);
						utils.getProjectManager().refresh();
					}

					utils.getProjectManager().setProjectCategory(jiraProject, projectCategory);
				}

				callbacks.afterProjectCreated(externalProject, jiraProject);
				log.log("Created Project: %s successfully", externalProject);
				stats.incrementCreated();
			} catch (final Exception e) {
				throw new Exception("Unable to import Project " + externalProject, e);
			}
		} else {
			log.log("Project %s already exists. Not imported", externalProject);
		}

		externalProject.setJiraId(jiraProject.getId());

		return jiraProject;
	}

	private void importVersions(final ExternalProject externalProject) {
		log.beginImportSection("Versions");
		final Collection<ExternalVersion> versions = dataBean.getVersions(externalProject, log);

		// Add the versions in order
		if (versions != null) {
			for (final ExternalVersion externalVersion : versions) {
				if (isShouldStopImport()) {
					break;
				}

				Version jiraVersion = versionManager.getVersion(externalProject.getJiraId(),
						externalVersion.getName());
				if (jiraVersion == null) {
					// Import version
					log.log("Importing version %s", externalVersion.getName());
					jiraVersion = utils.createVersion(externalProject, externalVersion, log);
					if (jiraVersion != null) {
                        callbacks.afterVersionCreated(externalVersion, jiraVersion);
                    } else {
						log.fail(null, "Unable to import version %s", externalVersion);
					}
				}
				mappings.addVersionMapping(externalProject.getName(), externalVersion.getName(), jiraVersion);
			}
		}
		log.endImportSection("Versions");
	}

	private void importComponents(final ExternalProject externalProject) {
		log.beginImportSection("Components");
		final Collection<ExternalComponent> components = dataBean.getComponents(externalProject, log);
		if (components != null) {
			for (final ExternalComponent externalComponent : components) {
				final Map<String, ProjectComponent> projectComponents = getProjectComponents(externalProject.getJiraId());

				if (isShouldStopImport()) {
					break;
				}

			  	ProjectComponent jiraComponent = projectComponents.get(externalComponent.getName().toUpperCase());
				if (jiraComponent == null) {
					// Import component
					log.log("Importing component %s", externalComponent.getName());
					String lead = null;
					if (StringUtils.isNotBlank(externalComponent.getLead())) {
						User leadUser = userProvider.getUser(externalComponent.getLead());
						if (leadUser != null) {
							lead = leadUser.getName();
						} else {
							log.warn("Component lead %s not found", externalComponent.getLead());
						}
					}

					jiraComponent = createProjectComponent(externalProject,
							new ExternalComponent(externalComponent.getName(),
									externalComponent.getId(), lead,
									externalComponent.getDescription()), log);
					if (jiraComponent != null) {
                        callbacks.afterComponentCreated(externalComponent, jiraComponent);
                    } else {
						log.fail(null, "Unable to import component %s", externalComponent);
					}
				}

				if (jiraComponent != null) {
					mappings.addComponentMapping(externalProject.getName(), externalComponent.getName(),
							jiraComponent);
				}
			}
		}
		log.endImportSection("Components");
	}

	@Override
	public void setDataBean(final ImportDataBean dataBean) {
		this.dataBean = dataBean;

		selectedProjects = null;

		userProvider = new UserProvider();
	}

	@Override
	public ImportDataBean getDataBean() {
		return this.dataBean;
	}

	private static class CreateLabelFromString implements LabelParser.CreateFromString<Label> {

		public Label create(final String stringIn) {
			return new Label(null, null, null, stringIn);
		}
	}

	private void importIssues(final ExternalProject externalProject) {
		final Map<String, String> existingIssues = getExternalIdsToIssueKeysMap(externalProject);
		final Iterator<ExternalIssue> issues = dataBean.getIssuesIterator(externalProject, log);
        final Set<String> assignees = Sets.newHashSet();
		while ((issues != null) && issues.hasNext()) {
			if (isShouldStopImport()) {
				break;
			}

			stats.incrementProgress();

			final ExternalIssue externalIssue = issues.next();
			final String oldId = externalIssue.getExternalId();

			if (existingIssues.containsKey(oldId)) {
				++this.skippedIssues;

				log.log("External issue %s already exists as %s, not importing.",
						oldId, existingIssues.get(oldId));
			} else {
				log.log("Importing issue: %s", externalIssue);

				try {
					if (createIssue(externalProject, externalIssue, assignees) != null) {
                        stats.incrementCreated();
                    }
				} catch (final ExternalException e) {
					log.fail(e, "Error importing issue %s", externalIssue);
				}

				// Reindex if required
				if (unindexedIssueGvs.size() >= MAX_ISSUES_PER_REINDEX) {
					reindexIssues();
				}
			}
		} // end while

        if (!assignees.isEmpty()) {
            try {
                externalUserUtils.addUsersToProjectRole(utils.getAuthenticationContext().getLoggedInUser(),
                        utils.getProjectManager().getProjectObj(externalProject.getJiraId()), UpgradeTask_Build175.ROLE_DEVELOPERS,
                        assignees);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
	}

	private Map<String, String> getExternalIdsToIssueKeysMap(Set<ExternalProject> projects) {
		BooleanQuery query = new BooleanQuery();
		for (ExternalProject project : projects) {
			query.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, "" + project.getJiraId())),
					BooleanClause.Occur.SHOULD);
		}
		return getExternalIdsToIssueKeysMap(query);
	}

	private Map<String, String> getExternalIdsToIssueKeysMap(ExternalProject project) {
		return getExternalIdsToIssueKeysMap(new TermQuery(new Term(DocumentConstants.PROJECT_ID,
				"" + project.getJiraId())));
	}

	private Map<String, String> getExternalIdsToIssueKeysMap(Query query) {
		final Map<String, String> issueKeyMappings = Maps.newHashMap();
		final Collection<CustomField> ids = customFieldManager.getCustomFieldObjectsByName(EXTERNAL_ISSUE_ID);

		// clean JIRA perhaps, no need to run query
		if (ids == null || ids.isEmpty()) {
			return issueKeyMappings;
		}

		final IndexSearcher searcher = searchServiceFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
		final Collector hitCollector = new DocumentHitCollector(searcher) {
			@Override
			public void collect(Document document) {
				final Issue issue = utils.getIssueFactory().getIssue(document);

				for (CustomField cf : ids) {
					final Object value = issue.getCustomFieldValue(cf);
					if (value != null) {
						issueKeyMappings.put(value.toString(), issue.getKey());
					}
				}
			}
		};
		try {
			searcher.search(query, hitCollector);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return issueKeyMappings;
	}

	private MutableIssue createIssue(@Nonnull ExternalProject externalProject, @Nonnull ExternalIssue externalIssue, @Nonnull Set<String> assignees)
			throws ExternalException {
		translateIssueConstants(externalIssue);

		final String summary = externalIssue.getSummary();
		if (summary != null && summary.length() > SummarySystemField.MAX_LEN) {
			log.log("Summary longer than max length of %1$d. Truncating to %1$d characters.", SummarySystemField.MAX_LEN);
			externalIssue.setSummary(StringUtils.abbreviate(summary, SummarySystemField.MAX_LEN.intValue()));
		}

		// Create issue
		final GenericValue issueGV;
		try {
			final MutableIssue convertedIssue = utils.convertExternalIssueToIssue(userProvider,
					externalIssue, externalProject, mappings, log);
			if (convertedIssue.getId() == null) {
				issueGV = utils.createIssue(convertedIssue,
						externalIssue.getStatus(), externalIssue.getResolution(), log);
			} else {
				issueGV = utils.updateIssue(convertedIssue, externalIssue.getStatus(), log);
			}
		} catch (final ImportException e) {
			throw new ExternalException("Unable to create or update issue: " + externalIssue, e);
		}

		// Save the issue for ever
		unindexedIssueGvs.add(issueGV);

		MutableIssue mutableIssue = utils.getIssueFactory().getIssue(issueGV);

        if (mutableIssue.getAssigneeId() != null) {
            assignees.add(mutableIssue.getAssigneeId());
        }

		if (externalIssue.getExternalId() != null) {
			externalIdToIssueKey.put(externalIssue.getExternalId(), mutableIssue.getKey());
		}

		// Add custom fields
		final List<ExternalCustomFieldValue> externalCustomFieldValues = externalIssue
				.getExternalCustomFieldValues();
		if (externalCustomFieldValues != null) {
			importExternalCustomFields(mutableIssue, externalCustomFieldValues);
		}

		// Always import external ID as the same name
		if (externalIssue.getExternalId() != null && !externalIssue.isAutoExternalId()) {
			importExternalCustomFields(mutableIssue, Lists.<ExternalCustomFieldValue>newArrayList(
					new ExternalCustomFieldValue(EXTERNAL_ISSUE_ID, CustomFieldConstants.TEXT_FIELD_TYPE,
							CustomFieldConstants.TEXT_FIELD_SEARCHER, externalIssue.getExternalId())));
		}

		// Add comments
		if (externalIssue.getComments() != null) {
			importComments(externalIssue, mutableIssue);
		}

		// Add voters
		if (externalIssue.getVoters() != null) {
			importVoters(externalIssue, mutableIssue);
		}

		// Add watchers
		if (externalIssue.getWatchers() != null) {
			importWatchers(externalIssue, issueGV);
		}

		// Add worklogs
		if (fieldManager.isTimeTrackingOn() && externalIssue.getWorklogs() != null) {
			importWorklog(externalIssue, mutableIssue.getGenericValue());
		}

		// Add attachments
		if (utils.areAttachmentsEnabled()) {
			try {
				final Collection<ExternalAttachment> attachments = dataBean.getAttachmentsForIssue(externalIssue, log);
				for (final ExternalAttachment attachment : attachments) {
					utils.attachFile(userProvider, attachment, mutableIssue, log);
				}
			} catch (final Exception e) {
				log.fail(e, "Failed to attach attachments to issue %s", mutableIssue.getKey());
			}
		}
		// Add subtasks - beware of recursion
		if (subTaskManager.isSubTasksEnabled()) {
			final List<ExternalIssue> subtasks = externalIssue.getSubtasks();
			for (ExternalIssue externalSubtask : subtasks) {
				final MutableIssue subtask = createIssue(externalProject, externalSubtask, assignees);
				try {
					subTaskManager.createSubTaskIssueLink(mutableIssue, subtask, mutableIssue.getAssigneeUser());
				} catch (CreateException e) {
					log.fail(e, "Failed to create sub-task link for %s", mutableIssue.getKey());
				}
			}
		}

		if (externalIssue.getHistory() != null) {
			historyImporter.importHistory(mutableIssue.getId(), externalIssue.getHistory());
		}

		return mutableIssue;
	}

	/**
	 * Translate resolutions, issue type, resolution. Don't translate Status because we already require it to match
	 * existing statuses.
	 *
	 * @param externalIssue
	 * @throws ExternalException
	 */
	private void translateIssueConstants(ExternalIssue externalIssue) throws ExternalException {
		final String[] constantTypes = {
				ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE,
				ConstantsManager.PRIORITY_CONSTANT_TYPE,
				ConstantsManager.RESOLUTION_CONSTANT_TYPE
		};

		for (final String constantType : constantTypes) {
			final String issueConstantKey = constantType.toLowerCase();
			final String constantValue = externalIssue.getField(issueConstantKey);
			if (constantValue == DefaultExternalIssueMapper.CLEAR_VALUE_MARKER) {
				continue;
			}
			if (StringUtils.isNotEmpty(constantValue)) {
				final IssueConstant constant = createConstantsManager.getConstant(constantValue, constantType);
				String id;
				if (constant == null) {
					// Create it the constant
					id = createConstantsManager.addConstant(constantValue, constantType);
					log.log("Created %s : %s with id %s", constantType, constantValue, id);
				} else {
					id = constant.getId();
				}
				externalIssue.setField(issueConstantKey, id);
			}
		}
	}

	private void importVoters(ExternalIssue externalIssue, MutableIssue issue) throws ExternalException {
		for (String username : externalIssue.getVoters()) {
			final User voter = userProvider.getUser(username);
			if (voter == null) {
				throw new ExternalException("No such user: " + username);
			}

			final String resolution = issue.getString("resolution");
			issue.setResolutionId(null); // hack to import votes on 'resolved' issues. JRA-6440
			try {
				if (!voteManager.addVote(voter, issue.getGenericValue())) {
					log.log("Failed to import vote on %s", issue.getKey());
				}
			} finally {
				issue.setResolutionId(resolution);
			}
		}
	}

	private void importWatchers(ExternalIssue externalIssue, GenericValue issueGV) throws ExternalException {
		for (String username : externalIssue.getWatchers()) {
			try {
				User watcher = userProvider.getUser(username);
				if (watcher != null) {
					watcherManager.startWatching(watcher, issueGV);
				} else {
					log.fail(null, "Watcher '%s' doesn't have an account in JIRA", username);
				}
			} catch (final RuntimeException e) {
				log.fail(e, "Failed to add a watcher to issue with id '%s'", externalIssue.getExternalId());
			}
		}
	}

	protected void importWorklog(ExternalIssue externalIssue, GenericValue issueGV) throws ExternalException {
		for (ExternalWorklog externalWorklog : externalIssue.getWorklogs()) {
			final Issue issue = utils.getIssueFactory().getIssue(issueGV);
			final User user = userProvider.getUser(externalWorklog.getAuthor());
			final String author = user != null ? user.getName() : null;
			final DateTime startDate = externalWorklog.getStartDate();
			final Worklog worklog = new WorklogImpl(worklogManager, issue, null, author, externalWorklog.getComment(),
					startDate != null ? startDate.toDate() : new Date(), null, null, externalWorklog.getTimeSpent());
			worklogManager.create(user, worklog, null, false);
		}
	}

	private void importComments(ExternalIssue externalIssue, Issue issue) {
		for (final ExternalComment externalComment : externalIssue.getComments()) {
			try {
				//
				// the comment should have the same performed date as the issue updated/create
				// unless its been explictly defined
				//
				DateTime commentDate = externalComment.getCreated();
				if (commentDate == null) {
					commentDate = externalIssue.getUpdated() != null ? externalIssue.getUpdated()
							: externalIssue.getCreated();
				}
				utils.addComments(userProvider, issue, new ExternalComment(externalComment.getBody(),
						externalComment.getAuthor(), commentDate), false, false, log);
			} catch (final ExternalException e) {
				log.fail(e, "Unable to import comment %s", externalComment);
			}
		}
	}

	protected CustomField verifyCustomFieldOrCreate(final List<CustomField> existingCustomFields, final Set<ExternalProject> projects, final ExternalCustomField customField) throws ExternalException {
		final List<CustomField> matchingCustomFields = Lists.newArrayList(Iterables.filter(
                Iterables.filter(existingCustomFields, new AbstractConfigBean2.CustomFieldPredicate(customField, projects)), new Predicate<CustomField>() {
            @Override
            public boolean apply(@Nullable CustomField input) {
                return input.getName().equals(customField.getName());
            }
        }));

		final CustomField createdCustomField;
		if (matchingCustomFields.isEmpty()) {
			log.log("Custom field not found. Creating a new custom field for %s", customField);
			createdCustomField = createCustomField(customField.getName(), customField.getTypeKey(), customField.getSearcherKey());
            callbacks.afterCustomFieldCreated(customField, createdCustomField);
			stats.getStage(ImportStats.Stage.CUSTOM_FIELDS).incrementCreated();
		} else {
			createdCustomField = Iterables.getFirst(matchingCustomFields, null);
        }

        final ExternalProject project = Iterables.getFirst(projects, null);
		if (createdCustomField != null && customField.getValueSet() != null && project != null) {
			for(String value : customField.getValueSet()) {
				// Add options as appropriate
                addOptions(createdCustomField, customField.createValue(value), new ProjectContext(project.getJiraId()));
			}
		}

		return createdCustomField;
	}

	protected void importExternalCustomFields(final MutableIssue issue, final Collection<ExternalCustomFieldValue> fieldValues)
			throws ExternalException {
		for (final ExternalCustomFieldValue customFieldValue : fieldValues) {
			CustomField customField = customFieldsMapping.get(customFieldValue.getFieldName());
			if (customField == null) {
				customField = verifyCustomFieldOrCreate(customFieldManager.getCustomFieldObjects(), selectedProjects,
						new ExternalCustomField(customFieldValue.getFieldName(), customFieldValue.getFieldName(), customFieldValue.getFieldType(), customFieldValue.getSearcherType()));
			}

			if (customField != null) {
                // Check if issue type id is handled by the custom field, if not extend it
                if (!customField.isAllIssueTypes()) {
                    associateCustomFieldWithIssueType(customField, issue.getIssueTypeObject().getId());
                }

				// Add options as appropriate
				addOptions(customField, customFieldValue, issue);

				// Add value
				final Object value = customFieldValue.getValue();
				if (DefaultExternalIssueMapper.CLEAR_VALUE_MARKER.equals(value)) {
					if (customField.canRemoveValueFromIssueObject(issue)) {
						customField.getCustomFieldType().updateValue(customField, issue, null);
					} else {
						log.warn("Removing value for custom field '%s' from issue %s is not supported, skipping.",
								customField.getName(), issue.getKey());
					}
				} else if (value != null) {
					try {
						// Special case/hack for Multi selects (well would need it for Cascades as well)
						final Object valueToAdd;

						final CustomFieldType customFieldType = customField.getCustomFieldType();
						// JRA-10515 updated this test for User/Group multi-fields as well.
						if (customFieldType instanceof VersionCFType) {
							valueToAdd = Collections2.transform((Collection<String>) value, new Function<String, Version>() {
										@Override
										public Version apply(@Nullable String version) {
											return versionManager.getVersion(issue.getProjectObject().getId(), version);
										}
									});
                        } else if (customFieldType instanceof MultiUserCFType) {
                            valueToAdd = Collections2.transform((Collection<String>) value, new Function<String, User>() {
                                @Override
                                public User apply(@Nullable String user) {
                                    return userProvider.getUser(user);
                                }
                            });
                        } else if (customFieldType instanceof MultiGroupCFType) {
                            valueToAdd = Collections2.transform((Collection<String>) value, new Function<String, Group>() {
                                @Override
                                public Group apply(@Nullable String group) {
                                    return userUtil.getGroupObject(group);
                                }
                            });
						} else if (customFieldType instanceof AbstractMultiCFType) {
							final CustomField finalCustomField = customField;
							valueToAdd = Collections2.transform((Collection<String>) value, new Function<String, Option>() {
                                @Override
                                public Option apply(@Nullable String input) {
                                    return optionsManager.getOptions(
                                            finalCustomField.getRelevantConfig(issue))
                                            .getOptionForValue(input, null);
                                }
                            });
						} else if (customFieldType instanceof SelectCFType) {
							valueToAdd = optionsManager.getOptions(
									customField.getRelevantConfig(issue)).getOptionForValue((String) value, null);
						} else if (customFieldType instanceof LabelsCFType) {
							valueToAdd = LabelParser.buildFromString(labelFactory, value.toString());
						} else {
							valueToAdd = customFieldType.getSingularObjectFromString(value.toString());
						}

						customField.createValue(issue, valueToAdd);
					} catch (final Exception e) {
						log.fail(e,
								"An error occurred while attempting to import value '%s' into the Custom Field '%s'.",
								value, customField.getName());
					}
				}
			}
		}
	}

	private void rewriteOldIssueKeys(final Map<String, String> issueKeyMappings, final String regex) {
		try {
			log.log("Rewriting old issue keys for %d issues", issueKeyMappings.size());

			for (final String issueKey : issueKeyMappings.values()) {
				final MutableIssue issue = utils.getIssueManager().getIssueObject(issueKey);
				if (issue != null) {
					final String oldSummary = issue.getSummary();
					final String newSummary = rewriteStringWithIssueKeys(regex, issueKeyMappings, oldSummary);

					final String oldDescription = issue.getDescription();
					if (oldDescription != null) {
						final String newDescription = rewriteStringWithIssueKeys(regex, issueKeyMappings,
								oldDescription);
						if (!oldDescription.equals(newDescription) || !oldSummary.equals(newSummary)) {
							log.log("Rewritten summary and/or description for issue %s", issueKey);

							issue.setSummary(newSummary);
							issue.setDescription(newDescription);
							issue.store();
						}
					}

					@SuppressWarnings("unchecked")
					final Collection<GenericValue> comments = utils.getGenericDelegator().findByAnd("Action",
							EasyMap.build("type", "comment", "issue", issue.getId()));

					for (final GenericValue comment : comments) {
						final String oldComment = comment.getString("body");
						final String newComment = rewriteStringWithIssueKeys(regex, issueKeyMappings, oldComment);
						if (!oldComment.equals(newComment)) {
							log.log("Rewritten comment for issue %s", issueKey);

							comment.setString("body", newComment);
							comment.store();
						}
					}
				}
			}
		} catch (final GenericEntityException e) {
			throw new DataAccessException(e);
		}
	}

	// This method is only ever used by the FogBugz importer. It is used to rewrite FogBugz issue references
	// ("case: 2842") into JIRA references ("JRA-2848"). The logic in here is so specific to the FogBugz regular expression
	// that I wouldn't try using it with anything else. Really, this method should be on the importer-specific ImportBean
	// rather than trying to be an all purpose method.
	public static String rewriteStringWithIssueKeys(final String regexPattern, final Map<String, String> keyLookupTable, final String s) {
		notNull(regexPattern);
		notNull(s);

		String result = s;

		final Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(s);

		// there may be multiple matching phrases in the string and we need to fix up all of them
		while (matcher.find()) {
			final String oldIds = matcher.group(1);
			final String cleanedOldIds = StringUtils.replace(oldIds, " ", "");
			final StringTokenizer st = new StringTokenizer(cleanedOldIds, ",");
			final StringBuffer sb = translateKeys(keyLookupTable, st);
			if (sb != null) {
				// our regex may include spaces so we need to preserve them
				if (oldIds.endsWith(" ")) {
					sb.append(" ");
				}

				result = result.replace(matcher.group(0), sb.toString());
			}
		}

		return result;
	}

	@Nullable

	private static StringBuffer translateKeys(final Map<String, String> keyLookupTable, final StringTokenizer tokenizer) {
		final StringBuffer sb = new StringBuffer();

		// if we have multiple issue keys in the phrase then we want to prepend "issues".
		// this means that "case: 46" becomes plain old "JRA-3848" but "cases: 48, 28" becomes "issues JRA-382, JRA-1928"
		// (note that this isn't internationalized in anyway.)
		if (tokenizer.countTokens() > 1) {
			sb.append("issues ");
		}

		while (tokenizer.hasMoreTokens()) {
			final String token = tokenizer.nextToken();
			if (keyLookupTable.containsKey(token)) {
				sb.append(keyLookupTable.get(token));
				if (tokenizer.hasMoreTokens()) {
					sb.append(", ");
				}
			} else {
				// If *any* of the ids don't match, just leave this entire match unchanged
				return null;
			}
		}
		return sb;
	}

	void importIssueLinks(final Map<String, String> issueKeyMappings, boolean shouldImportSubTasks, boolean shouldImportRegularLinks) {
		if (!shouldImportRegularLinks && !shouldImportSubTasks) {
			return;
		}
		final StringBuilder info = getIssueLinkingInfoHeader(shouldImportSubTasks, shouldImportRegularLinks);

		log.beginImportSection("Issue " + info);
		final Collection<ExternalLink> links = dataBean.getLinks(log);
		if (links != null) {
			for (final ExternalLink externalLink : links) {
				if (StringUtils.isBlank(externalLink.getName())) {
					continue; // skip links that were not configured
				}
				if (!shouldImportSubTasks && externalLink.isSubtask()) {
					continue;
				}
				if (!shouldImportRegularLinks && !externalLink.isSubtask()) {
					continue;
				}
				try {
					final String sourceKey = issueKeyMappings.get(externalLink.getSourceId());
					if (sourceKey == null) {
						log.fail(null,
								formatLinkingFailureIssueNotFoundMessage(externalLink, externalLink.getSourceId()));
						continue;
					}
					final String destinationKey = issueKeyMappings.get(externalLink.getDestinationId());
					if (destinationKey == null) {
						log.fail(null, "%s",
								formatLinkingFailureIssueNotFoundMessage(externalLink, externalLink.getDestinationId()));
						continue;
					}
                    if (!StringUtils.equals(sourceKey, destinationKey)) {
                        log.log("Linking '%s' and '%s' as %s", sourceKey, destinationKey, externalLink.getName());

                        utils.createIssueLink(sourceKey, destinationKey, externalLink.getName(),
                                externalLink.isSubtask(), log);
                    }
				} catch (final ExternalException e) {
					log.fail(null, "%s", formatLinkingFailureMessage(externalLink, e.getMessage()));
				}
			}
		}
		log.endImportSection("Issue " + info);
	}

	private String formatLinkingFailureIssueNotFoundMessage(ExternalLink externalLink, String missingId) {
		final String message = getI18nHelper().getText("jira-importer-plugin.import.importedIssueNotFound", missingId);
		return formatLinkingFailureMessage(externalLink, message);
	}

	private String formatLinkingFailureMessage(ExternalLink externalLink, String message) {
		return getI18nHelper().getText("jira-importer-plugin.import.linkingFailure",
				externalLink.getName(),
				externalLink.getSourceId(),
				externalLink.getDestinationId(),
				message);
	}

	private StringBuilder getIssueLinkingInfoHeader(boolean shouldImportSubTasks, boolean shouldImportRegularLinks) {
		final StringBuilder info = new StringBuilder();
		if (shouldImportRegularLinks) {
			info.append("Links");
		}
		if (shouldImportSubTasks) {
			if (info.length() > 0) {
				info.append(" & ");
			}
			info.append("Subtasks");
		}
		return info;
	}

	private void reindexIssues() {
		try {
			log.log("Reindexing last %d issues imported ...", unindexedIssueGvs.size());
			final long l = indexManager.reIndexIssues(unindexedIssueGvs);
			log.log("Reindexing took %d ms.", l);

			unindexedIssueGvs.clear();
		} catch (final IndexException e) {
			log.fail(e, "Reindexing failed");
		}
	}

	private boolean isShouldStopImport() {
		return isAborted();
	}

	@Nullable
	public ImportStats getStats() {
		if (stats != null) {
			stats.setRunning(running.get());
			stats.setAborted(aborted.get());
		}
		return stats;
	}

	public ImportLogger getLog() {
		return log;
	}

	public boolean isRunning() {
		return running.get();
	}

	public void setRunning() {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException("Importer is already running.");
		}
	}

	public boolean isAborted() {
		return aborted.get();
	}

	public void abort(final String user) {
		this.aborted.set(true);
		this.abortedBy = user;
	}

	public String getAbortedBy() {
		return abortedBy;
	}

	public Set<ExternalProject> getSelectedProjects() {
		if (selectedProjects == null) {
			selectedProjects = dataBean.getSelectedProjects(log);
		}
		return selectedProjects;
	}

	public final boolean isExternalUserManagementEnabled() {
		return utils.getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
	}

	private I18nHelper getI18nHelper() {
		return utils.getAuthenticationContext().getI18nHelper();
	}

	public class UserProvider implements com.atlassian.jira.plugins.importer.external.UserProvider {
		@SuppressWarnings({"unchecked"})
		private final Map<String, String> aliasedByEmail = new CaseInsensitiveMap();

		@Nullable
		public User getUserByEmail(String email) {
			if (StringUtils.isNotEmpty(email)) {
				return (User) Iterables.getOnlyElement(crowdService.search(new UserQuery(
						User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
						StringUtils.stripToEmpty(email).toLowerCase()), 0, 1)), null);
			}
			return null;
		}

		@Nullable
		public User getUser(ExternalUser externalUser) {
			User user = crowdService.getUser(externalUser.getName());
			if (user == null) {
				user = getUserByEmail(externalUser.getEmail());
				if (user != null) {
					aliasedByEmail.put(externalUser.getName(), user.getName());
				}
			}
			return user;
		}

		@Override
		public User getUser(@Nullable String username) {
			if (username != null) {
				User user = crowdService.getUser(username);
				if (user == null && aliasedByEmail.containsKey(username)) {
					user = crowdService.getUser(aliasedByEmail.get(username));
				}
				return user;
			}
			return null;
		}
	}

    private void associateCustomFieldWithIssueType(final CustomField customField, String issueTypeId) {
        final List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        if (schemes.isEmpty() || schemes.size() > 1) {
            log.warn("Custom field '%s' has multiple configuration schemes. Importer is unable to extend custom field context automatically.", customField.getName());
            return;
        }
        final FieldConfigScheme scheme = Iterables.getOnlyElement(schemes);
        final Map<String, FieldConfig> issueTypes = Maps.newHashMap(scheme.getConfigs());
        final FieldConfig config = (FieldConfig) Iterables.getFirst(scheme.getConfigsByConfig().keySet(), fieldConfigManager.createWithDefaultValues(customField));
        if (!issueTypes.containsKey(issueTypeId)) {
            issueTypes.put(issueTypeId, config);

            final FieldConfigScheme configScheme = new FieldConfigScheme.Builder(scheme).setConfigs(issueTypes).toFieldConfigScheme();
            fieldConfigSchemeManager.updateFieldConfigScheme(configScheme, Collections.singletonList(jiraContextTreeManager.getRootNode()), customField);
            customFieldManager.refresh();
        }
    }

	private void associateCustomFieldWithScreen(final CustomField customField, @Nullable FieldScreen screen) {
		if (screen == null) {
			screen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
		}

		if ((screen != null) && (screen.getTabs() != null) && !screen.getTabs().isEmpty()) {
			final FieldScreenTab tab = screen.getTab(0);
			tab.addFieldScreenLayoutItem(customField.getId());
		}
	}

	private CustomField createCustomField(final String customFieldName, @Nullable final String type, @Nullable final String searcherType)
			throws ExternalException {
		try {
			// Create cf of the correct type
			CustomFieldType cfType;
			CustomFieldSearcher searcher = null;

			if (StringUtils.isNotEmpty(type)) {
				cfType = customFieldManager.getCustomFieldType(type);
			} else {
				cfType = customFieldManager.getCustomFieldType(TEXT_FIELD_TYPE);
			}

			if (cfType == null) {
				throw new ExternalException("Cannot create custom field [" + customFieldName + "] because its type ["
						+ type + "] is not recognized by this JIRA instance");				}

			if (StringUtils.isNotEmpty(searcherType)) {
				searcher = customFieldManager.getCustomFieldSearcher(searcherType);
			} else {
				List searchers = customFieldManager.getCustomFieldSearchers(cfType);
				if (searchers != null && !searchers.isEmpty()) {
					searcher = (CustomFieldSearcher) searchers.get(0);
				}
			}

			final CustomField customField = customFieldManager
					.createCustomField(customFieldName, customFieldName, cfType, searcher,
							Lists.newArrayList(Iterables.transform(selectedProjects, new Function<ExternalProject, ProjectContext>() {
								@Override
								public ProjectContext apply(@Nullable ExternalProject input) {
									return new ProjectContext(input.getJiraId());
								}
							})), EasyList.buildNull());

			associateCustomFieldWithScreen(customField, null);

			return customField;

		} catch (final GenericEntityException e) {
			throw new ExternalException(e);
		}
	}

	protected void addOptions(final CustomField customFieldObject, final ExternalCustomFieldValue customFieldValue,
			final IssueContext context) {
		if (customFieldObject != null
				&& customFieldObject.getCustomFieldType() instanceof MultipleSettableCustomFieldType) {
			final FieldConfig config = customFieldObject.getRelevantConfig(context);
			final Options options = customFieldObject.getOptions(null, config, null);
			final Collection<Object> values = customFieldValue.getValue() instanceof Collection ?
					(Collection<Object>) customFieldValue.getValue() : Lists.newArrayList(customFieldValue.getValue());

			for (final Object value : values) {
				if (value != null) {
					String valueAsString = value.toString();
					if (StringUtils.isNotBlank(valueAsString)
							&& options.getOptionForValue(valueAsString, null) == null) {
						final long sequence = options.size();
						optionsManager.createOption(config, null, sequence, valueAsString);
					}
				}
			}
		}
	}

	public Map<String, ProjectComponent> getProjectComponents(final Long projectId) {
		return Maps.uniqueIndex(componentManager.findAllForProject(projectId), new Function<ProjectComponent, String>() {
			@Override
			public String apply(ProjectComponent input) {
				return input.getName().toUpperCase();
			}
		});
	}

	@Nullable
	public ProjectComponent createProjectComponent(final ExternalProject externalProject,
			final ExternalComponent externalComponent, ImportLogger log) {
		try {
			final String componentName = externalComponent.getName();
			final ProjectComponent projectComponent = componentManager
					.create(componentName,
							externalComponent.getDescription(), externalComponent.getLead(),
							AssigneeTypes.PROJECT_DEFAULT,
							externalProject.getJiraId());
			return projectComponent;
		} catch (final Exception e) {
			log.warn(e, "Problems encoutered while creating Component %s", externalComponent);
		}
		return null;
	}
}
