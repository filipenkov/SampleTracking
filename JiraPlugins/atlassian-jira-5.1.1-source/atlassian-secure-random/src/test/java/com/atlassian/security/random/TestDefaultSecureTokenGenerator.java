package com.atlassian.security.random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.doAnswer;

public class TestDefaultSecureTokenGenerator
{
    @Mock
    private SecureRandomService service;
    private SecureTokenGenerator generator;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        generator = new DefaultSecureTokenGenerator(service);
    }
            
    @Test
    public void testGetInstance() throws Exception
    {
        SecureTokenGenerator instance = DefaultSecureTokenGenerator.getInstance();
        assertNotNull("instance should not be null", instance);
        assertSame("same instance both times", instance, DefaultSecureTokenGenerator.getInstance());
    }

    @Test
    public void testGenerateToken() throws Exception
    {
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                byte[] bytes = (byte[]) invocation.getArguments()[0];
                assertEquals(20, bytes.length);

                // populate bytes with 0, 1, .. 19
                for (int i = 0; i < bytes.length; i++)
                {
                    bytes[i] = (byte) i;
                }

                return null;
            }
        }).when(service).nextBytes(Matchers.<byte[]>any());

        String token = generator.generateToken();

        assertEquals("000102030405060708090a0b0c0d0e0f10111213", token);
    }
}
