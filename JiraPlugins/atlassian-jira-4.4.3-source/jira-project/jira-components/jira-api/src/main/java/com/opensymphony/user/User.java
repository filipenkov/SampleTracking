package com.opensymphony.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.opensymphony.user.provider.CredentialsProvider;

import java.util.Collections;
import java.util.List;

/**
 * Entity to represent an actual User of the system.
 *
 * The user is backed by Crowd's Embedded Service.
 *
 * @deprecated since 4.3 - use {@link com.atlassian.crowd.embedded.api.User} instead
 */
@Deprecated
public class User extends Entity implements com.atlassian.crowd.embedded.api.User
{
    private final CrowdService crowdService;
    private final long directoryId;
    private final boolean active;
    private String emailAddress;
    private String displayName;

    public User(final com.atlassian.crowd.embedded.api.User crowdUser, CrowdService crowdService)
    {
        super(crowdUser.getName(), UserManager.getInstance().getAccessor());
        this.crowdService = crowdService;

        directoryId = crowdUser.getDirectoryId();
        active = crowdUser.isActive();
        emailAddress = crowdUser.getEmailAddress();
        displayName = crowdUser.getDisplayName();
    }

    /**
     * Constructor is only called by UserManager.
     *
     * If the user with the supplied name exists, then the user's
     * attributes 
     *
     * @param name username.
     * @param providerAccessor accessors.
     * @param crowdService service
     */
    public User(final String name, final ProviderAccessor providerAccessor, CrowdService crowdService)
    {
        super(name, providerAccessor);
        this.crowdService = crowdService;
        final com.atlassian.crowd.embedded.api.User crowdUser = crowdService.getUser(name);
        if (crowdUser != null)
        {
            this.name = crowdUser.getName();
            directoryId = crowdUser.getDirectoryId();
            active = crowdUser.isActive();
            emailAddress = crowdUser.getEmailAddress();
            displayName = crowdUser.getDisplayName();
        }
        else
        {
            // crowdUser=null should not happen in production (except by a race condition), however letting it happen is
            // consistent with old OSUser implementation and many Unit Tests will follow this path.
            // Make the user active
            active = true;
            directoryId = 0;
        }
    }

    /**
     * Convenience method to access property. This actually persists the new email address.
     *
     * @param email new email address.
     * @deprecated Use {@link com.atlassian.crowd.embedded.api.CrowdService#updateUser(com.atlassian.crowd.embedded.api.User)} instead. Since v4.4.
     */
    public void setEmail(final String email)
    {
        emailAddress = email;

        update();
    }

    /**
     * Convenience method to access property.
     *
     * @return email address.
     *
     * @deprecated Use {@link #getEmailAddress()} instead. Since v4.3.
     */
    public String getEmail()
    {
        return emailAddress;
    }

    /**
     * Convenience method to access property. This actually persists the new full name.
     *
     * @param fullName new display name.
     *
     * @deprecated Use {@link com.atlassian.crowd.embedded.api.CrowdService#updateUser(com.atlassian.crowd.embedded.api.User)} instead. Since v4.4.
     */
    public void setFullName(final String fullName)
    {
        displayName = fullName;
        update();
    }

    /**
     * Convenience method to access property.
     *
     * @return the full name.
     *
     * @deprecated Use {@link #getDisplayName()} instead. Since v4.3.
     */
    public String getFullName()
    {
        return displayName;
    }

    /**
     * List all Groups that User is a member of.
     * Modifiying this Collection will not add or remove Groups.
     *
     * @return list of groups the user belongs to.
     * @deprecated Use {@link com.atlassian.jira.security.groups.GroupManager#getGroupNamesForUser(String)} instead. Since v4.4.
     */
    public List<String> getGroups()
    {
        if (getAccessProvider() == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(getAccessProvider().listGroupsContainingUser(getName()));
    }

    /**
     * Change the User's password.
     *
     * @param password new password.
     * @throws ImmutableException should never be thrown as all users from this implementation are "mutable".
     * 
     * @deprecated Use {@link com.atlassian.crowd.embedded.api.CrowdService#updateUserCredential(com.atlassian.crowd.embedded.api.User, String)} instead. Since v4.4.
     */
    public void setPassword(final String password) throws ImmutableException
    {
        if (mutable && getCredentialsProvider().changePassword(name, password))
        {
            return;
        }

        throw new ImmutableException();
    }

    /**
     * Add this User as a member to supplied Group.
     * Returns whether any modifications were made.
     *
     * @param group group to add membership.
     * @return <code>true</code> if membership added successfully.
     * 
     * @deprecated Use {@link com.atlassian.crowd.embedded.api.CrowdService#addUserToGroup(com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.Group)} instead. Since v4.4.
     */
    public boolean addToGroup(final Group group)
    {
        if (group == null)
        {
            return false;
        }

        return group.getAccessProvider().addToGroup(getName(), group.getName());
    }

    /**
     * Verify that the supplied password matches the stored password for the user.
     *
     * @param password current password.
     * @return <code>true</code> if the authentication was successful.
     *
     * @deprecated Use {@link com.atlassian.crowd.embedded.api.CrowdService#authenticate(String, String)} instead. Since v4.4.
     */
    public boolean authenticate(final String password)
    {
        if (password == null)
        {
            return false;
        }

        return getCredentialsProvider().authenticate(name, password);
    }

    /**
     * Determine whether User is member of supplied Group.
     *
     * @param group group to check membership.
     * @return <code>true</code> if the user is a member of the group.
     *
     * @deprecated Use {@link com.atlassian.jira.security.groups.GroupManager#isUserInGroup(com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.Group)} instead. Since v4.4.
     */
    public boolean inGroup(final Group group)
    {
        if (group == null)
        {
            return false;
        }

        return group.getAccessProvider().inGroup(getName(), group.getName());
    }

    /**
     * Determine whether User is member of supplied Group.
     *
     * @param groupName name of group to check membership.
     * @return <code>true</code> if the user is a member of the group.
     *
     * @deprecated Use {@link com.atlassian.jira.security.groups.GroupManager#isUserInGroup(String, String)} instead. Since v4.4.
     */
    public boolean inGroup(final String groupName)
    {
        if (groupName == null)
        {
            return false;
        }

        try
        {
            return inGroup(getUserManager().getGroup(groupName));
        }
        catch (final EntityNotFoundException e)
        {
            return false;
        }
    }

    /**
     * Remove User from CredentialsProvider (providing it is mutable).
     *
     * Note that this also removes all custom attributes (profile) of the user too
     * by virtue of cascade on the Crowd Service.
     * 
     * @deprecated Use {@link com.atlassian.crowd.embedded.api.CrowdService#removeUser(com.atlassian.crowd.embedded.api.User)} instead. Since v4.4.
     */
    @Override
    public void remove() throws ImmutableException
    {
        final CredentialsProvider credentialsProvider = getCredentialsProvider();

        if (!mutable)
        {
            throw new ImmutableException("User is not mutable");
        }

        if (credentialsProvider == null)
        {
            throw new ImmutableException("No credentials provider for user");
        }

        if (!credentialsProvider.remove(name))
        {
            throw new ImmutableException("Credentials provider failed to remove user");
        }
    }

    /**
     * Remove this User as a member from supplied Group.
     * Returns whether any modifications were made.
     *
     * @param group group to remove membership.
     * @return <code>true</code> if user successfully removed from group.
     * 
     * @deprecated Use {@link com.atlassian.crowd.embedded.api.CrowdService#removeUserFromGroup(com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.Group)}} instead. Since v4.4.
     */
    public boolean removeFromGroup(final Group group)
    {
        if (group == null)
        {
            return false;
        }

        return group.getAccessProvider().removeFromGroup(getName(), group.getName());
    }

    /**
     * Force update to underlying data-stores.
     *
     * This allows providers that do not update persistent data on the fly to store changes.
     * If any of the providers are immutable and fields that cannot be updated have changed,
     * ImmutableException shall be thrown.
     *
     * @deprecated Use {@link CrowdService#updateUser(com.atlassian.crowd.embedded.api.User)} for mutating users. Since v4.4.
     */
    @Override
    public void store() throws ImmutableException
    {
        super.store();

        update();
    }

    /**
     * Persists the current user in the embedded Crowd back-end.
     */
    protected void update()
    {
        try
        {
            crowdService.updateUser(this);
        }
        catch (InvalidUserException e)
        {
            throw new RuntimeException(e);
        }
        catch (OperationNotPermittedException e)
        {
            throw new RuntimeException(e);
        }
    }

    // methods from the Crowd User interface

    public long getDirectoryId()
    {
        return directoryId;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public boolean equals(Object o)
    {
        // implement equals() that is compatible with other implementations of Crowd User
        return (o instanceof com.atlassian.crowd.embedded.api.User) && UserComparator.equal(this, (com.atlassian.crowd.embedded.api.User) o);
    }

    @Override
    public int hashCode()
    {
        // implement hashCode() that is compatible with other implementations of Crowd User
        return UserComparator.hashCode(this);
    }

    public int compareTo(final com.atlassian.crowd.embedded.api.User other)
    {
        // implement compareTo() that is compatible with other implementations of Crowd User
        return UserComparator.compareTo(this, other);
    }
}
