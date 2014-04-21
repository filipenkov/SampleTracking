package com.atlassian.modzdetector;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A highly performant and thread-safe implementation of OutputStream which does nothing.
 *
 * @since 0.8
 */
final class NullOutputStream extends OutputStream
{
    final public void write(final int b) throws IOException
    {
        // do nothing
    }
}
