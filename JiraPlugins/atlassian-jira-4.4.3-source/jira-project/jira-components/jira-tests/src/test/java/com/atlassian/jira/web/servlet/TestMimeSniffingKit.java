package com.atlassian.jira.web.servlet;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.local.ListeningTestCase;

import java.io.File;
import java.io.IOException;

/**
 * A test for MimeSniffing based on a refectoring of the the previous TestViewAttachmentServletNice
 */
public class TestMimeSniffingKit extends ListeningTestCase
{
    private static final String SAMPLE_IE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";
    private static final String SAMPLE_NON_IE_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9";

    @Test
    public void testGetContentDispositionDefaultPolicy() throws IOException
    {
        MockApplicationProperties map = new MockApplicationProperties();
        testWorkaroundPolicy(map);
    }

    @Test
    public void testGetContentDispositionWorkaroundPolicy() throws IOException {
        MockApplicationProperties ap = new MockApplicationProperties();
        ap.setString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING, APKeys.MIME_SNIFFING_WORKAROUND);
        testWorkaroundPolicy(ap);
    }

    private void testWorkaroundPolicy(MockApplicationProperties map) throws IOException
    {
        // IE tests
        assertEquals("attachment", getContentDisposition("dodgysmelly<html", "dafile", "image/gif", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("dodgysmelly<script", "dafile", "image/jpeg", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("dodgysmelly<table", "dafile", "image/png", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("dodgysmelly<plaintext", "dafile", "text/plain", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("dodgysmelly<plaintext", "dafile", "whatever/unknown", SAMPLE_IE_USER_AGENT, map));
        assertEquals("inline", getContentDisposition("no html tags in this file", "dafile", "image/gif", SAMPLE_IE_USER_AGENT, map));
        // html always attachment
        assertEquals("attachment", getContentDisposition("no html tags in this file but mime type is html", "dafile", "text/html", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("no html tags in this file but mime type is xml", "dafile", "text/xml", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("no html tags in this file but mime type is xml", "dafile", "application/xml", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("no html tags in this file but mime type is xml and xhtml", "dafile", "application/xhtml+xml", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("no html tags in this file but mime type is xhtml", "dafile", "text/xhtml", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("not much here, but filename has html ext", "dafile.htm", "whatever/something", SAMPLE_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("not much here, but filename has html ext", "dafile.html", "text/plain", SAMPLE_IE_USER_AGENT, map));

        // non IE tests
        assertEquals("inline", getContentDisposition("dodgysmelly<html", "dafile", "image/gif", SAMPLE_NON_IE_USER_AGENT, map));
        assertEquals("inline", getContentDisposition("dodgysmelly<script", "dafile", "image/jpeg", SAMPLE_NON_IE_USER_AGENT, map));
        assertEquals("inline", getContentDisposition("dodgysmelly<table", "dafile", "image/png", SAMPLE_NON_IE_USER_AGENT, map));
        assertEquals("inline", getContentDisposition("dodgysmelly<plaintext", "dafile", "text/plain", SAMPLE_NON_IE_USER_AGENT, map));
        assertEquals("inline", getContentDisposition("dodgysmelly<plaintext", "dafile", "whatever/unknown", SAMPLE_NON_IE_USER_AGENT, map));
        assertEquals("inline", getContentDisposition("no html tags in this file", "dafile", "image/gif", SAMPLE_NON_IE_USER_AGENT, map));
        // html always attachment
        assertEquals("attachment", getContentDisposition("no html tags in this file but mime type is html", "dafile", "text/html", SAMPLE_NON_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("not much here, but filename has html ext", "dafile.htm", "whatever/something", SAMPLE_NON_IE_USER_AGENT, map));
        assertEquals("attachment", getContentDisposition("not much here, but filename has html ext", "dafile.html", "text/plain", SAMPLE_NON_IE_USER_AGENT, map));
    }

    private String getContentDisposition(final String fileContents, String filename, String mimetype, String useragent, final ApplicationProperties ap) throws IOException
    {
        final MimeSniffingKit mimeSniffingKit = new MimeSniffingKit(ap)
        {
            @Override
            public byte[] getLeadingFileBytes(final File attachmentFile, final int numBytes) throws IOException
            {
                return fileContents.getBytes("UTF-8");
            }

            @Override
            File getFileForAttachment(final Attachment attachment)
            {
                return null;
            }
        };

        Attachment attachment = getMockAttachment(filename, mimetype);

        return mimeSniffingKit.getContentDisposition(attachment, useragent);
    }

    private Attachment getMockAttachment(final String filename, final String mimeType)
    {
        // just overriding what we need to answer the content disposition call graph
        MockGenericValue mgv = new MockGenericValue("attachment");
        Attachment mockAttachment = new Attachment(null, mgv)
        {
            public String getFilename()
            {
                return filename;
            }

            public String getMimetype()
            {
                return mimeType;
            }

            public Issue getIssueObject()
            {
                return null;
            }
        };
        return mockAttachment;
    }
}
