/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.local.ListeningTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class TestFileIconBean extends ListeningTestCase
{
    InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
    MimeManager voidMimeManager = new MimeManager(emptyStream)
    {
        public String getSuggestedMimeType(String fileName)
        {
            return "";
        }
    };

    private final FileIconBean.FileIcon mimeType = new FileIconBean.FileIcon(".zip", "text/plain", "", "");

    @Test
    public void testHandlesNothing()
    {
        FileIconBean mimeBean = new FileIconBean(Collections.EMPTY_LIST, voidMimeManager);
        assertNull(mimeBean.getFileIcon("fileName", "mimeType"));
    }

    @Test
    public void testGetViaMimeType()
    {
        List fileIcons = EasyList.build(mimeType);
        FileIconBean fileBean = new FileIconBean(fileIcons, voidMimeManager);
        assertEquals(mimeType, fileBean.getFileIcon("", "text/plain"));
    }

    @Test
    public void testGetViaFileName()
    {
        List fileIcons = EasyList.build(mimeType);
        FileIconBean fileBean = new FileIconBean(fileIcons, voidMimeManager);
        assertNull(fileBean.getFileIcon("fileName", "mimeType"));
        assertEquals(mimeType, fileBean.getFileIcon("abc.zip", ""));
    }

    @Test
    public void testGetViaFileNameWithTrailingSpace()
    {
        List fileIcons = EasyList.build(mimeType);
        FileIconBean fileBean = new FileIconBean(fileIcons, voidMimeManager);
        assertNull(fileBean.getFileIcon("fileName", "mimeType"));
        assertEquals(mimeType, fileBean.getFileIcon("abc.zip ", ""));
    }

    @Test
    public void testGetViaMimeManager()
    {
        List fileIcons = EasyList.build(mimeType);

        //dodgy mime manager that always suggests text/plain
        MimeManager mimeManager = new MimeManager(null)
        {
            public String getSuggestedMimeType(String fileName)
            {
                return "text/plain";
            }
        };

        FileIconBean fileBean = new FileIconBean(fileIcons, mimeManager);
        assertEquals(mimeType, fileBean.getFileIcon("", ""));
    }
}
