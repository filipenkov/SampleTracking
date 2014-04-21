package com.atlassian.plugins.rest.module;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestApiVersion
{
    @Test
    public void testUnparsableVersion()
    {
        testInvalidVersion(null);
        testInvalidVersion("");
        testInvalidVersion("a");
        testInvalidVersion("a.a");
        testInvalidVersion("1.1.alpha&");
    }

    private void testInvalidVersion(String version)
    {
        try
        {
            new ApiVersion(version);
            fail(version + " should not be a valid version");
        }
        catch (InvalidVersionException e)
        {
            assertEquals(version, e.getInvalidVersion());
        }
    }

    @Test
    public void testCanParseVersionWithMajorOnly()
    {
        final ApiVersion version = new ApiVersion("2");
        assertEquals(new Integer(2), version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getClassifier());
        assertEquals("2", version.toString());
    }

    @Test
    public void testCanParseVersionWithMajorAndMinorOnly()
    {
        final ApiVersion version = new ApiVersion("2.3");
        assertEquals(new Integer(2), version.getMajor());
        assertEquals(new Integer(3), version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getClassifier());
        assertEquals("2.3", version.toString());
    }

    @Test
    public void testCanParseVersionWithMajorAndMinorAndMicro()
    {
        final ApiVersion version = new ApiVersion("2.3.4");
        assertEquals(new Integer(2), version.getMajor());
        assertEquals(new Integer(3), version.getMinor());
        assertEquals(new Integer(4), version.getMicro());
        assertNull(version.getClassifier());
        assertEquals("2.3.4", version.toString());
    }

    @Test
    public void testCanParseVersionWithMajorAndMinorAndMicroAndClassifier()
    {
        final String versionString = "2.3.4.alpha1";
        final ApiVersion version = new ApiVersion(versionString);
        assertEquals(new Integer(2), version.getMajor());
        assertEquals(new Integer(3), version.getMinor());
        assertEquals(new Integer(4), version.getMicro());
        assertEquals("alpha1", version.getClassifier());
        assertEquals(versionString, version.toString());
    }
}
