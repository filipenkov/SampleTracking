package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import org.apache.commons.lang3.StringUtils;

/**
 * Utilities for web section keys.
 *
 * @since 1.0
 */
public final class SectionKeys
{
    private SectionKeys()
    {
        throw new AssertionError("Don't instantiate me");
    }


    public static String fullSectionKey(AdminLinkSection section)
    {
        return fullSectionKey(section.getLocation(), section.getId());
    }

    public static String fullSectionKey(String location, String sectionId)
    {
        return StringUtils.isNotEmpty(location) ? location + "/" + sectionId : sectionId;
    }
}
