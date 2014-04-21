package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;

import java.util.Collection;

/**
 * This provides access to {@link com.atlassian.jira.portal.Portlet} objects
 *
 * @since v3.13
 */
public interface PortletAccessManager
{
    /**
     * Called to return a {@link com.atlassian.jira.portal.Portlet} that can be seen by the User and which
     * has the key of portletKey
     *
     * @param user       the user context in which to return the Portlet
     * @param portletKey the key of the Portlet
     * @return a Portlet if the user can see it or null if they dont have permission or the portletKey is invalid
     */
    Portlet getPortlet(User user, String portletKey);

    /**
     * Called to return a {@link com.atlassian.jira.portal.Portlet} that can be seen by the User and which
     * has the key of portletKey
     *
     * @param user       the user context in which to return the Portlet
     * @param portletKey the key of the Portlet
     * @return a Portlet if the user can see it or null if they dont have permission or the portletKey is invalid
     */
    Portlet getPortlet(com.opensymphony.user.User user, String portletKey);

    /**
     * This returns a Portlet based on portletKey
     *
     * @param portletKey the key of the Portlet
     * @return a Portlet or null if one can be found with the specified key
     */
    Portlet getPortlet(final String portletKey);

    /**
     * Return all the portlets available on the system.
     * 
     * @return a collection of all the portlets available on the system.
     */
    Collection<Portlet> getAllPortlets();

    /**
     * This returns all {@link Portlet}'s that a user can see
     *
     * @param user the User in play
     * @return a non null Collection of Portlet objects
     */
    Collection<Portlet> getVisiblePortlets(User user);

    /**
     * This returns all {@link Portlet}'s that a user can see
     *
     * @param user the User in play
     * @return a non null Collection of Portlet objects
     */
    Collection<Portlet> getVisiblePortlets(com.opensymphony.user.User user);

    /**
     * This returns true if the specified user is allowed to see the Portlet represented by the given portletKey
     *
     * @param user       the user to test.
     * @param portletKey the key of the Portlet
     * @return true if the user has permission to view the Portlet
     */
    boolean canUserSeePortlet(User user, String portletKey);

    /**
     * This returns true if the specified user is allowed to see the Portlet represented by the given portletKey
     *
     * @param user       the user to test.
     * @param portletKey the key of the Portlet
     * @return true if the user has permission to view the Portlet
     */
    boolean canUserSeePortlet(com.opensymphony.user.User user, String portletKey);

    /**
     * This returns true if the specified user is allowed to see the passed Portlet
     *
     * @param user       the user to test.
     * @param portlet the portlet the check
     * @return true if the user has permission to view the Portlet
     */
    boolean canUserSeePortlet(User user, Portlet portlet);

    /**
     * This returns true if the specified user is allowed to see the passed Portlet
     *
     * @param user       the user to test.
     * @param portlet the portlet the check
     * @return true if the user has permission to view the Portlet
     */
    boolean canUserSeePortlet(com.opensymphony.user.User user, Portlet portlet);
}
