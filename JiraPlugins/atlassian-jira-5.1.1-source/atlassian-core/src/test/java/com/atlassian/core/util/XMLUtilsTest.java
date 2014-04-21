package com.atlassian.core.util;

import junit.framework.TestCase;

/**
 * @since v3.19
 */
public class XMLUtilsTest extends TestCase
{
    private final char[] BAD_CO_CONTROL_CHARS = new char[]
            {
                    '\u0000',
                    '\u0007',
                    '\u0011',
                    '\u0014'
            };

    private final char[] BAD_SURROGATE_CHARS = new char[]
            {
                    '\uD84C',
                    '\uDF84'
            };

    private final char[] BAD_OTHER_CHARS = new char[]
            {
                    '\uFFFE',
                    '\uFFFF',
            };

    public void testNullInputHandling()
    {
        assertEquals("", XMLUtils.escape(null));
        //
        // this is inconsistent however I don't want to fix this inconsistency and break any applications
        // its been this way since 14/09/05 and its gonna continue so.  At least we have a test to reflect its
        // behaviour.
        //
        assertEquals(null, XMLUtils.escapeForCdata(null));
    }

    public void testEscape()
    {
        // these two MUST be escaped
        assertEquals("&lt;", XMLUtils.escape("<"));
        assertEquals("&lt;", XMLUtils.escape('<'));
        assertEquals("&amp;", XMLUtils.escape("&"));
        assertEquals("&amp;", XMLUtils.escape('&'));

        // the rest MAY be
        assertEquals("&gt;", XMLUtils.escape(">"));
        assertEquals("&gt;", XMLUtils.escape('>'));
        assertEquals("&quot;", XMLUtils.escape("\""));
        assertEquals("&quot;", XMLUtils.escape('"'));
        assertEquals("&apos;", XMLUtils.escape("'"));
        assertEquals("&apos;", XMLUtils.escape('\''));

        // no C0 control characters allowed
        assertCharIsReplaced(BAD_CO_CONTROL_CHARS);

        // no surrogates allowed
        assertCharIsReplaced(BAD_SURROGATE_CHARS);

        assertCharIsReplaced(BAD_OTHER_CHARS);
        
        // C1 control character should be escaped
        // http://www.cafeconleche.org/books/effectivexml/chapters/03.html
        assertEquals("&#128;", XMLUtils.escape("\u0080"));
        assertEquals("&#128;", XMLUtils.escape('\u0080'));
        assertEquals("&#159;", XMLUtils.escape("\u009f"));
        assertEquals("&#159;", XMLUtils.escape('\u009f'));

        // valid white space is left alone
        assertEquals(" ", XMLUtils.escape(" "));
        assertEquals(" ", XMLUtils.escape(' '));
        assertEquals("\t", XMLUtils.escape("\t"));
        assertEquals("\t", XMLUtils.escape('\t'));
        assertEquals("\r", XMLUtils.escape("\r"));
        assertEquals("\r", XMLUtils.escape('\r'));
        assertEquals("\n", XMLUtils.escape("\n"));
        assertEquals("\n", XMLUtils.escape('\n'));

    }

    public void testCharEscape()
    {
        for (char c : BAD_CO_CONTROL_CHARS)
        {
            assertEquals("\uFFFD", XMLUtils.escape(c));
        }
        for (char c : BAD_SURROGATE_CHARS)
        {
            assertEquals("\uFFFD", XMLUtils.escape(c));
        }
        for (char c : BAD_OTHER_CHARS)
        {
            assertEquals("\uFFFD", XMLUtils.escape(c));
        }
    }

    private void assertCharIsReplaced(final char[] bad_chars)
    {
        for (char bad_char : bad_chars)
        {
            String badStr = new StringBuilder().append(bad_char).toString();
            assertEquals("Expecting uFFFD but got " + (int) bad_char, "\uFFFD", XMLUtils.escape(badStr));
        }
    }

    public void testEscapeCDataSection()
    {
        // general input
        assertEquals("general input", XMLUtils.escapeForCdata("general input"));

        // when ]] is present it starts a new CDATA section
        assertEquals("cdata with with ]]]]><![CDATA[> in it", XMLUtils.escapeForCdata("cdata with with ]]> in it"));

        // when entity references are not resolved
        assertEquals("here is some <tags> that are &lt; eft alone", XMLUtils.escapeForCdata("here is some <tags> that are &lt; eft alone"));
        assertEquals("here is some <tags>]]]]><![CDATA[> that are &lt; eft alone", XMLUtils.escapeForCdata("here is some <tags>]]> that are &lt; eft alone"));


        // no C0 control characters allowed
        assertCDataCharIsReplaced(BAD_CO_CONTROL_CHARS);
        // no surrogates allowed
        assertCDataCharIsReplaced(BAD_SURROGATE_CHARS);
        // others no good
        assertCDataCharIsReplaced(BAD_OTHER_CHARS);

        // put it all together        
        assertEquals("here is[\uFFFD\uFFFD] some\r\t\n <tags>]]]]><![CDATA[> that are &lt; eft alone", XMLUtils.escapeForCdata("here is[\u0007\uD84C] some\r\t\n <tags>]]> that are &lt; eft alone"));

    }

    private void assertCDataCharIsReplaced(final char[] bad_chars)
    {
        final String replacmentStr = "\uFFFD";
        for (char bad_char : bad_chars)
        {
            String badStr = new StringBuilder().append(bad_char).toString();
            assertEquals("Expecting uFFFD but got " + (int) bad_char, replacmentStr, XMLUtils.escapeForCdata(badStr));
        }
    }
}
