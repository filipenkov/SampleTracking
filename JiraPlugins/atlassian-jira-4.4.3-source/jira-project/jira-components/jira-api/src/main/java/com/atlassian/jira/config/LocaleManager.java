package com.atlassian.jira.config;

import com.atlassian.jira.util.I18nHelper;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Provides Locale information for this JIRA instance.
 *
 * @since v4.0
 */
public interface LocaleManager
{
    /**
     * Returns a set of locales supported by this JIRA instance.  This is typically the language packs installed.
     *
     * @return a set of locales supported
     */
    Set<Locale> getInstalledLocales();

    /**
     * Returns a mapping of localeString to its displayname.  Also includes a 'Default' locale.
     *
     * @param defaultLocale The locale to use as the default
     * @param i18nHelper Required to internationalize the 'Default'
     * @return A mapping from localeString to its displayname.
     */
    Map<String, String> getInstalledLocalesWithDefault(Locale defaultLocale, I18nHelper i18nHelper);

    /**
     * Given a string, return the corresponding Locale.
     *
     * @param locale Locale in string form
     * @return The {@link java.util.Locale} object
     */
    Locale getLocale(String locale);
}
