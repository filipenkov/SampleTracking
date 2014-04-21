package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.MultiBulkMoveBean;
import com.atlassian.jira.workflow.WorkflowManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BulkMigrate extends BulkMove
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private String sameAsBulkEditBean;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected final BulkMigrateOperation bulkMigrateOperation;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public BulkMigrate(SearchService searchService, BulkMoveOperation bulkMoveOperation, FieldManager fieldManager, WorkflowManager workflowManager, ConstantsManager constantsManager, IssueFactory issueFactory, BulkMigrateOperation bulkMigrateOperation, PermissionManager permissionManager)
    {
        super(searchService, bulkMoveOperation, fieldManager, workflowManager, constantsManager, issueFactory, permissionManager);
        this.bulkMigrateOperation = bulkMigrateOperation;
    }


    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDetails()
    {
        BulkEditBean rootBulkEditBean = getRootBulkEditBean();
        if (rootBulkEditBean == null)
        {
            return redirectToStart();
        }

        // Initialise
        rootBulkEditBean.setOperationName(getBulkMigrateOperation().getOperationName());
        rootBulkEditBean.resetMoveData();
        rootBulkEditBean.initMultiBulkBean();

        rootBulkEditBean.clearAvailablePreviousSteps();
        rootBulkEditBean.addAvailablePreviousStep(1);
        rootBulkEditBean.addAvailablePreviousStep(2);

        // Ensure that bulk notification can be disabled
        if (isCanDisableMailNotifications())
            rootBulkEditBean.setSendBulkNotification(false);
        else
            rootBulkEditBean.setSendBulkNotification(true);

        rootBulkEditBean.setCurrentStep(3);

        return SUCCESS;
    }

    public String doStart() throws Exception
    {
        return SUCCESS;
    }

    public String doChooseContext() throws Exception
    {
        BulkEditBean currentRootBulkEditBean = getCurrentRootBulkEditBean();
        if (currentRootBulkEditBean == null) return redirectToStart();

        // Resets all bulk edit beans
        MultiBulkMoveBean rootMultiBulkMoveBean = currentRootBulkEditBean.getRelatedMultiBulkMoveBean();
        Map bulkEditBeans = rootMultiBulkMoveBean.getBulkEditBeans();
        for (Iterator iterator = bulkEditBeans.values().iterator(); iterator.hasNext();)
        {
            BulkEditBean bulkEditBean = (BulkEditBean) iterator.next();
            bulkEditBean.resetMoveData();
        }

        // Validate & commit context
        getBulkMigrateOperation().chooseContext(currentRootBulkEditBean, getLoggedInUser(), this, this);

        if (invalidInput())
        {
            return INPUT;
        }

        // Re-organise the MultiBulkEditBean into being keyed by destination contexts
        rootMultiBulkMoveBean.remapBulkEditBeansByTargetContext();
        // Find fields to manually edit, fields to remove, and find subtasks that need to be moved with their parent.
        getBulkMigrateOperation().getBulkMoveOperation().finishChooseContext(rootMultiBulkMoveBean, getLoggedInUser());
        // Check if any subtasks need to be moved and initialise accordingly.
        boolean needsSubTaskChooseContext = false;
        for (Iterator iterator = rootMultiBulkMoveBean.getBulkEditBeans().values().iterator(); iterator.hasNext();)
        {
            BulkEditBean targetContextBean = (BulkEditBean) iterator.next();
            // Set up all the sub task beans
            if (targetContextBean.getSubTaskBulkEditBean() != null)
            {
                needsSubTaskChooseContext = true;
                targetContextBean.initMultiBulkBeanWithSubTasks();
            }
        }

        // Choose sub task status
        if (needsSubTaskChooseContext)
        {
            return getRedirect("BulkMigrateChooseSubTaskContext!default.jspa");
        }
        else
        {
            return getNextRedirect();
        }
    }

    public String doChooseSubTaskContext() throws Exception
    {
        BulkEditBean rootBulkEditBean = getRootBulkEditBean();
        if (rootBulkEditBean == null) return redirectToStart();

        // Resets all bulk edit beans
        for (Iterator iterator = rootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans().values().iterator(); iterator.hasNext();)
        {
            BulkEditBean currentRootBulkEditBean = (BulkEditBean) iterator.next();

            if (currentRootBulkEditBean.getRelatedMultiBulkMoveBean() != null)
            {
                Map subTaskBeans = currentRootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans();
                for (Iterator iterator1 = subTaskBeans.values().iterator(); iterator1.hasNext();)
                {
                    BulkEditBean subTaskBean = (BulkEditBean) iterator1.next();
                    subTaskBean.resetMoveData();
                }

                // Validate & commit context
                getBulkMigrateOperation().chooseContext(currentRootBulkEditBean, getLoggedInUser(), this, this);
            }
        }

        if (invalidInput())
        {
            return INPUT;
        }

        // Re-organise the MultiBulkEditBean into being keyed by destination contexts
        for (Iterator iterator = rootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans().values().iterator();
             iterator.hasNext();)
        {
            BulkEditBean currentRootBulkEditBean = (BulkEditBean) iterator.next();
            MultiBulkMoveBean multiBulkMoveBean = currentRootBulkEditBean.getRelatedMultiBulkMoveBean();
            if (multiBulkMoveBean != null)
            {
                // Re-organise the MultiBulkEditBean into being keyed by destination contexts
                multiBulkMoveBean.remapBulkEditBeansByTargetContext();
                // Find fields to manually edit, and fields to remove.
                getBulkMigrateOperation().getBulkMoveOperation().finishChooseContext(multiBulkMoveBean, getLoggedInUser());
            }
        }

        // Check if status change is required for any issues
        return getNextRedirect();
    }


    public String doChooseStatus() throws Exception
    {
        if (getBulkEditBean() == null) return redirectToStart();

        getBulkMigrateOperation().setStatusFields(getCurrentRootBulkEditBean());
        return getRedirect("BulkMigrateSetFields!default.jspa");
    }

    public String doSetFields() throws Exception
    {
        if (getBulkEditBean() == null) return redirectToStart();

        getBulkMigrateOperation().validatePopulateFields(getCurrentRootBulkEditBean(), this, this);

        if (invalidInput())
        {
            return ERROR;
        }

        // If there's another layer of sub-tasking
        if (getBulkEditBean().getRelatedMultiBulkMoveBean() != null)
        {
            setSubTaskPhase(true);
            return getNextRedirect();
        }

        // If there's another bulk edit bean to migrate
        if (!getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().isLastBulkEditBean())
        {
            getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().progressToNextBulkEditBean();
            return getNextRedirect();

        }
        else
        {
            // It's the end of the road
            if (isSubTaskPhase())
            {
                setSubTaskPhase(false);
            }

            // Do it again for the parent
            if (!getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().isLastBulkEditBean())
            {
                getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().progressToNextBulkEditBean();

                return getNextRedirect();
            }
            else
            {
                // Progress to the final level
                progressToLastStep();
                return "confirm";
            }
        }
    }

    // Verify and perform the move operation
    public String doPerform() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        // Ensure the user has the global BULK CHANGE permission
        if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getLoggedInUser()))
        {
            addErrorMessage(getText("bulk.change.no.permission", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            return ERROR;
        }

        // Ensure the user can perform the operation
        if (!getBulkMigrateOperation().canPerform(getRootBulkEditBean(), getLoggedInUser()))
        {
            addErrorMessage(getText("bulk.move.cannotperform"));
            return ERROR;
        }

        // If this is a Bulk Move, then check that the issues have not been already moved in the meantime:
        final String movedIssueKey = findFirstMovedIssueKey();
        if (movedIssueKey != null)
        {
            addErrorMessage(getText("bulk.move.error.issue.already.moved", movedIssueKey));
            return ERROR;
        }

        try
        {
            getBulkMigrateOperation().perform(getRootBulkEditBean(), getLoggedInUser());
        }
        catch (Exception e)
        {
            log.error("Error while performing Bulk Edit operation.", e);
            addErrorMessage(getText("bulk.edit.perform.error"));
            return ERROR;
        }

        return finishWizard();
    }

    /**
     * Looks at the issues we are about to move/migrate, and if any have been moved in the meantime, then
     * we will return the key of the fist issue which has been moved.
     *
     * @return issue key of the first issue that has been moved.
     *         or null if none have been moved.
     */
    private String findFirstMovedIssueKey()
    {
        //  Check whether the user has the move permission on all original selected issues
        List selectedIssues = getBulkEditBean().getSelectedIssues();
        for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext();)
        {
            Issue selectedIssue = (Issue) iterator.next();
            // Now get the latest value in the DB for this Issue.
            final MutableIssue latestIssue = getIssueManager().getIssueObject(selectedIssue.getId());
            if (!latestIssue.getKey().equals(selectedIssue.getKey()))
            {
                return selectedIssue.getKey();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public BulkEditBean getBulkEditBean()
    {
        if (getCurrentRootBulkEditBean() != null)
        {
            return getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().getCurrentBulkEditBean();
        }
        else
        {
            return null;
        }
    }

    public MultiBulkMoveBean getMultiBulkMoveBean()
    {
        return getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean();
    }

    public IssueContext getCurrentIssueContext()
    {
        return getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().getCurrentIssueContext();
    }

    public BulkEditBean getCurrentRootBulkEditBean()
    {
        BulkEditBean currentBulkEditBean = null;
        if (!isSubTaskPhase())
        {
            currentBulkEditBean = getRootBulkEditBean();
        }
        else
        {
            if (getRootBulkEditBean() != null)
            {
                currentBulkEditBean = getRootBulkEditBean().getRelatedMultiBulkMoveBean().getCurrentBulkEditBean();
            }
        }
        return currentBulkEditBean;
    }

    public String getOperationDetailsActionName()
    {
        return getBulkMigrateOperation().getOperationName() + "Details.jspa";
    }

    public String getprojectFieldName(BulkEditBean bulkEditBean)
    {
        return bulkEditBean.getKey() + "pid";
    }

    public String getSameAsBulkEditBean()
    {
        return sameAsBulkEditBean;
    }

    public void setSameAsBulkEditBean(String sameAsBulkEditBean)
    {
        this.sameAsBulkEditBean = sameAsBulkEditBean;
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    protected BulkMigrateOperation getBulkMigrateOperation()
    {
        return bulkMigrateOperation;
    }

    public String getRedirect(final String defaultUrl)
    {
        return super.getRedirect(defaultUrl + "?subTaskPhase=" + isSubTaskPhase());
    }

    private String getNextRedirect()
    {
        if (!getBulkMigrateOperation().isStatusValid(getCurrentRootBulkEditBean()))
        {
            return getRedirect("BulkMigrateChooseStatus!default.jspa");
        }
        else
        {
            return getRedirect("BulkMigrateSetFields!default.jspa");
        }
    }
}


