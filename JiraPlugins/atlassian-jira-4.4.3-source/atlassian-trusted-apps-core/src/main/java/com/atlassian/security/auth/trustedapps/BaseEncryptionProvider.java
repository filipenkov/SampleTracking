package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.ApplicationRetriever.RetrievalException;

/**
 * Base class for encryption provider, provides methods that are not crypto-specific
 */
public abstract class BaseEncryptionProvider implements EncryptionProvider
{
    public Application getApplicationCertificate(String baseUrl) throws RetrievalException
    {
        return new URLApplicationRetriever(baseUrl, this).getApplication();
    }

    // ///CLOVER:OFF
    public String generateUID()
    {
        return UIDGenerator.generateUID();
    }
    // /CLOVER:ON
}
