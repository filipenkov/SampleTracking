package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.util.NotNull;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * HTMLAssertions implementation.
 *
 * @since v4.0.2
 */
public class HTMLAssertionsImpl implements HTMLAssertions
{
    public void assertContains(final String original, final String expected)
    {
        new TextAssertionsImpl().assertTextPresent(unescape(original), expected);
    }

    public void assertResponseContains(final WebTester tester, final String expected)
    {
        assertContains(getResponseText(tester), expected);
    }

    static String unescape(final @NotNull String escapedText)
    {
        return StringEscapeUtils.unescapeHtml(escapedText);
    }

    static String getResponseText(final @NotNull WebTester tester)
    {
        try
        {
            return tester.getDialog().getResponse().getText();
        }
        catch (final IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    static class WriterStream extends OutputStream
    {
        private final Writer writer;

        WriterStream(final @NotNull Writer writer)
        {
            this.writer = writer;
        }

        @Override
        public void write(final int b) throws IOException
        {
            writer.write(b);
        }

        @Override
        public void flush() throws IOException
        {
            writer.flush();
        }
    }
}
