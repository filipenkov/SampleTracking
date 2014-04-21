package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage.Code;

import junit.framework.TestCase;

public class TestTransportErrorCtors extends TestCase
{
    public void testNullCodeCtor() throws Exception
    {
        try
        {
            new TransportErrorMessage(null, "message", new String[] { "param" });
            fail("Exception expected");
        }
        catch (RuntimeException expected)
        {
        }
    }

    public void testNullSingleMessageCtor() throws Exception
    {
        try
        {
            new TransportErrorMessage(Code.SYSTEM, null, new String[] { "param" });
            fail("Exception expected");
        }
        catch (RuntimeException expected)
        {
        }
    }

    public void testNullStringArrayCtor() throws Exception
    {
        try
        {
            new TransportErrorMessage(Code.SYSTEM, "message", (String[]) null);
            fail("Exception expected");
        }
        catch (RuntimeException expected)
        {
        }
    }

    public void testStringArrayContainsNullCtor() throws Exception
    {
        try
        {
            new TransportErrorMessage(Code.SYSTEM, "message", new String[] { "one", null });
            fail("Exception expected");
        }
        catch (RuntimeException expected)
        {
        }
    }

    public void testNullSingleStringCtor() throws Exception
    {
        try
        {
            new TransportErrorMessage(Code.SYSTEM, "message", (String) null);
            fail("Exception expected");
        }
        catch (RuntimeException expected)
        {
        }
    }

    public void testNullSecondStringCtor() throws Exception
    {
        try
        {
            new TransportErrorMessage(Code.SYSTEM, "message", "one", null);
            fail("Exception expected");
        }
        catch (RuntimeException expected)
        {
        }
    }

    public void testNullThirdStringCtor() throws Exception
    {
        try
        {
            new TransportErrorMessage(Code.SYSTEM, "message", "one", "two", null);
            fail("Exception expected");
        }
        catch (RuntimeException expected)
        {
        }
    }

    public void testCtorWorks() throws Exception
    {
        TransportErrorMessage message = new TransportErrorMessage(Code.UNKNOWN, "what is happening? {0}", new String[] {"dunno"});
        assertNotNull(message.getCode());
        assertSame(Code.UNKNOWN, message.getCode());
        assertSame(Code.Severity.ERROR, message.getCode().getSeverity());
        assertEquals("ERROR", message.getCode().getSeverity().toString());

        assertNotNull(message.getFormattedMessage());
        assertEquals("what is happening? dunno", message.getFormattedMessage());

        assertNotNull(message.getParameters());
        assertEquals(1, message.getParameters().length);
        assertEquals("dunno", message.getParameters()[0]);
    }
}