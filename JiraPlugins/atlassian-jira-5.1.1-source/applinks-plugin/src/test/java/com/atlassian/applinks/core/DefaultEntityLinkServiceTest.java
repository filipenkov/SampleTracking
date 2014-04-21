package com.atlassian.applinks.core;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.api.application.bamboo.BambooProjectEntityType;
import com.atlassian.applinks.api.application.fecru.FishEyeCrucibleApplicationType;
import com.atlassian.applinks.api.application.fecru.FishEyeRepositoryEntityType;
import com.atlassian.applinks.api.application.jira.JiraProjectEntityType;
import com.atlassian.applinks.api.application.refapp.RefAppApplicationType;
import com.atlassian.applinks.api.event.EntityLinkAddedEvent;
import com.atlassian.applinks.api.event.EntityLinkDeletedEvent;
import com.atlassian.applinks.application.bamboo.BambooProjectEntityTypeImpl;
import com.atlassian.applinks.application.fecru.FishEyeRepositoryEntityTypeImpl;
import com.atlassian.applinks.application.jira.JiraProjectEntityTypeImpl;
import com.atlassian.applinks.core.link.DefaultEntityLinkBuilderFactory;
import com.atlassian.applinks.core.property.EntityLinkProperties;
import com.atlassian.applinks.core.property.MockPluginSettingsPropertySet;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.applinks.core.rest.client.MockEntityLinkClient;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.IdentifiableType;
import com.atlassian.applinks.spi.application.TypeId;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultEntityLinkServiceTest
{
    private static final String JRA = "JRA";
    private static final String CONF = "CONF";
    private static final Set<String> PROJECTS = ImmutableSet.of(JRA, CONF);

    private static final String JIRA_REP_KEY = "jira";
    private static final String JIRA_REP_NAME = "jira Repository";
    private static final URI JIRA_REP_URL;

    private static final String ATL_REP_KEY = "atlassian";
    private static final String ATL_REP_NAME = "atlassian Repository";
    private static final URI ATL_REP_URL;

    private static final String CONF_REP_KEY = "confluence";
    private static final String CONF_REP_NAME = "confluence Repository";
    private static final URI CONF_REP_URL;

    private static final ApplicationId ATLASEYE_ID = new ApplicationId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final String FISHEYE_REPOSITORY_I18N = "applinks.fecru.repository";

    private static final ApplicationId LEGACY_PUBLIC_FISHEYE_ID = new ApplicationId("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private static final String JIRA_BUILD_KEY = "JFUNCTRUNK";
    private static final String JIRA_BUILD_NAME = "JIRA Functional Tests - Trunk";
    private static final URI JIRA_BUILD_URL;

    private static final ApplicationId JBAC_ID = new ApplicationId("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final String BAMBOO_PROJECT_I18N = "applinks.bamboo.project";

    static
    {
        try
        {
            JIRA_REP_URL = new URI("http://atlaseye.atlassian.com/changelog/jira");
            ATL_REP_URL = new URI("http://atlaseye.atlassian.com/changelog/atlassian");
            CONF_REP_URL = new URI("http://atlaseye.atlassian.com/changelog/confluence");
            JIRA_BUILD_URL = new URI("http://jira.bamboo.atlassian.com/browse/JFUNCTRUNK");
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static final JiraProjectEntityType JIRA_PROJECT = new JiraProjectEntityTypeImpl(null, null);
    private static final FishEyeRepositoryEntityType FISHEYE_REPOSITORY = new FishEyeRepositoryEntityTypeImpl(null, null);
    private static final BambooProjectEntityType BAMBOO_PROJECT = new BambooProjectEntityTypeImpl(null, null);

    private static class UnregisteredEntityType implements EntityType, IdentifiableType
    {
        public TypeId getId()
        {
            return new TypeId("unregistered");
        }

        public Class<? extends ApplicationType> getApplicationType()
        {
            return RefAppApplicationType.class;
        }

        public String getI18nKey()
        {
            return "not.registered";
        }

        public String getPluralizedI18nKey()
        {
            return "not.registered";
        }

        public String getShortenedI18nKey()
        {
            return "not.registered";
        }

        public URI getIconUrl()
        {
            return null;
        }

        public URI getDisplayUrl(ApplicationLink link, String entityKey) {
            return null;
        }
    }

    private static final EntityType notARealType = new UnregisteredEntityType();

    DefaultEntityLinkService service;
    PropertyService propertyService;
    ApplicationLinkService applicationLinkService;
    InternalHostApplication internalHostApplication;
    InternalTypeAccessor typeAccessor;
    DefaultEntityLinkBuilderFactory entityLinkFactory;
    MockEventPublisher eventPublisher;

    ApplicationLink atlaseye;
    ApplicationLink legacyPublicFishEye;
    ApplicationLink jbac;

    @Before
    public void setUp() throws Exception
    {
        propertyService = mock(PropertyService.class);
        applicationLinkService = mock(ApplicationLinkService.class);
        typeAccessor = mock(InternalTypeAccessor.class);
        eventPublisher = new MockEventPublisher();
        entityLinkFactory = new DefaultEntityLinkBuilderFactory(null);
        internalHostApplication = new MockInternalHostApplication()
        {
            @Override
            public boolean doesEntityExist(final String key, final Class<? extends EntityType> type)
            {
                return PROJECTS.contains(key) && JIRA_PROJECT.getClass().isAssignableFrom(type);
            }

            @Override
            public boolean doesEntityExistNoPermissionCheck(final String key, final Class<? extends EntityType> type)
            {
                return PROJECTS.contains(key) && JIRA_PROJECT.getClass().isAssignableFrom(type);
            }

            @Override
            public Iterable<EntityReference> getLocalEntities()
            {
                return Arrays.<EntityReference>asList(new SimpleEntityReference(JRA, JIRA_PROJECT));
            }

            @Override
            public EntityReference toEntityReference(final Object domainObject)
            {
                if (!(domainObject instanceof String))
                {
                    throw new IllegalArgumentException("mock InternalHostApplication requires String domain objects");
                }
                else if (!PROJECTS.contains((String) domainObject))
                {
                    throw new IllegalArgumentException("Not a valid project!");
                }

                return new EntityReference()
                {
                    public String getKey()
                    {
                        return (String) domainObject;
                    }

                    public EntityType getType()
                    {
                        return JIRA_PROJECT;
                    }

                    public URI getDisplayUrl()
                    {
                        return URIUtil.uncheckedCreate("http://jira.atlassian.com/browse/" + domainObject);
                    }

                    public String getName()
                    {
                        return (String) domainObject;
                    }

                    public URI getIconUrl()
                    {
                        return URIUtil.uncheckedCreate("http://jira.atlassian.com/favicon.ico");
                    }
                };
            }
        };

        final FishEyeCrucibleApplicationType fishEyeCrucibleApplicationType = mock(FishEyeCrucibleApplicationType.class);
        final BambooApplicationType bambooApplicationType = mock(BambooApplicationType.class);

        atlaseye = mock(ApplicationLink.class);
        when(atlaseye.getType()).thenReturn(fishEyeCrucibleApplicationType);
        when(atlaseye.getId()).thenReturn(ATLASEYE_ID);
        when(atlaseye.getDisplayUrl()).thenReturn(new URI("http://atlaseye.atlassian.com"));
        when(applicationLinkService.getApplicationLink(ATLASEYE_ID)).thenReturn(atlaseye);

        legacyPublicFishEye = mock(ApplicationLink.class);
        when(legacyPublicFishEye.getType()).thenReturn(fishEyeCrucibleApplicationType);
        when(legacyPublicFishEye.getId()).thenReturn(LEGACY_PUBLIC_FISHEYE_ID);
        when(applicationLinkService.getApplicationLink(LEGACY_PUBLIC_FISHEYE_ID)).thenReturn(legacyPublicFishEye);

        jbac = mock(ApplicationLink.class);
        when(jbac.getType()).thenReturn(bambooApplicationType);
        when(jbac.getId()).thenReturn(JBAC_ID);
        when(jbac.getDisplayUrl()).thenReturn(new URI("http://jira.bamboo.atlassian.com"));
        when(applicationLinkService.getApplicationLink(JBAC_ID)).thenReturn(jbac);

        expectLocalEntityPropertySet(CONF, JIRA_PROJECT);
        expectLocalEntityPropertySet(JRA, JIRA_PROJECT);

        setUpType(JIRA_PROJECT);
        setUpType(FISHEYE_REPOSITORY);
        setUpType(BAMBOO_PROJECT);
        setUpType(notARealType);

        service = new DefaultEntityLinkService();

        service.setApplicationLinkService(applicationLinkService);
        service.setHostApplication(internalHostApplication);
        service.setPropertyService(propertyService);
        service.setTypeAccessor(typeAccessor);
        service.setEntityLinkClient(new MockEntityLinkClient());
        service.setEntityLinkBuilderFactory(entityLinkFactory);
        service.setEventPublisher(eventPublisher);
    }

    @SuppressWarnings("unchecked")
    private <T extends EntityType> void setUpType(final T type)
    {
        when(typeAccessor.loadEntityType(type.getClass().getName())).thenReturn(type);
        when(typeAccessor.getEntityType((Class) type.getClass())).thenReturn(type);
        when(typeAccessor.loadEntityType(((IdentifiableType) type).getId())).thenReturn(type);
    }

    private <T extends EntityType> void removeType(final T type)
    {
        when(typeAccessor.loadEntityType(type.getClass().getName())).thenReturn(null);
        when(typeAccessor.getEntityType(type.getClass())).thenReturn(null);
        when(typeAccessor.loadEntityType(((IdentifiableType) type).getId())).thenReturn(null);
    }

    @Test
    public void testEmpty()
    {
        assertFalse(service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass()).iterator().hasNext());
        assertFalse(service.getEntityLinks(JRA).iterator().hasNext());

        assertFalse(service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator().hasNext());
        assertFalse(service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()).iterator().hasNext());

        assertNull(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
        assertNull(service.getPrimaryEntityLinkForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()));
    }

    @Test
    public void testEntityLinkCreation()
    {
        // test linking jac:JRA -> atlaseye:jira
        assertLinkToJiraRep(linkJraToJiraRepository(true));
        testJraLinkToJiraRepository();

        // test linking jac:CONF -> atlaseye:confluence
        assertLinkToConfluenceRep(linkConfToConfluenceRepository(false));
        testJraLinkToJiraRepository(); //make sure adding a link hasn't effected JRA's links..
        testConfLinkToConfluenceRepository();

        // test linking jac:JRA -> atlaseye:atlassian (in addition to atlaseye:jira)
        assertLinkToAtlassianRep(linkProjectToAtlassianRepository(JRA, false));
        testJraLinkedToJiraAndAtlassianRepositories();

        // test linking jac:CONF -> atlaseye:atlassian, and making atlassian the *PRIMARY* link for CONF
        assertLinkToAtlassianRep(linkProjectToAtlassianRepository(CONF, true));
        testJraLinkedToJiraAndAtlassianRepositories(); //make sure this hasn't effected JRA's links either..
        testConfLinkedToAtlassianAndConfluenceRepositories();

        // test linking jac:JRA -> jbac:jfunctrunk
        assertLinkToJiraBuild(linkJraToJiraBuild(false)); //primary flag should be ignored (always true) for first link of type

        Iterator<EntityLink> it = service.getEntityLinks(JRA).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertLinkToJiraBuild(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertLinkToJiraBuild(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        // test get links by super class
        it = service.getEntityLinks(JRA, FishEyeRepositoryEntityType.class).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(JRA, BAMBOO_PROJECT.getClass()).iterator();
        assertLinkToJiraBuild(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), BAMBOO_PROJECT.getClass()).iterator();
        assertLinkToJiraBuild(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(JRA, notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        assertNull(service.getPrimaryEntityLinkForKey(JRA, JIRA_PROJECT.getClass(), notARealType.getClass()));
        assertNull(service.getPrimaryEntityLink(JRA, notARealType.getClass()));

        // 'jira' should still be the primary fisheye-repository associated with JRA
        assertLinkToJiraRep(service.getPrimaryEntityLinkForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()));
        assertLinkToJiraRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));

        // 'bamboo' should be the primary bamboo-build for JRA
        assertLinkToJiraBuild(service.getPrimaryEntityLinkForKey(JRA, JIRA_PROJECT.getClass(), BAMBOO_PROJECT.getClass()));
        assertLinkToJiraBuild(service.getPrimaryEntityLink(JRA, BAMBOO_PROJECT.getClass()));

        // test linking JRA to a bamboo build hasn't somehow messed with CONF's links
        testConfLinkedToAtlassianAndConfluenceRepositories();
    }

    @Test
    public void testEntityLinkCreationEvents()
    {
        assertLinkToJiraRep(linkJraToJiraRepository(true));
        final Iterator<EntityLink> it = service.getEntityLinks(JRA).iterator();
        assertEquals(it.next(), eventPublisher.getLastFired(EntityLinkAddedEvent.class).getEntityLink());
    }

    @Test
    public void testEntityLinkDeletion()
    {
        final EntityLink linkToJiraRep = linkJraToJiraRepository(true);
        assertLinkToJiraRep(linkToJiraRep);
        final EntityLink linkToAtlassianRep = linkProjectToAtlassianRepository(JRA, false);
        assertLinkToAtlassianRep(linkToAtlassianRep);

        // check links created correctly
        Iterator<EntityLink> it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        EntityLinkProperties entityLinkProperties =  mock(EntityLinkProperties.class);
        when(propertyService.getProperties(linkToAtlassianRep)).thenReturn(entityLinkProperties);

        // delete the link to the atlassian rep
        assertTrue(service.deleteEntityLink(JRA, JIRA_PROJECT.getClass(), linkToAtlassianRep));
        verify(propertyService).getProperties(linkToAtlassianRep);
        verify(entityLinkProperties).removeAll();

        // check it's deleted
        it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertFalse(it.hasNext());

        // check the original is still primary
        assertLinkToJiraRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
    }

    @Test
    public void testEntityLinkDeletionEvents()
    {
        assertLinkToJiraRep(linkJraToJiraRepository(true));
        final EntityLink linkToAtlassianRep = service.getEntityLinks(JRA).iterator().next();
        EntityLinkProperties entityLinkProperties =  mock(EntityLinkProperties.class);
        when(propertyService.getProperties(linkToAtlassianRep)).thenReturn(entityLinkProperties);
        assertTrue(service.deleteEntityLink(JRA, JIRA_PROJECT.getClass(), linkToAtlassianRep));
        assertEquals(linkToAtlassianRep.getKey(), eventPublisher.getLastFired(EntityLinkDeletedEvent.class).getEntityKey());
    }

    @Test
    public void testPrimaryEntityLinkDeletion()
    {
        final EntityLink linkToJiraRep = linkJraToJiraRepository(true);
        assertLinkToJiraRep(linkToJiraRep);
        final EntityLink linkToAtlassianRep = linkProjectToAtlassianRepository(JRA, false);
        assertLinkToAtlassianRep(linkToAtlassianRep);

        // check links created correctly
        Iterator<EntityLink> it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        EntityLinkProperties entityLinkProperties =  mock(EntityLinkProperties.class);
        when(propertyService.getProperties(linkToJiraRep)).thenReturn(entityLinkProperties);

        // delete the link to the jira rep
        assertTrue(service.deleteEntityLink(JRA, JIRA_PROJECT.getClass(), linkToJiraRep));

        // check it's deleted
        it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        // check the atlassian rep is now the primary
        assertLinkToAtlassianRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));

        when(propertyService.getProperties(linkToAtlassianRep)).thenReturn(entityLinkProperties);

        // check that after deleting the last entity of a particular type, a newly linked entity is correctly marked as primary
        service.deleteEntityLink(JRA, JIRA_PROJECT.getClass(), linkToAtlassianRep);

        assertFalse(service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator().hasNext());
        assertNull(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));

        assertLinkToJiraRep(linkJraToJiraRepository(false));

        it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertFalse(it.hasNext());
        assertLinkToJiraRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
    }

    @Test
    public void testDeleteEntityLinksFor()
    {
        assertLinkToJiraRep(linkJraToJiraRepository(true));
        final EntityLink linkToAtlassianRep = service.getEntityLinks(JRA).iterator().next();
        EntityLinkProperties entityLinkProperties =  mock(EntityLinkProperties.class);
        when(propertyService.getProperties(linkToAtlassianRep)).thenReturn(entityLinkProperties);
        service.deleteEntityLinksFor(atlaseye);
        assertFalse(service.getEntityLinks(JRA).iterator().hasNext());
        verify(propertyService).getProperties(linkToAtlassianRep);
        verify(entityLinkProperties).removeAll();
        assertEquals(linkToAtlassianRep.getKey(), eventPublisher.getLastFired(EntityLinkDeletedEvent.class).getEntityKey());
    }

    @Test
    public void testGetEntityLinksForApplicationLink() throws Exception
    {
        linkJraToJiraRepository(true);
        final Iterable<EntityLink> iterable = service.getEntityLinksForApplicationLink(atlaseye);
        assertLinkToJiraRep(iterable.iterator().next());
    }

    @Test
    public void testMakePrimary()
    {
        final EntityLink linkToJiraRep = linkJraToJiraRepository(true);
        assertLinkToJiraRep(linkToJiraRep);
        final EntityLink linkToAtlassianRepFromJra = linkProjectToAtlassianRepository(JRA, false);
        assertLinkToAtlassianRep(linkToAtlassianRepFromJra);
        final EntityLink linkToConfluenceRep = linkConfToConfluenceRepository(true);
        assertLinkToConfluenceRep(linkToConfluenceRep);
        final EntityLink linkToAtlassianRepFromConf = linkProjectToAtlassianRepository(CONF, false);
        assertLinkToAtlassianRep(linkToAtlassianRepFromConf);

        // check primaries are correct initially
        assertLinkToJiraRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
        assertLinkToConfluenceRep(service.getPrimaryEntityLink(CONF, FISHEYE_REPOSITORY.getClass()));

        service.makePrimary(JRA, JIRA_PROJECT.getClass(), linkToAtlassianRepFromJra);

        // check the atlassian rep is now the primary for JRA (but CONF still has the confluence rep as primary)
        assertLinkToAtlassianRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
        assertLinkToConfluenceRep(service.getPrimaryEntityLink(CONF, FISHEYE_REPOSITORY.getClass()));

        service.makePrimary(CONF, JIRA_PROJECT.getClass(), linkToAtlassianRepFromJra);

        // check atlassian rep is now the primary for both JRA & CONF
        assertLinkToAtlassianRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
        assertLinkToAtlassianRep(service.getPrimaryEntityLink(CONF, FISHEYE_REPOSITORY.getClass()));

        // check there's still just two links for each project
        Iterator<EntityLink> it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        // now try switching the primary back
        service.makePrimary(JRA, JIRA_PROJECT.getClass(), linkToJiraRep);
        service.makePrimary(CONF, JIRA_PROJECT.getClass(), linkToConfluenceRep);

        assertLinkToJiraRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
        assertLinkToConfluenceRep(service.getPrimaryEntityLink(CONF, FISHEYE_REPOSITORY.getClass()));

        it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testInvalid()
    {
        try
        {
            service.getEntityLinks(new Object());
            fail("Should throw exception for invalid domain obj");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            service.getEntityLinks("bleh");
            fail("Should throw exception for invalid key");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            service.getEntityLinksForKey("bleh", JIRA_PROJECT.getClass());
            fail("Should throw exception for invalid key");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testTypeNotInstalled()
    {
        linkJraToJiraRepository(true);
        linkProjectToAtlassianRepository(JRA, false);
        linkJraToJiraBuild(false);
        linkConfToConfluenceRepository(true);
        linkProjectToAtlassianRepository(CONF, false);

        Iterator<EntityLink> it = service.getEntityLinks(JRA).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertLinkToJiraBuild(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        removeType(BAMBOO_PROJECT);

        it = service.getEntityLinks(JRA).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        removeType(FISHEYE_REPOSITORY);

        assertFalse(service.getEntityLinks(JRA).iterator().hasNext());
        assertFalse(service.getEntityLinks(CONF).iterator().hasNext());

        setUpType(BAMBOO_PROJECT);

        it = service.getEntityLinks(JRA).iterator();
        assertLinkToJiraBuild(it.next());
        assertFalse(it.hasNext());

        assertFalse(service.getEntityLinks(CONF).iterator().hasNext());
    }

    private void testConfLinkedToAtlassianAndConfluenceRepositories()
    {
        Iterator<EntityLink> it = service.getEntityLinks(CONF).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(CONF, JIRA_PROJECT.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(CONF, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF, notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(CONF, JIRA_PROJECT.getClass(), notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        // 'atlassian' should be the new primary fisheye-repository for CONF
        assertLinkToAtlassianRep(service.getPrimaryEntityLinkForKey(CONF, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()));
        assertLinkToAtlassianRep(service.getPrimaryEntityLink(CONF, FISHEYE_REPOSITORY.getClass()));
    }

    private void testJraLinkedToJiraAndAtlassianRepositories()
    {
        Iterator<EntityLink> it = service.getEntityLinks(JRA).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertLinkToAtlassianRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(JRA, notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        // 'jira' should still be the primary fisheye-repository associated with JRA
        assertLinkToJiraRep(service.getPrimaryEntityLinkForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()));
        assertLinkToJiraRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
    }

    private void testJraLinkToJiraRepository()
    {

        Iterator<EntityLink> it = service.getEntityLinks(JRA).iterator();
        assertLinkToJiraRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(JRA, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToJiraRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(JRA, notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(JRA, JIRA_PROJECT.getClass(), notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        assertLinkToJiraRep(service.getPrimaryEntityLinkForKey(JRA, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()));
        assertLinkToJiraRep(service.getPrimaryEntityLink(JRA, FISHEYE_REPOSITORY.getClass()));
    }

    private void testConfLinkToConfluenceRepository()
    {
        Iterator<EntityLink> it = service.getEntityLinks(CONF).iterator();
        assertLinkToConfluenceRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(CONF, JIRA_PROJECT.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF, FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(CONF, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()).iterator();
        assertLinkToConfluenceRep(it.next());
        assertFalse(it.hasNext());

        it = service.getEntityLinks(CONF, notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        it = service.getEntityLinksForKey(CONF, JIRA_PROJECT.getClass(), notARealType.getClass()).iterator();
        assertFalse(it.hasNext());

        assertLinkToConfluenceRep(service.getPrimaryEntityLinkForKey(CONF, JIRA_PROJECT.getClass(), FISHEYE_REPOSITORY.getClass()));
        assertLinkToConfluenceRep(service.getPrimaryEntityLink(CONF, FISHEYE_REPOSITORY.getClass()));
    }

    private void expectLocalEntityPropertySet(final String key, final EntityType type)
    {
        when(propertyService.getLocalEntityProperties(key, ((IdentifiableType) type).getId())).thenReturn(new MockPluginSettingsPropertySet());
    }

    private EntityLink linkJraToJiraRepository(final boolean primary)
    {
        return service.addEntityLink(JRA, JIRA_PROJECT.getClass(),
                entityLinkFactory
                        .builder()
                        .applicationLink(atlaseye)
                        .key(JIRA_REP_KEY)
                        .type(FISHEYE_REPOSITORY)
                        .name(JIRA_REP_NAME)
                        .primary(primary)
                        .build()
        );
    }

    private EntityLink linkProjectToAtlassianRepository(final String project, final boolean primary)
    {
        return service.addEntityLink(project, JIRA_PROJECT.getClass(),
                entityLinkFactory
                        .builder()
                        .applicationLink(atlaseye)
                        .key(ATL_REP_KEY)
                        .type(FISHEYE_REPOSITORY)
                        .name(ATL_REP_NAME)
                        .primary(primary)
                        .build()
        );
    }

    private EntityLink linkJraToJiraBuild(final boolean primary)
    {
        return service.addEntityLink(JRA, JIRA_PROJECT.getClass(),
                entityLinkFactory
                        .builder()
                        .applicationLink(jbac)
                        .key(JIRA_BUILD_KEY)
                        .type(BAMBOO_PROJECT)
                        .name(JIRA_BUILD_NAME)
                        .primary(primary)
                        .build()
        );
    }

    private EntityLink linkConfToConfluenceRepository(final boolean primary)
    {
        return service.addEntityLink(CONF, JIRA_PROJECT.getClass(),
                entityLinkFactory
                        .builder()
                        .applicationLink(atlaseye)
                        .key(CONF_REP_KEY)
                        .type(FISHEYE_REPOSITORY)
                        .name(CONF_REP_NAME)
                        .primary(primary)
                        .build()
        );
    }

    private void assertLinkToJiraBuild(final EntityLink link)
    {
        assertEquals(JIRA_BUILD_KEY, link.getKey());
        assertEquals(JIRA_BUILD_NAME, link.getName());
        assertEquals(JIRA_BUILD_URL, link.getDisplayUrl());
        assertEquals(BAMBOO_PROJECT, link.getType());
        assertEquals(BAMBOO_PROJECT_I18N, link.getType().getI18nKey());
        assertEquals(JBAC_ID, link.getApplicationLink().getId());
    }

    private void assertLinkToJiraRep(final EntityLink link)
    {
        assertEquals(JIRA_REP_KEY, link.getKey());
        assertEquals(JIRA_REP_NAME, link.getName());
        assertEquals(JIRA_REP_URL, link.getDisplayUrl());
        assertAtlasEyeRep(link);
    }

    private void assertLinkToAtlassianRep(final EntityLink link)
    {
        assertEquals(ATL_REP_KEY, link.getKey());
        assertEquals(ATL_REP_NAME, link.getName());
        assertEquals(ATL_REP_URL, link.getDisplayUrl());
        assertAtlasEyeRep(link);
    }

    private void assertAtlasEyeRep(final EntityLink link)
    {
        assertEquals(FISHEYE_REPOSITORY.getClass(), link.getType().getClass());
        assertEquals(FISHEYE_REPOSITORY_I18N, link.getType().getI18nKey());
        assertEquals(ATLASEYE_ID, link.getApplicationLink().getId());
    }

    private void assertLinkToConfluenceRep(final EntityLink link)
    {
        assertEquals(CONF_REP_KEY, link.getKey());
        assertEquals(CONF_REP_NAME, link.getName());
        assertEquals(CONF_REP_URL, link.getDisplayUrl());
        assertAtlasEyeRep(link);
    }

    private static class SimpleEntityReference implements EntityReference
    {
        private final String key;
        private final EntityType type;

        public SimpleEntityReference(final String key, final EntityType type)
        {
            this.key = key;
            this.type = type;
        }

        public String getKey()
        {
            return key;
        }

        public EntityType getType()
        {
            return type;
        }

        public String getName()
        {
            return null;
        }

        public URI getDisplayUrl()
        {
            return null;
        }

        public URI getIconUrl()
        {
            return null;
        }
    }
}
