package com.atlassian.security.auth.trustedapps;

import junit.framework.TestCase;

public class TestTransportErrorMessageParser extends TestCase
{
    public void testParseSingle() throws Exception
    {
        TransportErrorMessage.Parser parser = new TransportErrorMessage.Parser();
        TransportErrorMessage message = parser.parse(TransportErrorMessage.Code.OLD_CERT.toString() + ";\t" + "This is a message with a param: {0};\t[\"paramValue\"]");
        assertNotNull(message);
        assertEquals("This is a message with a param: paramValue", message.getFormattedMessage());
    }

    public void testParseThrowsIfTooShort() throws Exception
    {
        try
        {
            new TransportErrorMessage.Parser().parse("This is a message with a param: {0};\t[\"paramValue\"]");
            fail("Should have thrown IllegalArgEx");
        }
        catch (IllegalArgumentException yay)
        {
        }
    }
}