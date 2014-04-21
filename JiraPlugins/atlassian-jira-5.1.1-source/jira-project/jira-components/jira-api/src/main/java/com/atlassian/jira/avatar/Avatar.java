package com.atlassian.jira.avatar;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.NotNull;

/**
 * Represents an icon for a project or some other entity in JIRA.
 *
 * @since v4.0
 */
@PublicApi
public interface Avatar
{
    /**
     * The type of Avatar.
     *
     * @return a non null Avatar.Type.
     */
    @NotNull
    Avatar.Type getAvatarType();

    /**
     * The base filename to the avatar image file. The actual file name will be modified with the id etc.
     *
     * @return the non null file name.
     */
    @NotNull
    String getFileName();

    /**
     * The MIME type of the avatar image file.
     *
     * @return the non null file name.
     */
    @NotNull
    String getContentType();

    /**
     * The database identifier for the Avatar, may be null if it hasn't yet been stored.
     *
     * @return the database id or null.
     */
    Long getId();

    /**
     * A String representation of the identity of the domain object that this avatar is an avatar for!
     * For example, if it is a user avatar, it would be the username (since that is the primary key), for a Project
     * it is the project ID as a String. The meaning of this should be determined by the
     * {@link com.atlassian.jira.avatar.Avatar.Type}.
     *
     * @return the owner id must not be null.
     */
    @NotNull
    String getOwner();

    /**
     * Indicates whether the Avatar is a system-provided one or if users have defined it.
     *
     * @return true only if the Avatar is a system-provided one.
     */
    boolean isSystemAvatar();

    /**
     * An indicator of the owner type of the avatar. E.g. project, user, group, role etc.
     */
    public static enum Type
    {

        PROJECT("project"), // all we support today
        USER("user");

        private String name;

        private Type(String name)
        {
            this.name = name;
        }

        /**
         * The canonical String representation of the type.
         *
         * @return the name.
         */
        public String getName()
        {
            return name;
        }

        public static Type getByName(final String name)
        {
            if(name == null)
            {
                return null;
            }
            if(PROJECT.getName().equals(name))
            {
                return PROJECT;
            }
            else if(USER.getName().equals(name))
            {
                return USER;
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * The standard sizes for avatars.
     */
    public static enum Size
    {
        /**
         * A small avatar (16x16 pixels).
         */
        SMALL("small", false, 16),

        /**
         * A large avatar (48x48 pixels).
         */
        LARGE("large", true, 48);

        /**
         * The value to pass back to the server for the size parameter.
         */
        final String param;

        /**
         * The number of pixels.
         */
        final Integer pixels;

        /**
         * Whether this is the default size.
         */
        final boolean isDefault;

        Size(String param, boolean isDefault, int pixels)
        {
            this.param = param;
            this.isDefault = isDefault;
            this.pixels = pixels;
        }
    }
}
