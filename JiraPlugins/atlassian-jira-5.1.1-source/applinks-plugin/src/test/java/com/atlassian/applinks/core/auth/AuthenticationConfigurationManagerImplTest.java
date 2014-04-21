package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.api.event.ApplicationLinkAuthConfigChangedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkEvent;
import com.atlassian.applinks.core.link.DefaultApplicationLink;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.sal.api.net.Response;
import org.apache.poi.hssf.record.formula.functions.T;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.event.api.EventPublisher;
import org.mockito.ArgumentMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class AuthenticationConfigurationManagerImplTest
{
    ApplicationLinkService applicationLinkService;
    PropertyService propertyService;
    AuthenticationConfigurationManager manager;
    EventPublisher eventPublisher;
    
    @Before
    public void setUp()
    {
        applicationLinkService = mock(ApplicationLinkService.class);
        propertyService = mock(PropertyService.class);
        eventPublisher = mock(EventPublisher.class);
        manager = new AuthenticationConfigurationManagerImpl(applicationLinkService, propertyService, eventPublisher);
    }

    class IsCorrectEvent<T extends ApplicationLinkEvent> extends ArgumentMatcher
    {
        private final ApplicationId applicationId;

        public IsCorrectEvent(ApplicationId applicationId)
        {
            this.applicationId = applicationId;
        }

        public boolean matches(Object event) {
            return ((T) event).getApplicationId().equals(applicationId);
        }
    }

    @Test
    public void testEventPublishedWhenProviderRegistered() throws Exception
    {
        ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        Map<String, String> config = new HashMap<String, String>();
        ApplicationLink applicationLink = new DefaultApplicationLink(applicationId, null, null, null, null);
        when(applicationLinkService.getApplicationLink(applicationId)).thenReturn(applicationLink);
        ApplicationLinkProperties applicationLinkProperties = mock(ApplicationLinkProperties.class);
        when(propertyService.getApplicationLinkProperties(applicationId)).thenReturn(applicationLinkProperties);
        manager.registerProvider(applicationId, BasicAuthenticationProvider.class, config);
        verify(applicationLinkProperties).setProviderConfig(BasicAuthenticationProvider.class.getName(), config);
        verify(eventPublisher).publish(argThat(new IsCorrectEvent<ApplicationLinkAuthConfigChangedEvent>(applicationId)));
    }

    
    @Test
    public void testEventPublishedWhenProviderUnregistered() throws TypeNotInstalledException
    {
        ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        ApplicationLink applicationLink = new DefaultApplicationLink(applicationId, null, null, null, null);
        when(applicationLinkService.getApplicationLink(applicationId)).thenReturn(applicationLink);
        ApplicationLinkProperties applicationLinkProperties = mock(ApplicationLinkProperties.class);
        when(propertyService.getApplicationLinkProperties(applicationId)).thenReturn(applicationLinkProperties);
        manager.unregisterProvider(applicationId, BasicAuthenticationProvider.class);
        verify(applicationLinkProperties).removeProviderConfig(BasicAuthenticationProvider.class.getName());
        verify(eventPublisher).publish(argThat(new IsCorrectEvent<ApplicationLinkAuthConfigChangedEvent>(applicationId)));
    }
}
