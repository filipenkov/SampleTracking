package com.atlassian.streams.common;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.spi.StreamsI18nResolver;

import com.google.common.collect.ImmutableList;

import org.springframework.beans.factory.annotation.Qualifier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of {@link I18nResolver} that allows the plugin to specify the request's language,
 * potentially overriding the user's and application's settings.  If the request language is not specified,
 * the implementation defers to the host application's {@link I18nResolver} implementation.
 *
 * @since 4.0
 */
public class StreamsI18nResolverImpl implements StreamsI18nResolver
{
    private final I18nResolver i18nResolver;
    private final LocaleResolver localeResolver;
    private final ThreadLocal<Locale> currentLocale;

    public StreamsI18nResolverImpl(@Qualifier("i18nResolver") I18nResolver i18nResolver, LocaleResolver localeResolver)
    {
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.localeResolver = checkNotNull(localeResolver, "localeResolver");
        currentLocale = new ThreadLocal<Locale>();
    }

    @Override
    public String getText(String key, Serializable... arguments)
    {
        for (@SuppressWarnings("unused") Locale locale : getCurrentLocale())
        {
            return resolveText(key, arguments);
        }
        return i18nResolver.getText(key, arguments);
    }

    @Override
    public String getText(String key)
    {
        for (@SuppressWarnings("unused") Locale locale : getCurrentLocale())
        {
            return resolveText(key);
        }
        return i18nResolver.getText(key);
    }

    @Override
    public String getText(Message message)
    {
        for (@SuppressWarnings("unused") Locale locale : getCurrentLocale())
        {
            return getText(message.getKey(), message.getArguments());
        }
        return i18nResolver.getText(message);
    }

    @Override
    public Message createMessage(String key, Serializable... arguments)
    {
        return i18nResolver.createMessage(key, arguments);
    }

    @Override
    public MessageCollection createMessageCollection()
    {
        return i18nResolver.createMessageCollection();
    }

    @Override
    public Map<String, String> getAllTranslationsForPrefix(String prefix, Locale locale)
    {
        return i18nResolver.getAllTranslationsForPrefix(prefix, locale);
    }

    @Override
    public Map<String, String> getAllTranslationsForPrefix(String prefix)
    {
        return i18nResolver.getAllTranslationsForPrefix(prefix);
    }

    @Override
    public String getRawText(String key)
    {
        return i18nResolver.getRawText(key);
    }

    // Unfortunately I18nResolver does not expose a resolveText() method, so we have to implement our own.
    private String resolveText(String key, Serializable... arguments)
    {
        String pattern = getTranslation(key);
        MessageFormat format = new MessageFormat(pattern, getCurrentLocale().get());
        return format.format(arguments);
    }

    private String getTranslation(final String key)
    {
        final Map<String, String> translations = getAllTranslationsForPrefix(key, getCurrentLocale().get());
        final String translation = translations.get(key);
        // Return the key if the key can't be resolved.
        return translation != null ? translation : key;
    }

    @Override
    public void setRequestLanguages(String requestLanguages)
    {
        if (requestLanguages == null)
        {
            setCurrentLocale(null);
            return;
        }

        Iterable<String> languages = toLocaleNames(requestLanguages);
        Map<String, Locale> localeMap = getLocaleMap(localeResolver.getSupportedLocales());

        for (String lang : languages)
        {
            Locale locale = localeMap.get(lang);
            if (locale != null)
            {
                setCurrentLocale(locale);
                break;
            }
        }
    }

    private Option<Locale> getCurrentLocale()
    {
        return Option.option(currentLocale.get());
    }

    private void setCurrentLocale(Locale locale)
    {
        currentLocale.set(locale);
    }

    private Iterable<String> toLocaleNames(String acceptLanguage)
    {
        // Example input: "es-es, es;q=0.9, en-gb;q=0.8, en;q=0.7, *;q=0.6"
        String[] languages = acceptLanguage.split(",|;");
        ArrayList<String> localeNames = new ArrayList<String>();
        for (String l : languages)
        {
            if (!l.startsWith("q="))
            {
                String[] langElem = l.trim().split("-");
                String language = langElem[0];
                String country = langElem.length > 1 ? langElem[1] : "";
                Locale locale = new Locale(language, country);
                localeNames.add(locale.toString());
            }
        }
        return ImmutableList.copyOf(localeNames);
    }


    private Map<String, Locale> getLocaleMap(Set<Locale> locales)
    {
        HashMap<String, Locale> localeMap = new HashMap<String, Locale>();
        for (Locale locale : locales)
        {
            localeMap.put(locale.toString(), locale);
            // Arbitrarily break ties for country-independent language
            localeMap.put(locale.getLanguage(), locale);
        }
        return localeMap;
    }
}
