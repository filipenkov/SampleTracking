package com.atlassian.modzdetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;

/**
 * A few utility methods for I/O. These are copied from commons-io and are
 * reproduced here in the interest of minimizing external dependencies.
 */
public class IOUtils
{
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static void closeQuietly(InputStream input)
    {
        try
        {
            if (input != null)
            {
                input.close();
            }
        }
        catch (IOException ioe)
        {
            // ignore
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException
    {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE)
        {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException
    {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer)))
        {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static String toString(InputStream input) throws IOException
    {
        Reader r = new InputStreamReader(input);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        for (int n; (n = r.read(buffer)) != -1;)
        {
            sw.write(buffer, 0, n);
        }
        return sw.toString();
    }
}
