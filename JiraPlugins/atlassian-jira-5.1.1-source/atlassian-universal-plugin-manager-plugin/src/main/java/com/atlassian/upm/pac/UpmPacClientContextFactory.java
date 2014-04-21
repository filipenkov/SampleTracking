package com.atlassian.upm.pac;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.plugins.client.service.ClientContextFactory;
import com.atlassian.plugins.service.ClientContext;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.license.internal.HostApplicationDescriptor;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.atlassian.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class UpmPacClientContextFactory implements ClientContextFactory
{
    private static final Logger log = getLogger(UpmPacClientContextFactory.class);
    
    private static final String DISABLE_DATA_COLLECTION_PROPERTY = "atlassian.upm.server.data.disable";
    private static final String UPM_CLIENT_TYPE = "upm";
    private static final int CONTEXT_UPDATE_MILLISECONDS = 24 * 60 * 60 * 1000;
    
    private final ApplicationProperties applicationProperties;
    private final HostApplicationDescriptor hostApplicationDescriptor;
    private final HostLicenseProvider hostLicenseProvider;
    private final ResettableLazyReference<ClientContext> context;
    private final AtomicReference<DateTime> nextContextUpdateDate;
    private final boolean disableDataCollection;

    public UpmPacClientContextFactory(ApplicationProperties applicationProperties,
                                      HostApplicationDescriptor hostApplicationDescriptor,
                                      HostLicenseProvider hostLicenseProvider)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.hostApplicationDescriptor = checkNotNull(hostApplicationDescriptor, "hostApplicationDescriptor");
        this.hostLicenseProvider = checkNotNull(hostLicenseProvider, "hostLicenseProvider");
        
        this.context = new ResettableLazyReference<ClientContext>()
        {
            @Override
            protected ClientContext create() throws Exception
            {
                return createContext();
            }
        };
        this.nextContextUpdateDate = new AtomicReference<DateTime>(new DateTime());
        
        disableDataCollection = Boolean.parseBoolean(System.getProperty(DISABLE_DATA_COLLECTION_PROPERTY));
    }
    
    @Override
    public ClientContext getClientContext()
    {
        final DateTime now = new DateTime();
        final DateTime nextUpdate = now.plusMillis(CONTEXT_UPDATE_MILLISECONDS);
        DateTime updated = nextContextUpdateDate.update(new Function<DateTime, DateTime>()
        {
            public DateTime get(DateTime input)
            {
                return now.isBefore(input) ? input : nextUpdate;
            }            
        });
        if (updated == nextUpdate)
        {
            context.reset();
        }
        
        return context.get();
    }
    
    private ClientContext createContext()
    {
        log.debug("Refreshing product license/user information");

        ClientContext.Builder builder = new ClientContext.Builder()
                .clientType(UPM_CLIENT_TYPE)
                .productName(applicationProperties.getDisplayName())
                .productVersion(applicationProperties.getVersion());
        
        if (!disableDataCollection)
        {
            String sen = null;
            String serverId = null;
            Boolean evaluation = null;
            for (ProductLicense productLicense: hostLicenseProvider.getHostApplicationLicense())
            {
                sen = productLicense.getSupportEntitlementNumber();
                serverId = productLicense.getServerId();
                evaluation = productLicense.isEvaluation();
                // If there are multiple licenses (Fe + Cru), just get these properties from one of them;
                // they should be the same, but there's no meaningful way to choose one anyway.
                break;
            }
            builder.productEvaluation(evaluation)
                    .sen(sen)
                    .serverId(serverId)
                    .userCount(hostApplicationDescriptor.getActiveUserCount());
        }
        
        return builder.build();
    }
}
