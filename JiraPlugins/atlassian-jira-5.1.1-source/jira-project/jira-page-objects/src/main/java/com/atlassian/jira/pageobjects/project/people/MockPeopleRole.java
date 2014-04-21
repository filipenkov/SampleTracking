package com.atlassian.jira.pageobjects.project.people;

import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A mock for a row on the Project Configuration people tab, useful for building expected test
 * outcomes
 *
 * @since v4.4
 */
public class MockPeopleRole implements PeopleRole
{
    private List<User> users;
    private List<Group> groups;
    private String name;

    public MockPeopleRole(final String name, final List<User> users, final List<Group> groups)
    {
        this.name = name;
        this.users = users;
        this.groups = groups;
    }

    public MockPeopleRole(final String name)
    {
        this.name = name;
        this.users = Lists.newArrayList();
        this.groups = Lists.newArrayList();
    }

    public MockPeopleRole addUser(final String name, final String avatarSrc)
    {
        users.add(new MockUser(name, avatarSrc));
        return this;
    }

    public MockPeopleRole addGroup(final String name, final boolean hasIcon)
    {
        groups.add(new MockGroup(name, hasIcon));
        return this;
    }

    @Override
    public List<User> getUsers()
    {
        return users;
    }

    @Override
    public List<Group> getGroups()
    {
        return groups;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    @Nonnull
    public EditPeopleRoleForm edit(final String dataField)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getName())
                .append(getUsers())
                .append(getGroups())
                .hashCode();
    }

    @Override
    public boolean equals(final Object other)
    {
        if(!(other instanceof PeopleRole))
        {
            return false;
        }
        final PeopleRole rhs = (PeopleRole) other;
        return new EqualsBuilder()
                .append(getName(), rhs.getName())
                .append(getUsers(), rhs.getUsers())
                .append(getGroups(), rhs.getGroups())
                .isEquals();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(getName())
                .append(getUsers())
                .append(getGroups())
                .toString();
    }

    public static class MockUser implements User
    {

        private String name;
        private String avatarSrc;

        public MockUser(final String name, final String avatarSrc)
        {
            this.name = name;
            this.avatarSrc = avatarSrc;
        }

        @Override
        public String getName()
        {
            return name;
        }

//        @Override
//        public String getAvatarSrc()
//        {
//            return avatarSrc;
//        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(getName())
//                    .append(getAvatarSrc())
                    .hashCode();
        }

        @Override
        public boolean equals(final Object other)
        {
            if(!(other instanceof User))
            {
                return false;
            }
            final User rhs = (User) other;
            return new EqualsBuilder()
                    .append(getName(), rhs.getName())
//                    .append(getAvatarSrc(), rhs.getAvatarSrc())
                    .isEquals();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append(getName())
//                    .append(getAvatarSrc())
                    .toString();
        }

    }

    public static class MockGroup implements Group
    {

        private boolean groupIcon;
        private String name;

        public MockGroup(final String name, final boolean groupIcon)
        {
            this.name = name;
            this.groupIcon = groupIcon;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean hasGroupIcon()
        {
            return groupIcon;
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(getName())
                    .append(hasGroupIcon())
                    .hashCode();
        }

        @Override
        public boolean equals(final Object other)
        {
            if(!(other instanceof Group))
            {
                return false;
            }
            final Group rhs = (Group) other;
            return new EqualsBuilder()
                    .append(getName(), rhs.getName())
                    .append(hasGroupIcon(), rhs.hasGroupIcon())
                    .isEquals();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append(getName())
                    .append(hasGroupIcon())
                    .toString();
        }

    }

}
