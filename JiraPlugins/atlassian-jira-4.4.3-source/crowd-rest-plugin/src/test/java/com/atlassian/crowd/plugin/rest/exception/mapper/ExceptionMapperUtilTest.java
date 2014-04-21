package com.atlassian.crowd.plugin.rest.exception.mapper;

import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class ExceptionMapperUtilTest
{
    @Test
    public void testStripNull()
    {
        assertNull(ExceptionMapperUtil.stripNonValidXMLCharacters(null));
    }

    @Test
    public void testStripEmptyString()
    {
        assertEquals(ExceptionMapperUtil.stripNonValidXMLCharacters(""), "");
    }

    @Test
    public void testNoStrip()
    {
        assertEquals("totally valid string 123", ExceptionMapperUtil.stripNonValidXMLCharacters("totally valid string 123"));
    }

    @Test
    public void testStripZerothUnicode()
    {
        assertEquals("[LDAP: error code 53 - 0000001F: SvcErr: DSID-031A0FC0, problem 5003 (WILL_NOT_PERFORM), data 0\n" +
                    "]; nested exception is javax.naming.OperationNotSupportedException: [LDAP: error code 53 - 0000001F: SvcErr: DSID-031A0FC0, problem 5003 (WILL_NOT_PERFORM), data 0\n" +
                    "]; remaining name 'cn=pi, dc=sydney,dc=atlassian,dc=com'", ExceptionMapperUtil.stripNonValidXMLCharacters("[LDAP: error code 53 - 0000001F: SvcErr: DSID-031A0FC0, problem 5003 (WILL_NOT_PERFORM), data 0\n" +
                    "\u0000]; nested exception is javax.naming.OperationNotSupportedException: [LDAP: error code 53 - 0000001F: SvcErr: DSID-031A0FC0, problem 5003 (WILL_NOT_PERFORM), data 0\n" +
                    "\u0000]; remaining name 'cn=pi, dc=sydney,dc=atlassian,dc=com'"));
    }

    @Test
    public void testStripUnicodes()
    {
        assertEquals("abcdefghijklmnopq\u0009\u0020\uFFFD", ExceptionMapperUtil.stripNonValidXMLCharacters("a\u0008b\u000Bc\u0018d\uFFFEe\uFFFFfghijklmnopq\u0009\u0020\uFFFD"));
    }
    
    @Test
    public void supplementaryCharactersAreHandled()
    {
        // A single character that is UTF-16-encoded as two Java chars
        String supplementaryCharacter = new String(Character.toChars(0x10400));

        assertEquals("Our string has two UTF-16 code points",
                2, supplementaryCharacter.length());
        
        assertEquals(supplementaryCharacter, ExceptionMapperUtil.stripNonValidXMLCharacters(supplementaryCharacter));
    }
}
