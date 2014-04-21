package com.atlassian.jira.issue.label;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.commons.lang.StringUtils;

public class TestLabelParser extends ListeningTestCase
{
    @Test
    public void testInvalidLabels()
    {
        assertFalse(LabelParser.isValidLabelName("label  name"));
        assertTrue(LabelParser.isValidLabelName("valid_label_name"));
    }

    @Test
    public void testGetCleanLabel()
    {
        assertEquals(null, LabelParser.getCleanLabel(null));
        assertEquals(null, LabelParser.getCleanLabel(""));
        assertEquals(null, LabelParser.getCleanLabel("  "));

        assertEquals("a_b", LabelParser.getCleanLabel("   a b   "));

        String lotsOfAs = StringUtils.repeat("a", 255);
        assertEquals(lotsOfAs, LabelParser.getCleanLabel(lotsOfAs));
        assertEquals(lotsOfAs, LabelParser.getCleanLabel(lotsOfAs + "bbb"));
        assertEquals(lotsOfAs, LabelParser.getCleanLabel("    " + lotsOfAs));

        assertEquals("ab", LabelParser.getCleanLabel("ab"));
    }
}
