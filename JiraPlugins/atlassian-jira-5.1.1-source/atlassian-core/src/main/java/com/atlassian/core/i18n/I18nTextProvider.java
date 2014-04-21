package com.atlassian.core.i18n;

/**
 * Minimal interface for an i18nBean
 */
public interface I18nTextProvider
{
    String getText(String key);

    String getText(String key, Object[] args);
}
