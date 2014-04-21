/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.issue.operation.IssueOperation;

@PublicApi
public interface BulkOperation extends IssueOperation
{
    /**
     * Determines whether the operation can be performed with the given
     * set of issues
     */
    public boolean canPerform(BulkEditBean bulkEditBean, User remoteUser);

    /**
     * Performs the operation on the given set of issues
     *
     * @throws Exception
     */
    public void perform(BulkEditBean bulkEditBean, User remoteUser) throws Exception;

    public String getOperationName();

    String getCannotPerformMessageKey();
}
