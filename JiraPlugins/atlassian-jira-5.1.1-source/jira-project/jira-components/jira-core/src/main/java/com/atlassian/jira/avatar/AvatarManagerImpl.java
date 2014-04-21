package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserNames;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.atlassian.jira.avatar.Avatar.Type.PROJECT;
import static com.atlassian.jira.avatar.Avatar.Type.USER;
import static com.atlassian.jira.config.properties.APKeys.JIRA_ANONYMOUS_USER_AVATAR_ID;
import static com.atlassian.jira.config.properties.APKeys.JIRA_DEFAULT_AVATAR_ID;
import static com.atlassian.jira.config.properties.APKeys.JIRA_DEFAULT_USER_AVATAR_ID;
import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Manager for Avatars.
 *
 * @since v4.0
 */
public class AvatarManagerImpl implements AvatarManager
{
    private static final Logger log = Logger.getLogger(AvatarManagerImpl.class);
    private static final String AVATAR_DIRECTORY = "data/avatars";
    private static final String AVATAR_CLASSPATH_PREFIX = "/avatars/";

    private AvatarStore store;
    private JiraHome jiraHome;
    private ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private ImageScaler scaler;

    public AvatarManagerImpl(AvatarStore store, JiraHome jiraHome, ApplicationProperties applicationProperties,
            final PermissionManager permissionManager)
    {
        this.store = store;
        this.jiraHome = jiraHome;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.scaler = new ImageScaler();
    }

    public Avatar getById(Long avatarId)
    {
        Assertions.notNull("avatarId", avatarId);
        return store.getById(avatarId);
    }

    public boolean delete(Long avatarId)
    {
        return delete(avatarId, true);
    }

    public boolean delete(Long avatarId, boolean alsoDeleteAvatarFile)
    {
        Assertions.notNull("avatarId", avatarId);
        final Avatar avatar = store.getById(avatarId);
        if (avatar != null)
        {
            if (alsoDeleteAvatarFile)
            {
                for (ImageSize size : ImageSize.values())
                {
                    deleteFile(getAvatarFile(avatar, size.getFilenameFlag()));
                }
            }
            return store.delete(avatarId);
        }
        else
        {
            return false;
        }
    }

    private void deleteFile(File file)
    {
        if (!file.delete())
        {
            file.deleteOnExit();
        }
    }

    public void update(Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.notNull("avatar.id", avatar.getId());
        store.update(avatar);
    }

    public Avatar create(Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.stateTrue("avatar.id must be null", avatar.getId() == null);
        return store.create(avatar);
    }

    public Avatar create(final Avatar avatar, final InputStream imageData, final Selection croppingSelection)
            throws DataAccessException, IOException
    {
        Assertions.notNull("avatar", avatar);
        if (avatar.isSystemAvatar())
        {
            throw new IllegalArgumentException("System avatars cannot be created with custom image data");
        }
        Assertions.notNull("imageData", imageData);

        File avatarFile = null;
        try
        {
            final Avatar created = create(avatar);
            final File largeAvatarFile = avatarFile = processImage(created, imageData, croppingSelection, ImageSize.largest());
            for (ImageSize size : ImageSize.values())
            {
                if(ImageSize.largest().equals(size))
                {
                    //already generated this one.
                    continue;
                }
                avatarFile = processImage(created, FileUtils.openInputStream(largeAvatarFile), null, size);
            }
            return created;
        }
        catch (RuntimeException failedCreate)
        {
            handleCreationFailure(avatarFile);
            throw failedCreate;
        }
    }

    private void handleCreationFailure(final File avatarFile)
    {
        try
        {
            if (avatarFile != null && avatarFile.exists() && !avatarFile.delete())
            {
                log.warn("Created avatar file '" + avatarFile
                        + "' but then failed to store to db. Failed to delete the file!");
            }
        }
        catch (RuntimeException failedDeleteFile)
        {
            log.warn("Created avatar file '" + avatarFile
                    + "' but then failed to store to db. Failed to delete the file!", failedDeleteFile);
        }
    }

    File processImage(final Avatar created, final InputStream imageData, final Selection croppingSelection, final ImageSize size)
            throws IOException
    {
        RenderedImage image = scaler.getSelectedImageData(ImageIO.read(imageData), croppingSelection, size.getPixels());
        File file = createAvatarFile(created, size.getFilenameFlag());
        ImageIO.write(image, AVATAR_IMAGE_FORMAT, file);
        return file;
    }

    File createAvatarFile(Avatar avatar, String flag) throws IOException
    {
        final File base = getAvatarBaseDirectory();
        createDirectoryIfAbsent(base);
        return new File(base, avatar.getId() + "_" + flag + avatar.getFileName());
    }

    public File getAvatarBaseDirectory()
    {
        return new File(jiraHome.getHome(), AVATAR_DIRECTORY);
    }

    private void createDirectoryIfAbsent(final File dir) throws IOException
    {
        if (!dir.exists() && !dir.mkdirs())
        {
            throw new IOException("Avatars directory is absent and I'm unable to create it. '" + dir.getAbsolutePath() + "'");
        }
        if (!dir.isDirectory())
        {
            throw new IllegalStateException("Avatars directory cannot be created due to an existing file. '" + dir.getAbsolutePath() + "'");
        }
    }

    public List<Avatar> getAllSystemAvatars(Avatar.Type type)
    {
        return store.getAllSystemAvatars(type);
    }

    public List<Avatar> getCustomAvatarsForOwner(final Avatar.Type type, final String ownerId)
    {
        return store.getCustomAvatarsForOwner(type, ownerId);
    }

    public boolean isAvatarOwner(final Avatar avatar, final String owner)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.notNull("owner", owner);
        return getCustomAvatarsForOwner(avatar.getAvatarType(), owner).contains(avatar);
    }

    void processAvatarData(final Avatar avatar, final Consumer<InputStream> dataAccessor, ImageSize size)
            throws IOException
    {
        final InputStream data;
        if (avatar.isSystemAvatar())
        {
            // load from classpath
            String path = AVATAR_CLASSPATH_PREFIX + size.getFilenameFlag() + avatar.getFileName();
            data = getClasspathStream(path);
            if (data == null)
            {
                log.error("System Avatar not found at the following resource path: " + path);
                throw new IOException("File not found");
            }
        }
        else
        {
            final File file = getOrGenerateAvatarFile(avatar, size);
            data = new FileInputStream(file);
        }
        try
        {
            dataAccessor.consume(data);
        }
        finally
        {
            data.close();
        }
    }

    private File getOrGenerateAvatarFile(Avatar avatar, ImageSize size) throws IOException
    {
        final File file = getAvatarFile(avatar, size.getFilenameFlag());
        //if this file doesn't exist and we requested something other than the largest size lets
        //try to generate a smaller image.
        if(!file.exists() && !ImageSize.largest().equals(size))
        {
            final File largeFile = getAvatarFile(avatar, ImageSize.largest().getFilenameFlag());
            if(largeFile.exists())
            {
                //generate a smaller image file for the avatar requested and return that!
                return processImage(avatar, FileUtils.openInputStream(largeFile), null, size);
            }
        }
        return file;
    }

    InputStream getClasspathStream(final String path)
    {
        return AvatarManagerImpl.class.getResourceAsStream(path);
    }

    File getAvatarFile(final Avatar avatar, final String sizeFlag)
    {
        final File base = getAvatarBaseDirectory();
        return new File(base, avatar.getId() + "_" + sizeFlag + avatar.getFileName());
    }

    public void readAvatarData(final Avatar avatar, ImageSize size, final Consumer<InputStream> dataAccessor) throws IOException
    {
        processAvatarData(avatar, dataAccessor, size);
    }



    public Long getDefaultAvatarId(Avatar.Type ofType)
    {
        Assertions.stateTrue("Can only handle Project & User avatars at this time.", Avatar.Type.PROJECT.equals(ofType) || Avatar.Type.USER.equals(ofType));
        String defaultAvatarId = null;
        if (Avatar.Type.PROJECT.equals(ofType))
        {
            defaultAvatarId = applicationProperties.getString(JIRA_DEFAULT_AVATAR_ID);
        }
        if (Avatar.Type.USER.equals(ofType))
        {
            defaultAvatarId = applicationProperties.getString(JIRA_DEFAULT_USER_AVATAR_ID);
        }
        return defaultAvatarId != null ? Long.valueOf(defaultAvatarId) : null;
    }

    @Override
    public Long getAnonymousAvatarId()
    {
        final String avatarId = applicationProperties.getString(JIRA_ANONYMOUS_USER_AVATAR_ID);
        return avatarId != null ? Long.valueOf(avatarId) : null;
    }

    @Override
    public boolean hasPermissionToView(final User remoteUser, final Avatar.Type type, final String ownerId)
    {
        Assertions.notNull("avatar", type);

        if (PROJECT.equals(type))
        {
            final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
            final Project project = getProject(ownerId);
            //can't edit non-existent project!
            if (project == null)
            {
                return false;
            }
            final boolean isProjectAdmin = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, remoteUser);
            final boolean hasBrowseProject = permissionManager.hasPermission(Permissions.BROWSE, project, remoteUser);
            return isAdmin || isProjectAdmin || hasBrowseProject;
        }
        else if (USER.equals(type))
        {
            final boolean isUser = permissionManager.hasPermission(Permissions.USE, remoteUser);
            final boolean remoteUserIsSame = UserNames.equal(ownerId, remoteUser);
            return isUser || remoteUserIsSame;
        }
        else
        {
            throw new UnsupportedOperationException("Avatar type '" + type + "' not supported!");
        }
    }

    @Override
    public boolean hasPermissionToEdit(final User remoteUser, final Avatar.Type type, final String ownerId)
    {
        Assertions.notNull("type", type);

        if (PROJECT.equals(type))
        {
            final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
            final Project project = getProject(ownerId);
            //can't edit non-existent project!
            if (project == null)
            {
                return false;
            }
            final boolean isProjectAdmin = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, remoteUser);
            return isAdmin || isProjectAdmin;
        }
        else if (USER.equals(type))
        {
            //only logged in users can modify someone's avatar image!
            if (isAnonymous(remoteUser) || ownerId == null)
            {
                return false;
            }
            final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
            final boolean isOwner = UserNames.equal(ownerId, remoteUser);
            return (isAdmin || isOwner);
        }
        else
        {
            throw new UnsupportedOperationException("Avatar type '" + type + "' not supported!");
        }
    }

    private Project getProject(String ownerId)
    {
        try
        {
            final Long projectId = Long.parseLong(ownerId);
            return getProjectManager().getProjectObj(projectId);
        }
        catch (NumberFormatException e)
        {
            log.error("Owner id must be a long for project avatar type but was '" + ownerId + "'.");
            return null;
        }
    }

    ProjectManager getProjectManager()
    {
        return ComponentAccessor.getProjectManager();
    }
}
