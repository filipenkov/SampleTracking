package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.NotNull;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.DefaultCurrentApplication;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v3.12
 */
public class DefaultCurrentApplicationFactory implements CurrentApplicationFactory
{
    static final class Keys
    {
        static final String PRIVATE_KEY_DATA = "jira.trustedapp.key.private.data";
        static final String PUBLIC_KEY_DATA = "jira.trustedapp.key.public.data";
        static final String UID = "jira.trustedapp.uid";
    }

    private final LazyReference<CurrentApplication> currentApplicationRef;

    public DefaultCurrentApplicationFactory(final @NotNull ApplicationProperties applicationProperties, final @NotNull JiraLicenseService jiraLicenseService)
    {
        currentApplicationRef = new LazyReference<CurrentApplication>()
        {
            @Override
            protected CurrentApplication create() throws Exception
            {
                return getOrCreateCurrentApplication(notNull("applicationProperties", applicationProperties), notNull("jiraLicenseService", jiraLicenseService));
            }
        };
    }

    public CurrentApplication getCurrentApplication()
    {
        return currentApplicationRef.get();
    }

    private static DefaultCurrentApplication getOrCreateCurrentApplication(final ApplicationProperties applicationProperties, final JiraLicenseService jiraLicenseService)
    {
        final String privateKeyData = applicationProperties.getText(Keys.PRIVATE_KEY_DATA);
        final String publicKeyData = applicationProperties.getText(Keys.PUBLIC_KEY_DATA);

        final PrivateKey privateKey;
        final PublicKey publicKey;
        if (StringUtils.isBlank(privateKeyData))
        {
            final KeyPair keyPair = generateNewKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            applicationProperties.setText(Keys.PRIVATE_KEY_DATA, KeyFactory.encode(privateKey));
            applicationProperties.setText(Keys.PUBLIC_KEY_DATA, KeyFactory.encode(publicKey));
        }
        else
        {
            privateKey = KeyFactory.getPrivateKey(privateKeyData);
            publicKey = KeyFactory.getPublicKey(publicKeyData);
        }

        String uid = applicationProperties.getString(Keys.UID);
        if (StringUtils.isBlank(uid))
        {
            uid = new UIDGenerator().generateUID(jiraLicenseService);
            applicationProperties.setString(Keys.UID, uid);
        }

        return new DefaultCurrentApplication(KeyFactory.getEncryptionProvider(), publicKey, privateKey, uid);
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
        final Random random = new Random();

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
            serverId = (serverId != null) ? serverId : String.valueOf(random.nextLong());
            // Grab the first 4 bytes of the hashed SID and convert it to an integer
            final byte[] idHash = ArrayUtils.subarray(DigestUtils.md5(serverId), 0, 3);
            return "jira:" + new BigInteger(1, idHash).intValue();
        }
    }
}