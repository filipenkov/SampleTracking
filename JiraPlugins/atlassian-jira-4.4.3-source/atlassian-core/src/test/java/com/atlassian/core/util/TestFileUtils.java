package com.atlassian.core.util;

import junit.framework.TestCase;

import java.io.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class TestFileUtils extends TestCase
{
    private final static String MAVEN_TARGET_DIR = "target";

    public void testCopyFile() throws Exception
    {
        File file1 = new File(MAVEN_TARGET_DIR + "/file1.txt");
        File file2 = new File(MAVEN_TARGET_DIR + "/file2.txt");
        FileUtils.saveTextFile("Test content", file1);
        FileUtils.saveTextFile("To be overwritten", file2);

        FileUtils.copyFile(file1, file2);

        assertEquals("Test content", IOUtils.toString(new FileInputStream(file2)));
    }

    public void testCopy() throws Exception
    {
        InputStream in = new ByteArrayInputStream("Test content".getBytes());
        OutputStream out = new ByteArrayOutputStream();
        FileUtils.copy(in, out);
        assertEquals("Test content", out.toString());
    }

    public void testShutdownInputStream() throws Exception
    {
        StubInputStream in = new StubInputStream();
        FileUtils.shutdownStream(in);
        assertTrue(in.wasClosed);
    }

    public void testShutdownInputStreamWithException() throws Exception
    {
        ExceptionalInputStream in = new ExceptionalInputStream();
        FileUtils.shutdownStream(in); // shouldn't throw an exception
    }

    public void testShutdownOutputStream() throws Exception
    {
        StubOutputStream out = new StubOutputStream();
        FileUtils.shutdownStream(out);
        assertTrue(out.wasClosed);
    }

    public void testShutdownOutputStreamWithException() throws Exception
    {
        ExceptionalOutputStream out = new ExceptionalOutputStream();
        FileUtils.shutdownStream(out); // shouldn't throw an exception
    }

    public void testSaveTestFile() throws IOException
    {
        File file = new File(MAVEN_TARGET_DIR + File.separator + "foo.tmp");
        FileUtils.saveTextFile("Hello World", file);
        assertEquals("Hello World", IOUtils.toString(new FileInputStream(file)));

        // overwrite the existing text in the file
        FileUtils.saveTextFile("Goodbye Moon", file);
        assertEquals("Goodbye Moon", IOUtils.toString(new FileInputStream(file)));
    }

    public void testMoveDir() throws Exception
    {
        final File baseDir = new File(MAVEN_TARGET_DIR, "TestFileUtils-testMoveDir");
        FileUtils.deleteDir(baseDir);
        File oldDir = new File(baseDir, "old");
        oldDir.mkdirs();
        FileUtils.saveTextFile("Test content", new File(oldDir, "test.txt"));

        File newDir = new File(baseDir, "new");
        assertTrue(FileUtils.moveDir(oldDir, newDir));

        assertTrue("directory has been moved", newDir.exists());
        assertTrue("old directory no longer exists", !oldDir.exists());
        assertEquals("Test content should be copied", "Test content",
            IOUtils.toString(new FileInputStream(new File(newDir, "test.txt"))));
    }

    private static class StubInputStream extends InputStream
    {
        public boolean wasClosed = false;

        public int read()
        {
            return 0;
        }

        public void close() throws IOException
        {
            wasClosed = true;
            super.close();
        }
    }

    private static class StubOutputStream extends OutputStream
    {
        public boolean wasClosed = false;

        public void write(int b)
        {
        }

        public void close() throws IOException
        {
            wasClosed = true;
            super.close();
        }
    }

    private static class ExceptionalInputStream extends InputStream
    {
        public int read()
        {
            return 0;
        }

        public void close() throws IOException
        {
            throw new IOException();
        }
    }

    private static class ExceptionalOutputStream extends OutputStream
    {
        public void write(int b)
        {
        }

        public void close() throws IOException
        {
            throw new IOException();
        }
    }
}
