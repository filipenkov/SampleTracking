package com.atlassian.gadgets.publisher.internal.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.atlassian.gadgets.publisher.internal.GadgetProcessor;

import org.apache.commons.io.IOUtils;

class PassThroughGadgetProcessor implements GadgetProcessor
{
    public void process(InputStream in, OutputStream out) throws IOException
    {
        IOUtils.copy(in, out);
    }
}
