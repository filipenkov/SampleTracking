package com.atlassian.gadgets.renderer.internal;

import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.springframework.beans.factory.annotation.Qualifier;

class UpdatableBlobCrypterSecurityToken extends BlobCrypterSecurityToken
{
    public UpdatableBlobCrypterSecurityToken(@Qualifier("blobCrypter") BlobCrypter crypter,
                                             String container, String domain)
    {
        super(crypter, container, domain);
    }

    @Override
    public String getUpdatedToken()
    {
        try
        {
            // just re-encrypt it which sets a new timestamp
            return encrypt();
        }
        catch (BlobCrypterException e)
        {
            // if we fail for some reason, just return null which indicates no update
            return null;
        }
    }
}
