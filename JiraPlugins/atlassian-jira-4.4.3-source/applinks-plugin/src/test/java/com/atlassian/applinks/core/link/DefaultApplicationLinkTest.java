package com.atlassian.applinks.core.link;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.PropertySet;
import com.atlassian.applinks.api.event.ApplicationLinkDetailsChangedEvent;
import com.atlassian.applinks.core.MockEventPublisher;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestFactoryFactory;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutableApplicationLink;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultApplicationLinkTest
{
    private ApplicationType applicationType;
    private PropertyService propertyService;
    private ApplicationLinkProperties applicationLinkProperties;
    private ApplicationLinkRequestFactoryFactory requestFactoryFactory;
    private ApplicationId serverId;
    private MockEventPublisher eventPublisher;

    @Before
    public void setUp()
    {
        applicationType = new ApplicationType()
        {
            public String getI18nKey()
            {
                return "some.other.i18n.key";
            }

            public Class getTypeClass()
            {
                return getClass();
            }

            public URI getIconUrl()
            {
                return null;
            }
        };
        propertyService = mock(PropertyService.class);
        applicationLinkProperties = mock(ApplicationLinkProperties.class);
        requestFactoryFactory = mock(ApplicationLinkRequestFactoryFactory.class);
        serverId = new ApplicationId("37d1c667-88ec-43d1-a959-67dc28cbc831");
        eventPublisher = new MockEventPublisher();
    }
    
    private DefaultApplicationLink createApplicationLink()
    {
        return new DefaultApplicationLink(
                serverId, applicationType, applicationLinkProperties, requestFactoryFactory, eventPublisher);
    }

    @Test
    public void testGetApplicationLinkProperties() throws Exception
    {
        final ApplicationLink applicationLink = createApplicationLink();
        when(applicationLinkProperties.getName()).thenReturn("BOB");
        when(applicationLinkProperties.getDisplayUrl()).thenReturn(new URI("http://mysystem:8080/app/display"));
        when(applicationLinkProperties.getRpcUrl()).thenReturn(new URI("http://mysystem:8080/app/rpc"));
        when(applicationLinkProperties.isPrimary()).thenReturn(false);

        assertEquals(applicationType, applicationLink.getType());
        assertEquals(serverId, applicationLink.getId());
        assertEquals("BOB", applicationLink.getName());
        assertEquals(new URI("http://mysystem:8080/app/display"), applicationLink.getDisplayUrl());
        assertEquals(new URI("http://mysystem:8080/app/rpc"), applicationLink.getRpcUrl());
    }

    @Test
    public void testAddCustomProperty() throws Exception
    {
        final ApplicationLink applicationLink = createApplicationLink();
        final PropertySet propertySet = mock(PropertySet.class);
        when(propertyService.getProperties(applicationLink)).thenReturn(propertySet);
        applicationLink.putProperty("customProperty", "test");
        verify(applicationLinkProperties).putProperty("customProperty", "test");
    }

    @Test
    public void testRemoveCustomProperty() throws Exception
    {
        final ApplicationLink applicationLink = createApplicationLink();
        final PropertySet propertySet = mock(PropertySet.class);
        when(propertyService.getProperties(applicationLink)).thenReturn(propertySet);
        applicationLink.removeProperty("customProperty");
        verify(applicationLinkProperties).removeProperty("customProperty");
    }
    
    @Test
    public void testUpdateRaisesApplicationLinkDetailsChangedEvent() throws Exception
    {
        final MutableApplicationLink applicationLink = createApplicationLink();
        when(applicationLinkProperties.getName()).thenReturn("BOB");
        when(applicationLinkProperties.getDisplayUrl()).thenReturn(new URI("http://mysystem:8080/app/display"));
        when(applicationLinkProperties.getRpcUrl()).thenReturn(new URI("http://mysystem:8080/app/rpc"));
        when(applicationLinkProperties.isPrimary()).thenReturn(false);
        final ApplicationLinkDetails details = ApplicationLinkDetails.builder(applicationLink).build();
        
        applicationLink.update(details);
        assertEquals(applicationLink.getId(), eventPublisher.getLastFired(ApplicationLinkDetailsChangedEvent.class).getApplicationId());
    }
}
