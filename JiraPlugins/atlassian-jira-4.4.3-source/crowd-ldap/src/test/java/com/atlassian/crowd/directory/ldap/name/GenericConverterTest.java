package com.atlassian.crowd.directory.ldap.name;

import junit.framework.TestCase;

import javax.naming.CompositeName;
import javax.naming.Name;

public class GenericConverterTest extends TestCase
{
    Converter converter;

    @Override
    public void setUp()
    {
        converter = new GenericConverter(new GenericEncoder());
    }

    @Override
    public void tearDown()
    {
        converter = null;
    }

    public void testGetName_DN() throws Exception
    {
        Name name = converter.getName("cn=my name, dc=example, dc=org");
        assertNotNull(name);
        assertEquals("cn=my name, dc=example, dc=org", name.get(0));
    }


    public void testGetName_DNAndBase() throws Exception
    {
        Name name = converter.getName("cn", "my name", new CompositeName("dc=example, dc=org"));
        assertNotNull(name);
        assertEquals("cn=my name, dc=example, dc=org", name.get(0));
    }

    public void testGetName_DNAndBase_withBackslash() throws Exception
    {
        Name name = converter.getName("cn", "my \\ name", new CompositeName("dc=example, dc=org"));
        assertNotNull(name);
        assertEquals("cn=my \\\\ name, dc=example, dc=org", name.get(0));
    }

    
}
