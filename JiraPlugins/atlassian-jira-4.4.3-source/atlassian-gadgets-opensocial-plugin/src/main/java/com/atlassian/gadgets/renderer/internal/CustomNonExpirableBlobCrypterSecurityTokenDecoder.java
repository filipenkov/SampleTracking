package com.atlassian.gadgets.renderer.internal;

import org.apache.shindig.common.crypto.BlobCrypter;
import org.springframework.beans.factory.annotation.Qualifier;

public class CustomNonExpirableBlobCrypterSecurityTokenDecoder extends CustomBlobCrypterSecurityTokenDecoder
{
    public CustomNonExpirableBlobCrypterSecurityTokenDecoder(@Qualifier("nonExpirableBlobCrypter") BlobCrypter crypter,
                                                             ContainerDomainProvider domainProvider)
    {
        super(crypter, domainProvider);
    }
}