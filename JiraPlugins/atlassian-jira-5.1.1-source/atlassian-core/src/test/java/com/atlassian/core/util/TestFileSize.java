package com.atlassian.core.util;

import junit.framework.TestCase;
import com.atlassian.core.util.FileSize;

public class TestFileSize extends TestCase
{
    public TestFileSize(String s)
    {
        super(s);
    }

    public void testLonglong()
    {
        assertEquals(FileSize.format(100000), FileSize.format(new Long(100000)));
    }

    public void testFormat()
    {
        assertEquals("0.5 kB", FileSize.format(512));
        assertEquals("1.0 kB", FileSize.format(1024));
        assertEquals("2 kB", FileSize.format(2048));
        assertEquals("400 kB", FileSize.format(1024 * 400));
        assertEquals("1024 kB", FileSize.format(1024 * 1024));
        assertEquals("1.20 MB", FileSize.format((long) (1024 * 1024 * 1.2)));
        assertEquals("20.00 MB", FileSize.format(1024 * 1024 * 20));
    }

    public void testFormatLessThan1KB()
    {
        // The javadoc says that < 1K will be format to KB to one decimal place.
        // 460/1024 = 0.44921875
        assertEquals("0.4 kB", FileSize.format(460));
        // 461/1024 = 0.450195313
        assertEquals("0.5 kB", FileSize.format(461));
    }

    public void testFormatLessThan1MB()
    {
        // The javadoc says Anything between a kilobyte and a megabyte is presented in kilobytes to zero decimal places.

        // You can see that depending on the exact number of bytes, you can get either "1.0 kB", or "1 kB".
        // This is inconsistent.
        assertEquals("1.0 kB", FileSize.format(1023));
        assertEquals("1.0 kB", FileSize.format(1024));
        assertEquals("1 kB", FileSize.format(1025));

        // check the rounding
        // 3583/1024 = 3.499023437 should round down to 3
        assertEquals("3 kB", FileSize.format(3583));
        // 3584/1024 = 3.5 should round up to 4
        assertEquals("4 kB", FileSize.format(3584));
    }

    public void testFormatGreaterThan1GB()
    {
        // Javadoc says "Anything greater than one megabyte is presented in megabytes to two decimal places."
        // Note that 1 GiB = 1048576 B.
        assertEquals("1024 kB", FileSize.format(1048575));
        assertEquals("1024 kB", FileSize.format(1048576));
        assertEquals("1.00 MB", FileSize.format(1048577));

        // check the rounding.
        // 1294991/1048576 = 1.234999657 should round down to 1.23
        assertEquals("1.23 MB", FileSize.format(1294991));
        // 1294992/1048576 = 1.23500061 should round up to 1.24
        assertEquals("1.24 MB", FileSize.format(1294992));
    }
}
