package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.DefaultCurrentApplication;
import com.atlassian.security.random.DefaultSecureRandomService;
import com.atlassian.security.random.SecureRandomService;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @since v3.12
 */
@EventComponent
public class DefaultCurrentApplicationStore implements CurrentApplicationStore
{
    private static final class Keys
    {
        private static final String PRIVATE_KEY_DATA = "jira.trustedapp.key.private.data";
        private static final String PUBLIC_KEY_DATA = "jira.trustedapp.key.public.data";
        private static final String UID = "jira.trustedapp.uid";
    }

    private final ApplicationProperties applicationProperties;
    private final ResettableLazyReference<Pair<KeyPair, CurrentApplication>> cache;
    private final JiraLicenseService licenseService;

    public DefaultCurrentApplicationStore(final ApplicationProperties applicationProperties,
            final JiraLicenseService jiraLicenseService)
    {
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.licenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.cache = new ResettableLazyReference<Pair<KeyPair, CurrentApplication>>()
        {
            @Override
            protected Pair<KeyPair, CurrentApplication> create()
            {
                return getOrCreateCurrentApplication();
            }
        };
    }

    @EventListener
    public void onClearCache(ClearCacheEvent event)
    {
        cache.reset();
    }

    public CurrentApplication getCurrentApplication()
    {
        return cache.get().second();
    }

    @Override
    public KeyPair getKeyPair()
    {
        return cache.get().first();
    }

    @Override
    public synchronized void setCurrentApplication(String applicationId, KeyPair pair)
    {
        notBlank("applicationId cannot be blank.", applicationId);
        Assertions.notNull("pair cannot be null.", pair);
        Assertions.notNull("pair.private cannot be null.", pair.getPrivate());
        Assertions.notNull("pair.public cannot be null.", pair.getPublic());

        applicationProperties.setText(Keys.PRIVATE_KEY_DATA, KeyFactory.encode(pair.getPrivate()));
        applicationProperties.setText(Keys.PUBLIC_KEY_DATA, KeyFactory.encode(pair.getPublic()));
        applicationProperties.setString(Keys.UID, applicationId);
        cache.reset();
    }

    @Override
    public void setCurrentApplication(String applicationId, String publicKey, String privateKey)
    {
        Assertions.notBlank("applicationId cannot be blank.", applicationId);
        Assertions.notNull("publicKey cannot be null.", publicKey);
        Assertions.notNull("privateKey cannot be null.", privateKey);

        PublicKey publicKeyObj = KeyFactory.getPublicKey(publicKey);
        if (publicKeyObj instanceof KeyFactory.InvalidPublicKey)
        {
            throw new IllegalArgumentException("publicKey is not a valid public key.", ((KeyFactory.InvalidKey) publicKeyObj).getCause());
        }

        PrivateKey privateKeyObj = KeyFactory.getPrivateKey(privateKey);
        if (privateKeyObj instanceof KeyFactory.InvalidPrivateKey)
        {
            throw new IllegalArgumentException("privateKey is not a valid private key.", ((KeyFactory.InvalidKey) privateKeyObj).getCause());
        }

        setCurrentApplication(applicationId, new KeyPair(publicKeyObj, privateKeyObj));
    }

    private synchronized Pair<KeyPair, CurrentApplication> getOrCreateCurrentApplication()
    {
        final String privateKeyData = applicationProperties.getText(Keys.PRIVATE_KEY_DATA);
        final String publicKeyData = applicationProperties.getText(Keys.PUBLIC_KEY_DATA);

        final KeyPair keyPair;
        if (isBlank(privateKeyData))
        {
            keyPair = generateNewKeyPair();
            applicationProperties.setText(Keys.PRIVATE_KEY_DATA, KeyFactory.encode(keyPair.getPrivate()));
            applicationProperties.setText(Keys.PUBLIC_KEY_DATA, KeyFactory.encode(keyPair.getPublic()));
        }
        else
        {
            PrivateKey privateKey = KeyFactory.getPrivateKey(privateKeyData);
            PublicKey publicKey = KeyFactory.getPublicKey(publicKeyData);

            keyPair = new KeyPair(publicKey, privateKey);
        }

        String uid = applicationProperties.getString(Keys.UID);
        if (isBlank(uid))
        {
            uid = new UIDGenerator().generateUID(licenseService);
            applicationProperties.setString(Keys.UID, uid);
        }

        CurrentApplication application = new DefaultCurrentApplication(keyPair.getPublic(), keyPair.getPrivate(), uid);
        return Pair.of(keyPair,  application);
    }

    private static KeyPair generateNewKeyPair()
    {
        try
        {
            return KeyFactory.getEncryptionProvider().generateNewKeyPair();
        }
        ///CLOVER:OFF
        catch (final NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (final NoSuchProviderException e)
        {
            throw new RuntimeException(e);
        }
        ///CLOVER:ON
    }

    private static class UIDGenerator
    {
        SecureRandomService secureRandom = DefaultSecureRandomService.getInstance();

        /**
         * Generate the UID. Note that this is dependent on there being a Server ID in the properties. If there isn't,
         * it'll use a random number. So the presence of the Server ID makes the return value stable and idempotent.
         * Without it it will always return something different each time.
         *
         * @param jiraLicenseService the JIRA license service
         * @return a uid.
         */
        String generateUID(final JiraLicenseService jiraLicenseService)
        {
            String serverId = jiraLicenseService.getServerId();
            // don't pass null into the md5 method
            serverId = (serverId != null) ? serverId : String.valueOf(secureRandom.nextLong());
            // Grab the first 4 bytes of the hashed SID and convert it to an integer
            final byte[] idHash = ArrayUtils.subarray(DigestUtils.md5(serverId), 0, 3);
            return "jira:" + new BigInteger(1, idHash).intValue();
        }
    }
}