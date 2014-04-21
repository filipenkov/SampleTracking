package com.atlassian.security.random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.SecureRandom;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultSecureRandomService
{
    @Mock
    private SecureRandom secureRandom;
    private SecureRandomService service;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        service = new DefaultSecureRandomService(secureRandom);
    }

    @Test
    public void testGetInstance() throws Exception
    {
        SecureRandomService instance = DefaultSecureRandomService.getInstance();
        assertNotNull("instance should not be null", instance);
        assertSame("same instance both times", instance, DefaultSecureRandomService.getInstance());
    }

    @Test
    public void testNextBytes() throws Exception
    {
        byte[] data = new byte[20];

        service.nextBytes(data);

        verify(secureRandom).nextBytes(data);
    }

    @Test
    public void testNextInt() throws Exception
    {
        int expected = 123;

        when(secureRandom.nextInt()).thenReturn(expected);

        int actual = service.nextInt();

        assertEquals(expected, actual);
    }

    @Test
    public void testNextIntN() throws Exception
    {
        int modulo = 123;
        int expected = 12;

        when(secureRandom.nextInt(modulo)).thenReturn(expected);

        int actual = service.nextInt(modulo);

        assertEquals(expected, actual);
    }

    @Test
    public void testNextLong() throws Exception
    {
        long expected = 123;

        when(secureRandom.nextLong()).thenReturn(expected);

        long actual = service.nextLong();

        assertEquals(expected, actual);
    }

    @Test
    public void testNextBoolean() throws Exception
    {
        boolean expected = true;

        when(secureRandom.nextBoolean()).thenReturn(expected);

        boolean actual = service.nextBoolean();

        assertEquals(expected, actual);
    }

    @Test
    public void testNextFloat() throws Exception
    {
        float expected = 12.3f;

        when(secureRandom.nextFloat()).thenReturn(expected);

        float actual = service.nextFloat();

        assertEquals(expected, actual);
    }

    @Test
    public void testNextDouble() throws Exception
    {
        double expected = 12.3f;

        when(secureRandom.nextDouble()).thenReturn(expected);

        double actual = service.nextDouble();

        assertEquals(expected, actual);
    }
}
