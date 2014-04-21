package com.atlassian.gadgets.renderer.internal.http;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.ContentFetcherFactory;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.RemoteContentFetcherFactory;
import org.apache.shindig.gadgets.oauth.OAuthFetcherFactory;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Adds Trusted Application headers to all requests.
 */
@Singleton
public class TrustedAppContentFetcherFactory extends ContentFetcherFactory
{
    private final TrustedApplicationsManager trustedAppsManager;
    private final UserManager userManager;

    @Inject
    public TrustedAppContentFetcherFactory(RemoteContentFetcherFactory remoteContentFetcherFactory,
            OAuthFetcherFactory oauthFetcherFactory,
            TrustedApplicationsManager trustedAppsManager,
            UserManager userManager)
    {
        super(remoteContentFetcherFactory, oauthFetcherFactory);
        this.trustedAppsManager = trustedAppsManager;
        this.userManager = userManager;
    }
    
    @Override
    public HttpResponse fetch(HttpRequest request) throws GadgetException
    {
        addTrustedAppHeaders(request, userManager.getRemoteUsername());
        return super.fetch(request);
    }

    private void addTrustedAppHeaders(HttpRequest request, String username)
    {
        EncryptedCertificate userCertificate = createCertificate(username);
        
        if (userCertificate!=null && !isBlank(userCertificate.getID()))
        {
            request.setHeader(TrustedApplicationUtils.Header.Request.ID, userCertificate.getID());
            request.setHeader(TrustedApplicationUtils.Header.Request.SECRET_KEY, userCertificate.getSecretKey());
            request.setHeader(TrustedApplicationUtils.Header.Request.CERTIFICATE, userCertificate.getCertificate());
        }
    }

    private EncryptedCertificate createCertificate(String username)
    {
        if (username != null && !username.equals(""))
        {
            return trustedAppsManager.getCurrentApplication().encode(username);
        }
        else
        {
            return null;
        }
    }
}
