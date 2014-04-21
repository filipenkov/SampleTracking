package com.atlassian.jira.plugin.viewissue;

import com.atlassian.core.util.FileSize;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.attachment.FileNameBasedVersionedAttachmentsList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentCreationDateComparator;
import com.atlassian.jira.issue.attachment.AttachmentFileNameCreationDateComparator;
import com.atlassian.jira.issue.attachment.AttachmentItem;
import com.atlassian.jira.issue.attachment.AttachmentItems;
import com.atlassian.jira.issue.attachment.AttachmentZipKit;
import com.atlassian.jira.issue.attachment.AttachmentsCategoriser;
import com.atlassian.jira.issue.attachment.MimetypesFileTypeMap;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.NonZipExpandableExtensions;
import com.atlassian.jira.web.util.FileIconBean;
import com.atlassian.jira.web.util.FileIconUtil;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;

/**
 * Provides context for the Attachments block on the View Issue page.
 *
 * @since v5.0
 */
public class AttachmentBlockContextProvider implements CacheableContextProvider
{
    private static final String ORDER_DESC = "desc";
    private static final String DEFAULT_ISSUE_ATTACHMENTS_ORDER = "asc";
    private static final String SORTBY_DATE_TIME = "dateTime";
    private static final String DEFAULT_ISSUE_ATTACHMENTS_SORTBY = "fileName";

    private final AttachmentManager attachmentManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ThumbnailManager thumbnailManager;
    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;
    private final NonZipExpandableExtensions nonZipExpandableExtensions;
    private final FileIconUtil fileIconUtil;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final DateTimeFormatter dateTimeFormatter;
    private final AttachmentZipKit attachmentZipKit;

    public AttachmentBlockContextProvider(AttachmentManager attachmentManager, JiraAuthenticationContext authenticationContext,
            VelocityRequestContextFactory velocityRequestContextFactory, ThumbnailManager thumbnailManager, UserManager userManager,
            ApplicationProperties applicationProperties,
            FileIconUtil fileIconUtil, IssueManager issueManager, PermissionManager permissionManager, DateTimeFormatter dateTimeFormatter)
    {
        this.attachmentManager = attachmentManager;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.thumbnailManager = thumbnailManager;
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
        this.nonZipExpandableExtensions = ComponentAccessor.getComponent(NonZipExpandableExtensions.class);
        this.fileIconUtil = fileIconUtil;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withStyle(COMPLETE) : null;
        attachmentZipKit = new AttachmentZipKit();
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");
        User user = authenticationContext.getLoggedInUser();

        final boolean zipEnabled = getZipSupport();

        AttachmentsCategoriser attachments = new AttachmentsCategoriser(thumbnailManager, new AttachmentsCategoriser.Source()
        {
            @Override
            public List<Attachment> getAttachments()
            {
                return attachmentManager.getAttachments(issue, attachmentComparator());
            }
        });

        paramsBuilder.add("iconGenerator", new IconGenerator());
        paramsBuilder.add("fileSizeFormatter", new FileSize());
        paramsBuilder.add("hasAttachments", !attachments.items().isEmpty());
        paramsBuilder.add("openSquareBracket", JiraUrlCodec.encode("["));
        paramsBuilder.add("closeSquareBracket", JiraUrlCodec.encode("]"));
        paramsBuilder.add("imageAttachments", convertToSimpleAttachments(issue, attachments.itemsThatHaveThumbs(), zipEnabled, user));
        paramsBuilder.add("fileAttachments", convertToSimpleAttachments(issue, attachments.itemsThatDoNotHaveThumbs(), zipEnabled, user));
        paramsBuilder.add("maximumNumberOfZipEntriesToShow", getMaximumNumberOfZipEntriesToShow());
        paramsBuilder.add("fullBaseUrl", JiraUrl.constructBaseUrl(getRequest(context)));

        return paramsBuilder.toMap();
    }

    /**
     * @return a Comparator&lt;Attachment&gt; according to the user's selection.
     */
    protected Comparator<Attachment> attachmentComparator()
    {
        final String attachmentSortBy = getAttachmentSortBy();
        final String attachmentOrder = getAttachmentOrder();

        Comparator<Attachment> attachmentComparator;
        if (SORTBY_DATE_TIME.equals(attachmentSortBy))
        {
            attachmentComparator = new AttachmentCreationDateComparator();
        }
        else
        {
            attachmentComparator = new AttachmentFileNameCreationDateComparator(authenticationContext.getLocale());
        }

        if (ORDER_DESC.equals(attachmentOrder))
        {
            attachmentComparator = Collections.reverseOrder(attachmentComparator);
        }

        return attachmentComparator;
    }

    private List<SimpleAttachment> convertToSimpleAttachments(Issue issue, AttachmentItems items, boolean zipEnabled, User user)
    {
        final FileNameBasedVersionedAttachmentsList attachmentsList = new FileNameBasedVersionedAttachmentsList(items.attachments());
        final CollectionBuilder<SimpleAttachment> builder = CollectionBuilder.newBuilder();

        for (AttachmentItem item : items)
        {
            boolean latestVersion = attachmentsList.isLatestVersion(item.attachment());
            boolean shouldExpandAsZip = shouldExpandAsZip(issue, item.attachment());
            boolean canDelete = canDeleteAttachment(issue, item.attachment(), user);
            builder.add(new SimpleAttachment(item.attachment(), latestVersion, zipEnabled && shouldExpandAsZip, item.thumbnail(), canDelete));
        }

        return builder.asList();
    }

    private boolean getZipSupport()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);
    }

    /**
     * Determines whether the specified attachment should be expanded as a zip file. Files are expanded if zip support
     * is on, the file extension is not one of the extensions specified by {@link com.atlassian.jira.web.bean.NonZipExpandableExtensions}
     * and if the file represents a valid zip file.
     *
     * @param attachment The attachment in play.
     * @return true if the the specified attachment should be expanded as a zip file; otherwise, false is returned.
     */
    private boolean shouldExpandAsZip(Issue issue, Attachment attachment)
    {
        final File attachmentFile = AttachmentUtils.getAttachmentFile(issue, attachment);
        final String attachmentExtension = FilenameUtils.getExtension(attachment.getFilename());

        return !nonZipExpandableExtensions.contains(attachmentExtension) && attachmentZipKit.isZip(attachmentFile);
    }

    private String getAttachmentOrder()
    {
        return getSessionBackedRequestParam("attachmentOrder", DEFAULT_ISSUE_ATTACHMENTS_ORDER, SessionKeys.VIEWISSUE_ATTACHMENT_ORDER);

    }

    private String getAttachmentSortBy()
    {
        return getSessionBackedRequestParam("attachmentSortBy", DEFAULT_ISSUE_ATTACHMENTS_SORTBY, SessionKeys.VIEWISSUE_ATTACHMENT_SORTBY);
    }

    private String getSessionBackedRequestParam(String requestParamName, String defaultValue, String sessionKey)
    {
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();

        final String requestParameter = requestContext.getRequestParameter(requestParamName);
        if (StringUtils.isNotBlank(requestParameter))
        {
            if (requestParameter.equals(defaultValue))
            {
                session.removeAttribute(sessionKey);
                return defaultValue;
            }
            else
            {
                session.setAttribute(sessionKey, requestParameter);
                return requestParameter;
            }
        }

        final String sortOrder = (String) session.getAttribute(sessionKey);
        return StringUtils.isNotBlank(sortOrder) ? sortOrder : defaultValue;

    }

    private int getMaximumNumberOfZipEntriesToShow()
    {
        String maximumNumberOfZipEntriesToShowAsString = applicationProperties.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_NUMBER_OF_ZIP_ENTRIES_TO_SHOW);
        int maximumNumberOfZipEntriesToShow = 30;
        try
        {
            maximumNumberOfZipEntriesToShow = Integer.parseInt(maximumNumberOfZipEntriesToShowAsString);
        }
        catch (NumberFormatException e)
        {
            //Ignoring error, we'll use the default of 30
        }
        return maximumNumberOfZipEntriesToShow;
    }

    private boolean canDeleteAttachment(Issue issue, Attachment attachment, User user)
    {
        return issueManager.isEditable(issue)
                && (permissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_ALL, issue, user)
                || (permissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_OWN, issue, user) && isUserAttachmentAuthor(attachment, user)));

    }

    private boolean isUserAttachmentAuthor(Attachment attachment, User user)
    {
        String attachmentAuthor = attachment.getAuthor();

        //if the author & the remote user are anonymous, return true
        if (attachmentAuthor == null && user == null)
        {
            return true;
        }

        //if the author but not the remote user are anonymous (or vice versa), return false
        else if (attachmentAuthor == null || user == null)
        {
            return false;
        }

        //if the attachment author is the remote user, return true
        return attachmentAuthor.equals(user.getName());
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    public class SimpleAttachment
    {
        private final Attachment attachment;
        private final boolean isLatest;
        private final boolean exapandAsZip;
        private final Thumbnail thumbnail;
        private boolean canDelete;
        private AttachmentZipKit.AttachmentZipEntries attachmentZipEntries;

        public SimpleAttachment(Attachment attachment, boolean latest, boolean exapandAsZip, Thumbnail thumbnail, boolean canDelete)
        {
            this.attachment = attachment;
            isLatest = latest;
            this.exapandAsZip = exapandAsZip;
            this.thumbnail = thumbnail;
            this.canDelete = canDelete;
        }

        public boolean isLatest()
        {
            return isLatest;
        }

        public Long getId()
        {
            return attachment.getId();
        }

        public String getMimetype()
        {
            return MimetypesFileTypeMap.getContentType(getFilename());
        }

        public String getFilename()
        {
            return attachment.getFilename();
        }

        public String getFilenameUrlEncoded()
        {
            return JiraUrlCodec.encode(attachment.getFilename(), true);
        }

        public String getCreatedFormatted()
        {
            return dateTimeFormatter.format(attachment.getCreated());
        }

        public String getFilesize()
        {
            return FileSize.format(attachment.getFilesize());
        }

        public String getAuthor()
        {
            return attachment.getAuthor();
        }

        public String getDisplayAuthor()
        {
            final User userObject = userManager.getUserObject(attachment.getAuthor());
            return userObject == null ? attachment.getAuthor() : userObject.getDisplayName();

        }

        public boolean isExpandAsZip()
        {
            return exapandAsZip;
        }

        public Thumbnail getThumbnail()
        {
            return thumbnail;
        }

        public String getThumbnailFilename()
        {
            return thumbnail == null || StringUtils.isBlank(thumbnail.getFilename()) ? "/images/broken_thumbnail.png" : JiraUrlCodec.encode(thumbnail.getFilename());
        }

        public boolean isCanDelete()
        {
            return canDelete;
        }

        /**
         * <p>Returns a list of zip entries for the specified attachment. The number of entries returned is limited to
         * the value of MAX_ZIP_ENTRIES.</p> <p/> <p>It is assumed that this attachment represents a valid zip file. In
         * order to find this out, use {@link com.atlassian.jira.web.action.issue.ViewIssue#shouldExpandAsZip(com.atlassian.jira.issue.attachment.Attachment)}.</p>
         *
         * @return A {@link java.util.List} of {@link com.atlassian.jira.issue.attachment.AttachmentZipKit.AttachmentZipEntry}
         *         for the specified attachment. Limited to {@link APKeys#JIRA_ATTACHMENT_NUMBER_OF_ZIP_ENTRIES_TO_SHOW}.
         */
        public AttachmentZipKit.AttachmentZipEntries getZipEntries()
        {
            if (attachmentZipEntries == null)
            {
                try
                {
                    File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
                    attachmentZipEntries = attachmentZipKit.listEntries(attachmentFile, getMaximumNumberOfZipEntriesToShow(), AttachmentZipKit.FileCriteria.ONLY_FILES);
                    return attachmentZipEntries;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            return attachmentZipEntries;
        }

    }

    public class IconGenerator
    {
        public SimpleIcon getIcon(SimpleAttachment attachment)
        {
            final FileIconBean.FileIcon fileIcon = fileIconUtil.getFileIcon(attachment.getFilename(), attachment.getMimetype());
            return new SimpleIcon(fileIcon == null ? "file.gif" : fileIcon.getIcon(), fileIcon == null ? "File" : fileIcon.getAltText());

        }

        public SimpleIcon getIcon(AttachmentZipKit.AttachmentZipEntry zipEntry)
        {
            final FileIconBean.FileIcon fileIcon = fileIconUtil.getFileIcon(zipEntry.getName(), null);
            return new SimpleIcon(fileIcon == null ? "file.gif" : fileIcon.getIcon(), fileIcon == null ? "File" : fileIcon.getAltText());

        }

        public class SimpleIcon
        {
            private final String icon;
            private final String altText;

            public SimpleIcon(String icon, String altText)
            {
                this.icon = icon;
                this.altText = altText;
            }

            public String getIcon()
            {
                return icon;
            }

            public String getAltText()
            {
                return altText;
            }
        }
    }

    private static HttpServletRequest getRequest(Map<String, Object> context)
    {
        return ((JiraHelper) context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER)).getRequest();
    }

}
