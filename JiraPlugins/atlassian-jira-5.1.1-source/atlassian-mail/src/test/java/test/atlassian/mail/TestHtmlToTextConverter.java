package test.atlassian.mail;

import com.atlassian.mail.HtmlToTextConverter;
import junit.framework.TestCase;
import org.apache.commons.lang.SystemUtils;

import java.io.IOException;

public class TestHtmlToTextConverter extends TestCase
{
    public void testParagraphsAndBreaks() throws IOException
    {
        assertEquals("I am a fish\nas am I\n\nand me",
            new HtmlToTextConverter().convert("<p>I am a fish<br>as am I<p>and me"));
    }

    /**
     * <p>There is a bug in JDK prior to 1.6 whereby this html "Some<br/>text" would be converted
     * to "Some\n>text" instead of "Some\ntext".</p>
     * <p>The bug is reported as
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4806463">Issue 4806463</a>.</p>
     *
     * This is why this test doesn't expect the same result depending on the jdk version.
     */
    public void testXHTMLBreaks() throws IOException
    {
        final String expected;
        if (SystemUtils.isJavaVersionAtLeast(160))
        {
            expected = "I am a fish\nas am I\nand me";
        }
        else
        {
            expected = "I am a fish\n>as am I\nand me";
        }

        assertEquals(expected, new HtmlToTextConverter().convert("I am a fish<br />as am I<br></br>and me"));
    }

    /*
     * This, believe it or not, is a perfectly valid HTML document. The <body> element is implied
     * by the fact you can't have paragraphs in a header
     */
    public void testIgnoreHeader() throws IOException
    {
        assertEquals("I am a fish\nToo",
            new HtmlToTextConverter().convert("<head><title>Fish</title><p>I am a fish<br>Too"));
    }
}
