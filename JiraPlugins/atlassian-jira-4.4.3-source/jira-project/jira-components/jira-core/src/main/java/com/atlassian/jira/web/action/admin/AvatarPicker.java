package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.FileSize;
import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.ImageScaler;
import com.atlassian.jira.avatar.Selection;
import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.FileNameCharacterCheckerUtil;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;
import webwork.config.Configuration;
import webwork.multipart.MultiPartRequestWrapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Web action for Avatar image selection or uploading page.
 *
 * @since v4.0
 */
public class AvatarPicker extends JiraWebActionSupport
{
    /**
     * JDK must support processing the types. THESE MUST BE STORED IN lower case.
     * Checked http://reference.sitepoint.com/html/mime-types-full
     */
    private static final List<String> CONTENT_TYPES = EasyList.build(
            "image/jpeg", "image/gif", "image/png", "image/pjpeg", "image/x-png"
    );

    /**
     * Fits the popup window.
     */
    private static final int MAX_SIDE_LENGTH = 500;

    private static final int FRAME_WIDTH = MAX_SIDE_LENGTH + 20;

    /**
     * ImageIO format name used to write the image to disk.
     */
    private static final String AVATAR_IMAGE_FORMAT = "png";

    private static final String TEMP_FILE_PREFIX = "JIRA-avatar";

    private static final String TEMP_FILE_EXTENSION = ".png";

    private static final String FORM_FIELD_IMAGE_FILE = "imageFile";

    private static final int DEFAULT_MAX_MEGAPIXELS = 5;

    /**
     * Action name.
     */
    private static final String CROP = "crop";

    /**
     * Action name.
     */
    private static final String SECURITY_BREACH = "securitybeach";

    /**
     * If uploading a new avatar, the project id is required.
     */
    private String ownerId;
    private String avatarField;
    private String avatarType;
    private String remove;
    private String updateUrl;
    // crop instructions
    private int offsetX = -1;
    private int offsetY = -1;
    private int width = -1; // because it's a square, width=height

    private Avatar avatar = null;
    private AvatarManager avatarManager;
    private Thumber thumber;
    private String selectedAvatar;

    public AvatarPicker(AvatarManager avatarManager)
    {
        this.avatarManager = avatarManager;
        thumber = new Thumber(Thumbnail.MimeType.PNG);
    }

    protected void doValidation()
    {
        if (isAllowedToPickAvatar())
        {
            TemporaryAvatar av = (TemporaryAvatar) ActionContext.getSession().get(SessionKeys.TEMP_AVATAR);
            if (av == null)
            {
                addErrorMessage("avatarpicker.upload.failure");
            }
            // check image crop insets make sense
            int maxOffset = MAX_SIDE_LENGTH - AvatarManager.ImageSize.LARGE.getPixels();
            if (offsetX < 0 || offsetY < 0 || offsetX > maxOffset || offsetY > maxOffset)
            {
                addErrorMessage("avatarpicker.crop.failure");
            }
        }
        super.doValidation();
    }

    public String doDefault() throws Exception
    {
        if (!isAllowedToPickAvatar())
        {
            return SECURITY_BREACH;
        }
        return super.doDefault();
    }

    public String doUpload() throws Exception
    {
        if (!isAllowedToPickAvatar())
        {
            return SECURITY_BREACH;
        }
        // validation
        // check the file does not have zero bytes
        MultiPartRequestWrapper multiPartRequest = getMultiPart();
        if (multiPartRequest == null)
        {
            return INPUT;
        }

        String contentType = multiPartRequest.getContentType(FORM_FIELD_IMAGE_FILE);
        String filesystemName = multiPartRequest.getFilesystemName(FORM_FIELD_IMAGE_FILE);
        if (!isImageContent(contentType))
        {
            if (filesystemName != null && filesystemName.indexOf(".") == -1)
            {
                log.info("Received avatar upload with unsupported content type: " + contentType + " and no extension");
                addErrorMessage(getText("avatarpicker.upload.contenttype.no.ext.failure"));
                return ERROR;
            }

            log.info("Received avatar upload with unsupported content type: " + contentType);
            addErrorMessage(getText("avatarpicker.upload.contenttype.failure"));
            return ERROR;
        }
        File file = multiPartRequest.getFile(FORM_FIELD_IMAGE_FILE);
        if (file == null)
        {
            if (filesystemName != null)
            {
                final Long attachmentSize = new Long(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE));
                addError(FORM_FIELD_IMAGE_FILE, getText(
                        "attachfile.error.filenotfoundortoolarge",
                        filesystemName, FileSize.format(attachmentSize)
                ));
                return ERROR;
            }
            else
            {
                addError(FORM_FIELD_IMAGE_FILE, getText("avatarpicker.upload.file.missing"));
                return ERROR;
            }
        }

        final String invalidChars = new FileNameCharacterCheckerUtil().assertFileNameDoesNotContainInvalidChars(filesystemName);
        if (invalidChars == null)
        {
            File scaledFile = clampSize(file, MAX_SIDE_LENGTH);

            if (scaledFile == null)
            {
                // error should have been added before
                return ERROR;
            }
            final Image image = thumber.getImage(scaledFile);
            setInitialCoordinates(image.getWidth(null), image.getHeight(null));
            // save a temporary avatar so we can do scaling
            TemporaryAvatar tempAvatar = new TemporaryAvatar(contentType, filesystemName, scaledFile);
            ActionContext.getSession().put(SessionKeys.TEMP_AVATAR, tempAvatar);
            if (image.getWidth(null) <= AvatarManager.ImageSize.LARGE.getPixels() && image.getHeight(null) == image.getWidth(null))
            {
                StringBuffer skipToEndUrl = new StringBuffer("/secure/project/AvatarPicker.jspa?ownerId=").append(ownerId)
                        .append("&width=").append(width)
                        .append("&offsetX=").append(offsetX)
                        .append("&offsetY=").append(offsetY)
                        .append("&avatarType=").append(avatarType);
                return getRedirect(skipToEndUrl.toString());
            }
            else
            {
                return CROP;
            }
        }
        else
        {
            addErrorMessage(getText("avatarpicker.upload.filename.failure", invalidChars));
            return ERROR;
        }
    }

    protected String doExecute() throws Exception
    {
        if (!isAllowedToPickAvatar())
        {
            return SECURITY_BREACH;
        }
        TemporaryAvatar temporaryAvatar = (TemporaryAvatar) ActionContext.getSession().remove(SessionKeys.TEMP_AVATAR);
        if (temporaryAvatar == null)
        {
            addErrorMessage(getText("avatarpicker.upload.failure"));
            return ERROR;
        }
        //noinspection SuspiciousNameCombination
        final Selection selection = new Selection(offsetX, offsetY, width, width);
        if (StringUtils.isBlank(getOwnerId()))
        {
            // avatar for new project
            ImageScaler scaler = new ImageScaler();
            FileInputStream tempAvFis = new FileInputStream(temporaryAvatar.getFile());
            final BufferedImage sourceImage = ImageIO.read(tempAvFis);
            final RenderedImage scaledImage = scaler.getSelectedImageData(sourceImage, selection, AvatarManager.ImageSize.LARGE.getPixels());
            IOUtil.shutdownStream(tempAvFis);
            File croppedTemporaryAvatarFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_EXTENSION);
            ImageIO.write(scaledImage, AVATAR_IMAGE_FORMAT, croppedTemporaryAvatarFile);
            croppedTemporaryAvatarFile.deleteOnExit();
            TemporaryAvatar croppedTemporaryAvatar = new TemporaryAvatar("image/png", temporaryAvatar.getOriginalFilename(), croppedTemporaryAvatarFile);
            ActionContext.getSession().put(SessionKeys.TEMP_AVATAR, croppedTemporaryAvatar);
            // since this is for the project create case, the avatar itself will be created by the create project process
        }
        else
        {
            Avatar newAvatar = AvatarImpl.createCustomAvatar(temporaryAvatar.getOriginalFilename(), temporaryAvatar.getContentType(), getRealAvatarType(), getOwnerId());
            avatar = avatarManager.create(newAvatar, new FileInputStream(temporaryAvatar.getFile()), selection);
        }
        if (temporaryAvatar.getFile().delete())
        {
            log.debug("Deleted temporary avatar file " + temporaryAvatar.getFile().getAbsolutePath());
        }
        else
        {
            log.info("Couldn't delete temporary avatar file. Will retry on shutdown. " + temporaryAvatar.getFile().getAbsolutePath());
        }

        return super.doExecute();
    }

    private boolean isAllowedToPickAvatar()
    {
        return StringUtils.isBlank(getOwnerId()) || avatarManager.hasPermissionToEdit(getRemoteUser(), getRealAvatarType(), getOwnerId());
    }

    private boolean isImageContent(final String contentType)
    {
        return contentType != null && CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    private File clampSize(final File sourceFile, int edgeSize) throws IOException
    {
        Image sourceImage = thumber.getImage(sourceFile);
        if(sourceImage == null)
        {
            addErrorMessage(getText("avatarpicker.upload.image.corrupted"));
            return null;
        }
        final int sourceHeight = sourceImage.getHeight(null);
        final int sourceWidth = sourceImage.getWidth(null);

        final int maxPixels = getMaxMegaPixels();
        if (sourceHeight * sourceWidth > (maxPixels * 1000000))
        {
            addErrorMessage(getText("avatarpicker.upload.too.big", maxPixels));
            return null;
        }

        if (sourceHeight > edgeSize || sourceWidth > edgeSize)
        {
            return cropImageToFile(edgeSize, sourceImage);
        }
        else
        {
            return sourceFile;
        }
    }

    private File cropImageToFile(final int edgeSize, final Image sourceImage)
            throws IOException
    {
        File prescaledFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_EXTENSION);
        Thumber.WidthHeightHelper dimensions = thumber.determineScaleSize(edgeSize, edgeSize, sourceImage.getWidth(null), sourceImage.getHeight(null));
        BufferedImage scaledImage = thumber.scaleImage(sourceImage, dimensions);
        ImageIO.write(scaledImage, AVATAR_IMAGE_FORMAT, prescaledFile);
        prescaledFile.deleteOnExit();
        return prescaledFile;
    }


    private int getMaxMegaPixels()
    {
        final String megaPixels = getApplicationProperties().getDefaultBackedString("jira.avatar.megapixels");
        if (StringUtils.isNotBlank(megaPixels) && StringUtils.isNumeric(megaPixels))
        {
            return Integer.parseInt(megaPixels);
        }

        return DEFAULT_MAX_MEGAPIXELS;

    }

    public List<Avatar> getSystemAvatars()
    {
        final List<Avatar> systemAvatars = avatarManager.getAllSystemAvatars(getRealAvatarType());
        //don't show the anonymous avatar for user avatars!
        if(Avatar.Type.USER.equals(getRealAvatarType()))
        {
            for (Iterator<Avatar> iterator = systemAvatars.iterator(); iterator.hasNext();)
            {
                final Avatar avatar = iterator.next();
                if(avatar.getId().equals(avatarManager.getAnonymousAvatarId()))
                {
                    iterator.remove();
                }
            }
        }

        return systemAvatars;
    }

    public List<Avatar> getUploadedAvatars()
    {
        if (StringUtils.isBlank(ownerId))
        {
            return Collections.emptyList();
        }
        return avatarManager.getCustomAvatarsForOwner(getRealAvatarType(), ownerId);
    }

    public String getAvatarField()
    {
        return avatarField;
    }

    public void setAvatarField(final String avatarField)
    {
        this.avatarField = avatarField;
    }

    MultiPartRequestWrapper getMultiPart()
    {
        return ServletActionContext.getMultiPartRequest();
    }

    public String getUrl(Avatar avatar)
    {
        String servlet = "projectavatar";
        if(Avatar.Type.USER.equals(getRealAvatarType()))
        {
            servlet = "useravatar";
        }
        if (avatar != null)
        {
            if (avatar.getOwner() != null)
            {
                return ActionContext.getRequest().getContextPath() + "/secure/" + servlet + "?size=large&ownerId=" + ownerId + "&avatarId=" + avatar.getId();
            }
            else
            {
                return ActionContext.getRequest().getContextPath() + "/secure/" + servlet + "?size=large&avatarId=" + avatar.getId();
            }
        }
        else
        {
            return "";
        }
    }

    public String getRemove()
    {
        return remove;
    }

    public void setRemove(final String remove)
    {
        this.remove = remove;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(final String ownerId)
    {
        this.ownerId = ownerId;
    }

    public Avatar getAvatar()
    {
        return avatar;
    }

    public void setOffsetX(final int offsetX)
    {
        this.offsetX = offsetX;
    }

    public void setOffsetY(final int offsetY)
    {
        this.offsetY = offsetY;
    }

    public void setWidth(final int width)
    {
        this.width = width;
    }

    public int getOffsetX()
    {
        return offsetX;
    }

    public int getOffsetY()
    {
        return offsetY;
    }

    public int getWidth()
    {
        return width;
    }

    public int getFrameWidth()
    {
        return FRAME_WIDTH;
    }

    private void setInitialCoordinates(final int width, final int height)
    {
        final int baseSize = AvatarManager.ImageSize.LARGE.getPixels();
        // Position initial selection not in the middle of the image, to make it obvious it's not a centered selection
        if (offsetX == -1)
        {
            if (width <= baseSize)
            {
                offsetX = 0;
            }
            else
            {
                offsetX = (width - baseSize) / 5;
            }
        }
        if (offsetY == -1)
        {
            if (height <= baseSize)
            {
                offsetY = 0;
            }
            else
            {
                offsetY = (height - baseSize) / 5;
            }
        }

        if (this.width == -1)
        {
            final int minWidth = Math.min(width, height);

            if (minWidth <= baseSize)
            {
                this.width = minWidth;
            }
            else
            {
                this.width = baseSize + ((minWidth - baseSize) / 3);
            }
        }

    }

    public boolean isTemporaryAvatarExistent()
    {
        final boolean tempAvatarExists = null != ActionContext.getSession().get(SessionKeys.TEMP_AVATAR);
        log.debug("does avatar exist? " + tempAvatarExists);
        return tempAvatarExists;
    }

    public String getTemporaryAvatarUrl()
    {
        // if the user chooses a new temporary avatar we need to keep making this url unique so that the javascript that
        // ajaxly retrieves this url always gets a unique one, forcing the browser to keep the image fresh
        return ServletActionContext.getRequest().getContextPath() + "/secure/temporaryavatar?cropped=true&magic=" + System.currentTimeMillis();
    }

    public Avatar.Type getRealAvatarType()
    {
        return Avatar.Type.getByName(getAvatarType());
    }

    public String getAvatarType()
    {
        return avatarType;
    }

    public void setAvatarType(final String avatarType)
    {
        this.avatarType = avatarType;
    }

    public void setSelectedAvatar(final String selectedAvatar)
    {
        this.selectedAvatar = selectedAvatar;
    }

    public String getSelectedAvatar()
    {
        return selectedAvatar;
    }

    public String getUpdateUrl()
    {
        return updateUrl;
    }

    public Long getDefaultAvatarId()
    {
        return avatarManager.getDefaultAvatarId(Avatar.Type.valueOf(avatarType.toUpperCase()));
    }

    public void setUpdateUrl(final String updateUrl)
    {
        this.updateUrl = updateUrl;
    }
}
