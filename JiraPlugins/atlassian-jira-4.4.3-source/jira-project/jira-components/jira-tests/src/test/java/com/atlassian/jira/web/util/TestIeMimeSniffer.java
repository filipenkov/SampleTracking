package com.atlassian.jira.web.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Tests the Ie6MimeSniffer class.
 *
 * @since v3.13
 */
public class TestIeMimeSniffer extends ListeningTestCase
{
    private static final Logger log = Logger.getLogger(TestIeMimeSniffer.class);
    public static final String[] EXPLOIT_FILES = new String[] {
            "1179826281.jpg",
            "1179826282.png",
            "test.png",
            "html.html",
            "xsshack.gif",
            "xsshack.jpg",
            "xsshack.png",
            "xsshack2.gif",
            "xsshack3.gif",
            "xssoj7.png"
    };

    @Test
    public void testStupid()
    {
        try
        {
            new Ie6MimeSniffer( -2);
        }
        catch (Exception yay)
        {

        }
        try
        {
            new Ie6MimeSniffer( -200000);
        }
        catch (Exception yay)
        {

        }

    }

    @Test
    public void testEmpty()
    {
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        assertFalse(ieMimeSniffer.smellsLikeHtml(new byte[] { }));
    }

    @Test
    public void testLotsOfZeroes()
    {
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        assertFalse(ieMimeSniffer.smellsLikeHtml(new byte[1000]));
    }

    @Test
    public void testByteBoundary()
    {
        String html = "123456789<html><head>Real HTML</head><body><h3>Hello World</h3><p>this is a test</p></body></html>";
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer( 13); // not quite enough to get the first tag
        assertFalse(ieMimeSniffer.smellsLikeHtml(getUtf8Bytes(html)));
        ieMimeSniffer = new Ie6MimeSniffer( 14); // that will do it
        assertTrue(ieMimeSniffer.smellsLikeHtml(getUtf8Bytes(html)));
    }

    @Test
    public void testBasicHtmlFile()
    {
        String html = "<html><head>Real HTML</head><body><h3>Hello World</h3><p>this is a test</p></body></html>";
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        assertTrue(ieMimeSniffer.smellsLikeHtml(getUtf8Bytes(html)));
    }

    @Test
    public void testBasicHtmlFileLeadingJunk()
    {
        String html = "zxbnxcvxzcvzxcvxzcvxz,cvxz,cvz,xmcvzxcvxzm02833r203249324023490"
                      + "<html><head>Real HTML</head><body><h3>Hello World</h3><p>this is a test</p></body></html>";
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        assertTrue(ieMimeSniffer.smellsLikeHtml(getUtf8Bytes(html)));
    }


    @Test
    public void testBasicHtmlFileTrailingJunk()
    {
        String html = "<html><head>Real HTML</head><body><h3>Hello World</h3><p>this is a test</p></body></html>"
                      + "zxbnxcvxzcvzxcvxzcvxz,cvxz,cvz,xmcvzxcvxzm02833r203249324023490";
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        assertTrue(ieMimeSniffer.smellsLikeHtml(getUtf8Bytes(html)));
    }

    @Test
    public void testMixedCase()
    {
        String html = "<<<<<<<<hEaD<<<<<<<<";
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        assertTrue(ieMimeSniffer.smellsLikeHtml(getUtf8Bytes(html)));
    }

    /**
     * Checks a series of files containing known exploits found by hanging around unsavoury parts of the internet.
     *
     * @throws IOException if any resource is not on the classpath.
     */
    @Test
    public void testSampleExploitFiles() throws IOException
    {
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();

        for (int i = 0; i < EXPLOIT_FILES.length; i++)
        {
            String exploitFile = EXPLOIT_FILES[i];
            log.info("checking exploit file " + exploitFile);
            byte[] file = getBytesFromResource(exploitFile);
            assertTrue("expected " + exploitFile + " to smell like html", ieMimeSniffer.smellsLikeHtml(file));
        }
    }

    @Test
    public void testNonAscii() throws IOException
    {
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        byte[] tag = getAsciiBytes("<html");
        byte[] bytes = ArrayUtils.addAll(new byte[] { (byte) 0x89 }, tag);
        assertTrue(ieMimeSniffer.smellsLikeHtml(bytes));
    }

    @Test
    public void testEndsWithHtmlString()
    {
        byte[] bytes = getAsciiBytes("af 880y9t35y8903t98[gtfr359<html");
        Ie6MimeSniffer ieMimeSniffer = new Ie6MimeSniffer();
        assertTrue(ieMimeSniffer.smellsLikeHtml(bytes));
    }

    @Test
    public void testContainsSubarray()
    {
        assertFalse(Ie6MimeSniffer.containsSubarray(getAsciiBytes("12345"), getAsciiBytes("456")));
        assertFalse(Ie6MimeSniffer.containsSubarray(getAsciiBytes("12345"), getAsciiBytes("w")));
        assertFalse(Ie6MimeSniffer.containsSubarray(getAsciiBytes("1212123"), getAsciiBytes("124")));
        assertTrue(Ie6MimeSniffer.containsSubarray(getAsciiBytes("12345"), getAsciiBytes("1")));
        assertTrue(Ie6MimeSniffer.containsSubarray(getAsciiBytes("12345"), getAsciiBytes("2")));
        assertTrue(Ie6MimeSniffer.containsSubarray(getAsciiBytes("12345"), getAsciiBytes("5")));
        assertTrue(Ie6MimeSniffer.containsSubarray(getAsciiBytes("12345"), getAsciiBytes("12345")));
        assertTrue(Ie6MimeSniffer.containsSubarray(getAsciiBytes("12345"), getAsciiBytes("34")));
        assertTrue(Ie6MimeSniffer.containsSubarray(getAsciiBytes("11112"), getAsciiBytes("12")));
    }

    @Test
    public void testToLowerCase()
    {
        byte[] numbers = getAsciiBytes("123456");
        Ie6MimeSniffer.toLowerCaseAscii(numbers);
        assertTrue(Arrays.equals(getAsciiBytes("123456"), numbers));

        byte[] mixedCase = getAsciiBytes("MiXeDStuDLYCapS");
        Ie6MimeSniffer.toLowerCaseAscii(mixedCase);
        assertTrue(Arrays.equals(getAsciiBytes("mixedstudlycaps"), mixedCase));
    }

    private byte[] getBytesFromResource(String filename) throws IOException
    {
        InputStream stream = getClass().getResourceAsStream(filename);
        if (stream == null)
        {
            throw new IllegalStateException("couldn't get file " + filename);
        }
        return IOUtil.toByteArray(stream);
    }

    private byte[] getUtf8Bytes(String s)
    {
        try
        {
            return s.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException annoying)
        {
            throw new Error(annoying);
        }
    }

    private byte[] getAsciiBytes(String s)
    {
        try
        {
            return s.getBytes("ASCII");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
