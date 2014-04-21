/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SummarySystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CloneIssueDetails extends CreateIssueDetails
{
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final IssueLinkManager issueLinkManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final SubTaskManager subTaskManager;
    private final AttachmentManager attachmentManager;
    private final FieldManager fieldManager;
    private final IssueFactory issueFactory;

    private IssueLinkType cloneIssueLinkType;
    private String cloneIssueLinkTypeName;

    private MutableIssue issueObject;

    // The original issue that is to be cloned.
    private Issue originalIssue;

    // The clone parent of the clone subtasks.
    private GenericValue cloneParent;
    // Whether or not to clone issue links as well
    private boolean cloneLinks;
    // Whether or not to clone issue's sub-tasks
    private boolean cloneSubTasks;
    // Whether or not to clone issue's attachments
    private boolean cloneAttachments;
    // Map of old IssueId -> new IssueId for cloned issues
    private final Map<Long, Long> newIssueIdMap = new HashMap<Long, Long>();

    public CloneIssueDetails(ApplicationProperties applicationProperties, PermissionManager permissionManager,
                             IssueLinkManager issueLinkManager, IssueLinkTypeManager issueLinkTypeManager, SubTaskManager subTaskManager,
                             AttachmentManager attachmentManager, FieldManager fieldManager, IssueCreationHelperBean issueCreationHelperBean,
                             IssueFactory issueFactory, IssueService issueService)
    {
        super(issueFactory, issueCreationHelperBean, issueService);
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.issueLinkManager = issueLinkManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.subTaskManager = subTaskManager;
        this.attachmentManager = attachmentManager;
        this.fieldManager = fieldManager;
        this.issueFactory = issueFactory;
    }

    // Initialise the clone issue with the data from the original issue
    public String doDefault() throws Exception
    {
        this.cloneSubTasks = true;
        this.cloneLinks = false;
        this.cloneAttachments = false;

        try
        {
            Issue issueObject = getIssueObject(getIssue());
            setOriginalIssue(issueObject);

            // Copy the details of the original issue for the clone issue
            setIssueDetails(issueObject);
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }

        // Summary can be modified - require futher input
        return INPUT;
    }

    // Set clone issue fields to same values as in issue
    public void setIssueDetails(Issue issue) throws GenericEntityException
    {
        SummarySystemField summaryField = (SummarySystemField) fieldManager.getOrderableField(IssueFieldConstants.SUMMARY);
        summaryField.populateFromIssue(getFieldValuesHolder(), getOriginalIssue());
        String summary = (String) getFieldValuesHolder().get(IssueFieldConstants.SUMMARY);
        if (StringUtils.isNotBlank(summary))
        {
            //JRADEV-1972 CLONE - the space is ignored when reading form a properties file
            final String clonePrefix = applicationProperties.getDefaultBackedString(APKeys.JIRA_CLONE_PREFIX)+" ";
            getFieldValuesHolder().put(IssueFieldConstants.SUMMARY, clonePrefix + summary);
        }
    }

    public FieldScreenRenderLayoutItem getFieldScreenRenderLayoutItem(String fieldId)
    {
        return getFieldScreenRenderer().getFieldScreenRenderLayoutItem(fieldManager.getOrderableField(fieldId));
    }

    public Issue getIssueObject(GenericValue genericValue)
    {
        return issueFactory.getIssue(genericValue);
    }

    public MutableIssue getIssueObject()
    {
        if (issueObject == null)
        {
            issueObject = issueFactory.cloneIssue(getOriginalIssue());
        }

        return issueObject;
    }

    protected void doValidation()
    {
        try
        {
            //calling getIssue() here may cause exceptions
            setOriginalIssue(getIssueObject(getIssue()));
        }
        catch (IssuePermissionException ipe)
        {
            return;
        }
        catch (IssueNotFoundException infe)
        {
            return;
        }

        // Initialise issue
        setPid(getOriginalIssue().getProject().getLong("id"));
        getIssueObject().setProject(getProject());
        setIssuetype(getOriginalIssue().getIssueType().getString("id"));
        getIssueObject().setIssueType(getIssueTypeGV());

        // Validate summary
        SummarySystemField summaryField = (SummarySystemField) fieldManager.getOrderableField(IssueFieldConstants.SUMMARY);
        summaryField.populateFromParams(getFieldValuesHolder(), ActionContext.getContext().getParameters());
        summaryField.validateParams(this, this, this, getIssueObject(), getFieldScreenRenderLayoutItem(IssueFieldConstants.SUMMARY));
    }

    protected void setFields()
    {
        SummarySystemField summaryField = (SummarySystemField) fieldManager.getOrderableField(IssueFieldConstants.SUMMARY);
        FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(getIssue()).getFieldLayoutItem(summaryField);
        summaryField.updateIssue(fieldLayoutItem, getIssueObject(), getFieldValuesHolder());

        getIssueObject().setCreated(null);
        getIssueObject().setUpdated(null);
        getIssueObject().setKey(null);
        getIssueObject().setVotes(null);
        getIssueObject().setStatus(null);
        getIssueObject().setWorkflowId(null);
        // Ensure that the 'time spent' and 'remaining estimated' are not cloned - JRA-7165
        // We need to copy the value of 'original estimate' to the value 'remaining estimate' as they must be kept in synch
        // until work is logged on an issue.
        getIssueObject().setEstimate(getIssueObject().getOriginalEstimate());
        getIssueObject().setTimeSpent(null);
        //JRA-18731: Cloning a resolved issue will result in an open issue.  The resolution date should be reset.
        getIssueObject().setResolutionDate(null);

        // If the user does not have permission to modufy the reporter, initialise the reporter to be the remote user
        if (!isCanModifyReporter())
        {
            getIssueObject().setReporter(getRemoteUser());
        }

        //filter archived versions
        getIssueObject().setFixVersions(filterArchivedVersions(getIssueObject().getFixVersions()));
        getIssueObject().setAffectedVersions(filterArchivedVersions(getIssueObject().getAffectedVersions()));


        // Retrieve custom fields for the issue type and project of the clone issue (same as original issue)
        List customFields = getCustomFields(getOriginalIssue());

        for (Iterator iterator = customFields.iterator(); iterator.hasNext();)
        {
            CustomField customField = (CustomField) iterator.next();

            // Set the custom field value of the clone to the value set in the original issue
            Object value = customField.getValue(getOriginalIssue());
            if (value != null)
                getIssueObject().setCustomFieldValue(customField, value);
        }
    }

    private Collection filterArchivedVersions(Collection versions)
    {
        // Remove archived versions
        List tempVers = new ArrayList();
        for (Iterator versionsIt = versions.iterator(); versionsIt.hasNext();) {
            Version version = (Version)versionsIt.next();
            if(!version.isArchived())
            {
                tempVers.add(version);
            }
        }
        return tempVers;
    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        try
        {
            // Copy issue's field values
            setFields();

            // Create the clone issue (without attachments)
            super.createIssue();
            final Issue newIssue = getIssueObject(getIssue());
            // Record the mapping from old ID to new ID
            newIssueIdMap.put(getOriginalIssue().getId(), newIssue.getId());

            // Set custom fields of clone issue to values of original issue
//            setCustomFields(getOriginalIssue());

            // Create link between the cloned issue and the original - sequence on links does not matter.
            final IssueLinkType cloneIssueLinkType = getCloneIssueLinkType();
            if (cloneIssueLinkType != null)
            {
                issueLinkManager.createIssueLink(getOriginalIssue().getId(), getIssue().getLong("id"), cloneIssueLinkType.getLong("id"), null, getRemoteUser());
            }

            cloneIssueAttachments(getOriginalIssue(), newIssue);
            // JRA-17222 - We want to know all the issues being cloned, so we can choose to create links to the new
            // version of cloned issues.
            Set<Long> originalIssueIdSet = getOriginalIssueIdSet(getOriginalIssue());
            cloneIssueLinks(getOriginalIssue(), newIssue, originalIssueIdSet);

            // Cloning a subtask or an issue?
            if (originalIssue.isSubTask())
            {
                // Retrieve the parent of the original subtask
                Issue subTaskParent = originalIssue.getParentObject();

                // Link the clone subtask to the parent of the original subtask (by this stage the getIssue() method returns the newly cloned issue)
                subTaskManager.createSubTaskIssueLink(subTaskParent.getGenericValue(), getIssue(), getRemoteUser());
            }
            else
            {
                setCloneParent(getIssue());

                // Create clones of subtasks
                cloneSubTasks(getOriginalIssue(), getCloneParent(), originalIssueIdSet);
            }

            return doPostCreationTasks();
        }
        catch (Exception e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.exception")+" " + e);
            return ERROR;
        }
    }

    /**
     * Returns the set of original issues that are being cloned.
     * This will obviously always include the given "original issue", and may also include the subtasks of this issue.
     *
     * @param originalIssue The issue being cloned
     * @return Set of ID's of the issue being cloned, and its subtasks if they are being cloned as well.
     */
    private Set<Long> getOriginalIssueIdSet(final Issue originalIssue)
    {
        Set<Long> originalIssues = new HashSet<Long>();
        originalIssues.add(originalIssue.getId());
        // Add subtasks if required
        if (subTaskManager.isSubTasksEnabled() && isCloneSubTasks())
        {
            for (final Issue issue : originalIssue.getSubTaskObjects())
            {
                originalIssues.add(issue.getId());
            }
        }
        return originalIssues;
    }

    public boolean isDisplayCopyLink()
    {
        if (issueLinkManager.isLinkingEnabled())
        {
            // See if there are any links to clone
            if (hasCopyableLinks(getOriginalIssue()))
            {
                return true;
            }
            else
            {
                // See if there are any links to copy on sub-tasks
                if (isHasSubTasks())
                {
                    for (Iterator iterator = getOriginalIssue().getSubTaskObjects().iterator(); iterator.hasNext();)
                    {
                        Issue subTask = (Issue) iterator.next();
                        if (hasCopyableLinks(subTask))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void cloneIssueLinks(Issue originalIssue, Issue clone, Set<Long> originalIssueIdSet) throws CreateException
    {
        // Clone the links if the user has chosen to do so and linking is actually enabled.
        if (isCloneLinks() && issueLinkManager.isLinkingEnabled())
        {
            Collection<IssueLink> inwardLinks = issueLinkManager.getInwardLinks(originalIssue.getId());
            for (final IssueLink issueLink : inwardLinks)
            {
                if (copyLink(issueLink))
                {
                    // JRA-17222. Check if this link is from another Issue in the "clone set"
                    Long sourceIssueId = issueLink.getSourceId();
                    if (originalIssueIdSet.contains(sourceIssueId))
                    {
                        // We want to create a link to the new cloned version of that issue, not the original
                        // This can return null if that issue is not cloned yet, but that is OK, we will create the link as an outward link after we clone the second one.
                        sourceIssueId = newIssueIdMap.get(sourceIssueId);
                    }
                    if (sourceIssueId != null)
                    {
                        log.debug("Creating inward link to " + clone.getKey() + " (cloned from " + originalIssue.getKey() + ", link " + issueLink + ")");
                        issueLinkManager.createIssueLink(sourceIssueId, clone.getId(), issueLink.getIssueLinkType().getId(), null, getRemoteUser());
                    }
                }
            }

            Collection<IssueLink> outwardLinks = issueLinkManager.getOutwardLinks(originalIssue.getId());
            for (final IssueLink issueLink : outwardLinks)
            {
                if (copyLink(issueLink))
                {
                    // JRA-17222. Check if this link is to another Issue in the "clone set"
                    Long destinationId = issueLink.getDestinationId();
                    if (originalIssueIdSet.contains(destinationId))
                    {
                        // We want to create a link to the new cloned version of that issue, not the original
                        // This can return null if that issue is not cloned yet, but that is OK, we will create the link as an outward link after we clone the second one.
                        destinationId = newIssueIdMap.get(destinationId);
                    }
                    if (destinationId != null)
                    {
                        log.debug("Creating outward link from " + clone.getKey() + " (cloned from " + originalIssue.getKey() + ", link " + issueLink + ")");
                        issueLinkManager.createIssueLink(clone.getLong("id"), destinationId, issueLink.getIssueLinkType().getId(), null, getRemoteUser());
                    }
                }
            }
        }
    }

    private void cloneIssueAttachments(Issue originalIssue, Issue clone) throws CreateException
    {
        // Clone the attachments if the user has chosen to do so and attachments are enabled. Note, that Create Attachment
        // permission is not checked, the same way Link Issue permission is not checked for cloning links.
        if (isCloneAttachments() && attachmentManager.attachmentsEnabled())
        {
            final List<Attachment> attachments = attachmentManager.getAttachments(originalIssue);
            final String remoteUserName = getRemoteUser() == null ? null : getRemoteUser().getName();
            for (Attachment attachment : attachments)
            {
                File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
                if (attachmentFile.exists() && attachmentFile.canRead())
                {
                    try
                    {
                        attachmentManager.createAttachmentCopySourceFile(attachmentFile, attachment.getFilename(), attachment.getMimetype(), remoteUserName, clone, Collections.EMPTY_MAP, new Timestamp(System.currentTimeMillis()));
                    }
                    catch (AttachmentException e)
                    {
                        log.warn("Could not clone attachment with id '" + attachment.getId() + "' and file path '" + attachmentFile.getAbsolutePath() + "' for issue with id '" + clone.getId() + "' and key '" + clone.getKey() + "'.", e);
                    }
                }
                else
                {
                    log.warn("Could not clone attachment with id '" + attachment.getId() + "' and file path '" + attachmentFile.getAbsolutePath() + "' for issue with id '" + clone.getId() + "' and key '" + clone.getKey() + "', " +
                             "because the file path " + (attachmentFile.exists() ? "is not readable." : "does not exist."));
                }
            }
        }
    }

    public boolean isDisplayCopyAttachments()
    {
        if (attachmentManager.attachmentsEnabled())
        {
            if (!attachmentManager.getAttachments(getOriginalIssue()).isEmpty())
            {
                // If an issue has attachments then we should allow to clone them
                return true;
            }
            else if (subTaskManager.isSubTasksEnabled())
            {
                // Otherwise need to check if at least one sub-task has an attachment
                Collection<Issue> subTasks = getOriginalIssue().getSubTaskObjects();
                if (subTasks != null && !subTasks.isEmpty())
                {
                    for (Issue subTask : subTasks)
                    {
                        if (!attachmentManager.getAttachments(subTask).isEmpty())
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;

    }

    public boolean isDisplayCopySubTasks()
    {
        return subTaskManager.isSubTasksEnabled() && isHasSubTasks();
    }

    private boolean hasCopyableLinks(Issue issue)
    {
        Collection inwardLinks = issueLinkManager.getInwardLinks(issue.getId());
        for (Iterator iterator = inwardLinks.iterator(); iterator.hasNext();)
        {
            IssueLink issueLink = (IssueLink) iterator.next();
            if (copyLink(issueLink))
                return true;
        }

        Collection outwardLinks = issueLinkManager.getOutwardLinks(issue.getId());
        for (Iterator iterator = outwardLinks.iterator(); iterator.hasNext();)
        {
            IssueLink issueLink = (IssueLink) iterator.next();
            if (copyLink(issueLink))
                return true;
        }

        return false;
    }

    private boolean isHasSubTasks()
    {
        return !getOriginalIssue().getSubTaskObjects().isEmpty();
    }

    private boolean copyLink(IssueLink issueLink)
    {
        // Do not copy system links types and do not copy the cloners link type, as it is used to record the relationship between cloned issues
        // So if the cloners link type does not exists, or the link is not of cloners link type, and is not a system link, then copy it
        return !issueLink.isSystemLink() &&
               (getCloneIssueLinkType() == null || !getCloneIssueLinkType().getId().equals(issueLink.getIssueLinkType().getId()));
    }

    public boolean isCloneLinks()
    {
        return cloneLinks;
    }

    public void setCloneLinks(boolean cloneLinks)
    {
        this.cloneLinks = cloneLinks;
    }

    public boolean isCloneSubTasks()
    {
        return cloneSubTasks;
    }

    public void setCloneSubTasks(boolean cloneSubTasks)
    {
        this.cloneSubTasks = cloneSubTasks;
    }

    public boolean isCloneAttachments()
    {
        return cloneAttachments;
    }

    public void setCloneAttachments(final boolean cloneAttachments)
    {
        this.cloneAttachments = cloneAttachments;
    }

    protected String doPostCreationTasks() throws Exception
    {
        if (getCloneParent() != null)
            // If an issue has been cloned - return view to the newly created issue clone.
            return returnCompleteWithInlineRedirect("/browse/" + getCloneParent().getString("key"));
        else
            // If a subtask has been cloned or an issue with no sub tasks - return view to the newly created clone.
            return returnCompleteWithInlineRedirect("/browse/" + getIssue().getString("key"));
    }

    // Clone sub-tasks if subtasks are enabled and exist for the original issue
    private void cloneSubTasks(Issue originalIssue, GenericValue cloneParent, Set<Long> originalIssueIdSet) throws Exception
    {
        if (subTaskManager.isSubTasksEnabled() && isCloneSubTasks())
        {
            // Iterate over all subtask links, retrieve subtasks and copy details for clone subtask
            for (Iterator iterator = originalIssue.getSubTaskObjects().iterator(); iterator.hasNext();)
            {
                Issue subTaskIssue = (Issue) iterator.next();
                // Set OriginalIssue to the sub-task we are cloning
                setOriginalIssue(subTaskIssue);
                // Reset the issue object so we can populate the new subtask with appropriate values
                issueObject = null;
                // This needs to be here to trick the super action into making the subtask the current issue to create
                validationResult = null;
                // Set the details of the clone subtask
                setIssueDetails(subTaskIssue);
                // Populate the new issue with values
                setFields();
                // JRA-15949. Set the parent id to the NEW parent. Otherwise we get the wrong parentId in the IssueEvent.
                getIssueObject().setParentId(cloneParent.getLong("id"));

                // Create the new issue
                super.createIssue();
                final Issue newSubTask = getIssueObject(getIssue());
                // Record the mapping from old ID to new ID
                newIssueIdMap.put(getOriginalIssue().getId(), newSubTask.getId());
                // Clone Links if needed
                cloneIssueLinks(subTaskIssue, newSubTask, originalIssueIdSet);
                // Link the clone subtask to the clone parent issue.
                subTaskManager.createSubTaskIssueLink(cloneParent, getIssue(), getRemoteUser());
                // Clone attachments
                cloneIssueAttachments(subTaskIssue, newSubTask);
            }
        }
    }

    // ------ Getters & Setters & Helper Methods -----------------

    public Issue getOriginalIssue()
    {
        return originalIssue;
    }

    public void setOriginalIssue(Issue originalIssue)
    {
        this.originalIssue = originalIssue;
    }

    public GenericValue getCloneParent()
    {
        return cloneParent;
    }

    public void setCloneParent(GenericValue cloneParent)
    {
        this.cloneParent = cloneParent;
    }

    // Retrieve the issue link type specified by the clone link name in the properties file.
    // If the name is unset - issue linking of originals to clones is not required - returns null.
    // Otherwise, returns null if the issue link type with the specified name cannot be found in the system.
    public IssueLinkType getCloneIssueLinkType()
    {
        if (cloneIssueLinkType == null)
        {
            final Collection cloneIssueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByName(getCloneLinkTypeName());

            if (!TextUtils.stringSet(getCloneLinkTypeName()))
            {
                // Issue linking is not required
                cloneIssueLinkType = null;
            }
            else if (cloneIssueLinkTypes == null || cloneIssueLinkTypes.isEmpty())
            {
                log.warn("The clone link type '" + getCloneLinkTypeName() + "' does not exist. A link to the original issue will not be created.");
                cloneIssueLinkType = null;
            }
            else
            {
                for (Iterator iterator = cloneIssueLinkTypes.iterator(); iterator.hasNext();)
                {
                    IssueLinkType issueLinkType = (IssueLinkType) iterator.next();
                    if (issueLinkType.getName().equals(getCloneLinkTypeName()))
                        cloneIssueLinkType = issueLinkType;
                }
            }
        }

        return cloneIssueLinkType;
    }

    // Determines whether a warning should be displayed.
    // If the link type name is unset in the properties file - issue linking of originals to clones is not required - do not display warning.
    public boolean isDisplayCloneLinkWarning()
    {
        return (TextUtils.stringSet(getCloneLinkTypeName()) && getCloneIssueLinkType() == null);
    }

    // "Modify Reporter" permission required to create the clone with the original reporter set.
    public boolean isCanModifyReporter()
    {
        return permissionManager.hasPermission(Permissions.MODIFY_REPORTER, getIssue(), getRemoteUser());
    }

    public String getCloneLinkTypeName()
    {
        if (cloneIssueLinkTypeName == null)
            cloneIssueLinkTypeName = applicationProperties.getDefaultBackedString(APKeys.JIRA_CLONE_LINKTYPE_NAME);

        return cloneIssueLinkTypeName;
    }

    @Override
    public GenericValue getProject()
    {
        return getProjectManager().getProject(getIssue());
    }

    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>();
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
