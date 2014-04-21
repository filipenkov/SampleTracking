package com.atlassian.applinks.core.plugin;

import org.dom4j.Element;

/**
 * This class contains the logic for reading the weight value from a module descriptor's XML element.
 */
public class DescriptorWeightAttributeParser
{
    public static final int DEFAULT_WEIGHT = 1000;

    /**
     * @param moduleDescriptorElement a module descriptor XML element.
     * @return the value of the <code>weight</code> attribute of the specified module descriptor element, or the
     *         system's default weight value if no weight was specified.
     */
    public static int getWeight(final Element moduleDescriptorElement)
    {
        try
        {
            return Integer.parseInt(moduleDescriptorElement.attributeValue("weight"));
        }
        catch (final NumberFormatException e)
        {
            return DEFAULT_WEIGHT;
        }
    }

}
