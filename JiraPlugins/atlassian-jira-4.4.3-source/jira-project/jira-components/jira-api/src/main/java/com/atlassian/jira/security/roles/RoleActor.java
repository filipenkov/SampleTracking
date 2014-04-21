package com.atlassian.jira.security.roles;

import com.opensymphony.user.User;

import java.util.Set;

/**
 * This is an abstraction that allows us to associate users with ProjectRoles.
 * <p>
 * Please note: implementations <strong>must be immutable</strong> as caching presumes this.
 */
public interface RoleActor
{
    /**
     * Returns the id for the Role Actor
     * @return the id for the Role Actor
     */
    Long getId();

    /**
     * This will get the ProjectRole that this RoleActor belongs to.
     * @return the project role that this instance belongs to.
     */
    Long getProjectRoleId();

    /**
     * Returns a pretty viewable representation of the contents of the RoleActor
     * (e.g. if a UserRoleActor, then their full name, James Brown, if a
     * GroupRoleActor, then the name of the group, Business Analysts).
     * NOTE that the returned value is used in {@link RoleActorComparator} for sorting.
     * @return pretty descriptor
     */
    String getDescriptor();

    /**
     * Returns a string that identifies the implementation type. This allows us to group common types.
     * @return implementation type
     */
    String getType();

    /**
     * Returns the string that identifies the target of this role actor (ex. if you are a group role actor, then
     * this will be the unique identifier of the group, the group name).
     * @return the target of the role actor
     */
    String getParameter();

    /**
     * Will provide a Set of users encapsulated by this RoleActor.
     * <p>
     * <em>Warning</em> This method will be changed to return a {@link com.atlassian.crowd.embedded.api.User} in a future release.
     *
     * @return a Set of users encapsulated by this RoleActor.
     */
    Set<com.opensymphony.user.User> getUsers();

    /**
     * Does this RoleActor contain the specified user.
     *
     * @param user
     *          the user to check if they are contained
     * @return true if the user is matched by this RoleActor
     * @deprecated This method will be changed to take a {@link com.atlassian.crowd.embedded.api.User}.
     */
    boolean contains(User user);
}