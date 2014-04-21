package com.atlassian.gadgets.oauth.serviceprovider.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.oauth.Request;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.TokenPropertiesFactory;
import com.atlassian.oauth.util.Check;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.atlassian.gadgets.oauth.serviceprovider.internal.OpenSocial.OPENSOCIAL_APP_URL;
import static com.atlassian.gadgets.oauth.serviceprovider.internal.OpenSocial.XOAUTH_APP_URL;

public class OpenSocialTokenPropertiesFactory implements TokenPropertiesFactory
{
    private final Log logger = LogFactory.getLog(getClass());
    
    private final GadgetSpecFactory gadgetSpecFactory;

    public OpenSocialTokenPropertiesFactory(GadgetSpecFactory gadgetSpecFactory)
    {
        this.gadgetSpecFactory = Check.notNull(gadgetSpecFactory, "gadgetSpecFactory");
    }
    
    public Map<String, String> newRequestTokenProperties(Request request)
    {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        String appUrl = getAppUrl(request);
        if (appUrl != null)
        {
            // AG-1399 - Make sure we have a valid URL before we save it.
            try
            {
                URI uri = URI.create(appUrl.trim());
                builder.put(XOAUTH_APP_URL, uri.toASCIIString());
            }
            catch (IllegalArgumentException e)
            {
                logger.warn("appUrl is not a valid URI: " + appUrl, e);
            }
        }
        return builder.build();
    }
    
    public Map<String, String> newAccessTokenProperties(ServiceProviderToken requestToken)
    {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        String appUrl = requestToken.getProperty(XOAUTH_APP_URL);
        if (appUrl != null)
        {
            try
            {
                builder.put(ALTERNAME_CONSUMER_NAME, getGadgetName(appUrl));
            }
            catch (IllegalArgumentException e)
            {
                // the app url is invalid or the gadget spec is invalid
            }
        }
        return builder.build();
    }

    private String getGadgetName(String appUrl)
    {
        try
        {
            GadgetSpec spec = gadgetSpecFactory.getGadgetSpec(new URI(appUrl), GadgetRequestContext.NO_CURRENT_REQUEST);
            return spec.getDirectoryTitle() != null ? spec.getDirectoryTitle() : spec.getTitle();
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("appUrl is not a valid URI");
        }
        catch (GadgetParsingException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.warn("Unable to parse gadget spec", e);
            }
            else
            {
                logger.warn("Unable to parse gadget spec at '" + appUrl + "': " + e.getMessage());
            }
            throw new IllegalArgumentException("Unable to parse gadget spec");
        }
    }

    /**
     * Returns the value of the {@code xoauth_app_url} or {@code opensocial_app_url} if one or the other is present.
     * If both are present, the value of the {@code xoauth_app_url} parameter is returned.  If neither is present,
     * {@code null} is returned.
     * 
     * @param message {@code OAuthMessage} to check for the {@code xoauth_app_url} or {@code opensocial_app_url} parameters
     * @return the value of the {@code xoauth_app_url} or {@code opensocial_app_url} if one or the other is present
     */
    private String getAppUrl(Request request)
    {
        if (request.getParameter(XOAUTH_APP_URL) != null)
        {
            return request.getParameter(XOAUTH_APP_URL);
        }
        else if (request.getParameter(OPENSOCIAL_APP_URL) != null)
        {
            return request.getParameter(OPENSOCIAL_APP_URL);
        }
        else
        {
            return null;
        }
    }
}
