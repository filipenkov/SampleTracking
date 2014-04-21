package com.atlassian.security.auth.trustedapps;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import com.atlassian.security.auth.trustedapps.ApplicationRetriever.InvalidApplicationDetailsException;

import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestReaderApplicationRetriever
{
    static final String BASE64_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB";
    static final byte[] PUBLIC_KEY = Base64.decode(getBytes(BASE64_PUBLIC_KEY));
    private static final String CHARSET = TrustedApplicationUtils.Constant.CHARSET_NAME;

    static byte[] getBytes(String input)
    {
        try
        {
            return input.getBytes(CHARSET);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
    }

    static String getString(byte[] bytes)
    {
        try
        {
            return new String(bytes, CHARSET);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
    }

    @Test
    public void testProtocolVersion0() throws Exception
    {
        final String cert = "appId:11112222\n" + BASE64_PUBLIC_KEY;
        EncryptionProvider provider = mock(EncryptionProvider.class);
        when(provider.toPublicKey(Mockito.<byte[]>anyObject())).thenReturn(new MockKey());
        final Application app = new ReaderApplicationRetriever(new StringReader(cert), provider).getApplication();
        assertEquals("appId:11112222", app.getID());
        assertNotNull(app.getPublicKey());
        verify(provider).toPublicKey(PUBLIC_KEY);
    }

    @Test
    public void testProtocolVersion1() throws Exception
    {
        final String cert = "appId:11112222\n" + BASE64_PUBLIC_KEY + "\n" + TrustedApplicationUtils.Constant.VERSION + "\n" + TrustedApplicationUtils.Constant.MAGIC;
        EncryptionProvider provider = mock(EncryptionProvider.class);
        when(provider.toPublicKey(Mockito.<byte[]>anyObject())).thenReturn(new MockKey());
        final Application app = new ReaderApplicationRetriever(new StringReader(cert), provider).getApplication();
        assertEquals("appId:11112222", app.getID());
        assertNotNull(app.getPublicKey());
        verify(provider).toPublicKey(PUBLIC_KEY);
    }

    @Test(expected = InvalidApplicationDetailsException.class)
    public void testProtocolVersion1BadMagic() throws Exception
    {
        final String cert = "appId:11112222\n" + BASE64_PUBLIC_KEY + "\n" + TrustedApplicationUtils.Constant.VERSION + "\n" + TrustedApplicationUtils.Constant.MAGIC + "123";
        EncryptionProvider provider = mock(EncryptionProvider.class);
        final ApplicationRetriever retriever = new ReaderApplicationRetriever(new StringReader(cert), provider);
        
        retriever.getApplication();
    }

    @Test
    public void testBadReaderThrowsCtorException() throws Exception
    {
        Reader reader = new Reader()
        {
            public void close() throws IOException
            {
            }

            public int read(char[] cbuf, int off, int len) throws IOException
            {
                throw new IOException("bad reader");
            }
        };
        try
        {
            new ReaderApplicationRetriever(reader, mock(EncryptionProvider.class));
            fail("Should have thrown RuntimeException");
        }
        catch (RuntimeException yay)
        {
            assertEquals("java.io.IOException: bad reader", yay.getMessage());
        }
    }
}