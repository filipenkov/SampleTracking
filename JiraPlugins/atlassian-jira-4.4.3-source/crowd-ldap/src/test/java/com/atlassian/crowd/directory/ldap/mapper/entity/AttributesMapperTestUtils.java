package com.atlassian.crowd.directory.ldap.mapper.entity;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

final class AttributesMapperTestUtils
{
    private AttributesMapperTestUtils()
    {
    }

    /**
     * Asserts that the given attributes object does not contain an empty string ("")
     * since some directory implementations do not like it.
     *
     * @param attributes attributes to be asserted.
     */
    static void assertAttributesValuesNeverEmpty(Attributes attributes)
    {
        final NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();

        try
        {
            while (attributeEnumeration.hasMore())
            {
                Attribute attribute = attributeEnumeration.next();
                NamingEnumeration values = attribute.getAll();
                while (values.hasMore())
                {
                    String val = (String)values.next();
                    assertNotNull("found null value in " + attribute.getID(), val);
                    assertFalse("not empty string as value in " + attribute.getID(), val.length() == 0);
                }
            }
        }
        catch (NamingException ne)
        {
            throw new RuntimeException("Error during enumerating attributes", ne);
        }
    }
}