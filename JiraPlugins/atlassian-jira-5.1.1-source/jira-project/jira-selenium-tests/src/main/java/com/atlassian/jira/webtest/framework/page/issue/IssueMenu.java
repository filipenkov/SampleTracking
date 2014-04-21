package com.atlassian.jira.webtest.framework.page.issue;

import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.page.PageSection;

/**
 * Issue menu at the top of the View Issue page. Also now as the 'Stalker Bar'.
 *
 * @since v4.3
 */
public interface IssueMenu extends PageSection<ViewIssue>
{

    /**
     * Invoke given <tt>issueOperation</tt> by clicking in this menu.
     *
     * @param issueOperation issue operation to invoke
     * @return view issue parent page
     */
    ViewIssue invoke(IssueOperation issueOperation);
}
