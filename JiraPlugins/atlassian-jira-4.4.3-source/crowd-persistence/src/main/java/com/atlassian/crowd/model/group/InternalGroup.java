package com.atlassian.crowd.model.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.InternalDirectoryEntity;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.util.InternalEntityUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * Encapsulates the concept of group.
 */
public class InternalGroup extends InternalDirectoryEntity implements InternalDirectoryGroup
{
    private String lowerName;
    private GroupType type;
    private String description;
    private boolean isLocal;

    protected InternalGroup()
    {

    }

    // this constructor is used by the importer
    public InternalGroup(final InternalEntityTemplate internalEntityTemplate, final Directory directory, final GroupTemplate groupTemplate)
    {
        super(internalEntityTemplate, directory);

        this.type = groupTemplate.getType();

        updateDetailsFrom(groupTemplate);
    }

    // constructor for new groups
    public InternalGroup(final Group group, final Directory directory)
    {
        super();

        Validate.notNull(directory, "directory argument cannot be null");

        setName(group.getName());
        this.directory = directory;
        this.type = group.getType();

        updateDetailsFrom(group);
    }

    // MUTATOR
    private void validateGroup(final Group group)
    {
        Validate.notNull(group, "group argument cannot be null");
        Validate.notNull(group.getDirectoryId(), "group argument cannot have a null directoryID");
        Validate.notNull(group.getName(), "group argument cannot have a null name");
        Validate.notNull(group.getType(), "type argument cannot be null");

        Validate.isTrue(group.getDirectoryId() == this.getDirectoryId(), "directoryID of updated group does not match the directoryID of the existing group.");
        Validate.isTrue(group.getName().equals(this.getName()), "group name of updated group does not match the group name of the existing group.");
    }

    // MUTATOR
    public void updateDetailsFrom(final Group group)
    {
        validateGroup(group);

        this.active = group.isActive();
        this.description = InternalEntityUtils.truncateValue(group.getDescription());
        // group type is not updated
    }

    // MUTATOR
    public void renameTo(String newName)
    {
        Validate.isTrue(StringUtils.isNotBlank(newName), "the new name cannot be null or blank");

        setName(newName);
    }

    @Override
    protected void setName(final String name)
    {
        InternalEntityUtils.validateLength(name);
        this.name = name;
        this.lowerName = toLowerCase(name);
    }

    public String getDescription()
    {
        return description;
    }

    public GroupType getType()
    {
        return type;
    }

    public String getLowerName()
    {
        return lowerName;
    }

    private void setLowerName(final String lowerName)
    {
        this.lowerName = lowerName;
    }

    private void setDescription(final String description)
    {
        this.description = description;
    }

    private void setType(final GroupType type)
    {
        this.type = type;
    }

    public boolean isLocal()
    {
        return isLocal;
    }

    public void setLocal(final boolean local)
    {
        isLocal = local;
    }

    @Override
    public boolean equals(final Object o)
    {
        return GroupComparator.equalsObject(this, o);
    }

    @Override
    public int hashCode()
    {
        return GroupComparator.hashCode(this);
    }

    public int compareTo(Group o)
    {
        return GroupComparator.compareTo(this, o);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("name", getName()).
                append("type", getType()).
                append("active", isActive()).
                append("description", getDescription()).
                append("lowerName", getLowerName()).
                append("createdDate", getCreatedDate()).
                append("updatedDate", getUpdatedDate()).
                append("directoryId", getDirectoryId()).
                toString();
    }
}

