package com.atlassian.streams.action;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadException;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.spi.StreamsLocaleProvider;

import com.google.common.collect.ImmutableMap;

import org.dom4j.Element;
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
public class ActionHandlerI18nTransformerTest
{
    private static final Locale LOCALE = Locale.ENGLISH;

    private ActionHandlerI18nTransformer transformer;

    @Mock I18nResolver i18nResolver;
    @Mock StreamsLocaleProvider localeProvider;
    @Mock Element configElement;
    @Mock ResourceLocation location;
    String filePath;
    @Mock DownloadableResource nextResource;

    @Before
    public void setup()
    {
        when(localeProvider.getUserLocale()).thenReturn(LOCALE);
        when(i18nResolver.getAllTranslationsForPrefix("prefix1", LOCALE)).thenReturn(ImmutableMap.of("key1", "value1", "key2", "value2"));
        when(i18nResolver.getAllTranslationsForPrefix("prefix2", LOCALE)).thenReturn(ImmutableMap.<String, String>of());

        transformer = new ActionHandlerI18nTransformer(i18nResolver, localeProvider);
    }

    @Test
    public void testPrefixExists()
    {
        when(location.getName()).thenReturn("prefix1.i18n.js");
        assertThat(getTransformed(), is(equalTo(getExpectedTransformation())));
    }

    @Test
    public void testPrefixDoesNotExist()
    {
        when(location.getName()).thenReturn("prefix2.i18n.js");
        assertThat(getTransformed(), is(equalTo("")));
    }

    @Test
    public void testNonI18nFileExtension()
    {
        when(location.getName()).thenReturn("dummy.js");
        assertThat(getTransformed(), is(equalTo("")));
    }

    /**
     * Returns the transformed contents.
     *
     * @return the transformed contents.
     */
    private String getTransformed()
    {
        try
        {
            DownloadableResource resource = transformer.transform(configElement, location, filePath, nextResource);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            resource.streamResource(out);
            return out.toString();
        }
        catch (DownloadException e)
        {
            return null;
        }
    }

    private String getExpectedTransformation()
    {
        return "ActivityStreams.i18n.put('key1', 'value1');\n"
               + "ActivityStreams.i18n.put('key2', 'value2');\n";
    }
}
