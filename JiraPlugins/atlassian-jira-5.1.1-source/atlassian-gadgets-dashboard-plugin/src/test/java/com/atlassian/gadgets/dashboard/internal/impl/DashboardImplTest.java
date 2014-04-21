package com.atlassian.gadgets.dashboard.internal.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetNotFoundException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.GadgetLayoutException;
import com.atlassian.gadgets.dashboard.internal.StateConverter;
import com.atlassian.gadgets.dashboard.internal.UserPref;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.gadgets.dashboard.spi.changes.AddGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.gadgets.dashboard.spi.changes.GadgetColorChange;
import com.atlassian.gadgets.dashboard.spi.changes.RemoveGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.UpdateGadgetUserPrefsChange;
import com.atlassian.gadgets.dashboard.spi.changes.UpdateLayoutChange;
import com.atlassian.gadgets.spec.DataType;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.UserPrefSpec;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static com.atlassian.gadgets.GadgetState.gadget;
import static com.atlassian.gadgets.dashboard.GadgetLayoutFactory.column;
import static com.atlassian.gadgets.dashboard.GadgetLayoutFactory.emptyGadgetLayout;
import static com.atlassian.gadgets.dashboard.GadgetLayoutFactory.newGadgetLayout;
import static com.atlassian.gadgets.spec.GadgetSpec.gadgetSpec;
import static com.atlassian.hamcrest.DeepIsEqual.deeplyEqualTo;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardImplTest
{
    @Mock StateConverter stateConverter;
    
    Dashboard dashboard;

    @Before
    public void setUp()
    {
        when(stateConverter.convertStateToGadget(isA(GadgetState.class), isA(GadgetRequestContext.class)))
            .thenAnswer(new Answer<Gadget>()
            {
                public Gadget answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    GadgetState state = (GadgetState) invocationOnMock.getArguments()[0];
                    Gadget gadget = mock(Gadget.class);
                    when(gadget.getState()).thenReturn(state);
                    when(gadget.getId()).thenReturn(state.getId());
                    when(gadget.getUserPrefs()).thenReturn(convertUserPrefs(state));
                    return gadget;
                }
            });

        DashboardState state = DashboardState.dashboard(DashboardId.valueOf("100")).title("Atlassian Menagerie").layout(Layout.AA).build();
        dashboard = new DashboardImpl(state, stateConverter, gadgetRequestContext().build());
    }

    @Test
    public void assertThatDefaultLayoutIs2EqualColumns()
    {
        assertThat(dashboard.getLayout(), is(equalTo(Layout.AA)));
    }

    @Test
    public void assertThatColumn1ExistsWhenLayoutIs2EqualColumns()
    {
        assertThat(dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator().hasNext(), is(false));
    }

    @Test(expected=IllegalArgumentException.class)
    public void assertThatColumn3DoesNotExistWhenLayoutIs2EqualColumns()
    {
        dashboard.getGadgetsInColumn(ColumnIndex.TWO);
    }

    @Test
    public void assertThatColumn3ExistsAfterChangingTo3ColumnLayout() throws GadgetLayoutException
    {
        dashboard.changeLayout(Layout.AAA, emptyGadgetLayout(Layout.AAA));
        assertThat(dashboard.getGadgetsInColumn(ColumnIndex.TWO).iterator().hasNext(), is(false));
    }

    @Test(expected=IllegalArgumentException.class)
    public void assertThatColumn2DoesNotExistAfterChangingTo1ColumnLayout() throws GadgetLayoutException
    {
        dashboard.changeLayout(Layout.A, emptyGadgetLayout(Layout.A));
        dashboard.getGadgetsInColumn(ColumnIndex.TWO);
    }

    @Test
    public void assertThatAppendGadgetWithoutColumnParameterAppendsToFirstColumn()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        dashboard.appendGadget(gadget1);
        dashboard.appendGadget(gadget2);

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
    }

    private Gadget createGadget(String id)
    {
        Gadget gadget = mock(Gadget.class);
        GadgetId gadgetId = GadgetId.valueOf(id);

        Map<String, String> initialPrefs = new HashMap<String, String>();
        initialPrefs.put("pref1","initial_value");
        GadgetState state = GadgetState.gadget(gadgetId).specUri(URI.create("http://specs/" + id)).userPrefs(initialPrefs).build(); //

        when(gadget.getState()).thenReturn(state);
        when(gadget.getId()).thenReturn(gadgetId);
        return gadget;
    }

    @Test
    public void assertThatAppendGadgetWithColumnParameterAppendsToDesiredColumn()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        dashboard.appendGadget(ColumnIndex.ONE, gadget1);
        dashboard.appendGadget(ColumnIndex.ONE, gadget2);

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
    }

    @Test
    public void assertThatAddGadgetWithoutColumnParameterPrependsToFirstColumn()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        dashboard.addGadget(gadget1);
        dashboard.addGadget(gadget2);

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
    }

    @Test
    public void assertThatAddGadgetWithColumnParameterPrependsToDesiredColumn()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        dashboard.addGadget(ColumnIndex.ONE, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
    }

    @Test
    public void assertThatDashboardContainsAddedGadgets()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        dashboard.addGadget(gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
        assertFalse(columnIt.hasNext());

        columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget3.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
        assertFalse(columnIt.hasNext());
    }

    @Test
    public void assertThatAllGadgetsAreInTheFirstColumnAfterChangingTo1ColumnLayout() throws GadgetLayoutException
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        dashboard.addGadget(gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        dashboard.changeLayout(Layout.A, newGadgetLayout(column(gadget1, gadget2, gadget3)));

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget3.getState())));
    }

    @Test(expected=GadgetLayoutException.class)
    public void assertThatChangeLayoutThrowsExceptionIfGadgetLayoutContainsMoreColumnsThanDashboardLayoutAllows() throws GadgetLayoutException
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        dashboard.addGadget(gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);

        dashboard.changeLayout(Layout.A, newGadgetLayout(column(gadget1), column(gadget2)));
    }

    @Test
    public void assertThatGadgetsAreInTheRightPlaceAfterRearranging() throws GadgetLayoutException
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        Gadget gadget4 = createGadget("4");
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);
        dashboard.addGadget(ColumnIndex.ZERO, gadget4);

        dashboard.rearrangeGadgets(newGadgetLayout(column(gadget3, gadget4), column(gadget2, gadget1)));

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget3.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget4.getState())));

        columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
    }

    @Test
    public void assertThatGadgetsNotOnTheDashboardAreIgnoredWhenRearranging() throws GadgetLayoutException
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        Gadget gadget4 = createGadget("4");
        Gadget gadgetNotOnDashboard = createGadget("doesnotexist");
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);
        dashboard.addGadget(ColumnIndex.ZERO, gadget4);

        dashboard.rearrangeGadgets(newGadgetLayout(column(gadget3, gadget4), column(gadget2, gadgetNotOnDashboard, gadget1)));

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget3.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget4.getState())));

        columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
    }

    @Test(expected=GadgetLayoutException.class)
    public void assertThatGadgetLayoutExceptionIsThrownIfProvidedGadgetLayoutContainsMoreColumnsThanDashboardLayout() throws GadgetLayoutException
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        dashboard.rearrangeGadgets(newGadgetLayout(column(gadget1), column(gadget2), column(gadget3)));
    }

    @Test(expected=GadgetLayoutException.class)
    public void assertThatGadgetLayoutExceptionIsThrownIfProvidedGadgetLayoutDoesNotContainAllGadgetsOnTheDashboard() throws GadgetLayoutException
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        dashboard.rearrangeGadgets(newGadgetLayout(column(gadget1), column(gadget3)));
    }

    @Test
    public void assertThatGadgetColorGetsChanged()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        when(gadget2.isLoaded()).thenReturn(true);
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        dashboard.changeGadgetColor(gadget3.getId(), Color.color1);

        GadgetState resultantState = GadgetState.gadget(gadget3.getState()).color(Color.color1).build();
        assertThat(dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator().next().getState(), is(equalTo(resultantState)));
    }

    @Test(expected=GadgetNotFoundException.class)
    public void assertThatTryingToChangeTheColorOfNonExistentGadgetThrowsException()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        dashboard.changeGadgetColor(GadgetId.valueOf("2000"), Color.color1);
    }

    @Test
    public void assertThatRemoveGadgetReallyRemovesTheGadget()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        dashboard.removeGadget(gadget2.getId());

        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
        assertFalse(columnIt.hasNext());

        columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget3.getState())));
        assertFalse(columnIt.hasNext());
    }

    @Test
    public void assertThatTryingToRemoveNonExistentGadgetDoesNothing()
    {
        Gadget gadget1 = createGadget("1");
        Gadget gadget2 = createGadget("2");
        Gadget gadget3 = createGadget("3");
        dashboard.addGadget(ColumnIndex.ZERO, gadget1);
        dashboard.addGadget(ColumnIndex.ONE, gadget2);
        dashboard.addGadget(ColumnIndex.ONE, gadget3);

        dashboard.removeGadget(GadgetId.valueOf("2000"));
        
        Iterator<Gadget> columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget1.getState())));
        assertFalse(columnIt.hasNext());
        
        columnIt = dashboard.getGadgetsInColumn(ColumnIndex.ONE).iterator();
        assertThat(columnIt.next().getState(), is(sameInstance(gadget3.getState())));
        assertThat(columnIt.next().getState(), is(sameInstance(gadget2.getState())));
        assertFalse(columnIt.hasNext());
    }

    @Test
    public void testUpdateUserPrefs()
    {
        final List<UserPrefSpec> userPrefs = ImmutableList.of
        (
            UserPrefSpec.userPrefSpec("pref1").defaultValue("defaultvalue").displayName("Pref 1").required(true).build()
        );
        GadgetSpec gadgetSpec = gadgetSpec(URI.create("http://gadget/url")).userPrefs(userPrefs).build();
        GadgetId gadgetId = GadgetId.valueOf("1");
        GadgetState gadgetState = gadget(gadgetId).specUri(URI.create("http://gadget/url")).build();

        dashboard.addGadget(new GadgetImpl(gadgetState, gadgetSpec));

        setUpUserPrefs(userPrefs);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("pref1", "value1");

        dashboard.updateGadgetUserPrefs(gadgetId, parameters);

        GadgetState resultState = GadgetState.gadget(gadgetState).userPrefs(parameters).build();
        assertThat(dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator().next().getState(), is(equalTo(resultState)));
    }

    private void setUpUserPrefs(final List<UserPrefSpec> userPrefs) {
        when(stateConverter.convertStateToGadget(isA(GadgetState.class), isA(GadgetRequestContext.class)))
            .thenAnswer(new Answer<Gadget>()
            {
                public Gadget answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    GadgetState state = (GadgetState) invocationOnMock.getArguments()[0];
                    Gadget gadget = mock(Gadget.class);
                    when(gadget.getState()).thenReturn(state);
                    when(gadget.getId()).thenReturn(state.getId());

                    UserPref userPref = new UserPrefImpl(userPrefs.get(0), state.getUserPrefs().get("pref1"));
                    when(gadget.getUserPrefs()).thenReturn(ImmutableList.of(userPref));

                    return gadget;
                }
            });
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateUserPrefsRejectsNullValueForRequiredPref()
    {
        List<UserPrefSpec> userPrefs = ImmutableList.of
        (
            UserPrefSpec.userPrefSpec("pref1").defaultValue("defaultvalue").displayName("Pref 1").required(true).build()
        );
        GadgetSpec gadgetSpec = gadgetSpec(URI.create("http://gadget/url")).userPrefs(userPrefs).build();
        GadgetId gadgetId = GadgetId.valueOf("1");
        GadgetState gadgetState = gadget(gadgetId).specUri(URI.create("http://gadget/url")).build();

        dashboard.addGadget(new GadgetImpl(gadgetState, gadgetSpec));

        setUpUserPrefs(userPrefs);

        Map<String, String> newValues = new HashMap<String, String>();
        newValues.put("pref1", null);
        dashboard.updateGadgetUserPrefs(gadgetId, newValues);
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateUserPrefsRejectsEmptyStringForRequiredPref()
    {
        List<UserPrefSpec> userPrefs = ImmutableList.of
        (
            UserPrefSpec.userPrefSpec("pref1").defaultValue("defaultvalue").displayName("Pref 1").required(true).build()
        );
        GadgetSpec gadgetSpec = gadgetSpec(URI.create("http://gadget/url")).userPrefs(userPrefs).build();
        GadgetId gadgetId = GadgetId.valueOf("1");
        GadgetState gadgetState = gadget(gadgetId).specUri(URI.create("http://gadget/url")).build();

        dashboard.addGadget(new GadgetImpl(gadgetState, gadgetSpec));

        setUpUserPrefs(userPrefs);

        Map<String, String> newValues = new HashMap<String, String>();
        newValues.put("pref1", "");
        dashboard.updateGadgetUserPrefs(gadgetId, newValues);
    }

    @Test
    public void updateUserPrefsTransformsEmptyStringIntoFalseValueForRequiredBooleanPref()
    {
        List<UserPrefSpec> userPrefs = ImmutableList.of
        (
            UserPrefSpec.userPrefSpec("pref1").dataType(DataType.BOOL).defaultValue("true").required(true).build()
        );
        GadgetSpec gadgetSpec = gadgetSpec(URI.create("http://gadget/url")).userPrefs(userPrefs).build();
        GadgetId gadgetId = GadgetId.valueOf("1");
        GadgetState gadgetState = gadget(gadgetId).specUri(URI.create("http://gadget/url")).build();

        dashboard.addGadget(new GadgetImpl(gadgetState, gadgetSpec));

        setUpUserPrefs(userPrefs);

        Map<String, String> newValues = new HashMap<String, String>();
        newValues.put("pref1", "");
        dashboard.updateGadgetUserPrefs(gadgetId, newValues);

        assertEquals(dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator().next().getState().getUserPrefs().get("pref1"), "false");
    }

    @Test
    public void updateUserPrefsAcceptsEmptyStringForNonRequiredPref()
    {
        List<UserPrefSpec> userPrefs = ImmutableList.of
        (
            UserPrefSpec.userPrefSpec("pref1").defaultValue("defaultvalue").displayName("Pref 1").required(false).build()
        );
        GadgetSpec gadgetSpec = gadgetSpec(URI.create("http://gadget/url")).userPrefs(userPrefs).build();
        GadgetId gadgetId = GadgetId.valueOf("1");
        GadgetState gadgetState = gadget(gadgetId).specUri(URI.create("http://gadget/url")).build();

        dashboard.addGadget(new GadgetImpl(gadgetState, gadgetSpec));

        setUpUserPrefs(userPrefs);

        Map<String, String> newValues = new HashMap<String, String>();
        newValues.put("pref1", "");
        dashboard.updateGadgetUserPrefs(gadgetId, newValues);

        assertEquals(dashboard.getGadgetsInColumn(ColumnIndex.ZERO).iterator().next().getState().getUserPrefs().get("pref1"), "");
    }

    @Test
    public void assertThatAppendGadgetOperationGetsRecordedInChangesList()
    {
        Gadget gadget1 = createGadget("1");
        dashboard.appendGadget(ColumnIndex.ONE, gadget1);

        List<DashboardChange> changesList = dashboard.getChanges();

        assertThat((AddGadgetChange)changesList.get(0), is(deeplyEqualTo(new AddGadgetChange(gadget1.getState(), ColumnIndex.ONE, 0))));
    }

    @Test
    public void assertThatPrependGadgetOperationGetsRecordedInChangesList()
    {
        Gadget gadget1 = createGadget("1");
        dashboard.addGadget(ColumnIndex.ONE, gadget1);

        List<DashboardChange> changesList = dashboard.getChanges();

        assertThat((AddGadgetChange)changesList.get(0), is(deeplyEqualTo(new AddGadgetChange(gadget1.getState(), ColumnIndex.ONE, 0))));
    }

    @Test
    public void assertThatRemoveGadgetOperationGetsRecordedInChangesList()
    {
        Gadget gadget1 = createGadget("1");
        dashboard.addGadget(ColumnIndex.ONE, gadget1);

        dashboard.removeGadget(gadget1.getId());

        List<DashboardChange> changesList = dashboard.getChanges();
        // first change in list is for adding the gadget (which we did in order to be able to remove it)
        assertThat((RemoveGadgetChange)changesList.get(1), is(deeplyEqualTo(new RemoveGadgetChange(gadget1.getId()))));
    }

    @Test
    public void assertThatRearrangeGadgetsOperationGetsRecordedInChangesList()
    {
        Gadget gadget1 = createGadget("1");
        dashboard.appendGadget(ColumnIndex.ONE, gadget1);
        dashboard.clearChanges();

        List <Iterable<GadgetId>> gadgetLayoutList = new ArrayList<Iterable<GadgetId>>();
        gadgetLayoutList.add(ImmutableList.<GadgetId>of(gadget1.getId()));
        gadgetLayoutList.add(ImmutableList.<GadgetId>of()); // in the new layout, column 1 is empty
        GadgetLayout gadgetLayout = new GadgetLayout(gadgetLayoutList);

        dashboard.rearrangeGadgets(gadgetLayout);

        List<DashboardChange> changesList = dashboard.getChanges();
        assertThat((UpdateLayoutChange)changesList.get(0), is(deeplyEqualTo(new UpdateLayoutChange(Layout.AA, gadgetLayout))));
    }

    @Test
    public void assertThatChangeLayoutOperationGetsRecordedInChangesList()
    {
        Gadget gadget1 = createGadget("1");
        dashboard.appendGadget(ColumnIndex.ONE, gadget1);
        dashboard.clearChanges();

        List <Iterable<GadgetId>> gadgetLayoutList = new ArrayList<Iterable<GadgetId>>();
        gadgetLayoutList.add(ImmutableList.<GadgetId>of());
        gadgetLayoutList.add(ImmutableList.<GadgetId>of(gadget1.getId()));
        GadgetLayout gadgetLayout = new GadgetLayout(gadgetLayoutList);

        dashboard.changeLayout(Layout.AB, gadgetLayout);

        List<DashboardChange> changesList = dashboard.getChanges();

        assertThat((UpdateLayoutChange)changesList.get(0), is(deeplyEqualTo(new UpdateLayoutChange(Layout.AB, gadgetLayout))));
    }

    @Test
    public void assertThatUpdateGadgetUserPrefsOperationGetsRecordedInChangesList()
    {
        List<UserPref> initialPrefs = new ArrayList<UserPref>();
        UserPref initialPref = mock(UserPref.class);
        when(initialPref.getName()).thenReturn("pref1");
        when(initialPref.getValue()).thenReturn("initial_value");
        initialPrefs.add(initialPref);

        Gadget gadget1 = createGadget("1");
        when(gadget1.getUserPrefs()).thenReturn(initialPrefs);
        when(gadget1.getColor()).thenReturn(Color.color4);

        dashboard.addGadget(ColumnIndex.ONE, gadget1);

        Map<String, String> newPrefs = new HashMap<String, String>();
        newPrefs.put("pref1","new_value");

        dashboard.updateGadgetUserPrefs(gadget1.getId(), newPrefs);

        List<DashboardChange> changesList = dashboard.getChanges();

        // first change in list is for adding the gadget (which we did in order to be able to change its preferences)
        assertThat((UpdateGadgetUserPrefsChange)changesList.get(1), is(deeplyEqualTo(new UpdateGadgetUserPrefsChange(gadget1.getId(), newPrefs))));
    }

    @Test
    public void assertThatChangeGadgetColorOperationGetsRecordedInChangesList()
    {
        Gadget gadget1 = createGadget("1");
        dashboard.appendGadget(ColumnIndex.ONE, gadget1);
        dashboard.clearChanges();

        dashboard.changeGadgetColor(gadget1.getId(), Color.color5);

        List<DashboardChange> changesList = dashboard.getChanges();

        assertThat((GadgetColorChange)changesList.get(0), is(deeplyEqualTo(new GadgetColorChange(gadget1.getId(), Color.color5))));
    }

    @Test
    public void assertThatChangesListGetsCleared()
    {
        Gadget gadget1 = createGadget("1");
        dashboard.appendGadget(ColumnIndex.ONE, gadget1);

        // make sure there is something actually in the list now
        List<DashboardChange> changesList = dashboard.getChanges();
        assertThat((AddGadgetChange)changesList.get(0), is(deeplyEqualTo(new AddGadgetChange(gadget1.getState(), ColumnIndex.ONE, 0))));

        dashboard.clearChanges();

        changesList = dashboard.getChanges();
        assertThat(changesList.size(), is(equalTo(0)));
    }

    private static <T> Matcher<? super T> equalTo(T actual)
    {
        return Matchers.equalTo(actual);
    }

    private Iterable<UserPref> convertUserPrefs(GadgetState state)
    {
        if (state.getUserPrefs() != null)
        {
            ImmutableList.Builder<UserPref> prefsBuilder = ImmutableList.builder();
            for (Map.Entry<String, String> entry : state.getUserPrefs().entrySet())
            {
                prefsBuilder.add(new UserPrefImpl(UserPrefSpec.userPrefSpec(entry.getKey()).defaultValue(entry.getValue()).build(), entry.getValue()));
            }
            return prefsBuilder.build();
        }
        else
        {
            return ImmutableList.of();
        }
    }
}
