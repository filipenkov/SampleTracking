/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util.mime;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.local.ListeningTestCase;

import java.io.ByteArrayInputStream;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

public class TestMimeManager extends ListeningTestCase
{
    private ByteArrayInputStream bai = new ByteArrayInputStream("image/thisGif gif\ntext/plain xml\napplication/vnd.openxmlformats docx".getBytes());

    @Test
    public void testMimeSanitiserChangesGenericMimeTypes()
    {
        MimeManager mimeManager = new MimeManager(bai);
        String genericMimeType = "application/octet-stream";
        String gifFileName = "image.gif";

        assertEquals("image/thisGif", mimeManager.getSanitisedMimeType(genericMimeType, gifFileName));
    }

    @Test
    public void testMimeSanitiserChangesZIPGenericMimeTypes()
    {
        MimeManager mimeManager = new MimeManager(bai);
        String genericMimeType = "application/x-zip-compressed";
        String gifFileName = "doc.docx";

        assertEquals("application/vnd.openxmlformats", mimeManager.getSanitisedMimeType(genericMimeType, gifFileName));
    }

    @Test
    public void testMimeSanitiserIgnoresExistingMimeTypes()
    {
        MimeManager mimeManager = new MimeManager(bai);
        String mimeType = "text/plain";
        String gifFileName = "image.gif";

        assertEquals("text/plain", mimeManager.getSanitisedMimeType(mimeType, gifFileName));
    }

    @Test
    public void testNullConstructor()
    {
        MimeManager mimeManager = new MimeManager(null);
        String mimeType = "text/plain";
        String gifFileName = "image.gif";

        //just check that it runs without exception
        mimeManager.getSanitisedMimeType(mimeType, gifFileName);
    }

    public void atest()
    {
        System.out.println("aaa");
        System.out.println("ClassLoaderUtils.getResource(\"mime.types\", this.getClass()) = " + ClassLoaderUtils.getResource("mime.types", this.getClass()));

        FileTypeMap fileTypeMap = new MimetypesFileTypeMap(ClassLoaderUtils.getResourceAsStream("mime.types", this.getClass()));
        String ft = fileTypeMap.getContentType("abc.xml");
        System.out.println("ft = " + ft);
    }
}
