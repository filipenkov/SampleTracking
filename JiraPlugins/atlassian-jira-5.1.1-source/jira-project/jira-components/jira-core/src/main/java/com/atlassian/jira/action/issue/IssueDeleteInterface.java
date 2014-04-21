package com.atlassian.jira.action.issue;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericValue;

/**
 * @deprecated Use {@link com.atlassian.jira.bc.issue.IssueService#delete(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.issue.IssueService.DeleteValidationResult)} instead. Since v4.4.
 */
public interface IssueDeleteInterface
{
    String execute() throws Exception;

    void setIssue(GenericValue issue);

    void setRemoteUser(User user);
}
