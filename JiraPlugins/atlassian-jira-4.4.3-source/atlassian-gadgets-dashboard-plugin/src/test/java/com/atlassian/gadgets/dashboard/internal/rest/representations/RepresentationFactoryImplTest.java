package com.atlassian.gadgets.dashboard.internal.rest.representations;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardUrlBuilder;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.impl.GadgetImpl;
import com.atlassian.gadgets.spec.DataType;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.UserPrefSpec;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.RenderedGadgetUriBuilder;
import com.atlassian.gadgets.view.View;

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
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class RepresentationFactoryImplTest
{
    private static final String TEST_DASHBOARD_ID = "100";

    @Mock
    RenderedGadgetUriBuilder renderedGadgetUriBuilder;
    @Mock
    DashboardUrlBuilder dashboardUrlBuilder;
    @Mock
    private Dashboard dashboard;

    private final DashboardId dashboardId = DashboardId.valueOf(TEST_DASHBOARD_ID);
    private GadgetRequestContext gadgetRequestContext;
    private RepresentationFactory factory;
    private static final String TEST_DASHBOARD_TITLE = "Test Dashboard";

    @Before
    public void setUp()
    {
        gadgetRequestContext = gadgetRequestContext().build();
        factory = new RepresentationFactoryImpl(renderedGadgetUriBuilder, dashboardUrlBuilder);
        when(dashboard.getId()).thenReturn(dashboardId);
        when(dashboard.getLayout()).thenReturn(Layout.AA);
        when(dashboard.getTitle()).thenReturn(TEST_DASHBOARD_TITLE);
    }

    @Test
    public void testCreateDashboardRepresentationSuccessWithNoGadgets()
    {
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO)).thenReturn(Collections.<Gadget>emptyList());
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ONE)).thenReturn(Collections.<Gadget>emptyList());

        final DashboardRepresentation representation = factory.createDashboardRepresentation(dashboard, gadgetRequestContext, true);
        assertEquals(TEST_DASHBOARD_ID, representation.getId());
        assertEquals(TEST_DASHBOARD_TITLE, representation.getTitle());
        assertEquals(Layout.AA, representation.getLayout());
        assertTrue(representation.isWritable());
        assertTrue(representation.getGadgets().isEmpty());
    }

    @Test
    public void testCreateDashboardRepresentationSuccess()
    {
        GadgetSpec spec = gadgetSpec(URI.create("http://example.com/gadget.xml"))
                .title("Test Gadget")
                .userPrefs(ImmutableList.of(
                        UserPrefSpec.userPrefSpec("Title Pref")
                                .dataType(DataType.ENUM)
                                .displayName("Title Pref display Name")
                                .defaultValue("MyTitle")
                                .enumValues(ImmutableMap.<String, String>builder()
                                        .put("MyTitle", "MyTitleDisplay")
                                        .put("SomeTitle", "SomeTitleDisplay")
                                        .build()
                                )
                                .build()
                ))
                .build();

        final GadgetId gadgetId = GadgetId.valueOf("1");
        GadgetState gadgetState = gadget(gadgetId).specUri(spec.getUrl()).build();
        Gadget gadget = new GadgetImpl(gadgetState, spec);

        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO)).thenReturn(newArrayList(gadget));
        when(renderedGadgetUriBuilder.build(isA(GadgetState.class),
                                            eq(ModuleId.valueOf(1)),
                                            isA(View.class),
                                            same(gadgetRequestContext)))
            .thenReturn(URI.create("/ifr/gadgetId/blah"));
        when(dashboardUrlBuilder.buildGadgetUserPrefsUrl(dashboardId, gadgetId)).thenReturn("/userprefs/action/url");
        when(dashboardUrlBuilder.buildGadgetUrl(dashboardId, gadgetId)).thenReturn("/gadget/url");
        when(dashboardUrlBuilder.buildGadgetColorUrl(dashboardId, gadgetId)).thenReturn("/gadget/color/url");
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ONE)).thenReturn(Collections.<Gadget>emptyList());

        final DashboardRepresentation representation = factory.createDashboardRepresentation(dashboard, gadgetRequestContext, false);
        assertEquals(TEST_DASHBOARD_ID, representation.getId());
        assertEquals(TEST_DASHBOARD_TITLE, representation.getTitle());
        assertEquals(Layout.AA, representation.getLayout());
        assertFalse(representation.isWritable());
        assertEquals(1, representation.getGadgets().size());
        final GadgetRepresentation gadgetRepresentation = representation.getGadgets().get(0);
        assertEquals("1", gadgetRepresentation.getId());
        assertEquals("Test Gadget", gadgetRepresentation.getTitle());
        assertEquals(Color.color7, gadgetRepresentation.getColor());
        assertEquals("/gadget/color/url", gadgetRepresentation.getColorUrl());
        assertEquals(Integer.valueOf(0), gadgetRepresentation.getColumn());
        assertEquals("http://example.com/gadget.xml", gadgetRepresentation.getGadgetSpecUrl());
        assertTrue(gadgetRepresentation.getHasNonHiddenUserPrefs());
        assertNull(gadgetRepresentation.getHeight());
        assertEquals("/gadget/url", gadgetRepresentation.getGadgetUrl());
        assertFalse(gadgetRepresentation.isMaximizable());
        assertEquals("/ifr/gadgetId/blah", gadgetRepresentation.getRenderedGadgetUrl());
        assertNull(gadgetRepresentation.getTitleUrl());

        final UserPrefsRepresentation prefsRepresentation = gadgetRepresentation.getUserPrefs();
        assertEquals("/userprefs/action/url", prefsRepresentation.getAction());
        final List<UserPrefRepresentation> userPrefRepresentations = prefsRepresentation.getFields();
        final UserPrefRepresentation userPref = userPrefRepresentations.get(0);
        assertEquals("enum", userPref.getType());
        assertEquals("Title Pref display Name", userPref.getDisplayName());
        assertEquals("Title Pref", userPref.getName());
        assertEquals("MyTitle", userPref.getValue());

        final List<UserPrefRepresentation.EnumValueRepresentation> options = userPref.getOptions();
        final UserPrefRepresentation.EnumValueRepresentation firstOption = options.get(0);
        assertEquals("MyTitleDisplay", firstOption.getLabel());
        assertEquals("MyTitle", firstOption.getValue());
        assertTrue(firstOption.isSelected());

        final UserPrefRepresentation.EnumValueRepresentation secondOption = options.get(1);
        assertEquals("SomeTitleDisplay", secondOption.getLabel());
        assertEquals("SomeTitle", secondOption.getValue());
        assertFalse(secondOption.isSelected());
    }

}
