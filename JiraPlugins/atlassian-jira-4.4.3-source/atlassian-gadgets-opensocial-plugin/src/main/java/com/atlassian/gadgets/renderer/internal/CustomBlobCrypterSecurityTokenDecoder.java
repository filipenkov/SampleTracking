package com.atlassian.gadgets.renderer.internal;

import java.util.Map;

import com.atlassian.gadgets.renderer.internal.AtlassianContainerConfig.Containers;

import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.auth.SecurityTokenException;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class CustomBlobCrypterSecurityTokenDecoder implements SecurityTokenDecoder
{
    private final BlobCrypter crypter;
    private final ContainerDomainProvider domainProvider;

    public CustomBlobCrypterSecurityTokenDecoder(@Qualifier("blobCrypter") BlobCrypter crypter,
                                                 ContainerDomainProvider domainProvider)
    {
        this.domainProvider = domainProvider;
        this.crypter = crypter; 
    }

    /**
     * Decrypt and verify the provided security token.
     */
    public SecurityToken createToken(Map<String, String> tokenParameters) throws SecurityTokenException
    {
        String token = tokenParameters.get(SecurityTokenDecoder.SECURITY_TOKEN_NAME);
        if (isEmpty(token))
        {
            // No token is present, assume anonymous access
            return null;
        }
        String[] fields = token.split(":");
        if (fields.length != 2)
        {
            throw new SecurityTokenException("Invalid security token " + token);
        }
        String container = fields[0];
        if (!Containers.ATLASSIAN.equals(container) && !Containers.DEFAULT.equals(container))
        {
            throw new SecurityTokenException("Unknown container " + token);
        }
        String crypted = fields[1];
        String activeUrl = tokenParameters.get(SecurityTokenDecoder.ACTIVE_URL_NAME);
        String domain = domainProvider.getDomain();
        try
        {
            return decrypt(crypter, container, domain, crypted, activeUrl);
        }
        catch (BlobCrypterException e)
        {
            throw new SecurityTokenException(e);
        }
    }
    
    private static final int MAX_TOKEN_LIFETIME_SECS = 3600;

    private static final String OWNER_KEY = "o";
    private static final String VIEWER_KEY = "v";
    private static final String GADGET_KEY = "g";
    private static final String GADGET_INSTANCE_KEY = "i";
    private static final String TRUSTED_JSON_KEY = "j";

    /**
     * Decrypt and verify a token.  Note this is not public, use BlobCrypterSecurityTokenDecoder
     * instead.
     * 
     * <p>Copied from {@link BlobCrypterSecurityToken} because it's decrypt method has package-only access.
     * 
     * @param crypter crypter to use for decryption
     * @param container container that minted the token
     * @param domain oauth_consumer_key to use for signed fetch with default key
     * @param token the encrypted token (just the portion after the first ":")
     * @return the decrypted, verified token.
     * 
     * @throws BlobCrypterException
     */
    static BlobCrypterSecurityToken decrypt(BlobCrypter crypter, String container, String domain, String token,
            String activeUrl) throws BlobCrypterException
    {
        Map<String, String> values = crypter.unwrap(token, MAX_TOKEN_LIFETIME_SECS);
        BlobCrypterSecurityToken t = new UpdatableBlobCrypterSecurityToken(crypter, container, domain);
        t.setOwnerId(values.get(OWNER_KEY));
        t.setViewerId(values.get(VIEWER_KEY));
        t.setAppUrl(values.get(GADGET_KEY));
        t.setActiveUrl(activeUrl);
        String moduleId = values.get(GADGET_INSTANCE_KEY);
        if (moduleId != null)
        {
            t.setModuleId(moduleId);
        }
        t.setTrustedJson(values.get(TRUSTED_JSON_KEY));
        return t;
    }
}
