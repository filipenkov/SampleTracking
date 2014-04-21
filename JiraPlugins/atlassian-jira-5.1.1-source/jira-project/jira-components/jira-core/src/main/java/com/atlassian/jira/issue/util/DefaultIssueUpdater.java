/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.util;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.util.ArrayList;
import java.util.List;

public class DefaultIssueUpdater implements IssueUpdater
{
    private final OfBizDelegator ofBizDelegator;
    private final IssueEventManager issueEventManager;

    public DefaultIssueUpdater(OfBizDelegator ofBizDelegator, IssueEventManager issueEventManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.issueEventManager = issueEventManager;
    }

    public void doUpdate(IssueUpdateBean iub, boolean generateChangeItems)
    {
        final GenericValue changedIssue = iub.getChangedIssue();
        changedIssue.set(IssueFieldConstants.UPDATED, UtilDateTime.nowTimestamp());

        // We want to store the fields that have changed, not the whole issue. Unfortunately, IssueUpdateBean.getChangeItems() can be incomplete,
        // so we need to compare the before and after issues to compute the list of modified fields.
        List<ChangeItemBean> modifiedFields = new ArrayList<ChangeItemBean>();
        modifiedFields.addAll(ChangeLogUtils.generateChangeItems(iub.getOriginalIssue(), changedIssue));
        if (iub.getChangeItems() != null)
        {
            modifiedFields.addAll(iub.getChangeItems());
        }

        // Now construct an empty GenericValue and populate it with only the fields that have been modified
        final GenericValue updateIssueGV = new GenericValue(changedIssue.getDelegator(), changedIssue.getModelEntity());
        updateIssueGV.setPKFields(changedIssue.getPrimaryKey().getAllFields());
        for(ChangeItemBean modifiedField: modifiedFields)
        {
            String fieldName = modifiedField.getField();
            if (IssueFieldConstants.ISSUE_TYPE.equals(fieldName)) {
                // issuetype is the only field whose name inside the GenericValue is different (type in this case).
                // I haven't found a generic way to map external field names to GV internal names, hence this ugly if...
                fieldName = "type";
            }
            if (updateIssueGV.getModelEntity().isField(fieldName))
            {
                updateIssueGV.put(fieldName, changedIssue.get(fieldName));
            }
        }
        // Also add the "update" field
        updateIssueGV.put(IssueFieldConstants.UPDATED, changedIssue.get(IssueFieldConstants.UPDATED));

        final Transaction txn = Txn.begin();

        GenericValue changeGroup;
        try
        {
            ofBizDelegator.storeAll(ImmutableList.of(updateIssueGV));

            changeGroup = ChangeLogUtils.createChangeGroup(iub.getUser(), iub.getOriginalIssue(), changedIssue,
                    iub.getChangeItems(), generateChangeItems);

            txn.commit();

            //only fire events if something has changed, comment on its own counts as special case
            if (changeGroup != null || iub.getComment() != null)
            {
                if (iub.isDispatchEvent())
                {
                    // Get the full newly updated issue from the database to make sure indexing is always accurate. (Status could be different)
                    GenericValue updatedIssue = ofBizDelegator.findByPrimaryKey("Issue", updateIssueGV.getLong("id"));
                    IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
                    issueEventManager.dispatchEvent(iub.getEventTypeId(), issueFactory.getIssue(updatedIssue),
                            iub.getUser(), iub.getComment(), iub.getWorklog(), changeGroup, iub.getParams(),
                            iub.isSendMail(), iub.isSubtasksUpdated());
                }
            }
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();
        }
    }
}