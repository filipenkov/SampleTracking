package com.atlassian.gadgets.renderer.internal;

import com.google.inject.Singleton;

import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.oauth.GadgetOAuthCallbackGenerator;
import org.apache.shindig.gadgets.oauth.OAuthCallbackGenerator;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthResponseParams;
import org.apache.shindig.gadgets.oauth.OAuthResponseParams.OAuthRequestException;

/**
 * Implementation of the OAuthCallbackGenerator that ignores the "dance" that the {@link GadgetOAuthCallbackGenerator}
 * does because we can't redirect to gadget specific callbacks and aren't implementing locked domains.
 */
@Singleton
public class AtlassianOAuthCallbackGenerator implements OAuthCallbackGenerator
{
    public String generateCallback(OAuthFetcherConfig fetcherConfig,
            String baseCallback,
            HttpRequest request,
            OAuthResponseParams responseParams) 
            throws OAuthRequestException
    {
        return baseCallback;
    }
}
