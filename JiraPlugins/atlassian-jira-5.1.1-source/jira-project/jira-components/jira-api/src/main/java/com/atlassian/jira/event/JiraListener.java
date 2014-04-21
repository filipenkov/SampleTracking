package com.atlassian.jira.event;

import java.util.EventListener;
import java.util.Map;

/**
 * The basic interface that all Listeners must implement.
 * <p/>
 * All Listeners must also have an empty constructor to that the ListenerFactory can create it.
 *
 * @see com.atlassian.jira.event.issue.IssueEventListener
 * @see com.atlassian.jira.event.user.UserEventListener
 */
public interface JiraListener extends EventListener
{
    /**
     * Initialise the listener.
     * <p/>
     * For custom listeners, the list of parameters is always empty.
     *
     * @param params initialisation parameters
     */
    public void init(Map params);

    /**
     * Get a list of the parameters for this listener.
     *
     * @return list of the parameters for this listener.
     */
    public String[] getAcceptedParams();

    /**
     * Indicates whether administrators can delete this listener from within the web interface.
     * <p/>
     * Basically only Atlassian listeners should return true from this.
     *
     * @return true if this is an Internal Listener.
     */
    public boolean isInternal();

    /**
     * Whether this listener class should be unique.  Some listeners are fine to have multiples, and some are not.
     * <p/>
     * Having multiple mail listeners could be fine - if you wanted multiple mails sent out.
     * <p/>
     * With other listeners, such as the cache listeners, it makes no sense to have multiple listeners of the one
     * class.
     *
     * @return Whether this listener class should be unique.
     */
    public boolean isUnique();

    /**
     * A textual description of the listener.  You can include HTML if required, but do not use tables, or DHTML, as the
     * description may be displayed inside tables / frames.
     * <p/>
     * A good description will describe what this listener does, and then explains the parameters required for
     * configuring the listener.
     * <p/>
     * If no description is appropriate, return null.
     *
     * @return A HTML description of the listener
     */
    public String getDescription();
}
