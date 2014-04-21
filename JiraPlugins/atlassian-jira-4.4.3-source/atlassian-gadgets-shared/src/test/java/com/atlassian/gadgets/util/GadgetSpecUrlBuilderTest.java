package com.atlassian.gadgets.util;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecUrlBuilderTest
{
    private static final String BASE_GADGET_SPEC_PATH = "rest/gadgets/1.0/g/";
    private static final String PLUGIN_KEY = "test.plugin";
    private static final String RESOURCE_LOCATION = "p@th/to/gadget.xml";
    private static final String ENCODED_RESOURCE_LOCATION = "p%40th/to/gadget.xml";
    private static final String GADGET_SPEC_PATH = BASE_GADGET_SPEC_PATH + PLUGIN_KEY + "/" + ENCODED_RESOURCE_LOCATION;
    private static final String MOD_KEY = "module001";
    private static final String GADGET_SPEC_PATH_WITH_MODKEY = BASE_GADGET_SPEC_PATH + PLUGIN_KEY + ":" + MOD_KEY + "/" + ENCODED_RESOURCE_LOCATION;
    private static final String BASE_URL = "http://example.com/dashboards";
    private static final String ABSOLUTE_GADGET_SPEC_URL = BASE_URL + "/" + GADGET_SPEC_PATH;
    private static final String ABSOLUTE_GADGET_SPEC_URL_MODKEY = BASE_URL + "/" + GADGET_SPEC_PATH_WITH_MODKEY;

    @Mock ApplicationProperties applicationProperties;
    @Mock WebResourceManager webResourceManager;

    GadgetSpecUrlBuilder urlBuilder;

    @Before
    public void setUp()
    {
        when(applicationProperties.getBaseUrl()).thenReturn(BASE_URL);
        urlBuilder = new GadgetSpecUrlBuilder(applicationProperties);
    }

    @Test
    public void testBuildGadgetSpecUrlFromPluginKeyAndLocationOnlyEncodesComponents()
    {
        assertEquals(GADGET_SPEC_PATH_WITH_MODKEY,
                     urlBuilder.buildGadgetSpecUrl(PLUGIN_KEY, MOD_KEY, RESOURCE_LOCATION));
    }

    @Test
    public void testParseGadgetSpecUrlParsesValidRelativeGadgetUrl()
    {
        assertEquals(new PluginGadgetSpec.Key(PLUGIN_KEY, RESOURCE_LOCATION),
                     urlBuilder.parseGadgetSpecUrl(GADGET_SPEC_PATH));
    }

    @Test
    public void testParseGadgetSpecUrlParsesValidAbsoluteGadgetUrlForThisServer()
    {
        assertEquals(new PluginGadgetSpec.Key(PLUGIN_KEY, RESOURCE_LOCATION),
                     urlBuilder.parseGadgetSpecUrl(ABSOLUTE_GADGET_SPEC_URL));
    }

    @Test
    public void testParseGadgetSpecUrlParsesValidAbsoluteGadgetUrlForThisServerWithModuleKey()
    {
        assertEquals(new PluginGadgetSpec.Key(PLUGIN_KEY, RESOURCE_LOCATION),
                     urlBuilder.parseGadgetSpecUrl(ABSOLUTE_GADGET_SPEC_URL_MODKEY));
    }

    @Test
    public void testParseGadgetSpecUrlParsesValidRelativeGadgetUrlForThisServerWithModuleKey()
    {
        assertEquals(new PluginGadgetSpec.Key(PLUGIN_KEY, RESOURCE_LOCATION),
                     urlBuilder.parseGadgetSpecUrl(GADGET_SPEC_PATH_WITH_MODKEY));
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void testParseGadgetSpecUrlFromAnotherServerThrowsInvalidGadgetSpecUriException()
    {
        urlBuilder.parseGadgetSpecUrl("http://otherhost:8080/" + GADGET_SPEC_PATH);
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void testParseGadgetSpecUrlMissingPluginKeyAndResourceLocationThrowsInvalidGadgetSpecUriException()
    {
        urlBuilder.parseGadgetSpecUrl(BASE_GADGET_SPEC_PATH);
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void testParseGadgetSpecUrlWithBlankPluginKeyThrowsInvalidGadgetSpecUriException()
    {
        urlBuilder.parseGadgetSpecUrl(BASE_GADGET_SPEC_PATH + "/" + ENCODED_RESOURCE_LOCATION);
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void testParseGadgetSpecUrlMissingPluginKeyThrowsInvalidGadgetSpecUriException()
    {
        // if we use ENCODED_RESOURCE_LOCATION here, it will think "p@ath" is a plugin key
        urlBuilder.parseGadgetSpecUrl(BASE_GADGET_SPEC_PATH + "gadget.xml");
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void testParseGadgetSpecUrlWithBlankResourceLocationThrowsInvalidGadgetSpecUriException()
    {
        urlBuilder.parseGadgetSpecUrl(BASE_GADGET_SPEC_PATH + PLUGIN_KEY + "/");
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void testParseGadgetSpecUrlMissingResourceLocationThrowsInvalidGadgetSpecUriException()
    {
        urlBuilder.parseGadgetSpecUrl(BASE_GADGET_SPEC_PATH + PLUGIN_KEY);
    }

    /* Null checking */

    @Test(expected=NullPointerException.class)
    public void testConstructGadgetSpecUrlBuilderWithNullApplicationPropertiesThrowsNullPointerException()
    {
        new GadgetSpecUrlBuilder(null);
    }


    @Test(expected=NullPointerException.class)
    public void testBuildGadgetSpecUrlWithNullPluginKeyThrowsNullPointerException()
    {
        urlBuilder.buildGadgetSpecUrl(null, MOD_KEY, RESOURCE_LOCATION);
    }

    @Test(expected=NullPointerException.class)
    public void testBuildGadgetSpecUrlWithNullLocationThrowsNullPointerException()
    {
        urlBuilder.buildGadgetSpecUrl(PLUGIN_KEY, MOD_KEY, null);
    }

    @Test(expected=NullPointerException.class)
    public void testBuildGadgetSpecUrlWithNullModuleKeyThrowsNullPointerException()
    {
        urlBuilder.buildGadgetSpecUrl(PLUGIN_KEY, null, RESOURCE_LOCATION);
    }

    @Test(expected=NullPointerException.class)
    public void testParseGadgetSpecUrlWithNullGadgetSpecUrlThrowsNullPointerException()
    {
        urlBuilder.parseGadgetSpecUrl(null);
    }
}
