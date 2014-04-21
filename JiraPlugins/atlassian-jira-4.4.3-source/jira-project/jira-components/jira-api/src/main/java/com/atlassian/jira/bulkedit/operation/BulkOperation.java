/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.issue.operation.IssueOperation;

public interface BulkOperation extends IssueOperation
{
    /**
     * Determines whether the operation can be performed with the given
     * set of issues
     */
    public boolean canPerform(BulkEditBean bulkEditBean, User remoteUser);

    /**
     * Determines whether the operation can be performed with the given
     * set of issues.
     *
     * @deprecated Use {@link #canPerform(com.atlassian.jira.web.bean.BulkEditBean, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public boolean canPerform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser);

    /**
     * Performs the operation on the given set of issues
     *
     * @throws Exception
     */
    public void perform(BulkEditBean bulkEditBean, User remoteUser) throws Exception;

    /**
     * Performs the operation on the given set of issues
     *
     * @throws Exception
     *
     * @deprecated Use {@link #perform(com.atlassian.jira.web.bean.BulkEditBean, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public void perform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser) throws Exception;

    public String getOperationName();

    String getCannotPerformMessageKey();
}
