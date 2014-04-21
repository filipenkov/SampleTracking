package com.atlassian.applinks.core;

import com.atlassian.applinks.api.*;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.applinks.application.confluence.ConfluenceApplicationTypeImpl;
import com.atlassian.applinks.application.jira.JiraApplicationTypeImpl;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestFactoryFactory;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestFactoryFactoryImpl;
import com.atlassian.applinks.core.link.InternalApplicationLink;
import com.atlassian.applinks.core.link.InternalEntityLinkService;
import com.atlassian.applinks.core.manifest.AppLinksManifestDownloader;
import com.atlassian.applinks.core.manifest.ManifestRetrieverDispatcher;
import com.atlassian.applinks.core.plugin.ApplicationTypeModuleDescriptor;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.applinks.core.property.MockPluginSettingsPropertySet;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.applinks.core.rest.client.MockApplicationLinkClient;
import com.atlassian.applinks.core.rest.ui.AuthenticationResource;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.IdentifiableType;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutableApplicationLink;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.net.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultApplicationLinkServiceTest
{
    //TODO test cascading delete of entity links! (including new primary selection)

    private static final String JIRA_I18N = "applinks.jira";
    private static final String CONF_I18N = "applinks.confluence";

    private static JiraApplicationType JIRA_TYPE;

    private static ConfluenceApplicationType CONF_TYPE;

    private static final URI JAC_RPC;
    private static final URI JAC_DISPLAY;
    private static final URI JDAC_RPC;
    private static final URI JDAC_DISPLAY;
    private static final URI CAC_DISPLAY;
    private static final URI CAC_RPC;
    private static final URI EAC_DISPLAY;
    private static final URI EAC_RPC;
    private static final URI GOOGLE_RPC;

    static
    {
        try
        {
            JAC_RPC = new URI("http://tinyurl.com/2g9mqh");
            JAC_DISPLAY = new URI("https://jira.atlassian.com");
            JDAC_RPC = new URI("http://developer.atlassian.com/jira");
            JDAC_DISPLAY = new URI("https://developer.atlassian.com/jira");
            CAC_DISPLAY = new URI("https://confluence.atlassian.com");
            CAC_RPC = new URI("http://localhost:8080/");
            EAC_DISPLAY = new URI("https://extranet.atlassian.com");
            EAC_RPC = new URI("http://localhost:8077/eac");
            GOOGLE_RPC = new URI("http://www.google.com");
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static final ApplicationId JAC_ID = new ApplicationId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final String JAC_NAME = "jac";
    private static final ApplicationId JDAC_ID = new ApplicationId("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final String JDAC_NAME = "JDAC";
    private static final ApplicationId CAC_ID = new ApplicationId("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final String CAC_NAME = "CAC";
    private static final ApplicationId EAC_ID = new ApplicationId("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final String EAC_NAME = "EAC";
    private static final String GOOGLE_NAME = "Google";

    private static final ApplicationLinkDetails JAC_DETAILS = ApplicationLinkDetails.builder().name(JAC_NAME)
            .displayUrl(JAC_DISPLAY).rpcUrl(JAC_RPC).build();
    private static final ApplicationLinkDetails JDAC_DETAILS = ApplicationLinkDetails.builder().name(JDAC_NAME)
            .displayUrl(JDAC_DISPLAY).rpcUrl(JDAC_RPC).build();
    private static final ApplicationLinkDetails CAC_DETAILS = ApplicationLinkDetails.builder().name(CAC_NAME)
            .displayUrl(CAC_DISPLAY).rpcUrl(CAC_RPC).build();
    private static final ApplicationLinkDetails EAC_DETAILS = ApplicationLinkDetails.builder().name(EAC_NAME)
            .displayUrl(EAC_DISPLAY).rpcUrl(EAC_RPC).build();
    private static final ApplicationLinkDetails PRIMARY_LINK_DETAILS = ApplicationLinkDetails.builder().name(GOOGLE_NAME)
            .displayUrl(GOOGLE_RPC).rpcUrl(GOOGLE_RPC).isPrimary(true).build();

    PluginAccessor pluginAccessor;
    ApplicationLinkRequestFactoryFactory requestFactoryFactory;
    PropertyService propertyService;
    InternalEntityLinkService entityLinkService;
    ApplicationTypeModuleDescriptor jiraTypeModuleDescriptor;
    ApplicationTypeModuleDescriptor ConfluenceApplicationTypeModuleDescriptor;
    MockEventPublisher eventPublisher;
    ManifestRetriever manifestRetriever;
    private ApplicationLink mockApplicationLink;
    private InternalHostApplication mockInternalHostApplication;
    private RestUrlBuilder mockRestUrlBuilder;
    private RequestFactory<Request<Request<?, Response>,Response>> mockRequestFactory;
    private AppLinkPluginUtil mockAppLinkPluginUtil;
    private WebResourceManager mockWebResourceManager;

    InternalTypeAccessor typeAccessor;
    DefaultApplicationLinkService service;
    private static final String ID_1 = "11111111-1111-1111-1111-111111111111";
    private static final String ID_2 = "22222222-2222-2222-2222-222222222222";
    private static final String ID_3 = "33333333-3333-3333-3333-333333333333";
    private static final String ID_4 = "44444444-4444-4444-4444-444444444444";
    private static final String ID_5 = "55555555-5555-5555-5555-555555555555";
    private static int applicationIdCount = 5;

    private static final String USERNAME = "john";
    private static final String PASSWORD = "secret";
    private MockPluginSettingsPropertySet globalAdminProperties;

    @Before
    public void setUp()
    {
        manifestRetriever = mock(ManifestRetrieverDispatcher.class);
        pluginAccessor = mock(PluginAccessor.class);
        propertyService = mock(PropertyService.class);
        requestFactoryFactory = mock(ApplicationLinkRequestFactoryFactoryImpl.class);
        entityLinkService = mock(InternalEntityLinkService.class);
        typeAccessor = mock(InternalTypeAccessor.class);
        eventPublisher = new MockEventPublisher();
        mockAppLinkPluginUtil = mock(AppLinkPluginUtil.class);
        mockWebResourceManager = mock(WebResourceManager.class);
        JIRA_TYPE = new JiraApplicationTypeImpl(mockAppLinkPluginUtil, mockWebResourceManager);
        CONF_TYPE = new ConfluenceApplicationTypeImpl(mockAppLinkPluginUtil, mockWebResourceManager);
        jiraTypeModuleDescriptor = mock(ApplicationTypeModuleDescriptor.class);
//        when(jiraTypeModuleDescriptor.getName()).thenReturn(JIRA_TYPE);
        when(jiraTypeModuleDescriptor.getI18nNameKey()).thenReturn(JIRA_I18N);

        ConfluenceApplicationTypeModuleDescriptor = mock(ApplicationTypeModuleDescriptor.class);
//        when(ConfluenceApplicationTypeModuleDescriptor.getName()).thenReturn(CONF_TYPE);
        when(ConfluenceApplicationTypeModuleDescriptor.getI18nNameKey()).thenReturn(CONF_I18N);

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(ApplicationTypeModuleDescriptor.class)).thenReturn(
                new ArrayList<ApplicationTypeModuleDescriptor>()
                {{
                        add(jiraTypeModuleDescriptor);
                        add(ConfluenceApplicationTypeModuleDescriptor);
                    }}
        );

        globalAdminProperties = new MockPluginSettingsPropertySet();
        when(propertyService.getGlobalAdminProperties()).thenReturn(globalAdminProperties);

        mockApplicationLink = mock(ApplicationLink.class);
        mockRestUrlBuilder = mock(RestUrlBuilder.class);
        mockRequestFactory = mock(RequestFactory.class);

        mockInternalHostApplication = mock(InternalHostApplication.class);
        when(mockInternalHostApplication.getId()).thenReturn(CAC_ID);
        when(mockInternalHostApplication.getType()).thenReturn(CONF_TYPE);

        setUpType(JIRA_TYPE);
        setUpType(CONF_TYPE);

        service = new DefaultApplicationLinkService(propertyService, requestFactoryFactory, entityLinkService,
                typeAccessor, new MockApplicationLinkClient(), eventPublisher, mockInternalHostApplication,
                mockRequestFactory, mockRestUrlBuilder, manifestRetriever, null);
    }

    @SuppressWarnings("unchecked")
    private <T extends ApplicationType> void setUpType(final T type)
    {
        when(typeAccessor.loadApplicationType(type.getClass().getName())).thenReturn(type);
        when(typeAccessor.getApplicationType((Class) type.getClass())).thenReturn(type);
        when(typeAccessor.loadApplicationType(((IdentifiableType)type).getId())).thenReturn(type);
    }

    private <T extends ApplicationType> void removeType(final T type)
    {
        when(typeAccessor.loadApplicationType(type.getClass().getName())).thenReturn(null);
        when(typeAccessor.getApplicationType(type.getClass())).thenReturn(null);
        when(typeAccessor.loadApplicationType(((IdentifiableType)type).getId())).thenReturn(null);
    }

    @Test
    public void testApplicationLinkCRUD() throws Exception
    {
        expectAdminPropertySet(JAC_ID);
        service.addApplicationLink(JAC_ID, JIRA_TYPE, JAC_DETAILS);

        assertJac(service.getApplicationLink(JAC_ID));
        assertJac(service.getPrimaryApplicationLink(JIRA_TYPE.getClass()));

        Iterator<ApplicationLink> it = service.getApplicationLinks().iterator();
        assertJac(it.next());
        assertFalse(it.hasNext());

        it = service.getApplicationLinks(JIRA_TYPE.getClass()).iterator();
        assertJac(it.next());
        assertFalse(it.hasNext());

        expectAdminPropertySet(CAC_ID);
        service.addApplicationLink(CAC_ID, CONF_TYPE, CAC_DETAILS);

        try
        {
            final ApplicationType typeX = new ApplicationType()
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
            service.addApplicationLink(new ApplicationId("dddddddd-dddd-dddd-dddd-dddddddddddd"), typeX, CAC_DETAILS);
            fail("Should have thrown TypeNotInstalledException");
        }
        catch (Exception expected)
        {
        }

        assertCac(service.getApplicationLink(CAC_ID));
        assertCac(service.getPrimaryApplicationLink(CONF_TYPE.getClass()));

        assertJac(service.getApplicationLink(JAC_ID));
        assertJac(service.getPrimaryApplicationLink(JIRA_TYPE.getClass()));

        it = service.getApplicationLinks().iterator(); // names are stored in a list, insertion order is maintained
        assertJac(it.next());
        assertCac(it.next());
        assertFalse(it.hasNext());

        it = service.getApplicationLinks(JIRA_TYPE.getClass()).iterator();
        assertJac(it.next());
        assertFalse(it.hasNext());

        it = service.getApplicationLinks(CONF_TYPE.getClass()).iterator();
        assertCac(it.next());
        assertFalse(it.hasNext());

        expectAdminPropertySet(JDAC_ID);
        service.addApplicationLink(JDAC_ID, JIRA_TYPE, JDAC_DETAILS);

        assertJdac(service.getApplicationLink(JDAC_ID));

        assertJac(service.getApplicationLink(JAC_ID));
        assertJac(service.getPrimaryApplicationLink(JIRA_TYPE.getClass()));

        it = service.getApplicationLinks(JIRA_TYPE.getClass()).iterator();
        assertJac(it.next());
        assertJdac(it.next());
        assertFalse(it.hasNext());

        it = service.getApplicationLinks().iterator(); // names are stored in a list, insertion order is maintained
        assertJac(it.next());
        assertCac(it.next());
        assertJdac(it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testApplicationLinkAddedEvent() throws Exception
    {
        expectAdminPropertySet(JAC_ID);
        final ApplicationLink link = service.addApplicationLink(JAC_ID, JIRA_TYPE, JAC_DETAILS);
        assertEquals(link, eventPublisher.getLastFired(ApplicationLinkAddedEvent.class).getApplicationLink());
    }

     @Test
    public void testApplicationLinkDeletedEvent() throws Exception
    {
        expectAdminPropertySet(JAC_ID);
        final ApplicationLink link = service.addApplicationLink(JAC_ID, JIRA_TYPE, JAC_DETAILS);
        service.deleteApplicationLink(link);
        assertEquals(link.getId(), eventPublisher.getLastFired(ApplicationLinkDeletedEvent.class).getApplicationId());
    }

    @Test
    public void testEmpty() throws Exception
    {
        assertNull(service.getApplicationLink(new ApplicationId("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")));
        assertNull(service.getPrimaryApplicationLink(JIRA_TYPE.getClass()));
        assertFalse(service.getApplicationLinks().iterator().hasNext());
        assertFalse(service.getApplicationLinks(JIRA_TYPE.getClass()).iterator().hasNext());
    }

    @Test
    public void testTypeNotInstalled() throws Exception
    {
        expectAdminPropertySet(JAC_ID, JDAC_ID, CAC_ID);
        service.addApplicationLink(JAC_ID, JIRA_TYPE, JAC_DETAILS);
        service.addApplicationLink(JDAC_ID, JIRA_TYPE, JDAC_DETAILS);
        service.addApplicationLink(CAC_ID, CONF_TYPE, CAC_DETAILS);

        Iterator<ApplicationLink> it = service.getApplicationLinks().iterator();
        assertJac(it.next());
        assertJdac(it.next());
        assertCac(it.next());
        assertFalse(it.hasNext());

        removeType(CONF_TYPE);

        assertJac(service.getApplicationLink(JAC_ID));
        assertJdac(service.getApplicationLink(JDAC_ID));
        try
        {
            service.getApplicationLink(CAC_ID);
            fail("getApplicationLink() for a disabled type should throw a " + TypeNotPresentException.class.getSimpleName());
        }
        catch (TypeNotInstalledException e)
        {
            // expected
        }

        it = service.getApplicationLinks().iterator();
        assertJac(it.next());
        assertJdac(it.next());
        assertFalse(it.hasNext());

        service.makePrimary(JAC_ID);
        service.makePrimary(JDAC_ID);

        try
        {
            service.makePrimary(CAC_ID);
            fail("makePrimary() for a disabled type should throw a " + TypeNotPresentException.class.getSimpleName());
        }
        catch (TypeNotInstalledException e)
        {
            // expected
        }

        removeType(JIRA_TYPE);

        try
        {
            service.getApplicationLink(JAC_ID);
            fail("getApplicationLink() for a disabled type should throw a " + TypeNotPresentException.class.getSimpleName());
        }
        catch (TypeNotInstalledException e)
        {
            // expected
        }

        try
        {
            service.makePrimary(JAC_ID);
            fail("makePrimary() for a disabled type should throw a " + TypeNotPresentException.class.getSimpleName());
        }
        catch (TypeNotInstalledException e)
        {
            // expected
        }

        assertFalse("getApplicationLinks() should return an empty iterable when all types are disabled", service.getApplicationLinks().iterator().hasNext());
    }

    @Test
    public void testAddApplicationLinks() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_1));
        final MutableApplicationLink link1 = service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);
        assertEquals(JAC_NAME, link1.getName());
        assertEquals(JIRA_TYPE, link1.getType());
        assertEquals(JAC_DETAILS.getRpcUrl(), link1.getRpcUrl());
        assertEquals(JAC_DETAILS.getDisplayUrl(), link1.getDisplayUrl());
        assertEquals(new ApplicationId(ID_1), link1.getId());
        assertTrue(link1.isPrimary());
    }

    @Test
    public void testAddApplicationLinkWithDuplicateId() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_1));
        service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);

        try
        {
            service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JDAC_DETAILS);
            fail("addApplicationLink() for duplicate id should throw an " + IllegalArgumentException.class.getSimpleName());
        }
        catch (final IllegalArgumentException e)
        {
           // expected
        }
    }

    @Test
    public void testAddApplicationLinkWithDuplicateName() throws Exception
    {
        // Create a first application link
        expectAdminPropertySet(new ApplicationId(ID_1));
        service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);

        // It is important to create 11 duplicates, to check the " - 10" is replaced with " - 11" even though it's 2 chars long.
        for (int counter = 2; counter < 11; counter++)
        {
            ApplicationId id = generateNewApplicationId();
            expectAdminPropertySet(id);
            InternalApplicationLink duplicateApplicationLink = service.addApplicationLink(id, JIRA_TYPE, JAC_DETAILS);
            
            String alternateName = String.format("%s - %d", JAC_DETAILS.getName(), counter);
            assertEquals("DefaultApplicationLinkService#addApplicationLink() should find an alternate name for the second application link", alternateName,
                    duplicateApplicationLink.getName());
        }
    }

    @Test
    public void testNameInUseReturnsTrueWhenAnotherLinkWithSameNameExists() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_1));
        final MutableApplicationLink link1 = service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);
        assertTrue(service.isNameInUse(JAC_DETAILS.getName(), null));
    }

    @Test
    public void testNameInUseReturnsFalseWhenNoOtherLinkWithSameNameExists() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_1));
        assertFalse(service.isNameInUse(JAC_DETAILS.getName(), null));

        final MutableApplicationLink link1 = service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);
        assertFalse(service.isNameInUse(JAC_DETAILS.getName(), link1.getId()));
    }

    @Test
    public void testKeepPrimaryWhenNewLinkOfSameTypeAdded() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_2));
        service.addApplicationLink(new ApplicationId(ID_2), JIRA_TYPE, PRIMARY_LINK_DETAILS);
        expectAdminPropertySet(new ApplicationId(ID_1));
        service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);
        final Iterable<ApplicationLink> iterable = service.getApplicationLinks(JiraApplicationType.class);
        final Iterator<ApplicationLink> linkIterator = iterable.iterator();
        final ApplicationLink applicationLink2 = linkIterator.next();
        assertEquals(new ApplicationId(ID_2), applicationLink2.getId());
        assertTrue(applicationLink2.isPrimary());
        linkIterator.next();
        assertFalse(linkIterator.hasNext());
    }

    @Test
    public void testDeletePrimaryApplicationLink() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_1));
        MutableApplicationLink link1 = service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);
        assertTrue(link1.isPrimary());
        expectAdminPropertySet(new ApplicationId(ID_2));
        final MutableApplicationLink link2 = service.addApplicationLink(new ApplicationId(ID_2), JIRA_TYPE, JDAC_DETAILS);
        assertFalse(link2.isPrimary());
        service.makePrimary(link2.getId());
        assertTrue(link2.isPrimary());

        link1 = service.getApplicationLink(new ApplicationId(ID_1));
        assertFalse(link1.isPrimary());

        service.deleteApplicationLink(link2);

        assertNull(service.getApplicationLink(new ApplicationId(ID_2)));
        link1 = service.getApplicationLink(new ApplicationId(ID_1));
        assertTrue(link1.isPrimary());
    }

    @Test
    public void testDontChangePrimaryAfterDeletingNonPrimaryApplicationLink() throws Exception
    {
        final ApplicationId id1 = new ApplicationId(ID_1);
        expectAdminPropertySet(id1);
        service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);
        final ApplicationId id2 = new ApplicationId(ID_2);
        expectAdminPropertySet(id2);
        service.addApplicationLink(new ApplicationId(ID_2), JIRA_TYPE, JDAC_DETAILS);
        final ApplicationId id3 = new ApplicationId(ID_3);
        expectAdminPropertySet(id3);
        //Primary is the second application link
        service.makePrimary(id2);
        final InternalApplicationLink nonPrimaryApplicationLink = service.addApplicationLink(new ApplicationId(ID_3), JIRA_TYPE, CAC_DETAILS);
        service.deleteApplicationLink(nonPrimaryApplicationLink);
        final InternalApplicationLink link1 = service.getApplicationLink(id1);
        assertFalse("Link2 should be the primary", link1.isPrimary());
        final InternalApplicationLink link2 = service.getApplicationLink(id2);
        assertTrue(link2.isPrimary());
    }

    @Test
    public void testSimpleDeleteApplicationLink() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_1));
        final MutableApplicationLink link1 = service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);
        assertTrue(link1.isPrimary());

        service.deleteApplicationLink(link1);
        assertNull(service.getApplicationLink(new ApplicationId(ID_1)));
    }

    @Test
    public void testSetPrimary() throws Exception
    {
        expectAdminPropertySet(new ApplicationId(ID_1));
        service.addApplicationLink(new ApplicationId(ID_1), JIRA_TYPE, JAC_DETAILS);

        expectAdminPropertySet(new ApplicationId(ID_2));
        service.addApplicationLink(new ApplicationId(ID_2), JIRA_TYPE, CAC_DETAILS);

        expectAdminPropertySet(new ApplicationId(ID_3));
        service.addApplicationLink(new ApplicationId(ID_3), JIRA_TYPE, JDAC_DETAILS);

        service.makePrimary(new ApplicationId(ID_1));

        final MutableApplicationLink link1 = service.getApplicationLink(new ApplicationId(ID_1));
        assertTrue(link1.isPrimary());

        final MutableApplicationLink link2 = service.getApplicationLink(new ApplicationId(ID_2));
        assertFalse(link2.isPrimary());

        final MutableApplicationLink link3 = service.getApplicationLink(new ApplicationId(ID_3));
        assertFalse(link3.isPrimary());
    }

    @Test
    public void testInconsistentPrimaryState()
    {
        expectAdminPropertySet(JAC_ID);
        service.addApplicationLink(JAC_ID, JIRA_TYPE, JAC_DETAILS);

        propertyService.getApplicationLinkProperties(JAC_ID).setIsPrimary(false);

        try
        {
            service.getPrimaryApplicationLink(JIRA_TYPE.getClass());
            fail("Should have thrown IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testTwoConfluences()
    {
        expectAdminPropertySet(CAC_ID, EAC_ID);
        service.addApplicationLink(CAC_ID, CONF_TYPE, CAC_DETAILS);
        service.addApplicationLink(EAC_ID, CONF_TYPE, EAC_DETAILS);

        final Iterator<ApplicationLink> it = service.getApplicationLinks(CONF_TYPE.getClass()).iterator();

        assertCac(it.next());
        assertEac(it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testCreateApplicationLink() throws Exception {
        final Manifest mockManifest = mock(Manifest.class);
        final PropertySet mockApplinksAdminProperties = new MockPropertySet();
        final PropertySet mockApplinksProperties = new MockPropertySet();

        when(manifestRetriever.getManifest(Mockito.eq(JAC_RPC), Mockito.any(JiraApplicationType.class))).thenReturn(mockManifest);
        when(mockManifest.getId()).thenReturn(JAC_ID);
        when(propertyService.getApplicationLinkProperties(JAC_ID)).thenReturn(new ApplicationLinkProperties(mockApplinksAdminProperties, mockApplinksProperties));

        final ApplicationLink applicationLink = service.createApplicationLink(JIRA_TYPE, JAC_DETAILS);

        assertTrue(((Collection<String>) globalAdminProperties.get(DefaultApplicationLinkService.APPLICATION_IDS)).contains(JAC_ID.get()));
        assertEquals(((IdentifiableType) JIRA_TYPE).getId().get(), mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.TYPE.key()));
        assertEquals(JAC_NAME, mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.NAME.key()));
        assertEquals(JAC_DISPLAY.toString(), mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.DISPLAY_URL.key()));
        assertEquals(JAC_RPC.toString(), mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.RPC_URL.key()));
        assertEquals(String.valueOf(true), mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.PRIMARY.key()));
        final LinkedList<Object> events = eventPublisher.getEvents();
        assertEquals(1, events.size());
        assertSame(applicationLink, ((ApplicationLinkAddedEvent) events.get(0)).getApplicationLink());
    }

    @Test
    public void testCreateApplicationLinkWithExistingIDFails() throws Exception {
        testCreateApplicationLink();

        try {
            service.createApplicationLink(JIRA_TYPE, JAC_DETAILS);
            fail();
        } catch (final IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testCreateSecondApplicationLinkOfType() throws Exception {
        testCreateApplicationLink();

        final Manifest mockManifest = mock(Manifest.class);
        final PropertySet mockApplinksAdminProperties = new MockPropertySet();
        final PropertySet mockApplinksProperties = new MockPropertySet();

        when(manifestRetriever.getManifest(Mockito.eq(JDAC_RPC), Mockito.any(JiraApplicationType.class))).thenReturn(mockManifest);
        when(mockManifest.getId()).thenReturn(JDAC_ID);
        when(propertyService.getApplicationLinkProperties(JDAC_ID)).thenReturn(new ApplicationLinkProperties(mockApplinksAdminProperties, mockApplinksProperties));

        final ApplicationLink applicationLink = service.createApplicationLink(JIRA_TYPE, JDAC_DETAILS);

        final Collection<String> ids = (Collection<String>) globalAdminProperties.get(DefaultApplicationLinkService.APPLICATION_IDS);
        assertTrue(ids.contains(JAC_ID.get()));
        assertTrue(ids.contains(JDAC_ID.get()));
        assertEquals(((IdentifiableType) JIRA_TYPE).getId().get(), mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.TYPE.key()));
        assertEquals(JDAC_NAME, mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.NAME.key()));
        assertEquals(JDAC_DISPLAY.toString(), mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.DISPLAY_URL.key()));
        assertEquals(JDAC_RPC.toString(), mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.RPC_URL.key()));
        assertNull(mockApplinksAdminProperties.getProperty(ApplicationLinkProperties.Property.PRIMARY.key()));
        final LinkedList<Object> events = eventPublisher.getEvents();
        assertEquals(2, events.size());
        assertSame(applicationLink, ((ApplicationLinkAddedEvent)events.get(1)).getApplicationLink());
    }

    @Test
    public void testIsAdminUserInRemoteApplicationTrue() throws Exception {
        testIsAdminUserInRemoteApplication(true);
    }

    @Test
    public void testIsAdminUserInRemoteApplicationFalse() throws Exception {
        testIsAdminUserInRemoteApplication(false);
    }

    private final void testIsAdminUserInRemoteApplication(final boolean result) throws ResponseException {
        expectIsAdminUserInRemoteApplication(result);
        assertEquals(result, service.isAdminUserInRemoteApplication(JAC_RPC, USERNAME, PASSWORD));
    }

    private final void expectIsAdminUserInRemoteApplication(final boolean result) throws ResponseException {
        final AppLinksManifestDownloader mockManifestDownloader = mock(AppLinksManifestDownloader.class);
        final Request<Request<?, Response>,Response> mockRequest = mock(Request.class);
        final Response mockResponse = mock(Response.class);

        when(mockRestUrlBuilder.getUrlFor(Mockito.<URI>any(), Mockito.eq(AuthenticationResource.class)))
                .thenReturn(new AuthenticationResource(mockManifestDownloader));
        when(mockRequestFactory.createRequest(Mockito.eq(Request.MethodType.GET), Mockito.<String>any())).thenReturn(mockRequest);
        when(mockRequest.addBasicAuthentication(USERNAME, PASSWORD)).thenReturn((Request) mockRequest);
        final ArgumentCaptor<ReturningResponseHandler> responseHandlerCaptor
                = ArgumentCaptor.forClass(ReturningResponseHandler.class);
        when(mockRequest.executeAndReturn(responseHandlerCaptor.capture())).thenAnswer(new Answer<Boolean>() {

            public final Boolean answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return (Boolean) responseHandlerCaptor.getValue().handle(mockResponse);
            }

        });
        when(mockResponse.isSuccessful()).thenReturn(result);
    }

    private void expectAdminPropertySet(final ApplicationId... ids)
    {
        for (final ApplicationId id : ids)
        {
            final MockPluginSettingsPropertySet propertySet = new MockPluginSettingsPropertySet();
            when(propertyService.getApplicationLinkProperties(id)).thenReturn(new ApplicationLinkProperties(propertySet, propertySet));
        }
    }

    private void assertCac(final ApplicationLink link)
    {
        assertEquals(CAC_ID, link.getId());
        assertEquals(CAC_NAME, link.getName());
        assertEquals(CONF_TYPE, link.getType());
        assertEquals(CONF_I18N, link.getType().getI18nKey());
        assertEquals(CAC_DISPLAY, link.getDisplayUrl());
        assertEquals(CAC_RPC, link.getRpcUrl());
    }

    private void assertEac(final ApplicationLink link)
    {
        assertEquals(EAC_ID, link.getId());
        assertEquals(EAC_NAME, link.getName());
        assertEquals(CONF_TYPE, link.getType());
        assertEquals(CONF_I18N, link.getType().getI18nKey());
        assertEquals(EAC_DISPLAY, link.getDisplayUrl());
        assertEquals(EAC_RPC, link.getRpcUrl());
    }

    private void assertJac(final ApplicationLink link)
    {
        assertEquals(JAC_ID, link.getId());
        assertEquals(JAC_NAME, link.getName());
        assertEquals(JIRA_TYPE, link.getType());
        assertEquals(JIRA_I18N, link.getType().getI18nKey());
        assertEquals(JAC_DISPLAY, link.getDisplayUrl());
        assertEquals(JAC_RPC, link.getRpcUrl());
    }

    private void assertJdac(final ApplicationLink link)
    {
        assertEquals(JDAC_ID, link.getId());
        assertEquals(JDAC_NAME, link.getName());
        assertEquals(JIRA_TYPE, link.getType());
        assertEquals(JIRA_I18N, link.getType().getI18nKey());
        assertEquals(JDAC_DISPLAY, link.getDisplayUrl());
        assertEquals(JDAC_RPC, link.getRpcUrl());
    }

    private ApplicationId generateNewApplicationId()
    {
        applicationIdCount++;
        // This formats the applicationIdCount on 10 characters
        String fullFigure = String.format("%010d", applicationIdCount);
        String lastFigure = String.valueOf(applicationIdCount % 10);

        // Takes ID_1 as an example
        String id = ID_1.substring(0, ID_1.length() - fullFigure.length());
        id = id.replaceAll("1", lastFigure);
        id += fullFigure;

        return new ApplicationId(id);
    }
}
