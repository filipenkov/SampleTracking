package com.atlassian.applinks.core;

import java.io.IOException;
import java.io.StringWriter;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkUIService;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringEscapeUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Standard implementation of {@link ApplicationLinkUIService}.
 */
public class DefaultApplicationLinkUIService implements ApplicationLinkUIService
{
    private static final String REQUEST_BANNER_TEMPLATE = "templates/fragments/auth_request_banner.vm";
    private static final String REQUEST_INLINE_TEMPLATE = "templates/fragments/auth_request_inline.vm";
    
    private final I18nResolver i18nResolver;
    private final TemplateRenderer templateRenderer;
    
    public DefaultApplicationLinkUIService(I18nResolver i18nResolver,
                                           TemplateRenderer templateRenderer)
    {
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
    }
    
    public MessageBuilder authorisationRequest(ApplicationLink appLink)
    {
        return new AuthRequestMessageBuilder(checkNotNull(appLink, "appLink"));
    }
    
    private class AuthRequestMessageBuilder implements ApplicationLinkUIService.MessageBuilder
    {
        private final ApplicationLink appLink;
        private MessageFormat format = MessageFormat.BANNER;
        private String contentHtml = "";
        
        AuthRequestMessageBuilder(ApplicationLink appLink)
        {
            this.appLink = appLink;
        }

        public MessageBuilder format(MessageFormat format)
        {
            this.format = checkNotNull(format, "format");
            return this;
        }   
        
        public MessageBuilder contentHtml(String contentHtml)
        {
            this.contentHtml = checkNotNull(contentHtml, "contentHtml");
            return this;
        }
        
        public String getHtml()
        {
            String template, messageHtml;
            
            String applinkId = appLink.getId().toString(),
                   appName = appLink.getName(),
                   appUri = appLink.getDisplayUrl().toString(),
                   authUri = appLink.createAuthenticatedRequestFactory().getAuthorisationURI().toString();
            
            switch (format)
            {
                case INLINE:
                    template = REQUEST_INLINE_TEMPLATE;
                    messageHtml = i18nResolver.getText("applinks.util.auth.request.inline",
                                                       StringEscapeUtils.escapeHtml(authUri));
                    break;
                    
                default:
                    template = REQUEST_BANNER_TEMPLATE;
                    messageHtml = i18nResolver.getText("applinks.util.auth.request",
                                                       StringEscapeUtils.escapeHtml(authUri),
                                                       StringEscapeUtils.escapeHtml(appUri),
                                                       StringEscapeUtils.escapeHtml(appName));
            }
            // Note, the reason we don't just use i18nResolver.getText in the templates instead of this
            // indirect messageHtml mechanism is that the i18n string contains link tags which we don't
            // want to get HTML-escaped.
    
            StringWriter buf = new StringWriter();
            ImmutableMap.Builder<String, Object> contextBuilder = ImmutableMap.<String, Object>builder()
                    .put("applinkId", applinkId)
                    .put("appName", appName)
                    .put("appUri", appUri)
                    .put("authUri", authUri)
                    .put("messageHtml", messageHtml)
                    .put("contentHtml", contentHtml);

            try
            {
                templateRenderer.render(template, contextBuilder.build(), buf);
            }
            catch (IOException e)
            {
            }
            return buf.toString();
        }
    }
}
