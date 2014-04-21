/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.movesubtask.operation;

import com.atlassian.jira.web.bean.MoveIssueBean;
import com.opensymphony.user.User;

public interface MoveSubTaskOperation
{
    /**
     * Determines whether the operation can be performed with the given issue
     */
    public boolean canPerform(MoveIssueBean moveIssueBean, User remoteUser);

    public String getNameKey();

    public String getDescriptionKey();

    public String getOperationName();

    /**
     * Gets the i18n key for why the operation can't be displayed
     *
     * @param moveIssueBean bean containing information about the move
     * @return the i18n key stating the reason why this operation can't be performed on the issue
     */
    String getCannotPerformMessageKey(MoveIssueBean moveIssueBean);
}
