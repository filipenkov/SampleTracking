package com.atlassian.security.auth.trustedapps;

import java.util.Arrays;

import com.atlassian.security.auth.trustedapps.ApplicationRetriever.ApplicationNotFoundException;

import org.mockito.Mockito;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestListApplicationRetriever extends TestCase
{
    final String encoding = "UTF-8";
    static final String BASE64_PUBLIC_KEY = TestReaderApplicationRetriever.BASE64_PUBLIC_KEY;
    static final byte[] PUBLIC_KEY = TestReaderApplicationRetriever.PUBLIC_KEY;

    public void testGetApplicationListV0() throws Exception
    {
        final MockKey key = new MockKey();
        EncryptionProvider provider = mock(EncryptionProvider.class);
        when((provider.toPublicKey(Mockito.<byte[]>anyObject()))).thenReturn(key);

        Application application = construct(new String[] { "appId", BASE64_PUBLIC_KEY }, provider).getApplication();
        assertSame(key, application.getPublicKey());
        verify(provider).toPublicKey(PUBLIC_KEY);
    }

    public void testGetApplicationListV1() throws Exception
    {

        final MockKey key = new MockKey();
        EncryptionProvider provider = mock(EncryptionProvider.class);
        when((provider.toPublicKey(Mockito.<byte[]>anyObject()))).thenReturn(key);

        Application application = construct(new String[] { "appId", BASE64_PUBLIC_KEY, TrustedApplicationUtils.Constant.VERSION.toString(), TrustedApplicationUtils.Constant.MAGIC }, provider).getApplication();
        assertSame(key, application.getPublicKey());
        verify(provider).toPublicKey(PUBLIC_KEY);
    }

    public void testGetApplicationListTooSmall() throws Exception
    {
        try
        {
            construct(new String[0]).getApplication();
            fail("Should have thrown NotFoundException");
        }
        catch (ApplicationNotFoundException expected)
        {
        }
    }

    public void testConstructorAppIdParamContainNoNullsV1()
    {
        construct(new String[] { "applicationId", "publicKey", TrustedApplicationUtils.Constant.VERSION.toString(), TrustedApplicationUtils.Constant.MAGIC });
    }

    public void testConstructorParamsContainNullsV1() throws Exception
    {
        try
        {
            construct(new String[] { null, null, null, null });
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testConstructorAppIdParamContainNullsV1() throws Exception
    {
        try
        {
            construct(new String[] { "applicationId", null, TrustedApplicationUtils.Constant.VERSION.toString(), TrustedApplicationUtils.Constant.MAGIC });
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testConstructorPublicKeyParamContainNullsV1() throws Exception
    {
        try
        {
            construct(new String[] { null, "publicKey", TrustedApplicationUtils.Constant.VERSION.toString(), TrustedApplicationUtils.Constant.MAGIC });
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testConstructorAppIdParamContainNoNullsV0()
    {
        construct(new String[] { "applicationId", "publicKey" });
    }

    public void testConstructorParamsContainNullsV0() throws Exception
    {
        try
        {
            construct(new String[] { null, null });
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testConstructorAppIdParamContainNullsV0() throws Exception
    {
        try
        {
            construct(new String[] { "applicationId", null });
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testConstructorPublicKeyParamContainNullsV0() throws Exception
    {
        try
        {
            construct(new String[] { null, "publicKey" });
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    private ApplicationRetriever construct(String[] input)
    {
        EncryptionProvider provider = mock(EncryptionProvider.class);
        return construct(input, provider);
    }

    private ApplicationRetriever construct(String[] input, EncryptionProvider provider)
    {
        return new ListApplicationRetriever(provider, Arrays.asList(input));
    }
}
