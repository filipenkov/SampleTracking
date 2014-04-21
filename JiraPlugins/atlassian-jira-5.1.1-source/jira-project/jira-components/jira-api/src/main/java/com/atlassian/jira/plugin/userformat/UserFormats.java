package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;

/**
 * Builds {@link UserFormat user formats} for a specific user format type. User Formats are used to ouput user
 * information from JIRA.
 *
 * @see UserFormatModuleDescriptor
 * @see UserFormat
 *
 * @since v4.4
 */
public interface UserFormats
{
    /**
     * Builds a user format for the specified type.
     *
     * @param type the type of user format to build.
     * @return A user format for the specified type or null if no user format could be found for the specfied type.
     */
    UserFormat forType(String type);
}
