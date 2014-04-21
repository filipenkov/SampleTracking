package com.atlassian.jira.service.util.handler;

import com.atlassian.core.util.RandomGenerator;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.util.FileNameCharacterCheckerUtil;
import com.atlassian.mail.MailUtils;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An AbstractMessageHandler that stores the parameter map.
 * <p/>
 * TODO Insert summary about other responsibilities/ features of this class.
 * <p/>
 * <h3>Attachment processing</h3> This class does a number of things including the processing of parts that need to be
 * saved as attachments within the {@link #saveAttachmentIfNecessary(javax.mail.Part, javax.mail.Message,
 * com.opensymphony.user.User, org.ofbiz.core.entity.GenericValue)} method.
 * <p/>
 * This method eventually calls others which attempt to locate all parts that are possible candidates and should be kept
 * after it is categorised a part as a particular type. These types are <ul> <li>Html - content type = text/html</li>
 * <li>PlainText - content type = text/plain</li> <li>Inline - any part that is not an 'external' attachment. Typically
 * this used to represent an inline image by most email clients</li> <li>Attachment - any part that is an 'external'
 * attachment that the user added to the email. These typically represent a binary or file that was attached and
 * accompanies the email.</li> </ul>
 * <p/>
 * A few protected methods within this class that are intended for overriding if behaviour needs to be customised. These
 * methods of interest are <ul> <li>{@link #attachAttachmentsParts(javax.mail.Part)}</li> <li>{@link
 * #attachHtmlParts(javax.mail.Part)} </li> <li>{@link #attachInlineParts(javax.mail.Part)} is abstract</li> <li>{@link
 * #attachMessagePart(javax.mail.Part, javax.mail.Message)} </li> <li>{@link #attachPlainTextParts(javax.mail.Part)} is
 * abstract</li> </ul>
 * <p/>
 * The two abstract attachXXX methods are implemented by sub classes.
 * <p/>
 * Numerous helper methods are available upon the {@link com.atlassian.mail.MailUtils MailUtils} class to assist with
 * simple tests.
 */
public abstract class AbstractMessageHandler implements MessageHandler
{
    private static final Logger log = Logger.getLogger(AbstractMessageHandler.class);
    protected static final String KEY_REPORTER = "reporterusername";
    protected static final String KEY_CATCHEMAIL = "catchemail";
    protected static final String KEY_CREATEUSERS = "createusers";
    protected static final String KEY_NOTIFYUSERS = "notifyusers";

    protected static final String KEY_FINGER_PRINT = "fingerprint";

    /**
     * Value for parameter {@link #KEY_FINGER_PRINT} which matches legacy behaviour, accepting emails even if this JIRA
     * sent them.
     */
    static final String VALUE_FINGER_PRINT_ACCEPT = "accept";

    /**
     * Default value for parameter {@link #KEY_FINGER_PRINT} which matches forwards emails if this JIRA sent them,
     * falling back to {@link #VALUE_FINGER_PRINT_IGNORE} if the forward email address is missing.
     */
    static final String VALUE_FINGER_PRINT_FORWARD = "forward";

    /**
     * Value for parameter {@link #KEY_FINGER_PRINT} which makes this handler ignore emails detected to have been sent
     * by this instance of JIRA.
     */
    static final String VALUE_FINGER_PRINT_IGNORE = "ignore";

    /**
     * Valid values for {@link #KEY_FINGER_PRINT}
     */
    private final List VALUES_FINGERPRINT = EasyList.build(VALUE_FINGER_PRINT_ACCEPT, VALUE_FINGER_PRINT_FORWARD, VALUE_FINGER_PRINT_IGNORE);

    protected static final String KEY_BULK = "bulk";
    //the 3 possible values of bulk key. (default in other cases)
    private static final String VALUE_BULK_IGNORE = "ignore";
    private static final String VALUE_BULK_FORWARD = "forward";
    private static final String VALUE_BULK_DELETE = "delete";

    protected static final String CONTENT_TYPE_TEXT = "text/plain";

    protected static final String HEADER_MESSAGE_ID = "message-id";
    protected static final String HEADER_IN_REPLY_TO = "in-reply-to";

    /**
     * filename used if one cannot be determined from an attached message
     */
    private static final String ATTACHED_MESSAGE_FILENAME = "attachedmessage";

    /**
     * The default filename assigned to attachments that do not contain a filename etc
     */
    private final static String DEFAULT_BINARY_FILE_NAME = "binary.bin";

    /**
     * This is a silly protected field that indicates whether the email should be deleted BUT only after the
     * canHandleMessage() method is called.  It is used as a mechanism to indicate two values from from a single method
     * call.
     * <p/>
     * One say it should be lined up against the wall and shot, with a 2 value object returned instead.  But in the
     * interests of binary compatibility its left as is for the moment.
     */
    protected boolean deleteEmail;

    private MessageErrorHandler errorHandler;

    protected Map params = new HashMap();

    /**
     * Username of default reporter, if sender not recognized.
     */
    public String reporteruserName;

    /**
     * New issues without this recipient are ignored.
     */
    public String catchEmail;

    /**
     * How to handle emails with header: "Precedence: bulk"
     */
    public String bulk;

    /**
     * Whether to create users if they do not exist
     */
    public boolean createUsers;

    public boolean notifyUsers;

    /**
     * Policy for handling email that has JIRA's fingerprint on it. The default configuration is "forward" which
     * indicates that the forward address should be sent the email instead. If there is no foward email address
     * configured or if the fingerPrintPolicy parameter is set to "ignore", the message will not be picked up. A value
     * of "accept" makes JIRA vulnerable to certain types of mail loops. JRA-12467
     */
    private String fingerPrintPolicy;

    final protected CommentManager commentManager;
    final protected IssueFactory issueFactory;
    final protected ApplicationProperties applicationProperties;
    private final JiraApplicationContext jiraApplicationContext;
    private static final FileNameCharacterCheckerUtil fileNameCharacterCheckerUtil = new FileNameCharacterCheckerUtil();
    private static final char INVALID_CHAR_REPLACEMENT = '_';

    protected AbstractMessageHandler()
    {
        this(ComponentAccessor.getComponent(CommentManager.class), ComponentAccessor.getComponent(IssueFactory.class),
            ComponentAccessor.getApplicationProperties(), ComponentAccessor.getComponent(JiraApplicationContext.class));
    }

    protected AbstractMessageHandler(final CommentManager commentManager, final IssueFactory issueFactory, final ApplicationProperties applicationProperties, final JiraApplicationContext jiraApplicationContext)
    {
        this.commentManager = commentManager;
        this.issueFactory = issueFactory;
        this.applicationProperties = applicationProperties;
        this.jiraApplicationContext = jiraApplicationContext;
    }

    public void init(final Map params)
    {
        this.params = params;

        if (params.containsKey(KEY_REPORTER))
        {
            reporteruserName = (String) params.get(KEY_REPORTER);
        }

        if (params.containsKey(KEY_CATCHEMAIL))
        {
            catchEmail = (String) params.get(KEY_CATCHEMAIL);
        }

        if (params.containsKey(KEY_BULK))
        {
            bulk = (String) params.get(KEY_BULK);
        }

        if (params.containsKey(KEY_CREATEUSERS))
        {
            createUsers = Boolean.valueOf((String) params.get(KEY_CREATEUSERS));

            if (createUsers)
            {
                // Check that the default reporter is NOT configured
                // As if it is configured and createing users is set to true,
                // it is ambiguous whether to create a new user or use the default reporter
                final boolean extUserMgmt = applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
                if (reporteruserName != null)
                {
                    if (extUserMgmt)
                    {
                        log.warn("Default Reporter Username set to '" + reporteruserName + "', " + KEY_CREATEUSERS + " is set to true and external user management is enabled.");
                        log.warn("Ignoring the " + KEY_CREATEUSERS + " flag. Using the default Reporter username '" + reporteruserName + "'.");
                    }
                    else
                    {
                        log.warn("Default Reporter Username set to '" + reporteruserName + "' and " + KEY_CREATEUSERS + " is set to true.");
                        log.warn("Ignoring the Default Reporter Username, users will be created if they do not exist.");
                    }
                }
                else if (extUserMgmt)
                {
                    log.warn(KEY_CREATEUSERS + " is set to true, but external user managment is enabled.  Users will NOT be created.");
                }

            }

            //JRA-13996: Don't use Boolean.getBoolean(String), it actually looks up to see if a system property of the passed name is
            // set to true.
            notifyUsers = !params.containsKey(KEY_NOTIFYUSERS) || Boolean.parseBoolean((String) params.get(KEY_NOTIFYUSERS));
        }
        else
        {
            log.debug("Defaulting to not creating users");
            createUsers = false;
            log.debug("Defaulting to notifying users since user creation is not specified");
            notifyUsers = true;
        }

        if (params.containsKey(KEY_FINGER_PRINT) && VALUES_FINGERPRINT.contains(params.get(KEY_FINGER_PRINT)))
        {
            fingerPrintPolicy = (String) params.get(KEY_FINGER_PRINT);
        }
        else
        {
            log.debug("Defaulting to fingerprint policy of 'forward'");
            fingerPrintPolicy = VALUE_FINGER_PRINT_FORWARD;
        }
    }

    /**
     * Perform the specific work of this handler for the given message.
     *
     * @return true if the message is to be deleted from the source.
     * @throws MessagingException if anything went wrong.
     */
    public abstract boolean handleMessage(Message message) throws MessagingException;

    /**
     * Validation call to be made at the start of handleMessage().<br> It sets a global boolean deleteEmail, whether the
     * email should be deleted if it cannot be handled. ie. return deleteEmail if canHandleMessage() is false
     *
     * @param message message to check if it can be handled
     * @return whether the message should be handled
     */
    protected boolean canHandleMessage(final Message message)
    {
        /**
         * JRA-15582 - The default handler behaviour is to NOT delete the email.  Each bit of code that
         * wants to delete email must explicitly set this flag to true.
         */
        deleteEmail = false;

        /**
         * If the message fails a finder print check, then we don't want to handle it.
         */
        if (!fingerPrintCheck(message))
        {
            return false;
        }

        if (checkBulk(message))
        {
            return false;
        }

        // if the recipient is specified, check it is present in the message and reject if not
        if (catchEmail != null)
        {
            //JRA-16176: If  a message's recipients cannot be parsed, then we assume that the message is invalid. This
            // will leave the message in the mail box if there is no forward address, otherwise it will forward the
            // message to the forward address.

            final boolean forCatchAll;
            try
            {
                forCatchAll = MailUtils.hasRecipient(catchEmail, message);
            }
            catch (final MessagingException exception)
            {
                deleteEmail = false;
                log.debug("Could not parse message recipients. Assuming message is bad.", exception);
                addError(getI18nBean().getText("admin.errors.bad.destination.address"), exception);

                return false;
            }

            if (!forCatchAll)
            {
                //
                // JRA-15580 - We should NEVER delete the email if its not intended for this "catchemail"
                //
                deleteEmail = false;
                logCantHandleRecipients(message);
                return false;
            }
            else
            {
                // its a hit on recipient address, we want to delete this email when we are done
                deleteEmail = true;
            }
        }

        return true;
    }

    private boolean checkBulk(final Message message)
    {
        try
        {
            if ("bulk".equalsIgnoreCase(getPrecedenceHeader(message)) || isDeliveryStatus(message) || isAutoSubmitted(message))
            {
                //default action is to process the email for backwards compatibility
                if (bulk != null)
                {
                    if (VALUE_BULK_IGNORE.equalsIgnoreCase(bulk))
                    {
                        log.debug("Ignoring email with bulk delivery type");
                        deleteEmail = false;
                        return true;
                    }
                    else if (VALUE_BULK_FORWARD.equalsIgnoreCase(bulk))
                    {
                        log.debug("Forwarding email with bulk delivery type");
                        addError(getI18nBean().getText("admin.forward.bulk.mail"));
                        deleteEmail = false;
                        return true;
                    }
                    else if (VALUE_BULK_DELETE.equalsIgnoreCase(bulk))
                    {
                        log.debug("Deleting email with bulk delivery type");
                        deleteEmail = true;
                        return true;
                    }
                }
            }
            return false;
        }
        catch (final MessagingException mex)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Error occured while looking for bulk headers - assuming not bulk email: " + mex.getMessage(), mex);
            }
            return false;
        }
    }

    /**
     * Determines if the given message is acceptable for handling based on the presence of any JIRA fingerprint headers
     * and this {@link com.atlassian.jira.service.util.handler.MessageHandler}'s configuration.
     *
     * @param message the message to check.
     * @return false only if this handler should not handle the message because of a JIRA fingerprint.
     */
    boolean fingerPrintCheck(final Message message)
    {
        boolean fingerPrintClean = true; // until proven guilty
        final List fingerPrintHeaders = getFingerPrintHeader(message);
        final String instanceFingerPrint = jiraApplicationContext.getFingerPrint();
        if (!fingerPrintHeaders.isEmpty())
        {
            if (log.isDebugEnabled())
            {
                log.debug("JIRA fingerprints found on on incoming email message: ");
                for (final Object fingerPrintHeader : fingerPrintHeaders)
                {
                    log.debug("fingerprint: " + fingerPrintHeader);
                }
            }
            if (fingerPrintHeaders.contains(instanceFingerPrint))
            {
                log.warn("Received message carrying this JIRA instance fingerprint (" + instanceFingerPrint + ")");
                if (VALUE_FINGER_PRINT_ACCEPT.equalsIgnoreCase(fingerPrintPolicy))
                {
                    log.warn("Handler is configured to accept such messages. Beware of mail loops: JRA-12467");
                }
                else if (VALUE_FINGER_PRINT_FORWARD.equalsIgnoreCase(fingerPrintPolicy))
                {
                    log.debug("Forwarding fingerprinted email.");
                    addError(getI18nBean().getText("admin.forward.mail.loop"));
                    fingerPrintClean = false;
                }
                else if (VALUE_FINGER_PRINT_IGNORE.equalsIgnoreCase(fingerPrintPolicy))
                {
                    log.info("Handler is configured to ignore this message.");
                    fingerPrintClean = false;
                }

            }
            else
            {
                log.info("Received message with another JIRA instance's fingerprint");
            }
        }
        return fingerPrintClean;
    }

    /**
     * Returns the values of the JIRA fingerprint headers on the message, or null if there is no such header. Messages
     * sent by v3.13 of JIRA and later should all carry the fingerprint header with the value being the instance's
     * "unique" fingerprint.
     *
     * @param message the message to get the header from.
     * @return the possibly empty list of values of the JIRA fingerprint headers of the sending instance.
     * @since v3.13
     */
    List<String> getFingerPrintHeader(final Message message)
    {
        List<String> headers = Collections.emptyList();
        try
        {
            final String[] headerArray = message.getHeader(Email.HEADER_JIRA_FINGER_PRINT);
            if (headerArray != null)
            {
                headers = Arrays.asList(headerArray);
            }
        }
        catch (final MessagingException e)
        {
            log.error("Failed to get mail header " + Email.HEADER_JIRA_FINGER_PRINT);
        }
        return headers;
    }

    /**
     * Loops through all the {@link Part}s, and for each one of type {@link Part#ATTACHMENT}, call {@link
     * #createAttachmentWithPart(Part,User,GenericValue) }.
     *
     * @param message The multipart message to search for attachments in
     * @param issue The issue to create attachments in
     * @return a collection of change items, one for each attachment added. If no attachments are added, returns and
     *         empty collection.
     * @throws IOException If there is a problem creating the attachment
     * @throws MessagingException If there is a problem reading the message
     */
    protected Collection<ChangeItemBean> createAttachmentsForMessage(final Message message, final GenericValue issue) throws IOException, MessagingException
    {
        final Collection<ChangeItemBean> attachmentChangeItems = new ArrayList<ChangeItemBean>();
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS))
        {
            final String disposition = message.getDisposition();
            if (message.getContent() instanceof Multipart)
            {
                final Multipart multipart = (Multipart) message.getContent();
                final Collection<ChangeItemBean> changeItemBeans = handleMultipart(multipart, message, issue);
                if ((changeItemBeans != null) && !changeItemBeans.isEmpty())
                {
                    attachmentChangeItems.addAll(changeItemBeans);
                }
            }
            //JRA-12123: Message is not a multipart, but it has a disposition of attachment.  This means that
            //we got a message with an empty body and an attachment.  We'll ignore inline.
            else if (Part.ATTACHMENT.equalsIgnoreCase(disposition))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Trying to add attachment to issue from attachment only message.");
                }

                final ChangeItemBean res = saveAttachmentIfNecessary(message, null, getReporter(message), issue);
                if (res != null)
                {
                    attachmentChangeItems.add(res);
                }
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to add message attachements to issue: JIRA Attachements are disabled.");
            }
        }

        return attachmentChangeItems;
    }

    /**
     * Adds an attachment to the issue if one exists on the message.
     *
     * @param multipart multipart - attachments
     * @param message original message
     * @param issue issue generic value
     * @return A Collection of {@link ChangeItemBean}s representing the added attachments if they were created. If no
     *         attachments were created, an empty collection is returned.
     * @throws MessagingException if reading of the multipart attributes fails
     * @throws IOException if getting or saving of the attachment content fails
     */
    private Collection<ChangeItemBean> handleMultipart(final Multipart multipart, final Message message, final GenericValue issue) throws MessagingException, IOException
    {
        final Collection<ChangeItemBean> attachmentChangeItems = new ArrayList<ChangeItemBean>();
        for (int i = 0, n = multipart.getCount(); i < n; i++)
        {
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Adding attachments for multi-part message. Part %d of %d.", i + 1, n));
            }

            final BodyPart part = multipart.getBodyPart(i);

            // there may be non-attachment parts (e.g. HTML email) - fixes JRA-1842
            final boolean isContentMultipart = part.getContent() instanceof Multipart;
            if (isContentMultipart)
            {
                // Found another multi-part - process it and collection all change items.
                attachmentChangeItems.addAll(handleMultipart((Multipart) part.getContent(), message, issue));
            }
            else
            {
                // JRA-15133: if this part is an attached message, skip it if:
                // * the option to ignore attached messages is set to true, OR
                // * this message is in reply to the attached one (redundant info)
                // Note: this is now covered by the shouldAttach() method
                final ChangeItemBean res = saveAttachmentIfNecessary(part, message, getReporter(message), issue);
                if (res != null)
                {
                    attachmentChangeItems.add(res);
                }
            }
        }

        return attachmentChangeItems;
    }

    /**
     * Checks if the containing message is "In-Reply-To" to the attached message. This is done through checking standard
     * email headers as specified in RFC-822.
     *
     * @param containingMessage a message, which may be the reply.
     * @param attachedMessage another message which may be the original
     * @return true if the first message is in reply to the second.
     * @throws ParseException if there was an error in retrieving the required message headers
     * @throws javax.mail.MessagingException if javamail complains
     */
    private boolean isMessageInReplyToAnother(final Message containingMessage, final Message attachedMessage) throws MessagingException, ParseException
    {
        // Note: we are using the fact that most common mail clients use In-Reply-To to reference the Message-ID of the
        // message being replied to. This is "defacto standard" but is not guaranteed by the spec.
        final String attachMessageId = getMessageId(attachedMessage);
        final String[] replyToIds = containingMessage.getHeader(HEADER_IN_REPLY_TO);

        if (log.isDebugEnabled())
        {
            log.debug("Checking if attachment was reply to containing message:");
            log.debug("\tAttachment mesage id: " + attachMessageId);
            log.debug("\tNew message reply to values: " + Arrays.toString(replyToIds));
        }

        if (replyToIds != null)
        {
            for (final String id : replyToIds)
            {
                if ((id != null) && id.equalsIgnoreCase(attachMessageId))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the Message-ID for a given message.
     *
     * @param message a message
     * @return the Message-ID
     * @throws ParseException if the Message-ID header was not present
     * @throws javax.mail.MessagingException if javamail complains
     */
    String getMessageId(final Message message) throws MessagingException, ParseException
    {
        // Note: we get an array because Message-ID is an arbitrary header, but really there can be only one Message-ID
        // value (if it is present)
        final String[] originalMessageIds = message.getHeader(HEADER_MESSAGE_ID);
        if ((originalMessageIds == null) || (originalMessageIds.length == 0))
        {
            final String msg = "Could not retrieve Message-ID header from message: " + message;
            log.debug(msg);
            throw new ParseException(msg);
        }
        return originalMessageIds[0];
    }

    /**
     * Saves an attachment if it should be attached...
     *
     * @param part the part containing the attachment
     * @param containingMessage the message which contained the part. Use null unless the containing message is relevant
     * e.g. when processing an attached part of content type "message/rfc822"
     * @param reporter the user who sent the part
     * @param issue the issue to attach to
     * @return the ChangeItemBean created by the AttachmentManager if the part was attached, or null
     * @throws MessagingException if javamail complains
     * @throws IOException if javamail complains
     */
    private ChangeItemBean saveAttachmentIfNecessary(final Part part, final Message containingMessage, final User reporter, final GenericValue issue) throws MessagingException, IOException
    {
        final boolean keep = shouldAttach(part, containingMessage);
        if (keep)
        {
            return createAttachmentWithPart(part, reporter, issue);
        }
        return null;
    }

    /**
     * This method determines if a particular part should be included added as an attachment to an issue.
     *
     * @param part the part to potentially attach
     * @param containingMessage the message which contained the part - may be null
     * @return true if the part should be attached; false otherwise
     * @throws java.io.IOException if javamail complains
     * @throws javax.mail.MessagingException if javamail complains
     */
    final protected boolean shouldAttach(final Part part, final Message containingMessage) throws MessagingException, IOException
    {
        Assertions.notNull("part", part);

        boolean attach;

        if (log.isDebugEnabled())
        {
            log.debug("Checking if attachment should be added to issue:");
            log.debug("\tContent-Type: " + part.getContentType());
            log.debug("\tContent-Disposition: " + part.getDisposition());
        }

        if (MailUtils.isPartMessageType(part) && (null != containingMessage))
        {
            log.debug("Attachment detected as a rfc/822 message.");
            attach = attachMessagePart(part, containingMessage);
        }
        else if (MailUtils.isPartAttachment(part))
        {
            log.debug("Attachment detected as an \'Attachment\'.");
            attach = attachAttachmentsParts(part);
        }
        else if (MailUtils.isPartInline(part))
        {
            log.debug("Attachment detected as an inline element.");
            attach = attachInlineParts(part);
        }
        else if (MailUtils.isPartPlainText(part))
        {
            log.debug("Attachment detected as plain text.");
            attach = attachPlainTextParts(part);
        }
        else if (MailUtils.isPartHtml(part))
        {
            log.debug("Attachment detected as HTML.");
            attach = attachHtmlParts(part);
        }
        else if (MailUtils.isPartRelated(containingMessage))
        {
            log.debug("Attachment detected as related content.");
            attach = attachRelatedPart(part);
        }
        else
        {
            attach = false;
        }

        if (log.isDebugEnabled())
        {
            if (attach)
            {
                log.debug("Attachment was added to issue");
            }
            else
            {
                log.debug("Attachment was ignored.");
            }
        }

        return attach;
    }

    /**
     * This method determines whether or not plain text parts should be attached.
     *
     * @param part the part to be attached - already determined to be type text/plain.
     * @return true if the part should be attached; false otherwise
     * @throws java.io.IOException if javamail complains
     * @throws javax.mail.MessagingException if javamail complains
     */
    abstract protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException;

    /**
     * This method determines whether or not HTML parts should be attached.
     *
     * @param part the part to be attached - already determined to be type text/html.
     * @return true if the part should be attached; false otherwise
     * @throws java.io.IOException if javamail complains
     * @throws javax.mail.MessagingException if javamail complains
     */
    abstract protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException;

    /**
     * Only attach an inline part if it's content is not empty and if it is not a signature part.
     *
     * @param part a mail part - assumed to have inline disposition
     * @return whether or not this inline part should be attached.
     * @throws MessagingException if Content-Type checks fail
     * @throws IOException if content checks fail
     */
    protected boolean attachInlineParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part) && !MailUtils.isPartSignaturePKCS7(part);
    }

    /**
     * Only attach an attachment part if it's content is not empty and if it is not a signature part.
     *
     * @param part a mail part - assumed to have attachment disposition
     * @return whether or not this inline part should be attached.
     * @throws MessagingException if Content-Type checks fail
     * @throws IOException if content checks fail
     */
    protected boolean attachAttachmentsParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part) && !MailUtils.isPartSignaturePKCS7(part);
    }

    /**
     * JRA-15133: if this part is an attached message, skip it if: <ul> <li>the option to ignore attached messages is
     * set to true, OR</li> <li>this message is in reply to the attached one (redundant info), OR</li> <li>if the
     * message is not in reply to the attached one, skip if the content is empty</li> </ul>
     * <p/>
     * This is required to handle the behaviour of some mail clients (e.g. Outlook) who, when replying to a message,
     * include the entire original message as an attachment with content type "message/rfc822". In these cases, the
     * attached message is redundant information, so we skip it.
     *
     * @param messagePart the Message part (already known to be of message content type)
     * @param containingMessage the original message which contains messagePart
     * @return true if the part should be attached, false otherwise
     * @throws java.io.IOException if javamail complains
     * @throws javax.mail.MessagingException if javamail complains
     */
    protected boolean attachMessagePart(final Part messagePart, final Message containingMessage) throws IOException, MessagingException
    {
        boolean keep = false;

        // only keep message parts if we are not ignoring them
        if (!shouldIgnoreEmailMessageAttachments())
        {
            // .. and the message part is not being replied to by this message
            if (!isReplyMessagePart(messagePart, containingMessage))
            {
                // .. and the message part is not empty
                keep = !MailUtils.isContentEmpty(messagePart);

                if (!keep && log.isDebugEnabled())
                {
                    log.debug("Attachment not attached to issue: Message is empty.");
                }
            }
            else
            {
                log.debug("Attachment not attached to issue: Detected as reply.");
            }
        }
        else
        {
            log.debug("Attachment not attached to issue: Message attachment has been disabled.");
        }

        return keep;
    }

    /**
     * JRA-15670: if this part is contained within a multipart/related message, keep it, as it may be a legitimate
     * attachment, but without the content disposition set to a sensible value (e.g. when using Outlook 2007 and Rich
     * Text format).
     *
     * @param part       the part contained within the related message
     * @return true if the part should be attached, false otherwise
     * @throws java.io.IOException if javamail complains
     * @throws javax.mail.MessagingException if javamail complains
     */
    protected boolean attachRelatedPart(final Part part) throws IOException, MessagingException
    {
        return !MailUtils.isContentEmpty(part);
    }

    /**
     * Tests if jira has been configured to ignore message attachments.
     *
     * @return Returns true if email message attachments should be ignored, otherwise returns false.
     */
    boolean shouldIgnoreEmailMessageAttachments()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS);
    }

    /**
     * Helper which tests if the incoming part is a reply to the containing message
     *
     * @param messagePart The part being tested
     * @param containingMessage The container message
     * @return True if the part is definitely a reply to the message, otherwise returns false;
     * @throws java.io.IOException if javamail complains
     * @throws javax.mail.MessagingException if javamail complains
     */
    private boolean isReplyMessagePart(final Part messagePart, final Message containingMessage) throws IOException, MessagingException
    {
        boolean replyMessage;

        try
        {
            replyMessage = isMessageInReplyToAnother(containingMessage, (Message) messagePart.getContent());
        }
        catch (final ParseException e)
        {
            log.debug("Can't tell if the message is in reply to the attached message -- will attach it in case");
            replyMessage = false;
        }

        return replyMessage;
    }

    /**
     * Create an attachment for a particular mime-part.  The BodyPart must be of type {@link Part#ATTACHMENT}.
     *
     * @param part part of disposition {@link Part#ATTACHMENT} to create the attachment from
     * @param reporter issue reporter
     * @param issue issue to create attachments in
     * @return A {@link ChangeItemBean} representing the added attachment, or null if no attachment was created
     * @throws IOException If there is a problem creating the attachment in the filesystem
     */
    protected ChangeItemBean createAttachmentWithPart(final Part part, final User reporter, final GenericValue issue) throws IOException
    {
        try
        {
            final String contentType = MailUtils.getContentType(part);
            final String rawFilename = part.getFileName();
            String filename = getFilenameForAttachment(part);

            final File file = getFileFromPart(part, (issue != null ? issue.getString("key") : "null"));

            if (log.isDebugEnabled())
            {
                log.debug("part=" + part);
                log.debug("Filename=" + filename + ", content type=" + contentType + ", content=" + part.getContent());
            }

            final AttachmentManager attachmentManager = getAttachmentManager();
            filename = renameFileIfInvalid(filename, issue, reporter);
            final ChangeItemBean cib = attachmentManager.createAttachment(file, filename, contentType, reporter, issue);
            if (cib != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Created attachment " + rawFilename + " for issue " + issue.get("key"));
                }
                return cib;
            }
            else
            {
                log.debug("Encountered an error creating the attachment " + rawFilename + " for issue " + issue.get("key"));
                return null;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while creating attachment for issue " + (issue != null ? issue.getString("key") : "null") + ": " + e, e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Handy method which takes a number of strategies when attempting to give a filename for a particular part. The
     * filename may already be present in the part or may need to be formulated or composed from other identifies within
     * the part (such as a subject, content type etc).
     *
     * @param part The part being tested.
     * @return The filename for the attachment or null if one was not present.
     * @throws MessagingException relays any MessagingException thrown by a lower layer such as java mail
     * @throws IOException relays any IOExceptions
     */
    protected String getFilenameForAttachment(final Part part) throws MessagingException, IOException
    {
        String filename = getFilenameFromPart(part);
        if (null == filename)
        {
            if (MailUtils.isPartMessageType(part))
            {
                filename = getFilenameFromMessageSubject(part);
            }
            else if (MailUtils.isPartInline(part))
            {
                filename = getFilenameFromContentType(part);
            }
        }

        // double check that filename extracting worked!
        if (null != filename)
        {
            if (StringUtils.isBlank(filename))
            {
                final String message = "Having found a filename(aka filename is not null) filename should not be an empty string, but is...";
                log.warn(message);

                // since empty string is invalid, return null and let a name be default or generated name be used instead.
                filename = null;
            }
        }

        return filename;
    }

    /**
     * Retrieves the filename from a mail part and MIME decodes it if necessary.
     *
     * @param part a mail part - may or may not have a file name.
     * @return the file name set on the part, or null.
     * @throws MessagingException if retrieving the file name fails.
     * @throws IOException if doing the MIME decoding fails.
     */
    private String getFilenameFromPart(final Part part) throws MessagingException, IOException
    {
        String filename = part.getFileName();
        if (null != filename)
        {
            filename = MailUtils.fixMimeEncodedFilename(filename);
        }
        return filename;
    }

    private String getFilenameFromMessageSubject(final Part part) throws MessagingException, IOException
    {
        // JRA-15133: determine filename from subject line of the message
        final Message message = (Message) part.getContent();
        String filename = message.getSubject();
        if (StringUtils.isBlank(filename))
        {
            // if no subject, use Message-ID
            try
            {
                filename = getMessageId(message);
            }
            catch (final ParseException e)
            {
                // no Message-ID, use constant
                filename = ATTACHED_MESSAGE_FILENAME;
            }
        }

        return filename;
    }

    /**
     * Allocates or composes a filename from a part, typically this is done by massaging the content type into a
     * filename.
     *
     * @param part The part
     * @return The composed filename
     * @throws MessagingException May be thrown by javamail
     * @throws IOException May be thrown by javamail.
     */
    private String getFilenameFromContentType(final Part part) throws MessagingException, IOException
    {
        String filename = DEFAULT_BINARY_FILE_NAME;

        final String contentType = MailUtils.getContentType(part);
        final int slash = contentType.indexOf("/");
        if (-1 != slash)
        {
            final String subMimeType = contentType.substring(slash + 1);

            // if its not a binary attachment convert the content type into a filename image/gif becomes image-file.gif
            if (!subMimeType.equals("bin"))
            {
                filename = contentType.substring(0, slash) + '.' + subMimeType;
            }
        }

        return filename;
    }

    /**
     * Replaces all invalid characters in the filename using {@link FileNameCharacterCheckerUtil#replaceInvalidChars(String,
     * char)} with {@link #INVALID_CHAR_REPLACEMENT} as the replacement character.
     *
     * @param filename filename to check if its valid
     * @param issue issue the file is to be attached
     * @param reporter the author of the comment to add to the issue if the filename is invalid
     * @return <li>if filename is null, returns null</li> <li>if its valid, returns filename</li> <li>if its invalid,
     *         returns filename with all invalid characters replaced with {@link #INVALID_CHAR_REPLACEMENT}</li>
     */
    protected String renameFileIfInvalid(final String filename, final GenericValue issue, final User reporter)
    {
        if (filename == null)
        {
            //let the attachmentManager handle the null filename when creating.
            return null;
        }

        //replace any invalid characters with the INVALID_CHAR_REPLACEMENT character
        final String replacedFilename = fileNameCharacterCheckerUtil.replaceInvalidChars(filename, INVALID_CHAR_REPLACEMENT);
        //if the filename has changed then add a comment to the issue to say it has been changed because of invalid characters
        if (!filename.equals(replacedFilename))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Filename was invalid: replacing '" + filename + "' with '" + replacedFilename + "'");
            }
            //add comment to the issue to map the original file name to the newly named filename
            commentManager.create(issueFactory.getIssue(issue), reporter.getName(), getI18nBean().getText(
                "admin.renamed.file.cause.of.invalid.chars", filename, replacedFilename), false);
            return replacedFilename;
        }
        return filename;
    }

    protected File getFileFromPart(final Part part, final String issueKey) throws IOException, MessagingException, GenericEntityException
    {
        File tempFile = null;
        try
        {
            tempFile = File.createTempFile("tempattach", "dat");
            final FileOutputStream out = new FileOutputStream(tempFile);

            try
            {
                part.getDataHandler().writeTo(out);
            }
            finally
            {
                out.close();
            }
        }
        catch (final IOException e)
        {
            log.error("Problem reading attachment from email for issue " + issueKey, e);
        }
        if (tempFile == null)
        {
            throw new IOException("Unable to create file?");
        }
        return tempFile;
    }

    AttachmentManager getAttachmentManager()
    {
        return ComponentAccessor.getAttachmentManager();
    }

    /**
     * Get the reporter from the email address who sent the message, or else create a new  user if creating users is set
     * to true, or use the default reporter if one is specified.
     * <p/>
     * If neither of these are found, return null.
     *
     * @param message The email message to search through.
     * @return The user who sent the email, or the default reporter, or null.
     * @throws MessagingException If there is a problem getting the user who created the message.
     */
    protected User getReporter(final Message message) throws MessagingException
    {
        User reporter = getAuthorFromSender(message);

        if (reporter == null)
        {
            //if createUsers is set, attempt to create a new reporter from the e-mail details
            if (createUsers)
            {
                reporter = createUserForReporter(message);
            }

            // If there's a default reporter set, and we haven't created a reporter yet, attempt to use the
            //default reporter.
            if ((reporteruserName != null) && (reporter == null))
            {
                // Sender not registered with JIRA, use default reporter
                reporter = OSUserConverter.convertToOSUser(UserUtils.getUser(reporteruserName));
                log.warn("Default reporter '" + reporteruserName + "' not found.");
            }
        }
        return reporter;
    }

    /**
     * For each sender of the given message in turn, look up a User first with a case-insensitively equal email address,
     * and failing that, with a username equal to the email address.
     * <p/>
     * JIRA wants to do this because when we create users in email handlers, we set email and username equal. If a user
     * subsequently changes their email address, we must not assume they don't exist and create them with the email
     * address as the username.
     *
     * @param message the message from which to get the User.
     * @return the User matching the sender of the message or null if none found.
     * @throws MessagingException if there's strife getting the message sender.
     */
    User getAuthorFromSender(final Message message) throws MessagingException
    {

        final List<String> senders = MailUtils.getSenders(message);
        User user = null;
        for (final String emailAddress : senders)
        {
            user = findUserByEmail(emailAddress);
            if (user != null)
            {
                break;
            }
            user = findUserByUsername(emailAddress);
            if (user != null)
            {
                break;
            }
        }

        return user;
    }

    /**
     * Finds the user with the given username or returns null if there is no such User. Convenience method which doesn't
     * throw up.
     *
     * @param username the username.
     * @return the User or null.
     */
    protected User findUserByUsername(final String username)
    {
        return OSUserConverter.convertToOSUser(UserUtils.getUser(username));
    }

    /**
     * Returns the first User found with an email address that equals the given emailAddress case insensitively.
     *
     * @param emailAddress the email address to match.
     * @return the User.
     */
    protected User findUserByEmail(final String emailAddress)
    {
        for (final com.atlassian.crowd.embedded.api.User user : UserUtils.getAllUsers())
        {
            if (emailAddress.equalsIgnoreCase(user.getEmailAddress()))
            {
                return OSUserConverter.convertToOSUser(user);
            }
        }
        return null;
    }

    /**
     * Tries to create a user using the details provided by the reporter.  Fails if external user managment is turned on
     * or, if no valid from email address was specified.
     *
     * @param message The original e-mail message.
     * @return A new user or null.
     */
    protected User createUserForReporter(final Message message)
    {
        User reporter = null;
        try
        {
            //If External User Management is set, throw an exception with the correct warning to be logged.
            if (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
            {
                log.warn("External user management is enabled. Contact your Administrator");
                return null;
            }

            // If reporter is not a recognised user, then create one from the information in the e-mail
            log.debug("Cannot find reporter for message. Creating new user.");

            final InternetAddress internetAddress = (InternetAddress) message.getFrom()[0];
            final String reporterEmail = internetAddress.getAddress();
            if (!TextUtils.verifyEmail(reporterEmail))
            {
                log.error("The email address [" + reporterEmail + "] received was not valid. Ensure that your mail client specified a valid 'From:' mail header. (see JRA-12203)");
                return null;
            }
            String fullName = internetAddress.getPersonal();
            if ((fullName == null) || (fullName.trim().length() == 0))
            {
                fullName = reporterEmail;
            }

            final String password = RandomGenerator.randomPassword();
            final UserUtil userUtil = ComponentAccessor.getUserUtil();

            if (notifyUsers)
            {
                reporter = userUtil.createUserWithEvent(reporterEmail, password, reporterEmail, fullName, UserEventType.USER_CREATED);
            }
            else
            {
                reporter = userUtil.createUserNoEvent(reporterEmail, password, reporterEmail, fullName);
            }
            log.debug("Created user " + reporterEmail + " as reporter of email-based issue.");
        }
        catch (final Exception e)
        {
            log.error("Error occurred while automatically creating a new user from email: ", e);
        }
        return reporter;
    }

    /**
     * Extract the 'Precedence' header value from the message
     */
    protected String getPrecedenceHeader(final Message message) throws MessagingException
    {
        final String[] precedenceHeaders = message.getHeader("Precedence");
        String precedenceHeader;

        if ((precedenceHeaders != null) && (precedenceHeaders.length > 0))
        {
            precedenceHeader = precedenceHeaders[0];

            if (!StringUtils.isBlank(precedenceHeader))
            {
                return precedenceHeader;
            }
        }
        return null;
    }

    protected boolean isDeliveryStatus(final Message message) throws MessagingException
    {
        final String contentType = message.getContentType();
        if ("multipart/report".equalsIgnoreCase(MailUtils.getContentType(contentType)))
        {
            return contentType.toLowerCase().contains("report-type=delivery-status");
        }
        else
        {
            return false;
        }
    }

    protected boolean isAutoSubmitted(final Message message) throws MessagingException
    {

        final String[] autoSub = message.getHeader("Auto-Submitted");
        if (autoSub != null)
        {
            for (final String auto : autoSub)
            {
                if (!"no".equalsIgnoreCase(auto))
                {
                    return true;
                }
            }
        }
        return false;
    }

    protected void recordMessageId(final String type, final Message message, final Long issueId) throws MessagingException
    {
        final String[] messageIds = message.getHeader("Message-Id");
        if ((messageIds != null) && (messageIds.length > 0))
        {
            // Record who the e-mail has come from
            final Address[] froms = message.getFrom();
            String fromAddress = null;
            if ((froms != null) && (froms.length > 0))
            {
                fromAddress = ((InternetAddress) froms[0]).getAddress();
            }

            ComponentAccessor.getMailThreadManager().createMailThread(type, issueId, fromAddress, messageIds[0]);
        }
    }

    protected GenericValue getAssociatedIssue(final Message message)
    {
        // Test if the message has In-Reply-To header to a message that is associated with an issue
        return ComponentAccessor.getMailThreadManager().getAssociatedIssue(message);
    }

    public void setErrorHandler(final MessageErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    public void addError(final String error)
    {
        if (errorHandler != null)
        {
            errorHandler.setError(error);
        }
    }

    public void addError(final String error, final Exception e)
    {
        if (errorHandler != null)
        {
            errorHandler.setError(error, e);
        }
    }

    /**
     * @deprecated Please use {@link #addError(String)} and return your own false :-) TODO: remove in 4.1
     */
    public boolean addErrorAndReturnFalse(final String error)
    {
        addError(error);
        return false;
    }

    /**
     * @deprecated Please use {@link #addError(String, Exception)} and return your own false :-) TODO: remove in 4.1
     */
    public boolean addErrorAndReturnFalse(final String error, final Exception e)
    {
        addError(error, e);
        return false;
    }

    protected MessageErrorHandler getErrorHandler()
    {
        return errorHandler;
    }

    protected I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    /**
     * This method just runs through all recipients of the email and builds up a debug string so that we can see who was
     * a recipient of the email.
     *
     * @param message the message that we can't handle.
     */
    private void logCantHandleRecipients(final Message message)
    {
        final Address[] addresses;
        try
        {
            addresses = message.getAllRecipients();
        }
        catch (final MessagingException e)
        {
            log.debug("Cannot handle message. Unable to parse recipient addresses.", e);
            return;
        }

        if ((addresses == null) || (addresses.length == 0))
        {
            log.debug("Cannot handle message.  No recipient addresses found.");
        }
        else
        {
            final StringBuffer recipients = new StringBuffer();

            for (int i = 0; i < addresses.length; i++)
            {
                final InternetAddress email = (InternetAddress) addresses[i];
                if (email != null)
                {
                    recipients.append(email.getAddress());
                    if ((i + 1) < addresses.length)
                    {
                        recipients.append(", ");
                    }
                }
            }
            log.debug("Cannot handle message as the recipient(s) (" + recipients.toString() + ") do not match the catch email " + catchEmail);
        }
    }

    void setFingerPrintPolicy(final String fingerPrintPolicy)
    {
        this.fingerPrintPolicy = fingerPrintPolicy;
    }
}
