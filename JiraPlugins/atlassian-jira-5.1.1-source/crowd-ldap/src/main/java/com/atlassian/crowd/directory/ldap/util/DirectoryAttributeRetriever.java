package com.atlassian.crowd.directory.ldap.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ldap.UncategorizedLdapException;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class DirectoryAttributeRetriever
{
    private static final Logger logger = Logger.getLogger(DirectoryAttributeRetriever.class);

    /**
     * Retrieves the first value from the collection of attributes for the
     * supplied name directoryAttributeName. If no value exists or if the value
     * is not safe for XML marshalling, <code>null</code> is returned.
     *
     * @param directoryAttributeName attribute name key.
     * @param directoryAttributes collection of attributes to examine.
     * @return first attribute value.
     */
    public static String getValueFromAttributes(String directoryAttributeName, Attributes directoryAttributes)
    {
        if (StringUtils.isBlank(directoryAttributeName))
        {
            return null;
        }

        String value = null;

        Attribute values = directoryAttributes.get(directoryAttributeName);
        if (values != null && values.size() > 0)
        {
            try
            {
                final Object attributeValue = values.get(0);
                if (attributeValue != null)
                {
                    value = attributeValue.toString();
                    if (StringUtils.isBlank(value) || !XmlValidator.isSafe(value))
                    {
                        logger.info("Unsafe or Blank attribute value for attribute <" + directoryAttributeName + ">: '" + value + "'.");
                        value = null;
                    }
                }
            }
            catch (javax.naming.NamingException e)
            {
                throw new UncategorizedLdapException(e);
            }
        }

        return value;
    }
}
