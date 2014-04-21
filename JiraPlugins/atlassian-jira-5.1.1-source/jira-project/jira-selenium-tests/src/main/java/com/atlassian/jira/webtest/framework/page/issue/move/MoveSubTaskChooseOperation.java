package com.atlassian.jira.webtest.framework.page.issue.move;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.page.FlowPage;

/**
 * First page in the Move Sub-task flow (started when 'Move' operation is invoked on an issue). Contains a selection of
 * the flow type: changing issue type, or changing parent. 
 *
 * @since v4.3
 */
public interface MoveSubTaskChooseOperation<P extends ParentPage> extends FlowPage<P, MoveSubTaskOperationDetails<P>>, IssueAware
{

    static enum FlowType
    {
        CHANGE_TYPE,
        CHANGE_PARENT
    }

    /**
     * Check whether given <tt>flowType</tt> is selectable in the current context. Sometimes a particular flow type
     * might not be possible to carry out, e.g. {@link MoveSubTaskChooseOperation.FlowType#CHANGE_TYPE}
     * in the situation where no custom sub-task types are defined in JIRA.
     *
     * @param flowType flow type to verify
     * @return timed condition querying whether <tt>flowType</tt> may be selected on this page
     */
    TimedCondition isSelectable(FlowType flowType);

    /**
     * Select flow type for the current flow.
     *
     * @param flowType flow type to select
     * @return this page instance
     * @throws IllegalStateException if the flow cannot be selected, which may be queried by
     * {@link #isSelectable(FlowType)}
     */
    MoveSubTaskChooseOperation<P> selectFlowType(FlowType flowType);
}
