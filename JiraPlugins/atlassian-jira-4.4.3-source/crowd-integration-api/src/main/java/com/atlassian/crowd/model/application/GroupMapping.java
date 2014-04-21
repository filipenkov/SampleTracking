package com.atlassian.crowd.model.application;

import com.atlassian.crowd.embedded.api.Directory;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;

import java.util.Comparator;

public class GroupMapping
{
    private Long id;
    private DirectoryMapping directoryMapping;
    private String groupName;

    // redundant
    private Application application;
    private Directory directory;

    protected GroupMapping()
    {
    }

    /**
     * Constructs a new GroupMapping with the specified group mapping ID. Used by XML import.
     *
     * @param id group mapping ID
     * @param directoryMapping mapping of a directory to an application
     * @param groupName name of group to map
     */
    public GroupMapping(final Long id, final DirectoryMapping directoryMapping, final String groupName)
    {
        this(directoryMapping, groupName);
        this.id = id;
    }

    /**
     * Constructs a new GroupMapping.
     * 
     * @param directoryMapping mapping of a directory to an application
     * @param groupName name of group to map
     */
    public GroupMapping(final DirectoryMapping directoryMapping, final String groupName)
    {
        this.directoryMapping = directoryMapping;
        this.groupName = groupName;
        this.application = directoryMapping.getApplication();
        this.directory = directoryMapping.getDirectory();
    }

    /**
     * Returns the group mapping ID.
     *
     * @return the group mapping ID
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Sets the group mapping ID.
     *
     * @param id group mapping ID
     */
    public void setId(final Long id)
    {
        this.id = id;
    }

    /**
     * Returns the application of this mapping.
     *
     * @return application of the mapping
     */
    public Application getApplication()
    {
        return application;
    }

    /**
     * Sets the application of this mapping.
     *
     * @param application application
     */
    public void setApplication(final Application application)
    {
        this.application = application;
    }

    /**
     * Returns the directory of the group.
     *
     * @return directory of the group
     */
    public Directory getDirectory()
    {
        return directory;
    }

    /**
     * Sets the directory of this group.
     *
     * @param directory directory of the group
     */
    public void setDirectory(final Directory directory)
    {
        this.directory = directory;
    }

    /**
     * Returns the name of the group being mapped.
     *
     * @return name of the group being mapped
     */
    public String getGroupName()
    {
        return groupName;
    }

    /**
     * Sets the name of the group being mapped.
     *
     * @param groupName name of the group being mapped
     */
    public void setGroupName(final String groupName)
    {
        this.groupName = groupName;
    }

    /**
     * Returns the directory mapping.
     *
     * @return directory mapping
     */
    public DirectoryMapping getDirectoryMapping()
    {
        return directoryMapping;
    }

    /**
     * Sets the directory mapping.
     *
     * @param directoryMapping directory mapping
     */
    public void setDirectoryMapping(final DirectoryMapping directoryMapping)
    {
        this.directoryMapping = directoryMapping;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GroupMapping))
        {
            return false;
        }

        GroupMapping that = (GroupMapping) o;

        if (getDirectoryMapping().getId() != null ? !getDirectoryMapping().getId().equals(that.getDirectoryMapping().getId()) : that.getDirectoryMapping().getId() != null)
        {
            return false;
        }
        if (getGroupName() != null ? !getGroupName().equals(that.getGroupName()) : that.getGroupName() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getDirectoryMapping().getId() != null ? getDirectoryMapping().getId().hashCode() : 0;
        result = 31 * result + (getGroupName() != null ? getGroupName().hashCode() : 0);
        return result;
    }

    public static final class COMPARATOR implements Comparator<GroupMapping>
    {
        public int compare(final GroupMapping firstMapping, final GroupMapping secondMapping)
        {
            return compareToInLowerCase(firstMapping.getGroupName(), (secondMapping.getGroupName()));
        }
    }
}
