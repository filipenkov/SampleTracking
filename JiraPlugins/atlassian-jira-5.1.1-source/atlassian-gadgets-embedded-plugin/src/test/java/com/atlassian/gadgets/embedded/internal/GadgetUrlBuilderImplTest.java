package com.atlassian.gadgets.embedded.internal;

import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.gadgets.spec.UserPrefSpec;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.RenderedGadgetUriBuilder;
import com.atlassian.gadgets.view.SecurityTokenFactory;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static com.atlassian.gadgets.GadgetState.gadget;
import static com.atlassian.gadgets.spec.GadgetSpec.gadgetSpec;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetUrlBuilderImplTest
{
    static final URI SPEC_URI = URI.create("http://gadget/url");
    static final UserPrefSpec USER_PREF_SPEC = UserPrefSpec.userPrefSpec("name1")
        .defaultValue("default_value")
        .build();

    @Mock ApplicationProperties applicationProperties;
    @Mock WebResourceManager webResourceManager;
    @Mock SecurityTokenFactory securityTokenFactory;
    @Mock GadgetSpecFactory gadgetSpecFactory;

    RenderedGadgetUriBuilder uriBuilder;

    @Before
    public void setUp()
    {
        uriBuilder = new GadgetUrlBuilder(applicationProperties, webResourceManager, securityTokenFactory, gadgetSpecFactory);

        when(securityTokenFactory.newSecurityToken(isA(GadgetState.class), eq("fred"))).thenReturn("atlassian:securitytoken");
        when(applicationProperties.getBaseUrl()).thenReturn("http://localhost:8080"); // if has / at end, has dbl slash in output...okay?
    }

    @Test
    public void testBuildRenderedGadgetUrlWithCachingOff() throws Exception
    {
        GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);

        assertThat(buildRenderedGadgetUri(gadgetState), containsString("nocache=1"));
    }
    
    @Test
    public void testRenderedGadgetUrlTurnsOnDebuggingWhenItIsEnabled()
    {
        GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);

        assertThat(buildRenderedGadgetUri(gadgetState), containsString("debug=1"));
    }

    @Test
    public void testBuildRenderedGadgetUrlWhenGadgetHasPrefWithNoDefaultSet() throws Exception
    {
        GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).userPrefs(ImmutableList.of(USER_PREF_SPEC)).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);

        assertThat(buildRenderedGadgetUri(gadgetState), containsString("up_name1=default_value"));
    }

    @Test
    public void testBuildRenderedGadgetUrlWhenGadgetHasPrefWithDefaultSet() throws Exception
    {
        GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).userPrefs(ImmutableMap.of("name1", "value1")).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).userPrefs(ImmutableList.of(USER_PREF_SPEC)).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);

        assertThat(buildRenderedGadgetUri(gadgetState), containsString("up_name1=value1"));
    }

    @Test
    public void testBuildRenderedGadgetUrlWhenGadgetHasPrefWithDefaultSetToEmpty() throws Exception
    {
        GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).userPrefs(ImmutableMap.of("name1", "")).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).userPrefs(ImmutableList.of(USER_PREF_SPEC)).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);

        assertThat(buildRenderedGadgetUri(gadgetState), containsString("up_name1=&"));
    }

    // the outcome of this test should change (to have the build url include &up_name1=value1) when AG-432 is implemented
    @Test
    public void testBuildRenderedGadgetUrlWhenGadgetHasNonSpecPrefSet() throws Exception
    {
        GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).userPrefs(ImmutableMap.of("name1", "value1")).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);

        assertThat(buildRenderedGadgetUri(gadgetState), not(containsString("up_name1=value1")));
    }

    @Test
    public void testBuildGadgetUrlWithViewParams() throws Exception
    {
        GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);

        String url = buildRenderedGadgetUriWithViewParams(
            gadgetState, ImmutableMap.of("writable", "false", "custom-param", "custom-value"));
        assertThat(url, containsString("view-params=%7B%22writable%22%3A%22false%22%2C%22custom-param%22%3A%22custom-value%22%7D"));
    }

    @Test
    public void testBuildRenderedGadgetUrlResolvesRelativeSpecUrl() throws Exception
    {
        URI specUri = URI.create("relative/gadget.xml");
        GadgetState gadgetState = gadget(GadgetId.valueOf("a:b")).specUri(specUri).build();

        GadgetSpec spec = gadgetSpec(SPEC_URI).userPrefs(Collections.<UserPrefSpec>emptyList()).build();
        when(gadgetSpecFactory.getGadgetSpec(eq(specUri), isA(GadgetRequestContext.class))).thenReturn(spec);

        assertThat(buildRenderedGadgetUri(gadgetState), containsString("url=http%3A%2F%2Flocalhost%3A8080%2Frelative%2Fgadget.xml"));
    }

    private String buildRenderedGadgetUri(GadgetState gadgetState)
    {
        return buildRenderedGadgetUriWithViewParams(gadgetState, Collections.<String, String>emptyMap());
    }

    private String buildRenderedGadgetUriWithViewParams(GadgetState gadgetState, Map<String, String> viewParams)
    {
        return uriBuilder.build(
            gadgetState,
            ModuleId.valueOf(1),
            new View.Builder().viewType(ViewType.CANVAS).addViewParams(viewParams).build(),
            gadgetRequestContext().locale(Locale.UK).viewer("fred").ignoreCache(true).debug(true).build()
        ).toString();
    }
}
