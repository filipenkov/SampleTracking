package com.atlassian.jira.util.velocity;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Locale;

public class TestNumberTool extends ListeningTestCase
{
    @Test
    public void testFormatWithLocales() {
        Double d = 3412.34d;
        assertEquals("3,412.34", new NumberTool(Locale.US).format(d));
        assertEquals("3\u00A0412,34", new NumberTool(new Locale("pl", "pl")).format(d));

        d = 0d;
        assertEquals("0", new NumberTool(Locale.US).format(d));
        assertEquals("0", new NumberTool(new Locale("pl", "pl")).format(d));

        d = -232323.2320d;
        assertEquals("-232,323.232", new NumberTool(Locale.US).format(d));
        assertEquals("-232\u00A0323,232", new NumberTool(new Locale("pl", "pl")).format(d));
    }
}
