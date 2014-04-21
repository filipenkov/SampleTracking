package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;

import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.opensocial.spi.GadgetSpecUrlRenderPermission;
import com.atlassian.gadgets.util.Uri;

/**
 * Simple implementation that allows only syntactically valid URIs which are
 * present in the directory.
 */
public class GadgetSpecUrlRenderPermissionImpl implements GadgetSpecUrlRenderPermission
{
    private final Directory directory;

    /**
     * Constructor.
     * @param directory the {@code Directory} implementation to check for
     * gadget specs
     */
    public GadgetSpecUrlRenderPermissionImpl(Directory directory)
    {
        this.directory = directory;
    }
    
    public Vote voteOn(String gadgetSpecUri)
    {
        if (Uri.isValid(gadgetSpecUri) && directory.contains(URI.create(gadgetSpecUri)))
        {
            return Vote.ALLOW;
        }
        else
        {
            return Vote.PASS;
        }
    }
}
