package com.atlassian.crowd.directory.ldap.util;

import org.junit.Test;
import org.springframework.ldap.core.DistinguishedName;

import static org.junit.Assert.assertEquals;

public class DNStandardiserTest
{
    @Test
    public void relaxedStandardisationLeavesDnsWithSpacesUnaffected()
    {
        assertEquals("ou=partners, dc=example, dc=test",
                DNStandardiser.standardise("ou=partners, dc=example, dc=test", false));
    }

    @Test
    public void relaxedStandardisationLeavesDnsWithoutSpaces()
    {
        assertEquals("ou=partners,dc=example,dc=test",
                DNStandardiser.standardise("ou=partners,dc=example,dc=test", false));
    }

    @Test
    public void strictStandardisationRemovesSpaces()
    {
        assertEquals("ou=partners,dc=example,dc=test",
                DNStandardiser.standardise("ou=partners, dc=example, dc=test", true));
    }

    @Test
    public void strictStandardisationLeavesDnsWithoutSpaces()
    {
        assertEquals("ou=partners,dc=example,dc=test",
                DNStandardiser.standardise("ou=partners,dc=example,dc=test", true));
    }

    @Test
    public void strictStandardisationConvertsValuesToLowerCase()
    {
        assertEquals("dc=example,dc=test",
                DNStandardiser.standardise("dc=Example,dc=Test", true));
    }

    @Test
    public void strictStandardisationConvertsDistinguishedNameInstancesToLowerCase()
    {
        DistinguishedName dn = new DistinguishedName("dc=Example,dc=Test");

        assertEquals("dc=example,dc=test",
                DNStandardiser.standardise(dn, true));
    }

    @Test
    public void relaxedStandardisationConvertsDistinguishedNameInstancesToLowerCase()
    {
        DistinguishedName dn = new DistinguishedName("dc=Example,dc=Test");

        assertEquals("dc=example,dc=test",
                DNStandardiser.standardise(dn, false));
    }

    @Test
    public void relaxedStandardisationOfDistinguishedNameInstancesRemovesSpaces()
    {
        DistinguishedName dn = new DistinguishedName("dc = Example, dc = Test");

        assertEquals("dc=example,dc=test",
                DNStandardiser.standardise(dn, false));
    }
}
