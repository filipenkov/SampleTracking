package com.atlassian.crowd.util;

import java.util.List;
import java.util.Locale;

/**
 * Configuration for the i18n helper. It includes locale and location for the i18n bundles.
 */
public interface I18nHelperConfiguration
{
    /**
     * @return locale for internationalisation.
     */
    Locale getLocale();

    /**
     * @return internationalisation bundle locations.
     */
    List<String> getBundleLocations();
}
