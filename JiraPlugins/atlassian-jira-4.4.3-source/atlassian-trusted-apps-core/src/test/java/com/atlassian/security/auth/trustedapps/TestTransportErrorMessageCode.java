package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage.Code;

import junit.framework.TestCase;

public class TestTransportErrorMessageCode extends TestCase
{
    public void testUnknownCode() throws Exception
    {
        Code code = Code.get("some.random.code.that.should.not.be.registered");
        assertNotNull(code);
        assertEquals("UNKNOWN", code.getCode());
        assertSame(Code.Severity.ERROR, code.getSeverity());
    }

    public void testBadMagicCode() throws Exception
    {
        Code code = Code.get("BAD_MAGIC");
        assertNotNull(code);
        assertEquals("BAD_MAGIC", code.getCode());
        assertSame(Code.BAD_MAGIC, code);
        assertSame(Code.Severity.FAIL, code.getSeverity());
    }
}
