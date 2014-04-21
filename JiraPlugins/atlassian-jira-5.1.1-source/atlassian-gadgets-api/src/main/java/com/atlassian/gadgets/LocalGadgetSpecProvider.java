package com.atlassian.gadgets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;

/**
 * A provider of gadget specs that are avaiable locally, without making an HTTP request to fetch them.  Implementations
 * of {@code GadgetSpecProvider} that serve gadget specs that are intended to be rendered by the same server that they
 * are published from should implement this interface in order to eliminate the need for the renderer to make a loopback
 * HTTP request to fetch the gadget spec.
 *
 * @since 1.1
 */
public interface LocalGadgetSpecProvider extends GadgetSpecProvider
{
    /**
     * Writes the gadget spec found at the specified URI, to the specified {@code OutputStream}. It is assumed that the
     * caller has already checked that the URI is provided by this provider by calling the {@link
     * #contains(java.net.URI)} method. If not, this method will throw a {@code com.atlassian.gadgets.GadgetSpecUriNotAllowedException}.
     *
     * @param gadgetSpecUri URI of the gadget spec to write. Must not be {@code null} or a {@code NullPointerException}
     *                      will be thrown.
     * @param output        the {@code OutputStream} to write the gadget spec XML to.  Must not be {@code null}, or a
     *                      {@code NullPointerException} will be thrown.
     * @throws GadgetSpecUriNotAllowedException
     *                              if the provided gadget spec URI cannot be written by this provider
     * @throws IOException          if an error occurs in I/O processing
     * @throws NullPointerException if any argument is {@code null}
     */
    void writeGadgetSpecTo(URI gadgetSpecUri, OutputStream output) throws GadgetSpecUriNotAllowedException, IOException;

    /**
     * Return the date the gadget spec was last modified.
     * 
     * @returns date the gadget spec was last modified.
     */
    Date getLastModified(URI gadgetSpecUri);
}
