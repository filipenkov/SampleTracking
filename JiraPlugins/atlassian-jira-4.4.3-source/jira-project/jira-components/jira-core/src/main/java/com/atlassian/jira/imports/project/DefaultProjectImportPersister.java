package com.atlassian.jira.imports.project;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.external.ExternalException;
import com.atlassian.jira.external.ExternalUtils;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.taskprogress.AbstractSubtaskProgressProcessor;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.util.IssueIdsIssueIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.web.util.AttachmentException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @since v3.13
 */
public class DefaultProjectImportPersister implements ProjectImportPersister
{
    private static final Logger log = Logger.getLogger(DefaultProjectImportPersister.class);

    private final UserUtil userUtil;
    private final ExternalUtils externalUtils;
    private final IssueFactory issueFactory;
    private final OfBizDelegator ofBizDelegator;
    private final IssueIndexManager issueIndexManager;
    private final IssueManager issueManager;
    private final ProjectFactory projectFactory;
    private final ProjectManager projectManager;
    private final VersionManager versionManager;
    private final AssociationManager associationManager;
    private final UserAssociationStore userAssociationStore;
    private final ProjectComponentManager projectComponentManager;
    private final AttachmentManager attachmentManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final CrowdService crowdService;
    private final ApplicationProperties applicationProperties;
    private final UserPropertyManager userPropertyManager;

    public DefaultProjectImportPersister(final UserUtil userUtil, final ExternalUtils externalUtils, final IssueFactory issueFactory,
            final OfBizDelegator ofBizDelegator, final IssueIndexManager issueIndexManager, final IssueManager issueManager,
            final ProjectFactory projectFactory, final ProjectManager projectManager, final VersionManager versionManager,
            final AssociationManager associationManager, final UserAssociationStore userAssociationStore, final ProjectComponentManager projectComponentManager,
            final AttachmentManager attachmentManager, final ChangeHistoryManager changeHistoryManager, final IssueLinkTypeManager issueLinkTypeManager,
            final CrowdService crowdService, final ApplicationProperties applicationProperties, final UserPropertyManager userPropertyManager)
    {
        this.userUtil = userUtil;
        this.externalUtils = externalUtils;
        this.issueFactory = issueFactory;
        this.ofBizDelegator = ofBizDelegator;
        this.issueIndexManager = issueIndexManager;
        this.issueManager = issueManager;
        this.projectFactory = projectFactory;
        this.projectManager = projectManager;
        this.versionManager = versionManager;
        this.associationManager = associationManager;
        this.userAssociationStore = userAssociationStore;
        this.projectComponentManager = projectComponentManager;
        this.attachmentManager = attachmentManager;
        this.changeHistoryManager = changeHistoryManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.crowdService = crowdService;
        this.applicationProperties = applicationProperties;
        this.userPropertyManager = userPropertyManager;
    }

    public Issue createIssue(final ExternalIssue externalIssue, final Date importDate, final User importAuthor)
    {
        try
        {
            final Issue issueForExternalIssue = createIssueForExternalIssue(externalIssue);
            final GenericValue issueGV = externalUtils.createIssue(issueForExternalIssue, externalIssue.getStatus(), externalIssue.getResolution());

            // Now go back and hack the issue key to be what we want it to be
            updateIssueKey(externalIssue, issueGV);

            // Create a change group/item to mark the issue such that we know it was created via a project import
            final ChangeItemBean changeItem = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "ProjectImport", "", "",
                String.valueOf(importDate.getTime()), importDate.toString());

            final MutableIssue issue = issueFactory.getIssue(issueGV);
            createChangeItem(importAuthor, issue, changeItem);

            return issue;
        }
        catch (final ExternalException e)
        {
            log.error("Unable to create issue with key '" + externalIssue.getKey() + "'.", e);
            return null;
        }
        catch (final RuntimeException e)
        {
            // DataAccessException can be thrown while trying to set some fields in an issue. Other RuntimeException may be possible?
            log.error("Unable to create issue with key '" + externalIssue.getKey() + "'.", e);
            return null;
        }
    }

    public Long createEntity(final EntityRepresentation entityRepresentation)
    {
        final GenericValue value = ofBizDelegator.makeValue(entityRepresentation.getEntityName());
        final ModelEntity modelEntity = value.getModelEntity();
        final Map map = entityRepresentation.getEntityValues();
        for (final Iterator i = modelEntity.getFieldsIterator(); i.hasNext();)
        {
            final ModelField modelField = (ModelField) i.next();
            final String name = modelField.getName();
            final String attr = (String) map.get(name);

            if (attr != null)
            {
                value.setString(name, attr);
            }
        }

        try
        {
            final GenericValue newValue = ofBizDelegator.createValue(value.getEntityName(), value.getAllFields());
            return newValue.getLong("id");
        }
        catch (final DataAccessException ex)
        {
            log.error(
                "DataAccessException occured while trying to create Entity type '" + entityRepresentation.getEntityName() + "' . " + entityRepresentation.getEntityValues(),
                ex);
            return null;
        }
    }

    public String createChangeItemForIssueLinkIfNeeded(final String issueId, final String issueLinkTypeId, final String linkedIssueKey, final boolean isSource, final User importAuthor)
    {
        Assertions.notBlank("issueId", issueId);
        Assertions.notBlank("issueLinkTypeId", issueLinkTypeId);
        Assertions.notBlank("linkedIssueKey", linkedIssueKey);
        final MutableIssue issue = issueManager.getIssueObject(new Long(issueId));
        if (issue == null)
        {
            // This should not happen, log it and move on
            log.warn("Attempted to create a change item for an issue link against an issue with id '" + issueId + "' but JIRA could not resolve the issue for that id, no change item will be created.");
            return null;
        }

        final boolean createChangeItem = createIssueLinkChangeItem(linkedIssueKey, issue);
        if (createChangeItem)
        {
            final ChangeItemBean changeItem = getChangeItemBean(issueLinkTypeId, linkedIssueKey, isSource);
            createChangeItem(importAuthor, issue, changeItem);
            issue.setUpdated(new Timestamp(System.currentTimeMillis()));
            issue.store();
            return issueId;
        }

        return null;
    }

    public void reIndexProject(final ProjectImportMapper projectImportMapper, final TaskProgressInterval taskProgressInterval, final I18nHelper i18n) throws IndexException
    {
        // Create a collection of Longs that are IDs of the new Issue object.
        final Collection newIssueIds = new ArrayList();
        for (final Iterator iterator = projectImportMapper.getIssueMapper().getAllMappedIds().iterator(); iterator.hasNext();)
        {
            final String issueIdAsString = (String) iterator.next();
            if (issueIdAsString != null)
            {
                try
                {
                    final Long issueId = new Long(issueIdAsString);
                    newIssueIds.add(issueId);
                }
                catch (final NumberFormatException e)
                {
                    log.warn("The Issue Mapper returned an invalid issue ID '" + issueIdAsString + "'.");
                }
            }
        }
        final IssuesIterable issuesIterable = getIssuesIterable(newIssueIds);
        final Context context = (taskProgressInterval == null) ? Contexts.percentageLogger(issuesIterable, log) : Contexts.percentageReporter(
            issuesIterable, taskProgressInterval.getTaskProgressSink(), i18n, log);
        issueIndexManager.reIndexIssues(issuesIterable, context);
    }

    public boolean createAssociation(final ExternalNodeAssociation nodeAssociation)
    {
        try
        {
            final GenericValue associationGV = associationManager.createAssociation(new Long(nodeAssociation.getSourceNodeId()),
                nodeAssociation.getSourceNodeEntity(), new Long(nodeAssociation.getSinkNodeId()), nodeAssociation.getSinkNodeEntity(),
                nodeAssociation.getAssociationType());
            return associationGV != null;
        }
        catch (final GenericEntityException e)
        {
            return false;
        }
    }

    public boolean createVoter(final ExternalVoter voter)
    {
        try
        {
            userAssociationStore.createAssociation(UserAssociationParser.ASSOCIATION_TYPE_VOTE_ISSUE, voter.getVoter(), IssueParser.ISSUE_ENTITY_NAME , new Long(voter.getIssueId()));
            return true;
        }
        catch (final DataAccessException e)
        {
            return false;
        }
    }

    public boolean createWatcher(final ExternalWatcher watcher)
    {
        try
        {
            userAssociationStore.createAssociation(UserAssociationParser.ASSOCIATION_TYPE_WATCH_ISSUE, watcher.getWatcher(), IssueParser.ISSUE_ENTITY_NAME , new Long(watcher.getIssueId()));
            return true;
        }
        catch (final DataAccessException e)
        {
            return false;
        }
    }

    public Project updateProjectDetails(final ExternalProject externalProject)
    {
        final GenericValue gvProject = projectManager.getProjectByKey(externalProject.getKey());
        if (gvProject == null)
        {
            // Major FUBAR
            throw new IllegalStateException(
                "Unable to find a project with key '" + externalProject.getKey() + "'. We can not create versions against a project that does not exist.");
        }
        gvProject.setString("name", externalProject.getName());
        gvProject.setString("description", externalProject.getDescription());
        gvProject.setString("lead", externalProject.getLead());
        gvProject.setString("url", externalProject.getUrl());

        // JRA-19699: cannot set null on this field - use either UNASSIGNED or PROJECT LEAD
        String assigneeType = externalProject.getAssigneeType();
        if (assigneeType == null)
        {
            if (isUnassignedIssuesAllowed())
            {
                assigneeType = String.valueOf(AssigneeTypes.UNASSIGNED);
            }
            else
            {
                assigneeType = String.valueOf(AssigneeTypes.PROJECT_LEAD);
            }
        }
        gvProject.setString("assigneetype", assigneeType);

        projectManager.updateProject(gvProject);
        // Email is stored separately :(
        final Project project = projectFactory.getProject(gvProject);
        setEmailSenderOnProject(project, externalProject.getEmailSender());
        return project;
    }

    ///CLOVER:OFF
    boolean isUnassignedIssuesAllowed()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }
    ///CLOVER:ON

    public Project createProject(final BackupProject backupProject) throws ExternalException
    {
        final GenericValue projectGV = externalUtils.createProject(backupProject.getProject());
        final Project project = projectFactory.getProject(projectGV);
        setEmailSenderOnProject(project, backupProject.getProject().getEmailSender());
        return project;
    }

    public Map /*<String,Version>*/createVersions(final BackupProject backupProject)
    {
        // Try to get the project
        final Project project = projectManager.getProjectObjByKey(backupProject.getProject().getKey());
        if (project == null)
        {
            throw new IllegalStateException(
                "Unable to find a project with key '" + backupProject.getProject().getKey() + "'. We can not create versions against a project that does not exist.");
        }

        final Long projectId = project.getId();
        final Map newVersions = new HashMap();
        // Add the versions in the specified sequence order so that they are in the right sequence in the db
        for (final Iterator iterator = getOrderedProjectVersions(backupProject).iterator(); iterator.hasNext();)
        {
            final ExternalVersion externalVersion = (ExternalVersion) iterator.next();
            try
            {
                final Version version = versionManager.createVersion(externalVersion.getName(), externalVersion.getReleaseDate(),
                    externalVersion.getDescription(), projectId, null);
                newVersions.put(externalVersion.getId(), version);
                if (externalVersion.isArchived())
                {
                    versionManager.archiveVersion(version, true);
                }
                if (externalVersion.isReleased())
                {
                    versionManager.releaseVersions(EasyList.build(version), true);
                }
            }
            catch (final Exception e)
            {
                log.error("There was a problem creating a project version for the project import.");
                throw new DataAccessException("There was a problem creating a project version for the project import.");
            }
        }
        return newVersions;
    }

    public Map /*<String,ProjectComponent>*/createComponents(final BackupProject backupProject)
    {
        // Try to get the project
        final Project project = projectManager.getProjectObjByKey(backupProject.getProject().getKey());
        if (project == null)
        {
            throw new IllegalStateException(
                "Unable to find a project with key '" + backupProject.getProject().getKey() + "'. We can not create components against a project that does not exist.");
        }

        final Long projectId = project.getId();
        final Map newComponents = new HashMap();
        // Add the versions in the specified sequence order so that they are in the right sequence in the db
        for (final Object element : backupProject.getProjectComponents())
        {
            final ExternalComponent externalComponent = (ExternalComponent) element;
            // We have seen data with null assignee-type, default this to AssigneeTypes.PROJECT_DEFAULT
            final long assigneeType = (externalComponent.getAssigneeType() != null) ? Long.parseLong(externalComponent.getAssigneeType()) : AssigneeTypes.PROJECT_DEFAULT;
            final ProjectComponent component = projectComponentManager.create(externalComponent.getName(), externalComponent.getDescription(),
                externalComponent.getLead(), assigneeType, projectId);
            if (component == null)
            {
                log.error("Could not create project component '" + externalComponent.getName() + "'.");
                throw new DataAccessException("Could not create project component '" + externalComponent.getName() + "'.");
            }
            newComponents.put(externalComponent.getId(), component);
        }
        return newComponents;
    }

    // Sort the project versions
    private Collection getOrderedProjectVersions(final BackupProject backupProject)
    {
        final ArrayList versions = new ArrayList(backupProject.getProjectVersions());
        Collections.sort(versions, new Comparator()
        {
            public int compare(final Object o, final Object o1)
            {
                final ExternalVersion ev0 = (ExternalVersion) o;
                final ExternalVersion ev1 = (ExternalVersion) o1;

                return ev0.getSequence().intValue() - ev1.getSequence().intValue();
            }
        });
        return versions;
    }

    public void updateProjectIssueCounter(final BackupProject backupProject, final long counter)
    {
        final GenericValue projectGV = projectManager.getProjectByKey(backupProject.getProject().getKey());
        if (projectGV != null)
        {
            projectGV.set("counter", new Long(counter));
            projectManager.updateProject(projectGV);
        }
    }

    public boolean createUser(final ExternalUser externalUser)
    {
        com.atlassian.crowd.embedded.api.User newUser = crowdService.getUser(externalUser.getName());
        if (newUser != null)
        {
            log.warn("User '" + externalUser.getName() + "' already exists, not creating the user from the backup files details.");
            return true;
        }

        ImmutableUser.Builder builder = ImmutableUser.newUser()
                .directoryId(-1l)
                .name(externalUser.getName())
                .displayName(externalUser.getFullname())
                .emailAddress(externalUser.getEmail())
                .active(true);

        // Crowd requires a password, so we set it randomly
        // and so the user cannot ever log in with it.
        Random random = new Random();
        String randomPassword = new BigInteger(130, random).toString(32) + "ABab23";
        try
        {
            newUser = crowdService.addUser(builder.toUser(), randomPassword);
        }
        catch (InvalidUserException e)
        {
            log.error("An error occurred while trying to create user '" + externalUser + "'.", e);
            return false;
        }
        catch (InvalidCredentialException e)
        {
            log.error("An error occurred while trying to create user '" + externalUser + "'.", e);
            return false;
        }
        catch (OperationNotPermittedException e)
        {
            log.error("An error occurred while trying to create user '" + externalUser + "'.", e);
            return false;
        }
        if (newUser == null)
        {
            log.error("An error occurred while trying to create user '" + externalUser + "'.");
            return false;
        }
        
        // Make sure we grant the user the JIRA use permission
        try
        {
            userUtil.addToJiraUsePermission(newUser);
        }
        catch (PermissionException e)
        {
            log.warn("User '" + externalUser.getName() + "' not added to Jira Use Permission, User Directory is read only.");
            return true;
        }
        // Iterate over all custom properties
        for (final Iterator iterator = externalUser.getUserPropertyMap().entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            // Store the properties in OS Property
            userPropertyManager.getPropertySet(newUser).setString(UserUtil.META_PROPERTY_PREFIX + entry.getKey(), (String) entry.getValue());
        }
        return true;
    }

    protected User convertToOSUser(final com.atlassian.crowd.embedded.api.User newUser)
    {
        return new User(newUser, crowdService);
    }

    public Attachment createAttachment(final ExternalAttachment externalAttachment)
    {
        Null.not("externalAttachment", externalAttachment);
        Null.not("attachedFile", externalAttachment.getAttachedFile());
        Null.not("fileName", externalAttachment.getFileName());
        Null.not("issueId", externalAttachment.getIssueId());

        final Issue issue = issueManager.getIssueObject(new Long(externalAttachment.getIssueId()));
        if (issue == null)
        {
            throw new IllegalArgumentException("Can not create an attachment against a null issue.");
        }

        try
        {
            return attachmentManager.createAttachmentCopySourceFile(externalAttachment.getAttachedFile(), externalAttachment.getFileName(),
                ExternalUtils.GENERIC_CONTENT_TYPE, externalAttachment.getAttacher(), issue, Collections.EMPTY_MAP,
                externalAttachment.getAttachedDate());
        }
        catch (final AttachmentException e)
        {
            log.error("Unable to create issue file attachment with name '" + externalAttachment.getFileName() + "'.", e);
            return null;
        }
    }

    void setEmailSenderOnProject(final Project project, final String emailSender)
    {
        if (emailSender != null)
        {
            final PropertySet propertySet = getPropertySet(project);
            propertySet.setString(ProjectKeys.EMAIL_SENDER, emailSender);
        }
    }

    PropertySet getPropertySet(final Project project)
    {
        return OFBizPropertyUtils.getPropertySet(project.getGenericValue());
    }

    ///CLOVER:OFF - this is a static call, we don't want to test this, we just test the input to the method
    void createChangeItem(final User author, final Issue issue, final ChangeItemBean changeItem)
    {
        ChangeLogUtils.createChangeGroup(author, issue, issue, EasyList.build(changeItem), false);
    }

    ///CLOVER:ON

    IssuesIterable getIssuesIterable(final Collection<Long> newIssueIds)
    {
        return new IssueIdsIssueIterable(newIssueIds, issueManager);
    }

    void updateIssueKey(final ExternalIssue externalIssue, final GenericValue issueGV)
    {
        issueGV.setString("key", externalIssue.getKey());
        try
        {
            issueGV.store();
        }
        catch (final GenericEntityException e)
        {
            final String message = "Unable to set the required key '" + externalIssue.getKey() + "' in the Issue that we just created (id = '" + issueGV.getLong("id") + "').";
            log.error(message, e);
            throw new DataAccessException(message, e);
        }
    }

    Issue createIssueForExternalIssue(final ExternalIssue externalIssue)
    {
        final MutableIssue issue = issueFactory.getIssue();

        issue.setProjectId(new Long(externalIssue.getProject()));

        issue.setIssueTypeId(externalIssue.getIssueType());

        issue.setReporterId(externalIssue.getReporter());
        issue.setAssigneeId(externalIssue.getAssignee());

        issue.setSummary(externalIssue.getSummary());
        issue.setDescription(externalIssue.getDescription());
        issue.setEnvironment(externalIssue.getEnvironment());
        issue.setPriorityId(externalIssue.getPriority());
        issue.setResolutionId(externalIssue.getResolution());

        issue.setCreated(toTimeStamp(externalIssue.getCreated()));
        issue.setUpdated(toTimeStamp(externalIssue.getUpdated()));
        issue.setDueDate(toTimeStamp(externalIssue.getDuedate()));
        //NOTE: This HAS to come after the setResolutionId() call, otherwhise that will override this date.
        issue.setResolutionDate(toTimeStamp(externalIssue.getResolutionDate()));

        issue.setVotes(externalIssue.getVotes());

        issue.setOriginalEstimate(externalIssue.getOriginalEstimate());
        issue.setTimeSpent(externalIssue.getTimeSpent());
        issue.setEstimate(externalIssue.getEstimate());
        if (externalIssue.getSecurityLevel() != null)
        {
            issue.setSecurityLevelId(new Long(externalIssue.getSecurityLevel()));
        }
        return issue;
    }

    ChangeItemBean getChangeItemBean(final String issueLinkTypeId, final String linkedIssueKey, final boolean isSource)
    {
        // Get the issue link for the type we are creating
        final IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(new Long(issueLinkTypeId));
        final ChangeItemBean changeItem;
        if (isSource)
        {
            changeItem = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, linkedIssueKey,
                "This issue " + issueLinkType.getOutward() + " " + linkedIssueKey);
        }
        else
        {
            changeItem = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, linkedIssueKey,
                "This issue " + issueLinkType.getInward() + " " + linkedIssueKey);
        }
        return changeItem;
    }

    boolean createIssueLinkChangeItem(final String linkedIssueKey, final Issue issue)
    {
        final List linkChangeItemsForIssue = changeHistoryManager.getChangeItemsForField(issue, "Link");
        // Run through all the change items for field Link and see if there is one for our key and if it has a corresponding
        // delete change item. We want to create the change item if:
        //   * there is no reference to the link
        //   * there is a reference to the link but there is also a reference to deleting the link
        boolean createItemFound = false;
        boolean deleteItemFound = false;
        for (final Iterator iterator = linkChangeItemsForIssue.iterator(); iterator.hasNext();)
        {
            final ChangeItemBean changeItemBean = (ChangeItemBean) iterator.next();
            if (linkedIssueKey.equals(changeItemBean.getFrom()))
            {
                createItemFound = true;
            }
            else if (linkedIssueKey.equals(changeItemBean.getTo()))
            {
                deleteItemFound = true;
            }
        }
        return !createItemFound || deleteItemFound;
    }

    private Timestamp toTimeStamp(final Date date)
    {
        if (date != null)
        {
            return new Timestamp(date.getTime());
        }
        else
        {
            return null;
        }
    }

    /**
     * Simple task processor that formats the message to tell about reindex progress.
     */
    static class ReindexTaskProgressProcessor extends AbstractSubtaskProgressProcessor
    {
        private final TaskProgressSink taskProgressSink;
        private final I18nHelper i18n;

        public ReindexTaskProgressProcessor(final TaskProgressInterval taskProgressInterval, final I18nHelper i18n)
        {
            super(taskProgressInterval, 100);
            this.i18n = i18n;
            taskProgressSink = (taskProgressInterval == null) ? null : taskProgressInterval.getTaskProgressSink();
        }

        public void processTaskProgress(final int progress)
        {
            // Be kind to null taskProgress
            if (taskProgressSink == null)
            {
                return;
            }
            final long percent = getOverallPercentageComplete(progress);
            final String message = i18n.getText("admin.indexing.percent.complete", new Long(progress));
            taskProgressSink.makeProgress(percent, i18n.getText("admin.indexing.indexing"), message);
        }
    }
}
