package com.atlassian.gadgets.dashboard.internal;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetState;

public interface GadgetFactory
{
    Gadget createGadget(String gadgetSpecUrl, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException, GadgetSpecUriNotAllowedException;
    Gadget createGadget(GadgetState state, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException, GadgetSpecUriNotAllowedException;
}
