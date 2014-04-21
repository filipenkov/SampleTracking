/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.util;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.event.issue.IssueEventDispatcher;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

public class DefaultIssueUpdater implements IssueUpdater
{
    private final DelegatorInterface genericDelegator;

    public DefaultIssueUpdater(DelegatorInterface genericDelegator)
    {
        this.genericDelegator = genericDelegator;
    }

    /**
     * Stores any changes to the issue optionally including a changelog and conditionally dispatches an IssueUpdate
     * event if the changes were real and made to significant fields.
     *
     * @param iub the description of the change.
     * @param generateChangeItems if true, a changelog group is created.
     * @throws JiraException if there's a problem storing the issue.
     */
    public void doUpdate(IssueUpdateBean iub, boolean generateChangeItems) throws JiraException
    {
        final GenericValue changedIssue = iub.getChangedIssue();
        changedIssue.set("updated", UtilDateTime.nowTimestamp());

        final Transaction txn = Txn.begin();

        GenericValue changeGroup;
        try
        {
            genericDelegator.storeAll(ImmutableList.of(changedIssue));

            changeGroup = ChangeLogUtils.createChangeGroup(iub.getUser(), iub.getOriginalIssue(), changedIssue,
                    iub.getChangeItems(), generateChangeItems);

            txn.commit();

            //only fire events if something has changed, comment on its own counts as special case
            if (changeGroup != null || iub.getComment() != null)
            {
                if (iub.isDispatchEvent())
                {
                    IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
                    IssueEventDispatcher.dispatchEvent(iub.getEventTypeId(), issueFactory.getIssue(changedIssue),
                            iub.getUser(), iub.getComment(), iub.getWorklog(), changeGroup, iub.getParams(),
                            iub.isSendMail(), iub.isSubtasksUpdated());
                }
            }
        }
        catch (GenericEntityException e)
        {
            throw new JiraException(e);
        }
        catch (Exception e)
        {
            throw new JiraException(e);
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();
        }
    }
}