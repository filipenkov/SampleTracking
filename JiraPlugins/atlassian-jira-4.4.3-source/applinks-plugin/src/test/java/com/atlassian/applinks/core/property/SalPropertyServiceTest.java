package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.application.jira.JiraApplicationTypeImpl;
import com.atlassian.applinks.application.jira.JiraProjectEntityTypeImpl;
import com.atlassian.applinks.application.refapp.RefAppCharlieEntityTypeImpl;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.IdentifiableType;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SalPropertyServiceTest
{
    private static final ApplicationId APP_ONE_ID = new ApplicationId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final ApplicationId APP_TWO_ID = new ApplicationId("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private static final EntityType TYPE_1 = new JiraProjectEntityTypeImpl(null, null);

    private static final EntityType TYPE_2 = new RefAppCharlieEntityTypeImpl(null, null);

    private final String TEST_PROP = "name";

    @Test
    public void testPropertySetsAreNamespaced()
    {
        final PluginSettings global = new MockPluginSettingsPropertySet();
        final PluginSettingsFactory pluginSettingsFactory = mock(PluginSettingsFactory.class);

        final InternalHostApplication internalHostApplication = mock(InternalHostApplication.class);
        final AppLinkPluginUtil pluginUtil = mock(AppLinkPluginUtil.class);
        final WebResourceManager webResourceManager = mock(WebResourceManager.class);
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(global);
        when(internalHostApplication.getType()).thenReturn(new JiraApplicationTypeImpl(pluginUtil, webResourceManager));
        final SalPropertyService salPropertyService = new SalPropertyService(pluginSettingsFactory);

        //set a bunch of properties in different property sets, with the same key

        salPropertyService.getGlobalAdminProperties().putProperty(TEST_PROP, "global");

        salPropertyService.getApplicationLinkProperties(APP_ONE_ID).setName("app.admin.one");
        salPropertyService.getApplicationLinkProperties(APP_TWO_ID).setName("app.admin.two");

        final ApplicationLink app1 = mock(ApplicationLink.class);
        when(app1.getId()).thenReturn(new ApplicationId("f2ea52f5-a614-4f8b-87af-619fe578de1b"));
        final ApplicationLink app2 = mock(ApplicationLink.class);
        when(app2.getId()).thenReturn(new ApplicationId("f2ea52f5-a614-4f8b-87af-619fe2342342"));

        salPropertyService.getProperties(app1).putProperty(TEST_PROP, "app.one");
        salPropertyService.getProperties(app2).putProperty(TEST_PROP, "app.two");

        final EntityLink entity1 = mock(EntityLink.class);
        when(entity1.getKey()).thenReturn("entity_1");
        when(entity1.getType()).thenReturn(TYPE_1);
        when(entity1.getApplicationLink()).thenReturn(app1);

        final EntityLink entity2 = mock(EntityLink.class); //same parent app & same type
        when(entity2.getKey()).thenReturn("entity_2");
        when(entity2.getType()).thenReturn(TYPE_1);
        when(entity2.getApplicationLink()).thenReturn(app1);

        final EntityLink entity3 = mock(EntityLink.class); //same parent app & diff type
        when(entity3.getKey()).thenReturn("entity_3");
        when(entity3.getType()).thenReturn(TYPE_2);
        when(entity3.getApplicationLink()).thenReturn(app1);

        final EntityLink entity4 = mock(EntityLink.class); //different parent app
        when(entity4.getKey()).thenReturn("entity_4");
        when(entity4.getType()).thenReturn(TYPE_2);
        when(entity4.getApplicationLink()).thenReturn(app2);

        salPropertyService.getProperties(entity1).putProperty(TEST_PROP, "entity_1");
        salPropertyService.getProperties(entity2).putProperty(TEST_PROP, "entity_2");
        salPropertyService.getProperties(entity3).putProperty(TEST_PROP, "entity_3");

        salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_1).getId()).putProperty(TEST_PROP, "k1t1");
        salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_2).getId()).putProperty(TEST_PROP, "k1t2");
        salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_1).getId()).putProperty(TEST_PROP, "k2t1");
        salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_2).getId()).putProperty(TEST_PROP, "k2t2");

        // check that no properties have been overwritten

        assertEquals(salPropertyService.getGlobalAdminProperties().getProperty(TEST_PROP), "global");
        assertEquals(salPropertyService.getApplicationLinkProperties(APP_ONE_ID).getName(), "app.admin.one");
        assertEquals(salPropertyService.getApplicationLinkProperties(APP_TWO_ID).getName(), "app.admin.two");
        assertEquals(salPropertyService.getProperties(app1).getProperty(TEST_PROP), "app.one");
        assertEquals(salPropertyService.getProperties(app2).getProperty(TEST_PROP), "app.two");
        assertEquals(salPropertyService.getProperties(entity1).getProperty(TEST_PROP), "entity_1");
        assertEquals(salPropertyService.getProperties(entity2).getProperty(TEST_PROP), "entity_2");
        assertEquals(salPropertyService.getProperties(entity3).getProperty(TEST_PROP), "entity_3");
        assertEquals(salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_1).getId()).getProperty(TEST_PROP), "k1t1");
        assertEquals(salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_2).getId()).getProperty(TEST_PROP), "k1t2");
        assertEquals(salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_1).getId()).getProperty(TEST_PROP), "k2t1");
        assertEquals(salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_2).getId()).getProperty(TEST_PROP), "k2t2");

        // check that removes are unique to each property set

        assertEquals(salPropertyService.getGlobalAdminProperties().removeProperty(TEST_PROP), "global");
        salPropertyService.getApplicationLinkProperties(APP_ONE_ID).remove();
        salPropertyService.getApplicationLinkProperties(APP_TWO_ID).remove();
        assertEquals(salPropertyService.getProperties(app1).removeProperty(TEST_PROP), "app.one");
        assertEquals(salPropertyService.getProperties(app2).removeProperty(TEST_PROP), "app.two");
        assertEquals(salPropertyService.getProperties(entity1).removeProperty(TEST_PROP), "entity_1");
        assertEquals(salPropertyService.getProperties(entity2).removeProperty(TEST_PROP), "entity_2");
        assertEquals(salPropertyService.getProperties(entity3).removeProperty(TEST_PROP), "entity_3");
        assertEquals(salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_1).getId()).removeProperty(TEST_PROP), "k1t1");
        assertEquals(salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_2).getId()).removeProperty(TEST_PROP), "k1t2");
        assertEquals(salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_1).getId()).removeProperty(TEST_PROP), "k2t1");
        assertEquals(salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_2).getId()).removeProperty(TEST_PROP), "k2t2");

        // check that removes were successful

        assertEquals(salPropertyService.getGlobalAdminProperties().getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getApplicationLinkProperties(APP_ONE_ID).getName(), null);
        assertEquals(salPropertyService.getApplicationLinkProperties(APP_TWO_ID).getName(), null);
        assertEquals(salPropertyService.getProperties(app1).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getProperties(app2).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getProperties(entity1).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getProperties(entity2).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getProperties(entity3).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_1).getId()).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getLocalEntityProperties("key.one", ((IdentifiableType) TYPE_2).getId()).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_1).getId()).getProperty(TEST_PROP), null);
        assertEquals(salPropertyService.getLocalEntityProperties("key.two", ((IdentifiableType) TYPE_2).getId()).getProperty(TEST_PROP), null);
    }

}
