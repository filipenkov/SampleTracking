package com.atlassian.jira.jql.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v4.0
 */
public class TestJqlDurationSupportImpl extends ListeningTestCase
{
    private JqlDurationSupportImpl support = new JqlDurationSupportImpl();

    @Test
    public void testValidate() throws Exception
    {
        assertInvalid("aaa", false);
        assertInvalid("-aaa", true);
        assertInvalid("0.0", false);
        assertInvalid("-0.0", true);
        assertInvalid("", false);
        assertInvalid("     ", false);

        assertValid("30", false);
        assertValid("30", true);
        assertValid("30m", false);
        assertValid("30m", true);
        assertValid("1h", false);
        assertValid("1h", true);
        assertValid("1h 30m", false);
        assertValid("1h 30m", true);
        assertInvalid("-30", false);
        assertValid("-30", true);
        assertInvalid("-30m", false);
        assertValid("-30m", true);
        assertInvalid("-1h", false);
        assertValid("-1h", true);
        assertInvalid("-1h 30m", false);
        assertValid("-1h 30m", true);
    }

    @Test
    public void testConvertToDuration() throws Exception
    {
        assertConverted(30L * 60, 30L);
        assertConverted(30L * 60, "30m");
        assertConverted(60L * 60, "1h");
        assertConverted(90L * 60, "1h 30m");

        // bad value
        assertNull(support.convertToDuration("xxxx"));
        assertNull(support.convertToDuration(""));
        assertNull(support.convertToDuration("      "));
    }

    @Test
    public void testConvertToIndexValue() throws Exception
    {
        assertIndexValue("000000000001e0", 30L);
        assertIndexValue("000000000001e0", "30m");
        assertIndexValue("000000000002s0", "1h");
        assertIndexValue("00000000000460", "1h 30m");

        // bad value
        assertNull(support.convertToIndexValue("xxxx"));
        assertNull(support.convertToIndexValue(""));
        assertNull(support.convertToIndexValue("     "));
        assertNull(support.convertToIndexValue(new QueryLiteral()));
    }

    private void assertConverted(Long expected, Object input)
    {
        Long actual = null;
        if (input instanceof Long)
        {
            actual = support.convertToDuration((Long) input);
        }
        else if (input instanceof String)
        {
            actual = support.convertToDuration((String) input);
        }
        assertEquals(expected, actual);
    }

    private void assertIndexValue(String expected, Object input)
    {
        String actual = null;
        if (input instanceof Long)
        {
            actual = support.convertToIndexValue((Long) input);
        }
        else if (input instanceof String)
        {
            actual = support.convertToIndexValue((String) input);
        }
        assertEquals(expected, actual);
    }

    private void assertValid(String durationString, final boolean allowNegatives) throws InvalidDurationException
    {
        assertTrue(support.validate(durationString, allowNegatives));
    }

    private void assertInvalid(String durationString, final boolean allowNegatives)
    {
        assertFalse(support.validate(durationString, allowNegatives));
    }
}
