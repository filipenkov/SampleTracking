package com.atlassian.gadgets.view;

import com.atlassian.gadgets.GadgetState;

/**
 * A factory for generating security tokens that is used when rendering gadgets.  
 */
public interface SecurityTokenFactory
{
    /**
     * Create a new security token for the gadget.  The {@code viewer} of the gadget is the user actually
     * making the request for the gadget to be displayed, but may not be the owner.
     * 
     * @param state Gadget to generate a security token for
     * @param viewer user that is making the request for the gadget to be displayed
     * @return security token that encodes the owner, viewer, and gadget information in a secure manner
     */
    String newSecurityToken(GadgetState state, String viewer);
}
