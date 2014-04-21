package com.atlassian.core.spool;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public abstract class AbstractSpoolTest extends TestCase
{
    private static int counter = -1;

    public byte[] getTestData(int size)
    {
        byte[] data = new byte[size];
        for (int x = 0; x < size; x++)
            data[x] = (byte) (Math.random() * Byte.MAX_VALUE);
        return data;
    }

    public void verifySpool(Spool spool, byte[] data) throws IOException
    {
        ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
        verifySpool(spool, dataStream);
    }

    public void verifySpool(Spool spool, InputStream dataStream) throws IOException
    {
        InputStream spoolStream = spool.spool(dataStream);
        dataStream.reset();
        assertTrue("Stream duplicated exactly", IOUtils.contentEquals(dataStream, spoolStream));
    }

    private static File generateFile(String prefix, String suffix, File dir)
    {
        if (counter == -1)
        {
            counter = new Random().nextInt() & 0xffff;
        }
        counter++;
        return new File(dir, prefix + Integer.toString(counter) + suffix);
    }

    public File getTestFile()
    {
        return generateFile("spool", "test", new File("target"));
    }

}
