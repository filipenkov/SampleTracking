package com.atlassian.crowd.model.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a mapping between an application and a directory. Only directories with a mapping to an application are
 * visible to the application.
 */
public class DirectoryMapping implements Serializable
{
    private Long id;
    private Application application;
    private Directory directory;
    private boolean allowAllToAuthenticate;

    private Set<GroupMapping> authorisedGroups = new HashSet<GroupMapping>();
    private Set<OperationType> allowedOperations = new HashSet<OperationType>();

    protected DirectoryMapping()
    {
    }

    /**
     * Constructs a new DirectoryMapping. Used by XML import.
     *
     * @param id ID of the directory mapping
     * @param application Application to map
     * @param directory Directory to map
     * @param allowAllToAuthenticate set to <tt>true</tt> if all users in <code>directory</code> are allowed to authenticate
     */
    public DirectoryMapping(final Long id, final Application application, final Directory directory, final boolean allowAllToAuthenticate)
    {
        this(application, directory, allowAllToAuthenticate);
        this.id = id;
    }

    /**
     * Constructs a new DirectoryMapping.
     *
     * @param application application to map
     * @param directory directory to map
     * @param allowAllToAuthenticate set to <tt>true</tt> if all users in <code>directory</code> are allowed to authenticate
     */
    public DirectoryMapping(final Application application, final Directory directory, final boolean allowAllToAuthenticate)
    {
        this.application = application;
        this.directory = directory;
        this.allowAllToAuthenticate = allowAllToAuthenticate;
    }

    /**
     * Constructs a new DirectoryMapping.
     *
     * @param application application to map
     * @param directory directory to map
     * @param allowAllToAuthenticate set to <tt>true</tt> if all users in <code>directory</code> are allowed to authenticate
     * @param allowedOperations the set of operations the application is allowed to perform on the directory
     */
    public DirectoryMapping(final Application application, final Directory directory, final boolean allowAllToAuthenticate, final Set<OperationType> allowedOperations)
    {
        this(application, directory, allowAllToAuthenticate);
        this.allowedOperations.addAll(allowedOperations);
    }

    /**
     * Returns the ID of the directory mapping.
     *
     * @return
     */
    public Long getId()
    {
        return id;
    }

    private void setId(final Long id)
    {
        this.id = id;
    }

    /**
     * Returns the mapped application.
     *
     * @return mapped application
     */
    public Application getApplication()
    {
        return application;
    }

    private void setApplication(final Application application)
    {
        this.application = application;
    }

    /**
     * Returns the mapped directory.
     *
     * @return mapped directory
     */
    public Directory getDirectory()
    {
        return directory;
    }

    private void setDirectory(final Directory directory)
    {
        this.directory = directory;
    }

    /**
     * Returns <tt>true</tt> if all the users in the directory are allowed to authenticate with the application. If the
     * value is false, then the user is required to be in an authorised group.
     *
     * @return <tt>true</tt> if all the users in the directory are allowed to authenticate with the application
     * @see {@link #getAuthorisedGroups()}
     */
    public boolean isAllowAllToAuthenticate()
    {
        return allowAllToAuthenticate;
    }

    /**
     * Sets whether all the users in the directory are allowed to authenticate with the application.
     *
     * @param allowAllToAuthenticate set to <tt>true</tt> if all the users in the directory are allowed to authenticate
     *                                  with the application.
     */
    public void setAllowAllToAuthenticate(final boolean allowAllToAuthenticate)
    {
        this.allowAllToAuthenticate = allowAllToAuthenticate;
    }

    /**
     * Returns a set of mappings to groups that are authorised to authenticate with the application. This set is not
     * used if {@link #isAllowAllToAuthenticate()} returns <tt>true</tt>.
     *
     * @return set of mappings to groups that are authorised to authenticate with the application
     */
    public Set<GroupMapping> getAuthorisedGroups()
    {
        return authorisedGroups;
    }

    private void setAuthorisedGroups(final Set<GroupMapping> authorisedGroups)
    {
        this.authorisedGroups = authorisedGroups;
    }

    /**
     * Returns <tt>true</tt> if the group is an authorised group. i.e. it is allowed
     *
     * @param groupName name of group
     * @return <tt>true</tt> if the group is an authorised group.
     */
    public boolean isAuthorised(String groupName)
    {
        for (GroupMapping mapping : getAuthorisedGroups())
        {
            if (mapping.getGroupName().equals(groupName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a mapping to a new authorised group.
     *
     * @param groupName name of new authorised group
     */
    public void addGroupMapping(String groupName)
    {
        authorisedGroups.add(new GroupMapping(this, groupName));
    }

    /**
     * Unauthorises a group.
     *
     * @param groupName name of group to unauthorise
     */
    public void removeGroupMapping(String groupName)
    {
        authorisedGroups.remove(new GroupMapping(this, groupName));
    }

    /**
     * Adds a list of operations that the application is allowed to perform on the directory.
     *
     * @param operationTypes list of operations that the application is allowed to perform on the directory
     */
    public void addAllowedOperations(OperationType... operationTypes)
    {
        allowedOperations.addAll(Sets.newHashSet(operationTypes));
    }

    /**
     * Add a new operation that the application is allowed to perform on the directory.
     *
     * @param operationType new operation that the application is allowed to perform on the directory
     */
    public void addAllowedOperation(OperationType operationType)
    {
        allowedOperations.add(operationType);
    }

    /**
     * Returns a set of operations that the application is allowed to perform on the directory.
     *
     * @return set of operations that the application is allowed to perform on the directory
     */
    public Set<OperationType> getAllowedOperations()
    {
        return allowedOperations;
    }

    /**
     * Sets (i.e. replaces) the list of operations that the application is allowed to perform on the directory.
     *
     * @param allowedOperations set of operations that the application is allowed to perform on the directory
     */
    public void setAllowedOperations(final Set<OperationType> allowedOperations)
    {
        this.allowedOperations = allowedOperations;
    }

    /**
     * Returns <tt>true</tt> if the application is allowed to perform the specified operation on the directory.
     *
     * @param operation operation to check
     * @return <tt>true</tt> if the application is allowed to perform the specified operation on the directory
     */
    public boolean isAllowed(OperationType operation)
    {
        return allowedOperations.contains(operation);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DirectoryMapping))
        {
            return false;
        }

        DirectoryMapping that = (DirectoryMapping) o;

        if (getApplication() != null && getApplication().getId() != null ? !getApplication().getId().equals(that.getApplication().getId()) : that.getApplication() != null && that.getApplication().getId() != null)
        {
            return false;
        }
        if (getDirectory() != null ? !getDirectory().getId().equals(that.getDirectory().getId()) : that.getDirectory() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getApplication() != null && getApplication().getId() != null ? getApplication().getId().hashCode() : 0;
        result = 31 * result + (getDirectory() != null ? getDirectory().getId().hashCode() : 0);
        return result;
    }
}
