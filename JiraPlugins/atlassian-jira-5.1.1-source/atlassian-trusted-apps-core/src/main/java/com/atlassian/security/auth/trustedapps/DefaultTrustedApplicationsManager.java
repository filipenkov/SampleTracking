package com.atlassian.security.auth.trustedapps;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation. Not thread safe.
 */
public class DefaultTrustedApplicationsManager implements TrustedApplicationsManager
{
    protected final CurrentApplication application;
    protected final Map<String, TrustedApplication> trustedApps;

    public DefaultTrustedApplicationsManager(final CurrentApplication application, final Map<String, TrustedApplication> trustedApps)
    {
        Null.not("application", application);
        Null.not("trustedApps", trustedApps);

        this.application = application;
        this.trustedApps = trustedApps;
    }

    // ////CLOVER:OFF
    public DefaultTrustedApplicationsManager()
    {
        this(new BouncyCastleEncryptionProvider());
    }

    // /CLOVER:ON

    public DefaultTrustedApplicationsManager(final EncryptionProvider encryptionProvider)
    {
        try
        {
            final KeyPair keyPair = encryptionProvider.generateNewKeyPair();
            Null.not("keyPair", keyPair);

            application = new DefaultCurrentApplication(encryptionProvider, keyPair.getPublic(), keyPair.getPrivate(),
                encryptionProvider.generateUID());
            trustedApps = new HashMap<String, TrustedApplication>();
        }
        catch (final NoSuchAlgorithmException e)
        {
            throw new AssertionError(e);
        }
        catch (final NoSuchProviderException e)
        {
            throw new AssertionError(e);
        }
    }

    public CurrentApplication getCurrentApplication()
    {
        return application;
    }

    public TrustedApplication getTrustedApplication(final String id)
    {
        return trustedApps.get(id);
    }
}
