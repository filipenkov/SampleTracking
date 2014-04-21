package com.atlassian.core.util;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

import junit.framework.TestCase;

public class TestImageInfo extends TestCase
{

    public void testCheckJPeg() throws Exception
    {
        assertTrue(checkImage("/image.jpg"));
    }

    public void testCheckPng() throws Exception
    {
        assertTrue(checkImage("/image.png"));
    }

    public void testCheckGif() throws Exception
    {
        assertTrue(checkImage("/image.gif"));
    }

    public void testCheckJPegWithInvalidData() throws Exception
    {
        assertFalse(checkImage("/image_invalid.jpg"));
    }

    private boolean checkImage(String filename)
    {
        ImageInfo imageInfo = new ImageInfo();
        InputStream stream = this.getClass().getResourceAsStream(filename);
        try
        {
            imageInfo.setInput(stream);
            return imageInfo.check();
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

}
