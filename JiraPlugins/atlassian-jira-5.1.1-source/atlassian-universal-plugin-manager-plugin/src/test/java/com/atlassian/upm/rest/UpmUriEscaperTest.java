package com.atlassian.upm.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UpmUriEscaperTest
{
    private static final String ORIGINAL = "my.plugin.key.html";
    private static final String EXPECTED = "my.plugin.key.html" + UpmUriEscaper.SUFFIX;

    @Test
    public void assertThatStringIsEscapedCorrectly()
    {
        assertEquals(EXPECTED, UpmUriEscaper.escape(ORIGINAL));
    }

    @Test
    public void assertThatUnescapingEscapedStringGetsOriginal()
    {
        assertEquals(ORIGINAL, UpmUriEscaper.unescape(UpmUriEscaper.escape(ORIGINAL)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertThatUnescapingUnEscapedStringDoesNothing()
    {
        UpmUriEscaper.unescape(ORIGINAL);
    }

    /**
     * In case someone's plugin key ends with our suffix, make sure that we still treat it the same.
     */
    @Test
    public void assertThatEscapingAlreadyEscapedStringReescapes()
    {
        assertEquals(EXPECTED + UpmUriEscaper.SUFFIX, UpmUriEscaper.escape(EXPECTED));
    }
}
