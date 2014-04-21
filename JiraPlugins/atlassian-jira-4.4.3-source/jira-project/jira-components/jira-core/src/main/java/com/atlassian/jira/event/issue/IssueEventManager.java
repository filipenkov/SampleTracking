package com.atlassian.jira.event.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import java.util.Map;

/**
 * Component responsible for dispatching issue events.
 *
 * @since v4.4
 */
public interface IssueEventManager
{

    /**
     * Dispatch event of given type, configuring whether or not a mail notification should be sent (useful e.g. for bull
     * edit).
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     */
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, boolean sendMail);

    /**
     * Dispatch event of given type with custom parameters.
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param params custom event parameters
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     */
    void dispatchEvent(Long eventTypeId, Issue issue, Map<String,Object> params, User remoteUser, boolean sendMail);
}
