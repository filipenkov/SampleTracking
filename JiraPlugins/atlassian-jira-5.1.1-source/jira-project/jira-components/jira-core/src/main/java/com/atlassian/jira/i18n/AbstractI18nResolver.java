package com.atlassian.jira.i18n;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;

import java.io.Serializable;

/**
 * Copied from sal-core.
 * <p>
 * IMPORTANT! JRA-25571 Do not add dependency on sal-core to jira-core: this causes Bugs due to ClassLoader issues.
 *
 * @since v5.0
 */
public abstract class AbstractI18nResolver implements I18nResolver
{
    private static final Serializable[] EMPTY_SERIALIZABLE = new Serializable[0];

    public String getText(String key, Serializable... arguments)
    {
        Serializable[] resolvedArguments = new Serializable[arguments.length];
        for (int i = 0; i < arguments.length; i++)
        {
            Serializable argument = arguments[i];
            if (argument instanceof Message)
            {
                resolvedArguments[i] = getText((Message) argument);
            }
            else
            {
                resolvedArguments[i] = arguments[i];
            }
        }
        return resolveText(key, resolvedArguments);
    }


    public String getText(String key)
    {
        return resolveText(key, EMPTY_SERIALIZABLE);
    }

    public String getText(Message message)
    {
        return getText(message.getKey(), message.getArguments());
    }

    public abstract String resolveText(String key, Serializable[] arguments);


	public Message createMessage(String key, Serializable... arguments)
	{
		return new DefaultMessage(key, arguments);
	}

	public MessageCollection createMessageCollection()
	{
		return new DefaultMessageCollection();
	}
}