package com.atlassian.jira.webtest.framework.page.issue.move;

import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.page.FlowPage;

/**
 * Second page in the Move Sub-task flow (started when 'Move' operation is invoked on an issue). 
 *
 * @since v4.3
 */
public interface MoveSubTaskOperationDetails<P extends ParentPage> extends FlowPage<P, MoveSubTask3>, IssueAware
{

    // TODO this is a flow page, make it so when necessary to implement
}
