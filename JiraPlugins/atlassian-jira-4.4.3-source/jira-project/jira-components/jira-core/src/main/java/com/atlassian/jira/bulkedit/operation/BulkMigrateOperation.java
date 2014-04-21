package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.MultiBulkMoveBean;
import com.atlassian.jira.workflow.WorkflowException;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Operation to Move issues from differring contexts to multiple target contexts.
 */
public class BulkMigrateOperation implements BulkOperation
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String OPERATION_NAME = "BulkMigrate";

    public static final String NAME_KEY = "bulk.move.operation.name";
    private static final String DESCRIPTION_KEY = "bulk.move.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.move.cannotperform";

    private static final Logger log = Logger.getLogger(BulkMigrateOperation.class);

    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final BulkMoveOperation bulkMoveOperation;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public BulkMigrateOperation(BulkMoveOperation bulkMoveOperation)
    {
        this.bulkMoveOperation = bulkMoveOperation;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public boolean canPerform(final BulkEditBean bulkEditBean, final User remoteUser)
    {
        return bulkMoveOperation.canPerform(bulkEditBean, remoteUser);
    }

    public boolean canPerform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser)
    {
        return bulkMoveOperation.canPerform(bulkEditBean, remoteUser);
    }

    public void perform(final BulkEditBean rootBulkEditBean, final User remoteUser) throws Exception
    {
        MultiBulkMoveBean multiBulkMoveBean = rootBulkEditBean.getRelatedMultiBulkMoveBean();
        for (Iterator iterator = multiBulkMoveBean.getBulkEditBeans().values().iterator(); iterator.hasNext();)
        {
            BulkEditBean bulkEditBean = (BulkEditBean) iterator.next();
            log.info("Performing move for project " + bulkEditBean.getTargetProjectGV().getString("name") + " issue type: " + bulkEditBean.getTargetIssueTypeGV().getString("name"));

            //This move changes the security level of the subtask, however the subtask move below, will overwrite this again
            // See  JRA-13937 - Bulk Move does not update the Security Level of subtasks for more details
            bulkMoveOperation.moveIssuesAndIndex(bulkEditBean, remoteUser);

            MultiBulkMoveBean relatedMultiBulkMoveBean = bulkEditBean.getRelatedMultiBulkMoveBean();
            if (relatedMultiBulkMoveBean != null && relatedMultiBulkMoveBean.getBulkEditBeans() != null)
            {
                for (Iterator iterator1 = relatedMultiBulkMoveBean.getBulkEditBeans().values().iterator(); iterator1.hasNext();)
                {
                    BulkEditBean subTaskBulkEditBean = (BulkEditBean) iterator1.next();
                    log.info("subTaskBulkEditBean move for project " + subTaskBulkEditBean.getTargetProjectGV().getString("name") + " issue type: " + subTaskBulkEditBean.getTargetIssueTypeGV().getString("name"));
                    bulkMoveOperation.moveIssuesAndIndex(subTaskBulkEditBean, remoteUser);
                }
            }
        }
    }

    public void perform(BulkEditBean rootBulkEditBean, com.opensymphony.user.User remoteUser) throws Exception
    {
        perform(rootBulkEditBean, (User) remoteUser);
    }

    public void chooseContext(BulkEditBean rootBulkEditBean, com.opensymphony.user.User remoteUser, I18nHelper i18nHelper, ErrorCollection errors)
    {
        // Loop through the child BulkEditBeans and do a chooseContext() on each.
        for (Iterator iterator = rootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans().values().iterator(); iterator.hasNext();)
        {
            BulkEditBean bulkEditBean = (BulkEditBean) iterator.next();
            bulkMoveOperation.chooseContext(bulkEditBean, remoteUser, i18nHelper, errors);
        }
    }

    public void chooseContextNoValidate(BulkEditBean rootBulkEditBean, com.opensymphony.user.User remoteUser)
    {
        bulkMoveOperation.chooseContextNoValidate(rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean(), remoteUser);
    }

    public boolean isStatusValid(BulkEditBean rootBulkEditBean)
    {
        return bulkMoveOperation.isStatusValid(rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean());
    }

    public void setStatusFields(BulkEditBean rootBulkEditBean) throws WorkflowException
    {
        bulkMoveOperation.setStatusFields(rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean());
    }

    public void validatePopulateFields(BulkEditBean rootBulkEditBean, I18nHelper i18nHelper, ErrorCollection errors)
    {
        bulkMoveOperation.validatePopulateFields(rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean(), errors, i18nHelper);
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public String getOperationName()
    {
        return OPERATION_NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }

    public BulkMoveOperation getBulkMoveOperation()
    {
        return bulkMoveOperation;
    }
}
