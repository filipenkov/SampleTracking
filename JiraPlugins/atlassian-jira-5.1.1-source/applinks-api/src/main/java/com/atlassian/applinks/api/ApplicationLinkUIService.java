package com.atlassian.applinks.api;

/**
 * Generates HTML fragments to support a standardized UX for displaying and
 * controlling application link status.
 * <p>
 * A typical use case:  A plugin servlet is generating some content that
 * involves making an application link request, and it gets a
 * {@link CredentialsRequiredException} indicating that the link is
 * unavailable.  The servlet can use ApplicationLinkUIService as follows
 * to generate a standard message box with a "please authenticate with
 * [application name]" link:
 * <pre>
 *     ApplicationLinkUIService.MessageBuilder messageBuilder =
 *         applicationLinkUIService.authorisationRequest(myAppLink)
 *             .format(ApplicationLinkUIService.MessageFormat.BANNER);
 *     String messageHtml = messageBuilder.getHtml();
 * </pre>
 * <p>
 * The {@link MessageFormat#BANNER} format is a standard AUI message box of
 * the "warning" style, which is meant to be displayed at the very top of a
 * page or a gadget.  This is appropriate if the overall page/gadget content
 * depends on the application link's state; successful completion of the
 * authentication/authorisation flow will cause the page/gadget to be
 * refreshed.  To display a smaller inline message that does not apply to
 * the entire page, use {@link MessageFormat#INLINE} instead.
 * <p>
 * In order for these elements to have the proper styling and behavior, the
 * front end must import CSS and Javascript from the web resource
 * "com.atlassian.applinks.applinks-plugin:applinks-public".
 * <p>
 * It is also possible to generate these elements entirely on the front end
 * instead, if this is more convenient for a particular plugin.  Functions
 * for this are in "applinks.public.js" in the applinks-public web resource.
 * <p>
 * Internationalization of message text is handled by the standard
 * {@link com.atlassian.sal.api.message.I18nResolver} mechanism.
 * 
 * @since 3.6
 */
public interface ApplicationLinkUIService
{
    /**
     * Returns a {@link MessageBuilder} to construct an HTML fragment with a
     * "please authenticate with [application]" message, and a link that lets the
     * user begin authentication.
     * @param appLink  an application link; cannot be null
     * @return a {@link MessageBuilder}; will not be null
     */
    MessageBuilder authorisationRequest(ApplicationLink appLink);
    
    /**
     * An HTML fragment builder returned by
     * {@link ApplicationLinkUIService#authorisationRequest(ApplicationLink)}.
     */
    public interface MessageBuilder
    {
        /**
         * Specifies whether to use a brief format (the default) or a banner format.
         * @param format a {@link MessageFormat}; cannot be null
         * @return the same MessageBuilder instance
         */
        MessageBuilder format(MessageFormat format);
        
        /**
         * Adds custom content to the message.  The content may contain HTML markup and will
         * not be escaped.
         * @param contentHtml an HTML string; cannot be null
         * @return the same MessageBuilder instance
         */
        MessageBuilder contentHtml(String contentHtml);
        
        /**
         * Returns the message/banner as an HTML string.
         * <p>
         * If using the {@link MessageFormat#BANNER} format (the default), this is a
         * {@code <div>} element; for the {@link MessageFormat#INLINE} format, it is a
         * {@code <span>}.
         */
        String getHtml();
    }
    
    /**
     * Constants for use with {@link MessageBuilder#format(MessageFormat)}.
     */
    public enum MessageFormat
    {
        /**
         * An inline element that can be displayed anywhere on a page.
         */
        INLINE,
        
        /**
         * A banner with a standard AUI border and icon.  This should be displayed above all
         * other content in the gadget, or, if not in a gadget, above all other content on
         * the page.
         */
        BANNER
    }
}
