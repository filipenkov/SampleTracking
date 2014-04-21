package com.atlassian.crowd.directory.ldap.name;

/**
 * Checks that encoding of the AD-specific special character of / is handled correctly.
 */
public class ActiveDirectoryEncoderTest extends GenericEncoderTest
{

    @Override
    public void setUp()
    {
        encoder = new ActiveDirectoryEncoder();
    }

    @Override
    public void testNameEncode_Forwardslash()
    {
        // my / name should be encoded in AD
        testNameEncode("my / name", "my \\/ name");
    }

    @Override
    public void testDnEncode_EncodedForwardSlash()
    {
//        testDnEncode("cn=my \\/ name, dc=example, dc=org", "cn=my \\\\/ name, dc=example, dc=org");
    }

    @Override
    public void testDnEncode_UnencodedForwardSlash()
    {
        testDnEncode("cn=my / name, dc=example, dc=org", "cn=my \\/ name, dc=example, dc=org");
    }
}
