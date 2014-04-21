package com.atlassian.jira.event;

import com.atlassian.jira.JiraManager;

import java.util.Map;

/**
 * Responsible for maintaining a event listeners.  These are generally configured in the database,
 * and implementing Listener classes must implement the {@link JiraListener} interface.
 */
public interface ListenerManager extends JiraManager
{
    /**
     * Returns a map of listeners. The map contains mappings from the listener name to listener class.
     *
     * @return A map with name -> class mappings.
     */
    Map<String, JiraListener> getListeners();

    /**
     * Reloads the map of listeners from the db.
     */
    void refresh();
}
