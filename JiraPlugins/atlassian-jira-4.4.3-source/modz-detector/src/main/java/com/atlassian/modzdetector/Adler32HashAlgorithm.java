package com.atlassian.modzdetector;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

/**
 * Adler-32 checksumming implementation.
 */
public class Adler32HashAlgorithm implements HashAlgorithm
{
    public String getHash(final InputStream stream)
    {
        try
        {
            final Adler32 adler = new Adler32();
            CheckedInputStream cis = new CheckedInputStream(stream, adler);
            IOUtils.copy(cis, new NullOutputStream());
            return Long.toHexString(adler.getValue());
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public String getHash(final byte[] bytes)
    {
        Adler32 adler = new Adler32();
        adler.update(bytes);
        return Long.toHexString(adler.getValue());
    }

    public String toString()
    {
        return "ADLER32 HEX";
    }
}
