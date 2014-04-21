package com.atlassian.security.auth.trustedapps;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDefaultTrustedApplicationsManager
{
    final CurrentApplication thisApp = mock(CurrentApplication.class);

    @Test
    public void testSimpleConstructor() throws Exception
    {
        final Map<String, TrustedApplication> appMap = new HashMap<String, TrustedApplication>();
        appMap.put("test", mock(TrustedApplication.class));
        final TrustedApplicationsManager manager = new DefaultTrustedApplicationsManager(thisApp, appMap);

        assertNotNull(manager.getCurrentApplication());
        assertSame(thisApp, manager.getCurrentApplication());
        assertNull(manager.getTrustedApplication("something"));
        assertNotNull(manager.getTrustedApplication("test"));
    }

    @Test
    public void testSimpleConstructorNullApp() throws Exception
    {
        try
        {
            new DefaultTrustedApplicationsManager(null, new HashMap<String, TrustedApplication>());
            fail("Should have thrown ex");
        }
        catch (final IllegalArgumentException yay)
        {}
    }

    @Test
    public void testSimpleConstructorNullMap() throws Exception
    {

        try
        {
            new DefaultTrustedApplicationsManager(thisApp, null);
            fail("Should have thrown ex");
        }
        catch (final IllegalArgumentException yay)
        {}
    }

    @Test
    public void testConstructorEncryptionProvider() throws Exception
    {
        final MockKey publicKey = new MockKey();
        final EncryptionProvider provider = mock(EncryptionProvider.class);
        when(provider.generateNewKeyPair()).thenAnswer(new Answer<KeyPair>(){
            public KeyPair answer(InvocationOnMock invocation) throws Throwable
            {
                return new KeyPair(publicKey, new MockKey());
            }
        });
        when(provider.generateUID()).thenReturn("this-is-a-uid");

        final TrustedApplicationsManager manager = new DefaultTrustedApplicationsManager(provider);
        assertNotNull(manager.getCurrentApplication());
        assertNotNull(manager.getCurrentApplication().getID());
        assertNotNull(manager.getCurrentApplication().getPublicKey());
        assertSame(publicKey, manager.getCurrentApplication().getPublicKey());
    }

    @Test(expected = AssertionError.class)
    public void testConstructorEncryptionProviderThrowsNoSuchAlgorithmException() throws Exception
    {
        final EncryptionProvider provider = mock(EncryptionProvider.class);
        when(provider.generateNewKeyPair()).thenThrow(new NoSuchAlgorithmException("some-algorithm"));
        new DefaultTrustedApplicationsManager(provider);
    }

    @Test(expected = AssertionError.class)
    public void testConstructorEncryptionProviderThrowsNoSuchProviderException() throws Exception
    {
        final EncryptionProvider provider = mock(EncryptionProvider.class);
        when(provider.generateNewKeyPair()).thenThrow(new NoSuchProviderException("some-algorithm"));
        new DefaultTrustedApplicationsManager(provider);
    }
}
