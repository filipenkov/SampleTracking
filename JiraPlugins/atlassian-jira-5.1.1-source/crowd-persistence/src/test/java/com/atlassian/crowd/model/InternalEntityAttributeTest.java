package com.atlassian.crowd.model;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InternalEntityAttributeTest
{
    private static final String SHORT_STRING = StringUtils.repeat("X", 255);
    private static final String LONG_STRING = StringUtils.repeat("X", 300);
    private static final String TRUNCATED_STRING = StringUtils.repeat("X", 252).concat("...");

    @Test
    public void testInternalEntityAttribute_ShortValue()
    {
        final InternalEntityAttribute attribute = new InternalEntityAttribute("name", SHORT_STRING);

        assertEquals(SHORT_STRING, attribute.getValue());
    }

    @Test
    public void testInternalEntityAttribute_LongValue()
    {
        final InternalEntityAttribute attribute = new InternalEntityAttribute("name", LONG_STRING);

        assertEquals(TRUNCATED_STRING, attribute.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInternalEntityAttribute_LongName()
    {
        new InternalEntityAttribute(LONG_STRING, "value");
    }
}
