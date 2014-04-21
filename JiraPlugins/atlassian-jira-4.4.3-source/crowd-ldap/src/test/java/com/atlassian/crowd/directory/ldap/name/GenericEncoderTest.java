package com.atlassian.crowd.directory.ldap.name;

import junit.framework.TestCase;

public class GenericEncoderTest extends TestCase
{
    Encoder encoder;

    @Override
    public void setUp()
    {
        encoder = new GenericEncoder();
    }

    @Override
    public void tearDown()
    {
        encoder = null;
    }

    protected void testNameEncode(String input, String output)
    {
        String result = encoder.nameEncode(input);
        assertEquals(output, result);
    }

    public void testNameEncode_Normal()
    {
        testNameEncode("my name", "my name");
    }

    public void testNameEncode_Backslash()
    {
        // my \ name - lots of backslashes because it's encoded for Java and Ldap and JNDI.
        testNameEncode("my \\ name", "my \\\\\\\\ name");
    }

    public void testNameEncode_DoubleBackslash()
    {
        // my \\ name
        testNameEncode("my \\\\ name", "my \\\\\\\\\\\\\\\\ name");
    }

    public void testNameEncode_Forwardslash()
    {
        // my / name should not be encoded, except in AD
        testNameEncode("my / name", "my / name");
    }

    public void testNameEncode_Plus()
    {
        // my + name
        testNameEncode("my + name", "my \\+ name");
    }

    public void testNameEncode_Comma()
    {
        // my , name
        testNameEncode("my , name", "my \\, name");
    }

    public void testNameEncode_DoubleQuote()
    {
        // my " name
        testNameEncode("my \" name", "my \\\" name");
    }

    public void testNameEncode_LeftAngleBracket()
    {
        // my < name
        testNameEncode("my < name", "my \\< name");
    }

    public void testNameEncode_RightAngleBracket()
    {
        // my > name
        testNameEncode("my > name", "my \\> name");
    }

    public void testNameEncode_Semicolon()
    {
        // my ; name
        testNameEncode("my ; name", "my \\; name");
    }

    public void testNameEncode_HashAtBeginning()
    {
        // # my name
        testNameEncode("# my name", "\\# my name");
    }

    public void testNameEncode_Equals()
    {
        // # my name
        testNameEncode("my = name", "my \\= name");
    }
    

    public void testDnEncode(String input, String output)
    {
        String result = encoder.dnEncode(input);
        assertEquals(output, result);
    }

    public void testDnEncode_Normal()
    {
        testDnEncode("cn=my name, dc=example, dc=org", "cn=my name, dc=example, dc=org");
    }

    public void testDnEncode_Backslash()
    {
        testDnEncode("cn=my \\\\ name, dc=example \\\\ domain, dc=org", "cn=my \\\\\\\\ name, dc=example \\\\\\\\ domain, dc=org");
    }

    public void testDnEncode_EncodedForwardSlash()
    {
        testDnEncode("cn=my \\/ name, dc=example, dc=org", "cn=my \\\\/ name, dc=example, dc=org");
    }

    public void testDnEncode_UnencodedForwardSlash()
    {
        testDnEncode("cn=my / name, dc=example, dc=org", "cn=my / name, dc=example, dc=org");
    }


}
