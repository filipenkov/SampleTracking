package com.atlassian.gadgets.spec;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;

/**
 * Factory for creating {@code GadgetSpecs}
 */
public interface GadgetSpecFactory
{
    /**
     * Create a {@code GadgetSpec} from a {@code GadgetState}
     * @param gadgetState the state to create a {@code GadgetSpec} from
     * @param gadgetRequestContext the context of this request
     * @return the created {@code GadgetSpec}
     * @throws GadgetParsingException
     */
    GadgetSpec getGadgetSpec(GadgetState gadgetState, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException;

    /**
     * Create a {@code GadgetSpec} from a spec {@code URI}
     * @param uri the spec uri to create a {@code GadgetSpec} from
     * @param gadgetRequestContext the context of this request
     * @return the created {@code GadgetSpec}
     * @throws GadgetParsingException
     */
    GadgetSpec getGadgetSpec(java.net.URI uri, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException;
}
