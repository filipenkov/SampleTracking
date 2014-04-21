package com.atlassian.jira.upgrade.tasks.jql;

import electric.xml.Element;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Class to help with parsing old JIRA XML parameter strings.
 *
 * @since v4.0
 */
final class JqlXmlSupport
{
    private static final String NAME_ATTRIBUTE = "name";

    private JqlXmlSupport()
    {
    }

    /**
     * Return the "name" of the paraemter represented by the passed XML.
     *
     * @param element the XML for the parameter.
     * @return the name of the parameter.
     * @throws IllegalArgumentException if element is null.
     */
    static String getName(final Element element)
    {
        notNull("element", element);

        if (element.getAttributeValue(NAME_ATTRIBUTE) != null)
        {
            return element.getAttributeValue(NAME_ATTRIBUTE);
        }
        else
        {
            return element.getTagName();
        }
    }

    /**
     * Return the text stored in a named sub-element. null will be returned when either the sub-element does not exist
     * or has no text.
     *
     * @param element the element whose sub-element we are to find. Cannot be null.
     * @param subElementName the name of sub-element. Cannot be blank.
     * @return the text stored in the named element. null will be returned when either the sub-element does not exist
     * @throws IllegalArgumentException if either element is null or subElement is blank.
     */
    static String getTextFromSubElement(final Element element, final String subElementName)
    {
        notNull("element", element);
        notBlank("subElementName", subElementName);

        final Element subElement = element.getElement(subElementName);
        if (subElement != null)
        {
            return subElement.getTextString();
        }
        else
        {
            return null;
        }
    }
}
