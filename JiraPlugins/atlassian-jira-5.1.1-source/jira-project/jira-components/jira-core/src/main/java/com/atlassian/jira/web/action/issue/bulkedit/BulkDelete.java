/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkDeleteOperation;
import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.bean.BulkEditBean;

public class BulkDelete extends AbstractBulkOperationDetailsAction
{
    private BulkOperation bulkDeleteOperation;
    final BulkOperationManager bulkOperationManager;
    private final PermissionManager permissionManager;

    public BulkDelete(SearchService searchService, BulkOperationManager bulkOperationManager, PermissionManager permissionManager)
    {
        super(searchService);
        this.bulkOperationManager = bulkOperationManager;
        this.permissionManager = permissionManager;
        bulkDeleteOperation = bulkOperationManager.getOperation(BulkDeleteOperation.NAME_KEY);
    }

    public boolean isHasAvailableActions() throws Exception
    {
        return getBulkDeleteOperation().canPerform(getBulkEditBean(), getLoggedInUser());
    }

    public String getOperationDetailsActionName()
    {
        return getBulkDeleteOperation().getOperationName() + "Details.jspa";
    }

    public void doPerformValidation()
    {
        try
        {
            // Ensure the user has the global BULK CHANGE permission
            if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getLoggedInUser()))
            {
                addErrorMessage(getText("bulk.change.no.permission", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }

            // Ensure the bulk delete operation can be performed
            if (!getBulkDeleteOperation().canPerform(getBulkEditBean(), getLoggedInUser()))
            {
                addErrorMessage(getText("bulk.delete.cannotperform.error", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }
        }
        catch (Exception e)
        {
            log.error("Error occured while testing operation.", e);
            addErrorMessage(getText("bulk.canperform.error"));
        }
    }

    public String doDetails() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart("bulk.delete.session.timeout.message");
        }

        // If user can disbale notifications for bulk operation - display screen with this option
        // Otherwise, proceed to confirmation screen.
        if (isCanDisableMailNotifications() && getBulkEditBean().isHasMailServer())
        {
            // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
            // clicking the "back" button of the browser (or something like that)

            BulkEditBean bulkEditBean = getBulkEditBean();
            bulkEditBean.clearAvailablePreviousSteps();
            bulkEditBean.addAvailablePreviousStep(1);
            bulkEditBean.addAvailablePreviousStep(2);

            // Ensure that bulk notification can be disabled
            if (isCanDisableMailNotifications())
                bulkEditBean.setSendBulkNotification(false);
            else
                bulkEditBean.setSendBulkNotification(true);

            bulkEditBean.setCurrentStep(3);
            return INPUT;
        }
        else
        {
            return getRedirect("BulkDeleteDetailsValidation.jspa");
        }
    }

    public String doDetailsValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)

        // Note: if the user is accessing JIRA from a URL not identical to baseURL, the redirect will cause them to lose their session,
        // and getBulkEditBean() will return null here (JT)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart("bulk.delete.session.timeout.message");
        }

        BulkEditBean bulkEditBean = getBulkEditBean();
        bulkEditBean.clearAvailablePreviousSteps();
        bulkEditBean.addAvailablePreviousStep(1);
        bulkEditBean.addAvailablePreviousStep(2);
        bulkEditBean.addAvailablePreviousStep(3);
        bulkEditBean.setCurrentStep(4);
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doPerform() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart("bulk.delete.session.timeout.message");
        }

        // Validate input
        doPerformValidation();
        if (invalidInput())
        {
            return ERROR;
        }

        try
        {
            getBulkDeleteOperation().perform(getBulkEditBean(), getLoggedInUser());
        }
        catch (Exception e)
        {
            log.error("Error while performing BulkDeleteOperation", e);
            addErrorMessage(getText("bulk.delete.perform.error"));
            return ERROR;
        }

        return finishWizard();
    }

    public BulkOperation getBulkDeleteOperation()
    {
        return bulkDeleteOperation;
    }
}
