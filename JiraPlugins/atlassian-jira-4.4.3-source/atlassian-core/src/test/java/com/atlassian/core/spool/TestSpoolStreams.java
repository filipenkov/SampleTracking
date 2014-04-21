package com.atlassian.core.spool;

import com.mockobjects.dynamic.Mock;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

public class TestSpoolStreams extends AbstractSpoolTest
{
    public void testDeferredSpoolOutputStreamInMemory() throws Exception
    {
        Mock mockFileFactory = new Mock(FileFactory.class);
        mockFileFactory.expectNotCalled("createNewFile");

        FileFactory fileFactory = (FileFactory) mockFileFactory.proxy();
        DeferredSpoolFileOutputStream out = new DeferredSpoolFileOutputStream(1024, fileFactory);
        byte[] data = getTestData(1024);

        IOUtils.copy(new ByteArrayInputStream(data), out);

        assertTrue(out.isInMemory());
        if (out.getFile() != null)
            assertFalse(out.getFile().exists());

        mockFileFactory.verify();
    }

    public void testDeferredSpoolOutputStreamToFile() throws Exception
    {
        Mock mockFileFactory = new Mock(FileFactory.class);
        File testSpoolFile = getTestFile();
        mockFileFactory.expectAndReturn("createNewFile", testSpoolFile);

        FileFactory fileFactory = (FileFactory) mockFileFactory.proxy();
        DeferredSpoolFileOutputStream out = new DeferredSpoolFileOutputStream(1024, fileFactory);
        byte[] data = getTestData(2048);

        IOUtils.copy(new ByteArrayInputStream(data), out);
        out.close();

        File spoolFile = out.getFile();

        assertFalse(out.isInMemory());
        assertNotNull(spoolFile);
        assertEquals(spoolFile, testSpoolFile);
        assertTrue(spoolFile.exists());

        IOUtils.contentEquals(new ByteArrayInputStream(data), out.getInputStream());

        mockFileFactory.verify();
    }

    public void testDeferredSpoolInputStreamRemovesSpoolOnClose() throws Exception
    {
        byte[] data = getTestData(2048);
        File testFile = getTestFile();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(testFile));
        IOUtils.write(data, bos);
        bos.close();

        SpoolFileInputStream dfis = new SpoolFileInputStream(testFile);
        dfis.close();

        assertFalse(testFile.exists());
    }
}
