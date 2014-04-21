package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Manager interface for {@link com.atlassian.jira.avatar.Avatar} domain objects.
 *
 * @since v4.0
 */
public interface AvatarManager
{
    /**
     * Represents the different sizes of avatars that can be requested!
     */
    public static enum ImageSize {
        LARGE(48, ""),
        MEDIUM(32, "medium_"),
        SMALL(16, "small_");

        private final int pixels;
        private final String filenameFlag;
        private final Selection originSelection;

        private final static ImageSize largest;

        static {
            ImageSize maxValue = SMALL;
            for (ImageSize imageSize : values())
            {
                if(imageSize.getPixels() > maxValue.getPixels())
                {
                    maxValue = imageSize;
                }
            }
            largest = maxValue;
        }

        ImageSize(int pixels, String filenameFlag)
        {
            this.pixels = pixels;
            this.filenameFlag = Assertions.notNull("filenameFlag", filenameFlag);
            this.originSelection = new Selection(0, 0, pixels, pixels);
        }

        public int getPixels()
        {
            return pixels;
        }

        public String getFilenameFlag()
        {
            return filenameFlag;
        }

        public Selection getOriginSelection()
        {
            return originSelection;
        }

        /**
         * In order to cater for future addition of larger sizes this method finds the largest image size.
         * @return The largest ImageSize
         */
        public static ImageSize largest()
        {
            return largest;
        }

        /**
         * Returns an avatar image size matching the text provided.  If none can be found
         * Large is the default (it's always easier to downscale on the client!
         * @param text the images size. Will match "s", "small", "SMALL"
         * @return the image size enum matching the string provided
         */
        public static ImageSize fromString(String text)
        {
            for (ImageSize value : values())
            {
                if(StringUtils.startsWithIgnoreCase(value.name(), text))
                {
                    return value;
                }
            }
            //fallback to large if none could be found.
            return largest();
        }
    }

    /**
     * @deprecated since v4.4.  use {@link ImageSize#LARGE} instead
     */
    public final AvatarSize LARGE = new AvatarSize(48, "");
    /**
     * @deprecated since v4.4.  use {@link ImageSize#SMALL} instead
     */
    public final AvatarSize SMALL = new AvatarSize(16, "small_");
    public final String AVATAR_IMAGE_FORMAT = "PNG";

    public static final String USER_AVATAR_ID_KEY = "user.avatar.id";

    /**
     * Retrieve the avatar with the given id.
     *
     * @param avatarId must not be null.
     * @return the Avatar if there is one or null if not.
     * @throws DataAccessException if there is a back-end database problem.
     */
    Avatar getById(Long avatarId) throws DataAccessException;

    /**
     * Delete the avatar with the given id and the file on disk.
     *
     * @param avatarId must not be null.
     * @return true only if there was an avatar with the given id which was deleted.
     * @throws DataAccessException if there is a back-end database problem.
     */
    boolean delete(Long avatarId) throws DataAccessException;

    /**
     * Delete the avatar with the given id.
     *
     * @param avatarId must not be null.
     * @param alsoDeleteAvatarFile if false, the avatar file will be left on disk.
     * @return true only if there was an avatar with the given id which was deleted.
     * @throws DataAccessException if there is a back-end database problem.
     */
    boolean delete(Long avatarId, boolean alsoDeleteAvatarFile);

    /**
     * Saves the avatar as an updated version of the avatar with the same id that is already in the database.
     *
     * @param avatar must not be null.
     * @throws DataAccessException if there is a back-end database problem.
     */
    void update(Avatar avatar) throws DataAccessException;

    /**
     * Creates a database record for the given avatar. Use the return value as the persistent avatar, not the one you
     * passed in.
     *
     * @param avatar must not be null, must have a null id.
     * @return the created avatar which has an assigned id.
     * @throws DataAccessException if there is a back-end database problem.
     */
    @NotNull
    Avatar create(Avatar avatar) throws DataAccessException;

    /**
     * Creates a database record for the given avatar and uses the content of the InputStream as the image. Use the
     * return value as the persistent avatar, not the one you passed in.
     *
     * @param avatar must not be null, must have a null id.
     * @param image the data of the original avatar image.
     * @param selection the cropping selection for the image or null to take whole image.
     * @return the created avatar which has an assigned id.
     * @throws DataAccessException if there is a back-end database problem.
     */
    @NotNull
    Avatar create(Avatar avatar, InputStream image, Selection selection) throws DataAccessException, IOException;

    /**
     * Provides a list of all system avatars.
     *
     * @param type The type of system avatars to return
     * @return the system avatars.
     * @throws DataAccessException if there is a back-end database problem.
     */
    @NotNull
    List<Avatar> getAllSystemAvatars(Avatar.Type type) throws DataAccessException;

    /**
     * Provides a list of all avatars that are of the given type which have the given owner.
     *
     * @param type the desired type of the avatars to retrieve.
     * @param ownerId the id of the owner, matches the type.
     * @return all the avatars that have the given type and owner, never null.
     * @throws DataAccessException if there is a back-end database problem.
     */
    @NotNull
    List<Avatar> getCustomAvatarsForOwner(Avatar.Type type, String ownerId) throws DataAccessException;

    /**
     * Tells whether the given avatar is owned by the given owner.
     *
     * @param avatar the avatar to check, must not be null.
     * @param owner the owner to check, must not be null.
     * @return true only if the given owner is the owner of the given avatar.
     */
    boolean isAvatarOwner(Avatar avatar, String owner);

    /**
     * Provides read-only access to the data of the avatar image as an {@link java.io.InputStream} passed to the
     * provided dataProcessor. The InputStream is closed after the dataProcessor completes. The dataProcessor is
     * immediately invoked on the data for the avatar.
     *
     * @param avatar the avatar for which the data is to be processed.
     * @param size the size to return for this avatar
     * @param dataAccessor something to read the data.
     * @throws IOException if an IOException occurs in the dataProcessor or in acquiring the InputStream for the
     * avatar.
     */
    void readAvatarData(final Avatar avatar, final ImageSize size, final Consumer<InputStream> dataAccessor) throws IOException;

    /**
     * Provides read-only access to the data of the large avatar image as an {@link java.io.InputStream} passed to the
     * provided dataProcessor. The InputStream is closed after the dataProcessor completes. The dataProcessor is
     * immediately invoked on the data for the avatar.
     *
     * @param avatar the avatar for which the data is to be processed.
     * @param dataAccessor something to read the data.
     * @throws IOException if an IOException occurs in the dataProcessor or in acquiring the InputStream for the
     * avatar.
     * @deprecated since 4.4. Use {@link #readAvatarData(Avatar, ImageSize, com.atlassian.jira.util.Consumer)} instead
     */
    void readLargeAvatarData(Avatar avatar, Consumer<InputStream> dataAccessor) throws IOException;

    /**
     * Provides read-only access to the data of the small avatar image as an {@link java.io.InputStream} passed to the
     * provided dataProcessor. The InputStream is closed after the dataProcessor completes. The dataProcessor is
     * immediately invoked on the data for the avatar.
     *
     * @param avatar the avatar for which the data is to be processed.
     * @param dataAccessor something to read the data.
     * @throws IOException if an IOException acquiring the InputStream for the avatar.
     * @deprecated since 4.4. Use {@link #readAvatarData(Avatar, ImageSize, com.atlassian.jira.util.Consumer)} instead
     */
    void readSmallAvatarData(Avatar avatar, Consumer<InputStream> dataAccessor) throws IOException;

    /**
     * Returns the directory for storing avatars.
     *
     * @return the directory.
     */
    @NotNull
    File getAvatarBaseDirectory();

    /**
     * Gets the default avatar for the given type.
     *
     * @param ofType the Avatar type.
     * @return the default Avatar.
     */
    @NotNull
    Long getDefaultAvatarId(Avatar.Type ofType);

    /**
     * Gets the avatar id to use to represent an unknown or anonymous user
     * @return the avatar id for an anonymous user
     */
    Long getAnonymousAvatarId();

    /**
     * Determines if the remoteUser provided has permission to view avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin, project admin or has browse permission
     * for the owner project.  For user avatars, the method checks that the remoteUser has use permission for JIRA or
     * the remoteUser and avatar owner are the same person
     *
     * @param remoteUser The remote user trying to view an avatar
     * @param type The type of avatar
     * @param ownerId The owner id of the avatar being viewed
     * @return true if the remote user has permission to view avatars owned by the owner provided.
     */
    boolean hasPermissionToView(final com.opensymphony.user.User remoteUser, final Avatar.Type type, final String ownerId);

    /**
     * Determines if the remoteUser provided has permission to view avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin, project admin or has browse permission
     * for the owner project.  For user avatars, the method checks that the remoteUser has use permission for JIRA or
     * the remoteUser and avatar owner are the same person
     *
     * @param remoteUser The remote user trying to view an avatar
     * @param type The type of avatar
     * @param ownerId The owner id of the avatar being viewed
     * @return true if the remote user has permission to view avatars owned by the owner provided.
     */
    boolean hasPermissionToView(final User remoteUser, final Avatar.Type type, final String ownerId);

    /**
     * Determines if the remoteUser provided has permission to edit avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin or project admin for the owner project.
     * For user avatars, the method checks that the remoteUser has admin permissions for JIRA or the remoteUser and
     * avatar owner are the same person.  If external user management is enabled this method returns false
     *
     * @param remoteUser The remote user trying to edit an avatar
     * @param type The type of avatar
     * @param ownerId The owner id of the avatar being edited
     * @return true if the remote user has permission to edit avatars owned by the owner provided.
     */
    boolean hasPermissionToEdit(final com.opensymphony.user.User remoteUser, final Avatar.Type type, final String ownerId);

    /**
     * Determines if the remoteUser provided has permission to edit avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin or project admin for the owner project.
     * For user avatars, the method checks that the remoteUser has admin permissions for JIRA or the remoteUser and
     * avatar owner are the same person.  If external user management is enabled this method returns false
     *
     * @param remoteUser The remote user trying to edit an avatar
     * @param type The type of avatar
     * @param ownerId The owner id of the avatar being edited
     * @return true if the remote user has permission to edit avatars owned by the owner provided.
     */
    boolean hasPermissionToEdit(final User remoteUser, final Avatar.Type type, final String ownerId);

    /**
     * Always returns true.
     *
     * @deprecated since v4.4. The 'jira.user.avatar.enabled' property is redundant and no longer honoured within JIRA.
     * Implementers of this interface should always return true for this method.  If this method returns other than true,
     * then the behaviour of JIRA may be unpredictable.
     * @return True if user avatars should be displayed including user hovers
     */
    boolean isUserAvatarsEnabled();
}
