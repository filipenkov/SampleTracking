package com.atlassian.renderer.v2.macro.basic;

import junit.framework.TestCase;

public class TestCssSizeValue extends TestCase
{
    public void testValue()
    {
        assertEquals(123, new CssSizeValue("123px").value());
        assertEquals(123, new CssSizeValue("123    px").value());
        assertEquals(123, new CssSizeValue("123").value());
        assertEquals(0, new CssSizeValue("dsfasdfdsafsd").value());
    }
    
    public void testIsValid()
    {
        assertTrue(new CssSizeValue("123px").isValid());
        assertTrue(new CssSizeValue("123pt").isValid());
        assertTrue(new CssSizeValue("123em").isValid());
        assertFalse(new CssSizeValue("123foobar").isValid());
        assertTrue(new CssSizeValue("123").isValid());
        assertTrue(new CssSizeValue("123     px").isValid());
        assertFalse(new CssSizeValue("sdfasdfdasfsdaf").isValid());
    }
}
