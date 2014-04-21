package com.atlassian.gadgets.dashboard.internal;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardState;

/**
 * Converts stored states into usable objects
 */
public interface StateConverter
{
    /**
     * Creates a {@code Dashboard} from a {@code DashboardState}
     * @param state the state to convert
     * @param gadgetRequestContext the context of this request
     * @return the created {@code Dashboard}
     */
    Dashboard convertStateToDashboard(DashboardState state, GadgetRequestContext gadgetRequestContext);

    /**
     * Creates a {@code Gadget} from a {@code GadgetState}
     * @param state the state to convert
     * @param gadgetRequestContext the context of this request
     * @return the created {@code Gadget}
     */
    Gadget convertStateToGadget(GadgetState state, GadgetRequestContext gadgetRequestContext);
}
