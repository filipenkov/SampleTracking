package com.atlassian.gadgets.renderer.internal.servlet;

import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.gadgets.servlet.OAuthCallbackServlet;
import org.springframework.beans.factory.annotation.Qualifier;

public class AtlassianOAuthCallbackServlet extends OAuthCallbackServlet
{
    public AtlassianOAuthCallbackServlet(@Qualifier("blobCrypter") BlobCrypter stateCrypter)
    {
        super.setStateCrypter(stateCrypter);
    }
}
