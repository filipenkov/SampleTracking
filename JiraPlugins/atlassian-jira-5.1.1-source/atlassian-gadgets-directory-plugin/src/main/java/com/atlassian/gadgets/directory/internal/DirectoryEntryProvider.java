package com.atlassian.gadgets.directory.internal;

import java.net.URI;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;

public interface DirectoryEntryProvider
{
    /**
     * Returns all the entries available, localized by the {@code locale}.
     *
     * @param gadgetRequestContext the context of this request
     * @return all the entries available
     */
    Iterable<Directory.Entry> entries(GadgetRequestContext gadgetRequestContext);

    /**
     * @param gadgetSpecUri uri of the gadget spec to check if it's in the directory
     * @return true if the gadget spec at the location specificied by the uri is in the directory, false otherwise
     */
    boolean contains(URI gadgetSpecUri);
}
