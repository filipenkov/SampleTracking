/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkDeleteOperation;
import com.atlassian.jira.bulkedit.operation.BulkEditOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * This action is used to present the user with a list of allowed bulk operations
 * on the selected issues
 * User: keithb
 * Date: Dec 3, 2003
 * Time: 12:26:25 PM
 * To change this template use Options | File Templates.
 */
public class BulkChooseOperation extends AbstractBulkOperationAction
{
    private Collection<BulkOperation> bulkOperations;
    private String operation;
    final BulkOperationManager bulkOperationManager;

    public BulkChooseOperation(SearchService searchService, BulkOperationManager bulkOperationManager)
    {
        super(searchService);
        this.bulkOperationManager = bulkOperationManager;
        bulkOperations = bulkOperationManager.getBulkOperations();
    }

    public String doDefault() throws Exception
    {
        if(getBulkEditBean() == null)
        {
            return redirectToStart("bulk.chooseoperation.session.timeout.message");
        }
        getBulkEditBean().addAvailablePreviousStep(1);
        getBulkEditBean().setCurrentStep(2);
        return getResult();
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(getOperation()))
        {
            addErrorMessage(getText("bulk.chooseoperation.chooseoperation.error"));
        }
        else
        {
            // Check if the operation exists
            if (!bulkOperationManager.isValidOperation(getOperation()))
            {
                addErrorMessage(getText("bulk.chosseoperation.invalid.operation"));
            }

            // If a bulk move, make sure that more than just sub-tasks were selected
            // TODO: this doesn't seem to work and is apparently not correct anyway, as we can "move" subtask issuetypes.
            if (BulkMoveOperation.NAME.equals(getOperation()) && getBulkEditBean() != null && getBulkEditBean().isOnlyContainsSubTasks())
            {
                addErrorMessage(getText("admin.errors.issuebulkedit.cannot.move.sub.tasks"));
            }
        }

        super.doValidation();
    }

    protected String doExecute() throws Exception
    {
        BulkEditBean beb = getBulkEditBean();
        String operationName = bulkOperationManager.getOperation(this.getOperation()).getOperationName();
        if(beb != null)
        {
            if (operationName != null)
            {
                // TODO: when the user chooses "move", operationName = "BulkMigrate", so BulkEditBean.operationName = null.
                // TODO: Shouldn't this just be beb.setOperationName(operationName), otherwise actually deal with an unexpected value??
                if (operationName.equals(BulkMoveOperation.NAME))
                {
                    beb.setOperationName(BulkMoveOperation.NAME);
                }
                else if (operationName.equals(BulkWorkflowTransitionOperation.NAME))
                {
                    beb.setOperationName(BulkWorkflowTransitionOperation.NAME);
                }
                else if (operationName.equals(BulkEditOperation.NAME))
                {
                    beb.setOperationName(BulkEditOperation.NAME);
                }
                else if (operationName.equals(BulkDeleteOperation.NAME))
                {
                    beb.setOperationName(BulkDeleteOperation.NAME);
                }
            }
        }
        else
        {
            return redirectToStart("bulk.chooseoperation.session.timeout.message");
        }

        return getRedirect(operationName + "Details.jspa");
    }

    public Collection<BulkOperation> getBulkOperations()
    {
        return bulkOperations;
    }

    public boolean isCanPerform(BulkOperation bulkOperation) throws Exception
    {
        return bulkOperation.canPerform(getBulkEditBean(), getLoggedInUser());
    }

    public boolean isHasAvailableOperations() throws Exception
    {
        for (BulkOperation bulkOperation : bulkOperations)
        {
            if (isCanPerform(bulkOperation))
            {
                return true;
            }
        }
        return false;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }
}
