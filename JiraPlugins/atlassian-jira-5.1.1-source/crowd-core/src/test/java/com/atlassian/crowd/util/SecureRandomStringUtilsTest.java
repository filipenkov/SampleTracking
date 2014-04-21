package com.atlassian.crowd.util;

import com.atlassian.security.random.SecureRandomService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SecureRandomStringUtilsTest
{
    @Mock private SecureRandomService random;
    private SecureRandomStringUtils utils;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        utils = new SecureRandomStringUtils(random);
    }

    @Test
    public void testRandomStringThreeAlphabet()
    {
        when(random.nextInt(3)).thenReturn(0).thenReturn(1).thenReturn(0).thenReturn(2);

        String string = utils.randomString(4, new char[] {'a', 'b', 'c'});

        assertEquals("abac", string);
    }

    @Test
    public void testRandomStringOneAlphabet()
    {
        when(random.nextInt(3)).thenReturn(0).thenReturn(1).thenReturn(0).thenReturn(2);

        String string = utils.randomString(4, new char[] {'a'});

        assertEquals("aaaa", string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomStringNoAlphabet()
    {
        utils.randomString(4, new char[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomStringNegativeLength()
    {
        utils.randomString(-1, new char[] {'a', 'b', 'c'});
    }

    @Test
    public void testRandomStringZeroLength()
    {
        String string = utils.randomString(0, new char[] {'a', 'b', 'c'});

        assertEquals(0, string.length());
    }

    @Test
    public void testRandomAlphanumericString()
    {
        when(random.nextInt(62)).thenReturn(0).thenReturn(1).thenReturn(0).thenReturn(61);

        String string = utils.randomAlphanumericString(4);

        assertEquals("010z", string);
    }
}
