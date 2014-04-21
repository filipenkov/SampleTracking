/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.MultiBulkMoveBean;
import com.atlassian.jira.workflow.WorkflowException;

/**
 * Operation for moving <strong>parent</strong> issues and their sub-tasks from one or many contexts to a single target
 * context.
 */
public interface BulkMoveOperation extends BulkOperation
{
    static final String NAME_KEY = "bulk.move.operation.name";
    static final String NAME = "BulkMove";

    boolean isStatusValid(BulkEditBean bulkEditBean);

    public void moveIssuesAndIndex(BulkEditBean bulkEditBean, User remoteUser) throws Exception;

    public void chooseContext(BulkEditBean bulkEditBean, User remoteUser, I18nHelper i18nHelper, ErrorCollection errors);

    public void chooseContextNoValidate(BulkEditBean bulkEditBean, User remoteUser);

    public void setStatusFields(BulkEditBean bulkEditBean) throws WorkflowException;

    public void validatePopulateFields(final BulkEditBean bulkEditBean, ErrorCollection errors, I18nHelper i18nHelper);

    /**
     * Does operations on the given BulkEditBean to finalise the "Choose Project and Issue Type" step of the Bulk Move.
     * <p>
     * This method should be called AFTER validation of the new context, and re-mapping the BulkEditBeans to be indexed
     * by Target context.
     * <ul>
     *   <li>Sets the target field layout (aka "Field Configuration") according to target Project and Issue Type.</li>
     *   <li>Decides which Issue Fields need to be edited by the user.</li>
     *   <li>Will delete values for fields that should be removed (ie not used in new context).</li>
     *   <li>Sets the SubTaskBulkEditBean if any of these issues have subtasks which will also need to be moved.</li>
     * </ul>
     * </p>
     *
     * @param bulkEditBean BulkEditBean containing Bulk Move information for a single target "Issue Context".
     * @param remoteUser User doing the operation.
     * @see #finishChooseContext(com.atlassian.jira.web.bean.MultiBulkMoveBean, User)
     */
    public void finishChooseContext(BulkEditBean bulkEditBean, User remoteUser);

    /**
     * Does the finishChooseContext() operation for all the BulkEditBeans in the given MultiBulkMoveBean.
     *
     * @param multiBulkMoveBean MultiBulkMoveBean (Contains Collection of BulkEditBean objects).
     * @param remoteUser User
     * @see #finishChooseContext(com.atlassian.jira.web.bean.BulkEditBean, User)
     */
    public void finishChooseContext(MultiBulkMoveBean multiBulkMoveBean, User remoteUser);

}