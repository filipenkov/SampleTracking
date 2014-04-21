package com.atlassian.crowd.directory.ldap.util;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

/**
 * Code based on: http://jira.springframework.org/browse/LDAP-176
 * To fix: http://jira.atlassian.com/browse/CWD-1445
 *
 * Utility class that helps with reading all attribute values from Active Directory using <em>Incremental Retrieval of
 * Multi-valued Properties</em>.
 *
 * <p>Example usage of this attribute mapper:
 * <pre>
 *     public void retrieveAttributeIncrementally(LdapTemplate ldap, LdapName entrDn, String attributeName, AttributeValueProcessor valueProcessor)
 *     {
 *         IncrementalAttributeMapper incrementalAttributeMapper = new IncrementalAttributeMapper(attributeName, valueProcessor);
 *
 *         while (incrementalAttributeMapper.hasMore())
 *         {
 *             ldap.lookup(entrDn, incrementalAttributeMapper.getAttributesArray(), incrementalAttributeMapper);
 *         }
 *     }
 * </pre>
 *
 * @author Marius Scurtescu
 * @see <a href="http://www.watersprings.org/pub/id/draft-kashi-incremental-00.txt">Incremental Retrieval of Multi-valued Properties</a>
 */
public class IncrementalAttributeMapper implements AttributesMapper
{
    private String attributeName;
    private boolean more = true;
    private RangeOption requestRange;
    private AttributeValueProcessor valueProcessor;
    private boolean omitFullRange = true;

    public IncrementalAttributeMapper(String attributeName, AttributeValueProcessor valueProcessor)
    {
        this(attributeName, valueProcessor, new RangeOption(0, RangeOption.TERMINAL_END_OF_RANGE));
    }

    public IncrementalAttributeMapper(String attributeName, AttributeValueProcessor valueProcessor, RangeOption requestRange)
    {
        this.attributeName = attributeName;
        this.valueProcessor = valueProcessor;
        this.requestRange = requestRange;
    }

    public boolean isOmitFullRange()
    {
        return omitFullRange;
    }

    public void setOmitFullRange(boolean omitFullRange)
    {
        this.omitFullRange = omitFullRange;
    }

    public Object mapFromAttributes(Attributes attributes) throws NamingException
    {
        if (!more)
            throw new IllegalStateException("No more attributes!");

        more = false;

        NamingEnumeration<String> attributeNameEnum = attributes.getIDs();

        while (attributeNameEnum.hasMore())
        {
            String attributeName = attributeNameEnum.next();

            if (attributeName.equals(this.attributeName))
            {
                processValues(attributes, this.attributeName);
            }
            else if (attributeName.startsWith(this.attributeName + ";"))
            {
                for (String option : attributeName.split(";"))
                {
                    RangeOption responseRange = RangeOption.parse(option);

                    if (responseRange != null)
                    {
                        more = requestRange.compareTo(responseRange) > 0;

                        if (more)
                        {
                            requestRange = responseRange.nextRange(RangeOption.TERMINAL_END_OF_RANGE);
                        }

                        processValues(attributes, attributeName);
                    }
                }
            }
        }

        return this;
    }

    private void processValues(Attributes attributes, String attributeName) throws NamingException
    {
        Attribute attribute = attributes.get(attributeName);
        NamingEnumeration valueEnum = attribute.getAll();

        while (valueEnum.hasMore())
        {
            valueProcessor.process(valueEnum.next());
        }
    }

    public boolean hasMore()
    {
        return more;
    }

    public String [] getAttributesArray()
    {
        StringBuilder attributeBuilder = new StringBuilder(attributeName);

        if (!(omitFullRange && requestRange.isFullRange()))
        {
            attributeBuilder.append(';');

            requestRange.toString(attributeBuilder);
        }

        return new String[]{attributeBuilder.toString()};
    }
}

