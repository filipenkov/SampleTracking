package com.atlassian.applinks.core.util;

import java.io.Serializable;

import com.atlassian.sal.api.message.I18nResolver;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since   v3.0
 */
public class MessageFactory
{
    private final I18nResolver resolver;

    public MessageFactory(I18nResolver resolver)
    {
        this.resolver = checkNotNull(resolver, "resolver");
    }

    public Message newI18nMessage(final String key, final Serializable... params)
    {
        checkNotNull(resolver, "resolver");
        checkNotNull(key, "key");
        checkNotNull(params, "params");
        return new Message()
        {
            public String toString()
            {
                return resolver.getText(key, params);
            }
        };
    }

    public Message newLocalizedMessage(final String message)
    {
        return new Message()
        {
            public String toString()
            {
                return message;
            }
        };
    }
}