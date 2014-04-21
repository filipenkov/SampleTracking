package com.atlassian.jira.event.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.util.ImportUtils;
import com.google.common.collect.Maps;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link com.atlassian.jira.event.issue.IssueEventManager}.
 *
 * @since v4.4
 */
public class DefaultIssueEventManager implements IssueEventManager
{
    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;

    public DefaultIssueEventManager(ApplicationProperties applicationProperties, EventPublisher eventPublisher)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
        this.eventPublisher = checkNotNull(eventPublisher);
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, boolean sendMail)
    {
        dispatchEvent(eventTypeId, issue, Collections.<String, Object>emptyMap(), remoteUser, sendMail);
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, Map<String,Object> params, User remoteUser, boolean sendMail)
    {
        Map<String, Object> copyOfParams = params != null ? Maps.newHashMap(params) : Maps.<String, Object>newHashMap();
        fillInDefaultParams(copyOfParams);

        publishEvent(new IssueEvent(issue, copyOfParams, remoteUser, eventTypeId, sendMail));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, Map params, User remoteUser)
    {
        publishEvent(new IssueEvent(issue, params, remoteUser, eventTypeId));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        fillInDefaultParams(copyOfParams);
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, boolean sendMail)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        fillInDefaultParams(copyOfParams);
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        fillInDefaultParams(copyOfParams);
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        fillInDefaultParams(copyOfParams);
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, GenericValue changelog, boolean sendMail, boolean subtasksUpdated)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();;
        fillInDefaultParams(copyOfParams);
        publishEvent(new IssueEvent(issue, remoteUser, null, null, changelog, copyOfParams, eventTypeId, sendMail, subtasksUpdated));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        fillInDefaultParams(copyOfParams);
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail, subtasksUpdated));
    }

    private static Map<String, Object> copyParams(final Map params)
    {
        Map<String,Object> copyOfParams = new HashMap<String,Object>();
        if (params != null)
        {
            copyOfParams.putAll(params);
        }
        return copyOfParams;
    }

    protected void fillInDefaultParams(Map<String, Object> params)
    {
        params.put(IssueEvent.BASE_URL_PARAM_NAME, applicationProperties.getString(APKeys.JIRA_BASEURL));
    }

    protected void publishEvent(IssueEvent event)
    {
        if (ImportUtils.isEnableNotifications())
        {
            eventPublisher.publish(event);
        }
    }


}
