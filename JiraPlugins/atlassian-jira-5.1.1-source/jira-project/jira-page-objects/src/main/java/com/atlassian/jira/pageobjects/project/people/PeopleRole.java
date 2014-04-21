package com.atlassian.jira.pageobjects.project.people;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A role on the project configuration people tab. Interface to facilitate easy
 * testing using assertEquals
 *
 * @since v4.4
 */
public interface PeopleRole
{
    @Nonnull
    List<User> getUsers();

    @Nonnull
    List<Group> getGroups();

    @Nonnull
    String getName();

    @Nonnull EditPeopleRoleForm edit(final String dataField);

    public interface User
    {
        String getName();

//        String getAvatarSrc();
    }

    public interface Group
    {
        String getName();

        boolean hasGroupIcon();
    }
}
