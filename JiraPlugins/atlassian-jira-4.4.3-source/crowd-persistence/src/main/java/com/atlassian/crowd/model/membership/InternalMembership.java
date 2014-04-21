package com.atlassian.crowd.model.membership;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.model.user.InternalUser;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * Encapsulates the concept of membership.
 */
public class InternalMembership implements Serializable
{
    private Long id;

    private Long parentId;
    private Long childId;
    private MembershipType membershipType;

    // redundant fields for optimisation
    private String parentName;
    private String lowerParentName;
    private String childName;
    private String lowerChildName;
    private GroupType groupType;
    private Directory directory;

    protected InternalMembership()
    {

    }

    /**
     * This constructor is only used for XML imports.
     *
     * @param id
     * @param parentId
     * @param childId
     * @param membershipType
     * @param groupType
     * @param parentName
     * @param childName
     * @param directory
     */
    public InternalMembership(final Long id, final Long parentId, final Long childId,
                              final MembershipType membershipType, GroupType groupType,
                              final String parentName, final String childName, final DirectoryImpl directory)
    {
        this.id = id;
        this.parentId = parentId;
        this.childId = childId;
        this.membershipType = membershipType;
        this.groupType = groupType;
        setParentName(parentName);
        setChildName(childName);
        this.directory = directory;
    }

    public InternalMembership(InternalGroup group, InternalUser user)
    {
        Validate.notNull(group, "group argument cannot be null");
        Validate.notNull(user, "user argument cannot be null");
        Validate.isTrue(group.getDirectoryId() == user.getDirectoryId(), "directoryIDs of the user and group do not match");

        this.parentId = group.getId();
        this.childId = user.getId();
        this.membershipType = MembershipType.GROUP_USER;
        this.groupType = group.getType();
        setParentName(group.getName());
        setChildName(user.getName());
        this.directory = group.getDirectory();
    }

    public InternalMembership(InternalGroup parentGroup, InternalGroup childGroup)
    {
        Validate.notNull(parentGroup, "parentGroup argument cannot be null");
        Validate.notNull(childGroup, "childGroup argument cannot be null");
        Validate.isTrue(parentGroup.getDirectoryId() == childGroup.getDirectoryId(), "directoryIDs of the parent and child group do not match");
        Validate.isTrue(parentGroup.getType().equals(childGroup.getType()), "groupTypes of the parent and child group do not match");

        this.parentId = parentGroup.getId();
        this.childId = childGroup.getId();
        this.membershipType = MembershipType.GROUP_GROUP;
        this.groupType = parentGroup.getType();
        setParentName(parentGroup.getName());
        setChildName(childGroup.getName());
        this.directory = parentGroup.getDirectory();
    }

    public Long getId()
    {
        return id;
    }

    private void setId(final Long id)
    {
        this.id = id;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public Long getChildId()
    {
        return childId;
    }

    public MembershipType getMembershipType()
    {
        return membershipType;
    }

    public String getParentName()
    {
        return parentName;
    }

    public String getChildName()
    {
        return childName;
    }

    public Directory getDirectory()
    {
        return directory;
    }

    public GroupType getGroupType()
    {
        return groupType;
    }

    public String getLowerParentName()
    {
        return lowerParentName;
    }

    public String getLowerChildName()
    {
        return lowerChildName;
    }

    private void setParentId(final Long parentId)
    {
        this.parentId = parentId;
    }

    private void setChildId(final Long childId)
    {
        this.childId = childId;
    }

    private void setMembershipType(final MembershipType membershipType)
    {
        this.membershipType = membershipType;
    }

    private void setParentName(final String parentName)
    {
        this.parentName = parentName;
        this.lowerParentName = toLowerCase(parentName);
    }

    private void setChildName(final String childName)
    {
        this.childName = childName;
        this.lowerChildName = toLowerCase(childName);
    }

    private void setDirectory(final DirectoryImpl directory)
    {
        this.directory = directory;
    }

    private void setGroupType(final GroupType groupType)
    {
        this.groupType = groupType;
    }

    private void setLowerParentName(final String lowerParentName)
    {
        this.lowerParentName = lowerParentName;
    }

    private void setLowerChildName(final String lowerChildName)
    {
        this.lowerChildName = lowerChildName;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof InternalMembership))
        {
            return false;
        }

        InternalMembership that = (InternalMembership) o;

        if (getChildId() != null ? !getChildId().equals(that.getChildId()) : that.getChildId() != null)
        {
            return false;
        }
        if (getParentId() != null ? !getParentId().equals(that.getParentId()) : that.getParentId() != null)
        {
            return false;
        }
        if (getMembershipType() != that.getMembershipType())
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getParentId() != null ? getParentId().hashCode() : 0;
        result = 31 * result + (getChildId() != null ? getChildId().hashCode() : 0);
        result = 31 * result + (getMembershipType() != null ? getMembershipType().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("parentId", getParentId()).
                append("childId", getChildId()).
                append("membershipType", getMembershipType()).
                append("groupType", getGroupType()).
                append("parentName", getParentName()).
                append("lowerParentName", getLowerParentName()).
                append("childName", getChildName()).
                append("lowerChildName", getLowerChildName()).
                append("directoryId", getDirectory().getId()).
                toString();
    }
}
