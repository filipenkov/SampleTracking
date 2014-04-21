package com.atlassian.gadgets.renderer.internal;

import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.renderer.internal.AtlassianContainerConfig.Containers;
import com.atlassian.gadgets.view.SecurityTokenFactory;
import com.atlassian.sal.api.ApplicationProperties;

import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.springframework.beans.factory.annotation.Qualifier;

import static com.atlassian.gadgets.util.Uri.resolveUriAgainstBase;

public class SecurityTokenFactoryImpl implements SecurityTokenFactory
{
    private final BlobCrypter crypter;
    private final ContainerDomainProvider domainProvider;
    private final ApplicationProperties applicationProperties;

    public SecurityTokenFactoryImpl(@Qualifier("blobCrypter") BlobCrypter crypter,
                                    ContainerDomainProvider domainProvider,
                                    ApplicationProperties applicationProperties)
    {
        this.crypter = crypter;
        this.domainProvider = domainProvider;
        this.applicationProperties = applicationProperties;
    }
    
    public String newSecurityToken(GadgetState state, String viewer)
    {
        BlobCrypterSecurityToken token = new UpdatableBlobCrypterSecurityToken(crypter, Containers.ATLASSIAN, domainProvider.getDomain());
        if (viewer != null)
        {
            // TODO change to module id when AG-396 is implemented
            token.setModuleId(state.getId().value());
            // for now just use the viewer as the owner, should we add a owner property on the GadgetState object?
            token.setOwnerId(viewer);
            token.setViewerId(viewer);
        }
        
        token.setAppUrl(absoluteGadgetUrl(state));
        try
        {
            return token.encrypt();
        }
        catch (BlobCrypterException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String absoluteGadgetUrl(GadgetState state)
    {
        return resolveUriAgainstBase(applicationProperties.getBaseUrl(), state.getGadgetSpecUri()).toASCIIString();
    }
}
