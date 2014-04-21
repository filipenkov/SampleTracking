package com.atlassian.streams.common;

import java.util.Locale;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamsI18nResolverTest
{
    private static final String ENGLISH_NAME = "English";
    private static final String FRENCH_NAME = "Fran\u00e7ais";
    private static final String GERMAN_NAME = "Deutsch";
    private static final String JAPANESE_NAME = "\u65e5\u672c\u8a9e"; // Nihongo
    private static final String LANGUAGE_KEY = "lang";

    @Mock I18nResolver i18nResolver;
    @Mock LocaleResolver localeResolver;
    StreamsI18nResolverImpl streamsI18nResolver;
    
    @Before
    public void setUp() throws Exception
    {
        streamsI18nResolver = new StreamsI18nResolverImpl(i18nResolver, localeResolver);
        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.getDefault(),
                                                                              Locale.FRANCE,
                                                                              Locale.GERMANY,
                                                                              Locale.JAPAN));

        when(i18nResolver.getAllTranslationsForPrefix(LANGUAGE_KEY, Locale.getDefault()))
            .thenReturn(ImmutableMap.of(Locale.getDefault().toString(), ENGLISH_NAME));
        when(i18nResolver.getAllTranslationsForPrefix(LANGUAGE_KEY, Locale.FRANCE)).thenReturn(ImmutableMap.of(LANGUAGE_KEY, FRENCH_NAME));
        when(i18nResolver.getAllTranslationsForPrefix(LANGUAGE_KEY, Locale.GERMAN)).thenReturn(ImmutableMap.of(LANGUAGE_KEY, GERMAN_NAME));
        when(i18nResolver.getAllTranslationsForPrefix(LANGUAGE_KEY, Locale.JAPAN)).thenReturn(ImmutableMap.of(LANGUAGE_KEY, JAPANESE_NAME));
        when(i18nResolver.getText(LANGUAGE_KEY)).thenReturn(ENGLISH_NAME);
    }

    @Test
    public void testRequestLanguagesForFrenchFrench() throws Exception
    {
        streamsI18nResolver.setRequestLanguages("fr-fr, fr;q=0.9, en-gb;q=0.8, en;q=0.7, *;q=0.5");
        assertThat(streamsI18nResolver.getText(LANGUAGE_KEY), is(equalTo(FRENCH_NAME)));
    }
    
    @Test
    public void testRequestLanguagesForCanadianFrench() throws Exception
    {
        streamsI18nResolver.setRequestLanguages("fr-ca, fr;q=0.9, en-ca;q=0.8, en;q=0.7, *;q=0.5");
        assertThat(streamsI18nResolver.getText(LANGUAGE_KEY), is(equalTo(FRENCH_NAME)));
    }

    @Test
    public void testRequestLanguagesForDefaultLocale() throws Exception
    {
        // Catalan or Spanish
        streamsI18nResolver.setRequestLanguages("ca-es, ca;q=0.9, es-es;q=0.8, es;q=0.7, *;q=0.5");
        assertThat(streamsI18nResolver.getText(LANGUAGE_KEY), is(equalTo(ENGLISH_NAME)));
    }

    @Test
    public void testSetRequestLanguageWithNullRestoresDefaultLocale() throws Exception
    {
        // Set StreamsI18nResolver to a French locale
        streamsI18nResolver.setRequestLanguages("fr-fr, fr;q=0.9, en-gb;q=0.8, en;q=0.7, *;q=0.5");
        assertThat(streamsI18nResolver.getText(LANGUAGE_KEY), is(equalTo(FRENCH_NAME)));

        streamsI18nResolver.setRequestLanguages(null);
        assertThat(streamsI18nResolver.getText(LANGUAGE_KEY), is(equalTo(ENGLISH_NAME)));
    }

}
