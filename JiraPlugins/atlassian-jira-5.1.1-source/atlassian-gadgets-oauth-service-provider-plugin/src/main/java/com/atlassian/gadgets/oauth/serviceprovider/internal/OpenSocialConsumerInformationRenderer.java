package com.atlassian.gadgets.oauth.serviceprovider.internal;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.oauth.serviceprovider.ConsumerInformationRenderException;
import com.atlassian.oauth.serviceprovider.ConsumerInformationRenderer;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.util.Check;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.atlassian.gadgets.oauth.serviceprovider.internal.OpenSocial.XOAUTH_APP_URL;

public class OpenSocialConsumerInformationRenderer implements ConsumerInformationRenderer
{
    private final Log logger = LogFactory.getLog(getClass());

    private final TemplateRenderer renderer;
    private final GadgetSpecFactory gadgetSpecFactory;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final UserManager userManager;
    private final I18nResolver i18nResolver;

    public OpenSocialConsumerInformationRenderer(TemplateRenderer renderer,
            GadgetSpecFactory gadgetSpecFactory,
            GadgetRequestContextFactory gadgetRequestContextFactory,
            UserManager userManager,
            I18nResolver i18nResolver)
    {
        this.renderer = Check.notNull(renderer, "renderer");
        this.gadgetSpecFactory = Check.notNull(gadgetSpecFactory, "gadgetSpecFactory");
        this.gadgetRequestContextFactory = Check.notNull(gadgetRequestContextFactory, "gadgetRequestContextFactory");
        this.userManager = Check.notNull(userManager, "userManager");
        this.i18nResolver = Check.notNull(i18nResolver, "i18nResolver");
    }

    public boolean canRender(ServiceProviderToken token, HttpServletRequest request)
    {
        return token.hasProperty(XOAUTH_APP_URL);
    }

    public void render(ServiceProviderToken token, HttpServletRequest request, Writer writer) throws IOException
    {
        final Map<String, Object> context = new HashMap<String, Object>();

        GadgetSpec spec;
        try
        {
            spec = getGadgetSpec(token, request);
            context.put("gadgetSpec", spec);
        }
        catch (ConsumerInformationRenderException e)
        {
            final String gadgetUri = token.getProperty(XOAUTH_APP_URL);
            logger.warn("Error parsing gadget from '" + gadgetUri + "'.", e);

            context.put("gadgetRetrievalError", true);
            context.put("gadgetUri", gadgetUri);
        }
        context.put("consumer", token.getConsumer());


        // Default the user's full name to "User Unknown" in case it is not found
        String userFullName;
        String username = userManager.getRemoteUsername();
        if (StringUtils.isNotBlank(username))
        {
            UserProfile profile = userManager.getUserProfile(username);
            if (profile != null && StringUtils.isNotBlank(profile.getFullName()))
            {
                userFullName = profile.getFullName();
            }
            else
            {
                userFullName = username;
            }
        }
        else
        {
            userFullName = i18nResolver.getText("com.atlassian.gadgets.oauth.serviceprovider.authorize.user.not.found");
        }
        context.put("userFullName", userFullName);

        try
        {
            renderer.render("opensocial-consumer-info.vm", context, writer);
        }
        catch (RenderingException e)
        {
            throw new ConsumerInformationRenderException("Could not render consumer information", e);
        }
    }

    /**
     * Returns the gadget spec for the token and request.
     * 
     * @param token token being authorized
     * @param request users request
     * @return gadget spec for the app acting as a consumer
     */
    private GadgetSpec getGadgetSpec(ServiceProviderToken token, HttpServletRequest request)
    {
        GadgetSpec spec;
        try
        {
            spec = gadgetSpecFactory.getGadgetSpec(
                URI.create(token.getProperty(XOAUTH_APP_URL)),
                gadgetRequestContextFactory.get(request)
            );
        }
        catch (GadgetParsingException e)
        {
            throw new ConsumerInformationRenderException("Parsing of the OpenSocial gadget failed", e);
        }
        return spec;
    }
}
