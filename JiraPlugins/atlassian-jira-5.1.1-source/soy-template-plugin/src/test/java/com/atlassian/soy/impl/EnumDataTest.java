package com.atlassian.soy.impl;

import com.google.template.soy.data.restricted.StringData;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnumDataTest {
    
    public enum TestEnum {
        ONE,
        TWO
    }
    
    @Test
    public void testEquals() throws Exception {
        final EnumData enumDataOne1 = new EnumData(TestEnum.ONE);
        final EnumData enumDataOne2 = new EnumData(TestEnum.ONE);
        final EnumData enumDataTwo = new EnumData(TestEnum.TWO);
        assertTrue(enumDataOne1.equals(enumDataOne1));
        assertEquals(enumDataOne1, enumDataOne2);
        assertFalse(enumDataOne1.equals(enumDataTwo));
        assertFalse(enumDataOne1.equals(null));
        assertFalse(enumDataOne1.equals("random object"));
        assertFalse(enumDataOne1.equals(StringData.forValue("random string")));
        assertEquals(enumDataOne1, StringData.forValue(TestEnum.ONE.name()));
        assertEquals(enumDataOne1, StringData.forValue(new String(TestEnum.ONE.name())));
    }
}
