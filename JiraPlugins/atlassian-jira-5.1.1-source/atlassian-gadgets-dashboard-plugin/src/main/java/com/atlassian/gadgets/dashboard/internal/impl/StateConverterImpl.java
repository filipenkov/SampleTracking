package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.GadgetFactory;
import com.atlassian.gadgets.dashboard.internal.StateConverter;

public class StateConverterImpl implements StateConverter
{
    private final GadgetFactory gadgetFactory;

    public StateConverterImpl(GadgetFactory gadgetFactory)
    {
        this.gadgetFactory = gadgetFactory;
    }

    public Dashboard convertStateToDashboard(DashboardState state, GadgetRequestContext gadgetRequestContext)
    {
        return new DashboardImpl(state, this, gadgetRequestContext);
    }

    public Gadget convertStateToGadget(GadgetState state, GadgetRequestContext gadgetRequestContext)
    {
        return gadgetFactory.createGadget(state, gadgetRequestContext);
    }
}
