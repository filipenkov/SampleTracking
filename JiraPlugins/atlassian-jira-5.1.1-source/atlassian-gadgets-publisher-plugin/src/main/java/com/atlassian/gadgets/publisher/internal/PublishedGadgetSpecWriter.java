package com.atlassian.gadgets.publisher.internal;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes XML gadget spec files from an underlying store to arbitrary output streams.
 */
public interface PublishedGadgetSpecWriter
{
    /**
     * Writes the gadget spec from the specified plugin, with the specified resource location within the plugin, to the
     * specified {@code OutputStream}.  Output will be buffered internally, so there is no need for the calling code to
     * wrap the {@code OutputStream} with a {@link java.io.BufferedOutputStream}.
     *
     * @param pluginKey    the plugin key of the {@link com.atlassian.plugin.Plugin} that contains the gadget spec to
     *                     store.  Must not be {@code null}, or a {@code NullPointerException} will be thrown.
     * @param location     the location of the gadget spec XML file resource within the plugin.  Must not be {@code
     *                     null}, or a {@code NullPointerException} will be thrown.
     * @param outputStream the {@code OutputStream} to write the gadget spec XML to.  Must not be {@code null}, or a
     *                     {@code NullPointerException} will be thrown.
     * @throws java.io.IOException  if an error occurs in I/O processing
     * @throws NullPointerException if any argument is {@code null}
     * @throws PublishedGadgetSpecNotFoundException
     *                              if no resource could be found at the specified path
     */
    void writeGadgetSpecTo(String pluginKey, String location, OutputStream outputStream)
        throws IOException, PublishedGadgetSpecNotFoundException;
}
