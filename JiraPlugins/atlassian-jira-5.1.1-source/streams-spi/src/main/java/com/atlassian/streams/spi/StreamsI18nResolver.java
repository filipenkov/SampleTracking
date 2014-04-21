package com.atlassian.streams.spi;

import com.atlassian.sal.api.message.I18nResolver;

/**
 * Allows the plugin to request translations in a requested language, potentially overriding the current session locale
 * used by the host application's {@link I18nResolver} implementation.
 *
 * @since 4.0
 */
public interface StreamsI18nResolver extends I18nResolver
{
    /**
     * Specify the HTTP request's languages (the {@code Accept-Language} header value.
     * The setting will default to the current session locale if none of the request
     * languages are supported by the application.
     * 
     * @param requestLanguages the value of the {@code Accept-Language} header
     */
    void setRequestLanguages(String requestLanguages);
}
