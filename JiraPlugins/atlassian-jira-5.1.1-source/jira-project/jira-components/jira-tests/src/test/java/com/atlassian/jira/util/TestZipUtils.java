package com.atlassian.jira.util;

import com.atlassian.jira.JiraTestUtil;
import com.google.common.io.ByteStreams;
import org.junit.Test;
import webwork.util.ClassLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.4
 */
public class TestZipUtils
{
    private static final URL RESOURCE = ClassLoaderUtils.getResource(JiraTestUtil.TESTS_BASE + "/util/zip-with-foo.zip", TestZipUtils.class);

    @Test(expected = IOException.class)
    public void testZipFileNotReadable() throws IOException
    {
        File zipFile = new File("ZXCVZXCVZXCVZXVCZXVZXCVZXV");
        ZipUtils.streamForZipFileEntry(zipFile, "some entry");
    }

    @Test
    public void testEntryDoesNotExist() throws IOException
    {
        String path = RESOURCE.getFile();
        File zipFile = new File(path);
        assertThat(zipFile.exists(), is(true));
        assertThat(ZipUtils.streamForZipFileEntry(zipFile, "bar"), nullValue());
    }

    @Test
    public void testExtantEntryStreamRetrieved() throws IOException
    {
        File zipFile = new File(RESOURCE.getFile());
        InputStream stream = ZipUtils.streamForZipFileEntry(zipFile, "foo");
        try
        {
            String entryContent = new String(ByteStreams.toByteArray(stream), "UTF-8");
            assertThat(entryContent, is("Tue 22 May 2012 12:35:20 EST\n"));
        }
        finally
        {
            stream.close();
        }
    }
}
