package com.atlassian.gadgets.publisher.internal.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.publisher.internal.GadgetProcessor;
import com.atlassian.gadgets.publisher.internal.PublishedGadgetSpecNotFoundException;

import org.apache.commons.io.IOUtils;

import static com.google.common.base.Preconditions.checkNotNull;

final class GadgetSpecProcessingWriter
{
    private final GadgetProcessor gadgetProcessor;
    
    GadgetSpecProcessingWriter(GadgetProcessor gadgetProcessor)
    {
        this.gadgetProcessor = checkNotNull(gadgetProcessor, "gadgetProcessor");
    }

    void write(PluginGadgetSpec pluginGadgetSpec, OutputStream output) throws IOException
    {
        checkNotNull(pluginGadgetSpec, "pluginGadgetSpec");
        checkNotNull(output, "output");
        
        InputStream gadgetSpecStream = pluginGadgetSpec.getInputStream();
        if (gadgetSpecStream == null)
        {
            throw new PublishedGadgetSpecNotFoundException(
                String.format("Could not write gadget spec: %s because the resource was not found", pluginGadgetSpec));
        }

        try
        {
            gadgetProcessor.process(gadgetSpecStream, output);
        }
        finally
        {
            IOUtils.closeQuietly(gadgetSpecStream);
        }
    }
}
