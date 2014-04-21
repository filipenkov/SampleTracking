package com.atlassian.gadgets.dashboard;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.junit.Test;

import static com.atlassian.gadgets.GadgetState.gadget;
import static org.junit.Assert.assertEquals;

public class DashboardStateTest
{
    @Test
    public void assertThatCanAddGadgetToColumn()
    {
        GadgetState gadget1 = gadget(GadgetId.valueOf("1")).specUri(URI.create("http://gadget/url1")).build();
        GadgetState gadget2 = gadget(GadgetId.valueOf("2")).specUri(URI.create("http://gadget/url2")).build();
        Iterable<GadgetState> column0 = ImmutableList.of(gadget1, gadget2);

        GadgetState gadget3 = gadget(GadgetId.valueOf("3")).specUri(URI.create("http://gadget/url3")).build();
        GadgetState gadget4 = gadget(GadgetId.valueOf("4")).specUri(URI.create("http://gadget/url4")).build();
        Iterable<GadgetState> column1 = ImmutableList.of(gadget3, gadget4);
        
        List<Iterable<GadgetState>> columns = new LinkedList<Iterable<GadgetState>>();
        columns.add(column0);
        columns.add(column1);
        DashboardState state = DashboardState.dashboard(DashboardId.valueOf("1")).title("Menagerie").columns(columns).build();
        GadgetState newGadget = gadget(GadgetId.valueOf("99")).specUri(URI.create("http://gadget/url")).build();

        // prepend gadget and make sure it appears
        DashboardState newState = state.prependGadgetToColumn(newGadget,ColumnIndex.ZERO);
        Iterable<GadgetState> gadgetsInColumn0 = newState.getGadgetsInColumn(ColumnIndex.ZERO);
        assertEquals(newGadget, gadgetsInColumn0.iterator().next());

        // now append a gadget
        newState = state.appendGadgetToColumn(newGadget,ColumnIndex.ONE);
        Iterable<GadgetState> gadgetsInColumn1 = newState.getGadgetsInColumn(ColumnIndex.ONE);
        assertEquals(newGadget, Iterables.get(gadgetsInColumn1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertThatPrependGadgetToNonExistentColumnThrowsIllegalArgumentException()
    {
        GadgetState gadget1 = gadget(GadgetId.valueOf("1")).specUri(URI.create("http://gadget/url1")).build();
        GadgetState gadget2 = gadget(GadgetId.valueOf("2")).specUri(URI.create("http://gadget/url2")).build();
        Iterable<GadgetState> column0 = ImmutableList.of(gadget1, gadget2);

        List<Iterable<GadgetState>> columns = new LinkedList<Iterable<GadgetState>>();
        columns.add(column0);
        DashboardState state = DashboardState.dashboard(DashboardId.valueOf("1")).title("Menagerie").columns(columns).layout(Layout.A).build();
        GadgetState newGadget = gadget(GadgetId.valueOf("99")).specUri(URI.create("http://gadget/url")).build();

        // should throw exception here because there is no column one
        state.prependGadgetToColumn(newGadget,ColumnIndex.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertThatAppendGadgetToNonExistentColumnThrowsIllegalArgumentException()
    {
        GadgetState gadget1 = gadget(GadgetId.valueOf("1")).specUri(URI.create("http://gadget/url1")).build();
        GadgetState gadget2 = gadget(GadgetId.valueOf("2")).specUri(URI.create("http://gadget/url2")).build();
        Iterable<GadgetState> column0 = ImmutableList.of(gadget1, gadget2);

        List<Iterable<GadgetState>> columns = new LinkedList<Iterable<GadgetState>>();
        columns.add(column0);
        DashboardState state = DashboardState.dashboard(DashboardId.valueOf("1")).title("Menagerie").columns(columns).layout(Layout.AA).build();
        GadgetState newGadget = gadget(GadgetId.valueOf("99")).specUri(URI.create("http://gadget/url")).build();

        // should throw exception here because there is no column two
        state.appendGadgetToColumn(newGadget,ColumnIndex.TWO);
    }
}
