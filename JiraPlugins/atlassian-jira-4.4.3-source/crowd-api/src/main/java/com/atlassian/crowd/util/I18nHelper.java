package com.atlassian.crowd.util;

/**
 * Gets text messages that allow for i18n.
 */
public interface I18nHelper
{
    /**
     * @param key i18n key.
     * @return internationalised text.
     */
    String getText(String key);

    /**
     * @param key i18n key.
     * @param value1 first value to interpolate.
     * @return internationalised text.
     */
    String getText(String key, String value1);

    /**
     * @param key i18n key.
     * @param value1 first value to interpolate.
     * @param value2 second value to interpolate.
     * @return internationalised text.
     */
    String getText(String key, String value1, String value2);

    /**
     * @param key i18n key.
     * @param parameters list, object array or value of parameter(s) to interpolate.
     * @return internationalised text.
     */
    String getText(String key, Object parameters);

    /**
     * @param key i18n key.
     * @return unescaped internationalised text.
     */
    String getUnescapedText(String key);
}
