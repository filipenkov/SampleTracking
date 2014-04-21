package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.FileSize;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.config.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AttachmentSystemField extends AbstractOrderableField implements HideableField
{
    private static final Logger log = Logger.getLogger(AttachmentSystemField.class);

    public static final String FILETOCONVERT = "filetoconvert";

    private final AttachmentManager attachmentManager;
    private final PermissionManager permissionManager;
    private final TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator;

    public AttachmentSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, AttachmentManager attachmentManager,
            JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, final TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator)
    {
        super(IssueFieldConstants.ATTACHMENT, "issue.field.attachment", velocityManager, applicationProperties, authenticationContext, permissionManager, null);
        this.attachmentManager = attachmentManager;
        this.permissionManager = permissionManager;
        this.temporaryAttachmentsMonitorLocator = temporaryAttachmentsMonitorLocator;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        final Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        final Long maxSizeRaw = Long.valueOf(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        velocityParams.put("maxSizeRaw", maxSizeRaw);
        velocityParams.put("maxSize", FileSize.format(maxSizeRaw));

        final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = temporaryAttachmentsMonitorLocator.get(true);
        final Object checkedFiles = operationContext.getFieldValuesHolder().get(getId());
        if(checkedFiles != null)
        {
            velocityParams.put("checkedFiles", checkedFiles);
        }
        else
        {
            //if the checked files object is null (meaning this is the first time we've come here) make
            //sure there's no stale attachments lying around!
            temporaryAttachmentsMonitor.clearEntriesForIssue(issue.getId());
        }
        velocityParams.put("tempFiles", temporaryAttachmentsMonitor.getByIssueId(issue.getId()));
        return renderTemplate("attachment-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }

    public boolean isShown(Issue issue)
    {
        GenericValue entity;
        if (issue.getGenericValue() == null)
        {
            entity = issue.getProject();
        }
        else
        {
            entity = issue.getGenericValue();
        }
        boolean hasCreateAttachmentPermission = permissionManager.hasPermission(Permissions.CREATE_ATTACHMENT, entity, getAuthenticationContext().getUser(), !issue.isCreated());

        return hasCreateAttachmentPermission && attachmentManager.attachmentsEnabled();
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        // Do nothing
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        // Do nothing
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        List<Long> tempAttachmentIds = (List<Long>) fieldValuesHolder.get(getId());

        if (tempAttachmentIds == null || tempAttachmentIds.isEmpty())
        {
            //there is no attachments to add
            return;
        }
        for (Long tempAttachmentId : tempAttachmentIds)
        {
            final TemporaryAttachment temporaryAttachment = getTemporaryAttachment(tempAttachmentId);
            if(temporaryAttachment == null || !temporaryAttachment.getFile().exists())
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("attachment.temporary.id.session.time.out"));
            }
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void createValue(Issue issue, Object value)
    {
        addAttachment(issue, (List<Long>) value);
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        issueChangeHolder.addChangeItems(addAttachment(issue, (List<Long>) modifiedValue.getNewValue()));
    }

    private List<ChangeItemBean> addAttachment(Issue issue, List<Long> tempAttachmentIds)
    {
        if (tempAttachmentIds == null || tempAttachmentIds.isEmpty())
        {
            //there is no temporary attchments - maybe we are from Jelly
            return Collections.emptyList();
        }
        try
        {
            final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = temporaryAttachmentsMonitorLocator.get(false);
            if(temporaryAttachmentsMonitor != null)
            {
                return attachmentManager.convertTemporaryAttachments(getAuthenticationContext().getLoggedInUser(), issue, tempAttachmentIds, temporaryAttachmentsMonitor);
            }
            else
            {
                log.error("Session timed out or cleared since validation happened.  No temporary attachments could be converted to attachments!");
            }
        }
        catch (AttachmentException e)
        {
            //if we get an error - there is not much we can do - we've already created the issue from the workflow.
            log.error("Error occurred while creating attachment.", e);
        }
        catch (GenericEntityException e)
        {
            //if we get an error - there is not much we can do - we've already created the issue from the workflow.
            log.error("Error occurred while creating attachment.", e);
        }

        return Collections.emptyList();
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            issue.setExternalFieldValue(getId(), fieldValueHolder.get(getId()));
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        // Attachments are handled separately
        return new MessagedResult(false);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // SHould never be called as needsMove() returns false
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        // Do nothing - remove can only be done through a separate means.
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return false;
    }

    public boolean hasValue(Issue issue)
    {
        return false;
    }

    public Object getValueFromParams(Map params) throws FieldValidationException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public List<IssueSearcher<?>> getAssociatedSearchers()
    {
        return Collections.emptyList();
    }

    protected Object getRelevantParams(Map params)
    {
        Collection<Long> temporaryAttachmentIds = new ArrayList<Long>();
        if(params.containsKey(FILETOCONVERT))
        {
            final String[] idStrings = (String[]) params.get(FILETOCONVERT);
            for (String idString : idStrings)
            {
                temporaryAttachmentIds.add(Long.parseLong(idString));
            }
        }

        return temporaryAttachmentIds;
    }

    private TemporaryAttachment getTemporaryAttachment(final Long temporaryAttachmentId)
    {
        final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = temporaryAttachmentsMonitorLocator.get(false);
        if(temporaryAttachmentsMonitor != null)
        {
            return temporaryAttachmentsMonitor.getById(temporaryAttachmentId);
        }
        return null;
    }
}
