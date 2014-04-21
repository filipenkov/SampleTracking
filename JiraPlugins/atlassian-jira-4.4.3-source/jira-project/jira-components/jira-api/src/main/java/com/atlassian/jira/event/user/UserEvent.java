/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.OSUserConverter;

import java.util.Map;

/**
 * A UserEvent. The user is the user that the event is occurring on. The initiating user is the person who triggered the
 * event.
 */
public class UserEvent extends AbstractEvent
{
    private com.opensymphony.user.User user;
    private final int eventType;
    private com.opensymphony.user.User initiatingUser;

    /**
     * @param user The user this event refers to
     */
    public UserEvent(User user, int eventType)
    {
        super();
        this.user = OSUserConverter.convertToOSUser(user);
        this.eventType = eventType;
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
        this.initiatingUser = authenticationContext.getUser();
    }

    /**
     * @param params Parameters retrieved by the Listener
     * @param user   The user this event refers to
     */
    public UserEvent(Map params, User user, int eventType)
    {
        super(params);
        this.user = OSUserConverter.convertToOSUser(user);
        this.eventType = eventType;
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
        this.initiatingUser = authenticationContext.getUser();
    }

    /**
     * Returns the user that the event is occurring on.
     * <p/>
     * <strong>Warning:<strong> this method will be changed to return {@link com.atlassian.crowd.embedded.api.User} in
     * the future.
     *
     * @return the user that the event is occurring on.
     */
    public com.opensymphony.user.User getUser()
    {
        return user;
    }

    /**
     * Returns the user who triggered the event.
     * <p/>
     * <strong>Warning:<strong> this method will be changed to return {@link com.atlassian.crowd.embedded.api.User} in
     * the future.
     *
     * @return the user who triggered the event.
     */
    public com.opensymphony.user.User getInitiatingUser()
    {
        return initiatingUser;
    }

    public int getEventType()
    {
        return eventType;
    }
}
