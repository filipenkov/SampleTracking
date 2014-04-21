package com.atlassian.plugins.rest.common.validation;

import com.atlassian.sal.api.message.I18nResolver;

import javax.validation.MessageInterpolator;
import java.util.Locale;

/**
 * Looks up messages using SAL's {@link I18nResolver}.  Does not support interpolation due to a lack of support in
 * {@link I18nResolver} to accept a map of replacement variables.
 *
 * @since 2.0
 */
public class SalMessageInterpolator implements MessageInterpolator
{
    private final I18nResolver i18nResolver;

    public SalMessageInterpolator(I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    public String interpolate(String s, Context context)
    {
        // We can't do interpolation since sal (2.0) has no way to pass it a variable map.
        String message = i18nResolver.getText(s);

        // It is possible via the API it could return null
        message = (message != null ? message : s);

        return message;


    }

    public String interpolate(String s, Context context, Locale locale)
    {
        // Can't do anything with locale
        return interpolate(s, context);
    }
}
