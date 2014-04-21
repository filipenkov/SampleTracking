package com.atlassian.gadgets.publisher.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.atlassian.gadgets.GadgetParsingException;

/**
 * Represents preprocessing operations applied to a gadget XML spec before being served to a client or persisted to a
 * store. Relevant operations are Atlassian-specific and are used to integrate better with Atlassian applications that
 * host the gadget dashboard.
 */
public interface GadgetProcessor
{
    /**
     * Applies an implementation-defined transformation to the XML stream provided by the specified {@code InputStream}
     * while writing the result to the specified {@code OutputStream}. The {@code InputStream} is guaranteed to be read
     * at most once in this implementation. Neither the {@code InputStream} nor the {@code OutputStream} may be {@code
     * null}.
     *
     * @param in  source of the gadget spec XML to process
     * @param out destination for the processed XML
     * @throws NullPointerException   if any argument is {@code null}
     * @throws IOException            if an error occurs in I/O processing
     * @throws GadgetParsingException if an error is detected in the gadget XML
     */
    void process(InputStream in, OutputStream out) throws IOException, GadgetParsingException;
}
