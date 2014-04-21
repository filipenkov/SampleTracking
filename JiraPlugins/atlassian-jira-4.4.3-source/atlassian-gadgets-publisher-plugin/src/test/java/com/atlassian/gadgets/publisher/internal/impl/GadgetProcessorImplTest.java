package com.atlassian.gadgets.publisher.internal.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Locale;
import java.util.Set;

import com.atlassian.gadgets.util.Uri;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetProcessorImplTest
{
    private static final String VALID_RESOURCE_MODULE_COMPLETE_KEY = "plugin.key:resource-name";
    private static final String VALID_RESOURCE_MODULE_COMPLETE_KEY2 = "plugin.key:resource-name2";
    private static final String VALID_RESOURCE_MODULE_COMPLETE_KEY3 = "plugin.key:resource-name3";
    private static final String MOCK_APP_BASE_URL = "http://example.org/baseUrl";
    private static final String MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS =
        MOCK_APP_BASE_URL + "/\\,./;'\"1!2@3#4$5%6^7&8*9(0)-_=+";
    private static final String RESOURCE_PATH = "/path/to/resource.rsrc";
    private static final String REPLACED_STATIC_RESOURCE_CALL = MOCK_APP_BASE_URL + RESOURCE_PATH;
    private static final String ENCODING = "UTF-8";

    @Mock ApplicationProperties applicationProperties;
    @Mock WebResourceManager webResourceManager;
    @Mock LocaleResolver localeResolver;
    @Mock I18nResolver i18nResolver;

    private GadgetProcessorImpl gadgetProcessor;

    @Before
    public void setup()
    {
        when(applicationProperties.getBaseUrl()).thenReturn(MOCK_APP_BASE_URL);
        when(webResourceManager.getStaticPluginResource(eq(VALID_RESOURCE_MODULE_COMPLETE_KEY),
                                                        anyString(),
                                                        eq(UrlMode.ABSOLUTE)))
            .thenReturn(REPLACED_STATIC_RESOURCE_CALL);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Iterable<String> elems = (Iterable<String>) args[0];
                Writer writer = ((Writer)args[1]);

                try
                {
                    writer.write(StringUtils.join(elems.iterator(), ","));
                }
                catch (IOException e)
                {
                    throw new RuntimeException();
                }

                return null;
            }})
        .when(webResourceManager).includeResources(anyList(), any(Writer.class), eq(UrlMode.ABSOLUTE));

        gadgetProcessor =
            new GadgetProcessorImpl(applicationProperties, webResourceManager, localeResolver, i18nResolver);
    }

    @Test(expected=NullPointerException.class)
    public void assertProcessWillNotTolerateNullInputStreams() throws IOException
    {
        gadgetProcessor.process(null, new NullOutputStream());
    }

    @Test(expected=NullPointerException.class)
    public void assertProcessWillNotTolerateNullOutputStreams() throws IOException
    {
        gadgetProcessor.process(new NullInputStream(0), null);
    }

    @Test
    public void assertUnicodeGadgetContentIsPreserved() throws IOException
    {
        String gadgetWithUnicodeContent =
            "<Module>"
            + "<ModulePrefs title='Unicode Snowman'/>"
            + "<Content>\u2603</Content>"
            + "</Module>";

        InputStream in = new ByteArrayInputStream(gadgetWithUnicodeContent.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(gadgetWithUnicodeContent)));
    }

    @Test
    public void assertAtlassianBaseUrlIsSubstitutedCorrectly() throws IOException
    {
        String testString = "<blah><more blah>\\n<container>__ATLASSIAN_BASE_URL__</container>\\n</moreblah></blah>";
        InputStream in = new ByteArrayInputStream(testString.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<blah><more blah>\\n<container>" +
                              MOCK_APP_BASE_URL +
                              "</container>\\n</moreblah></blah>")));

    }

    @Test
    public void assertAtlassianBaseUrlWithSpecialCharactersIsSubstitutedCorrectly() throws IOException
    {
        when(applicationProperties.getBaseUrl()).thenReturn(MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS);
        String testString = "<blah><more blah>\\n<container>__ATLASSIAN_BASE_URL__</container>\\n</moreblah></blah>";
        InputStream in = new ByteArrayInputStream(testString.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<blah><more blah>\\n<container>" +
                              MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS +
                              "</container>\\n</moreblah></blah>")));

    }

    @Test
    public void assertNullAtlassianBaseUrlCausesReturnOfOriginalString() throws IOException
    {
        when(applicationProperties.getBaseUrl()).thenReturn(null);
        String testString = "<blah><more blah>\\n<container>__ATLASSIAN_BASE_URL__</container>\\n</moreblah></blah>";
        InputStream in = new ByteArrayInputStream(testString.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(testString)));
    }

    @Test
    public void assertEmptyAtlassianBaseUrlCausesReturnOfOriginalString() throws IOException
    {
        when(applicationProperties.getBaseUrl()).thenReturn("");
        String testString = "<blah><more blah>\\n<container>__ATLASSIAN_BASE_URL__</container>\\n</moreblah></blah>";
        InputStream in = new ByteArrayInputStream(testString.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(testString)));
    }

    // #requireResource tests:
    // We'll need to make these tests more rigorous, maybe by having
    // WebResourceManager read a test atlassian-plugin.xml. For now, though,
    // we want to make sure a valid requireResource is detected and removed.
    // Invalid ones should be ignored.

    @Test
    public void assertRequireResourceIsProcessedCorrectly() throws IOException
    {
        String requireResource = "#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\")";
        InputStream in = new ByteArrayInputStream(appendInclude(requireResource).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(VALID_RESOURCE_MODULE_COMPLETE_KEY)));
    }

    @Test
    public void assertInvalidRequireResourceIsIgnored() throws IOException
    {
        String requireResourceWithTypo = "#requirResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\")";
        InputStream in = new ByteArrayInputStream(appendInclude(requireResourceWithTypo).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(requireResourceWithTypo)));
    }

    @Test
    public void assertRequireResourceWithBadPluginKeyIsIgnored() throws IOException
    {
        String badRequireResource = "#requireResource(\"correct.key:bad?plugin!name\")";
        InputStream in = new ByteArrayInputStream(appendInclude(badRequireResource).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(badRequireResource)));
    }

    @Test
    public void assertEmbeddedRequireResourceIsProcessedCorrectly() throws IOException
    {
        String requireResource =
            "<xml><nonsense><blah>\n" +
            "#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\")\n" +
            "#includeResources()\n" +
            "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(appendInclude(requireResource).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<xml><nonsense><blah>\n" +
                              "\n" +
                              VALID_RESOURCE_MODULE_COMPLETE_KEY +
                              "\n" +
                              "</blah></nonsense></xml>")));
    }

    @Test
    public void assertEmbeddedInvalidRequireResourceIsIgnored() throws IOException
    {
        String badRequireResource =
            "<xml><nonsense><blah>\n" +
            "#requireResorce(\"ignored.key:ignored-name\")\n" +
            "#includeResources()\n" +
            "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(appendInclude(badRequireResource).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<xml><nonsense><blah>\n" +
                              "#requireResorce(\"ignored.key:ignored-name\")\n" +
                              "\n" +
                              "</blah></nonsense></xml>")));
    }

    @Test
    public void assertValidRequireResourceIsReplacedAndInvalidIsNot() throws IOException
    {
        String mixedRequireResource =
            "<xml><nonsense><blah>\n" +
            "#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\")\n" +
            "#requireResourc(\"ignored.key:ignored-name\")\n" +
            "#includeResources()\n" +
            "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(appendInclude(mixedRequireResource).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<xml><nonsense><blah>\n" +
                              "\n" +
                              "#requireResourc(\"ignored.key:ignored-name\")\n" +
                              VALID_RESOURCE_MODULE_COMPLETE_KEY +
                              "\n" +
                              "</blah></nonsense></xml>")));
    }

    @Test
    public void assertRequireResourceUrlWithMissingQuoteIsNotReplaced() throws IOException
    {
        String requireResource = "#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + ")";
        InputStream in = new ByteArrayInputStream(appendInclude(requireResource).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(requireResource)));
    }

    // #includeResources tests

    @Test
    public void assertIncludeResourcesWithoutParenthesesStillWorks() throws IOException
    {
        String requireResource = "test text#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\")";
        InputStream inWithParens =
            new ByteArrayInputStream((requireResource + "#includeResources()").getBytes(ENCODING));
        ByteArrayOutputStream outWithParens = new ByteArrayOutputStream();
        gadgetProcessor.process(inWithParens, outWithParens);

        InputStream inWithoutParens =
            new ByteArrayInputStream((requireResource + "#includeResources").getBytes(ENCODING));
        ByteArrayOutputStream outWithoutParens = new ByteArrayOutputStream();
        gadgetProcessor.process(inWithoutParens, outWithoutParens);

        assertThat(outWithParens.toString(ENCODING), is(equalTo("test text" + VALID_RESOURCE_MODULE_COMPLETE_KEY)));
        assertThat(outWithoutParens.toString(ENCODING), is(equalTo("test text" + VALID_RESOURCE_MODULE_COMPLETE_KEY)));
    }

    @Test
    public void assertMisspelledIncludeResourcesIsANoop() throws IOException
    {
        String badIncludeResources =
            "test text#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\")#includeResorces()";
        InputStream in = new ByteArrayInputStream(badIncludeResources.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        gadgetProcessor.process(in, out);

        assertThat(out.toString(ENCODING), is(equalTo(badIncludeResources)));
    }

    @Test
    public void assertIntermixedRequireResourceCallsAreProcessedCorrectlyByInterwovenIncludeResourcesCalls()
            throws IOException
    {
        String requireResource =
            "<xml><nonsense><blah>\n" +
            "<Content>\n" +
            "#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\")\n" +
            "#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY2 + "\")\n" +
            "#includeResources()\n" +
            "</Content>\n" +
            "<Content>\n" +
            "#requireResource(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY3 + "\")\n" +
            "#includeResources()\n" +
            "</Content>\n" +
            "</blah></nonsense></xml>";

        InputStream in = new ByteArrayInputStream(appendInclude(requireResource).getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<xml><nonsense><blah>\n<Content>\n" +
                              "\n" +
                              "\n" +
                              VALID_RESOURCE_MODULE_COMPLETE_KEY + "," + VALID_RESOURCE_MODULE_COMPLETE_KEY2 +
                              "\n" +
                              "</Content>\n<Content>\n" +
                              "\n" +
                              VALID_RESOURCE_MODULE_COMPLETE_KEY3 +
                              "\n" +
                              "</Content>\n</blah></nonsense></xml>")));
    }

    // #staticResourceUrl tests

    @Test
    public void assertValidStaticResourceUrlIsProcessedCorrectly() throws IOException
    {
        String staticResourceUrl = "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"static.resource\")";
        InputStream in = new ByteArrayInputStream(staticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(REPLACED_STATIC_RESOURCE_CALL)));
    }

    @Test
    public void assertValidStaticResourceUrlInSubdirectoryIsProcessedCorrectly() throws IOException
    {
        String staticResourceUrl = "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"path/to/static.resource\")";
        InputStream in = new ByteArrayInputStream(staticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(REPLACED_STATIC_RESOURCE_CALL)));
    }

    @Test
    public void assertStaticResourceUrlWithTypoIsIgnored() throws IOException
    {
        String staticResourceUrlWithTypo = "#staticResourcUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"static.resource\")";
        InputStream in = new ByteArrayInputStream(staticResourceUrlWithTypo.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(staticResourceUrlWithTypo)));        
    }

    @Test
    public void assertStaticResourceUrlCallWithOnlyOneArgumentIsIgnored() throws IOException
    {
        String staticResourceUrlWithMissingArgument = "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",)";
        InputStream in = new ByteArrayInputStream(staticResourceUrlWithMissingArgument.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(staticResourceUrlWithMissingArgument)));
    }

    @Test
    public void assertStaticResourceUrlCallWithSpaceAfterCommaIsProcessedCorrectly()
        throws IOException
    {
        String staticResourceUrl =
            "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\", \"static.resource\")";
        InputStream in = new ByteArrayInputStream(staticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(REPLACED_STATIC_RESOURCE_CALL)));
    }

    @Test
    public void assertValidStaticResourceUrlWithSpecialCharactersIsProcessedCorrectly() throws IOException
    {
        when(webResourceManager.getStaticPluginResource(eq(VALID_RESOURCE_MODULE_COMPLETE_KEY),
                                                        anyString(),
                                                        eq(UrlMode.ABSOLUTE)))
            .thenReturn(MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS + RESOURCE_PATH);
        String staticResourceUrl =
            "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"static.resource\")";
        InputStream in = new ByteArrayInputStream(staticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS + RESOURCE_PATH)));
    }

    @Test
    public void assertEmbeddedStaticResourceUrlIsProcessedCorrectly() throws IOException
    {
        String staticResourceUrl =
            "<xml><nonsense><blah>\n" +
            "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"resource.name\")\n" +
            "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(staticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<xml><nonsense><blah>\n" +
                              REPLACED_STATIC_RESOURCE_CALL + "\n" +
                              "</blah></nonsense></xml>")));
    }

    @Test
    public void assertEmbeddedInvalidStaticResourceUrlIsIgnored() throws IOException
    {
        String badStaticResourceUrl = "<xml><nonsense><blah>\n" +
                "#staticResorceUrl(\"ignored.key:ignored-name\",\"resource.name\")\n" +
                "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(badStaticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(badStaticResourceUrl)));
    }

    @Test
    public void assertValidStaticResourceUrlIsReplacedAndInvalidIsNot() throws IOException
    {
        String mixedStaticResourceUrl =
            "<xml><nonsense><blah>\n" +
            "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"resource.name\")\n" +
            "#staticResourcUrl(\"ignored.key:ignored-name\",\"resource.name\")\n" +
            "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(mixedStaticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<xml><nonsense><blah>\n" +
                              REPLACED_STATIC_RESOURCE_CALL + "\n" +
                              "#staticResourcUrl(\"ignored.key:ignored-name\",\"resource.name\")\n" +
                              "</blah></nonsense></xml>")));
    }

    @Test
    public void assertMultipleValidStaticResourceUrlCallsAreReplaced() throws IOException
    {
        String dualStaticResourceUrl =
            "<xml><nonsense><blah>\n" +
            "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"resource.name\")\n" +
            "#staticResourceUrl(\"" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"resource.name\")\n" +
            "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(dualStaticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<xml><nonsense><blah>\n" +
                              REPLACED_STATIC_RESOURCE_CALL + "\n" +
                              REPLACED_STATIC_RESOURCE_CALL + "\n" +
                              "</blah></nonsense></xml>")));
    }

    @Test
    public void assertStaticResourceUrlWithMissingQuoteIsNotReplaced() throws IOException
    {
        String staticResourceUrl = "#staticResourceUrl(" + VALID_RESOURCE_MODULE_COMPLETE_KEY + "\",\"static.resource\")";
        InputStream in = new ByteArrayInputStream(staticResourceUrl.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(staticResourceUrl)));
    }

    // #supportedLocales tests

    @Test
    public void assertValidSupportedLocalesIsReplaced() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales(\"hello\")"
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.FRENCH));
        when(i18nResolver.getAllTranslationsForPrefix("hello", new Locale("")))
            .thenReturn(ImmutableMap.of("hello.world", "Hello, world",
                                        "hello.country", "Hello, wherever you are"));
        when(i18nResolver.getAllTranslationsForPrefix("hello", Locale.FRENCH))
            .thenReturn(ImmutableMap.of("hello.world", "Bonjour tout le monde",
                                        "hello.country", "Bonjour, France"));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>"
                              + "<ModulePrefs title='Supported Locales'>"
                              + "<Locale>"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Hello, world</msg>"
                              + "<msg name=\"hello.country\">Hello, wherever you are</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "<Locale lang=\"fr\">"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Bonjour tout le monde</msg>"
                              + "<msg name=\"hello.country\">Bonjour, France</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "</ModulePrefs>"
                              + "</Module>")));
    }

    @Test
    public void assertValidSupportedLocalesIsReplacedAndEscapedWhenMessagesHaveSpecialCharacters() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales(\"hello\")"
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.FRENCH));
        when(i18nResolver.getAllTranslationsForPrefix("hello", new Locale("")))
            .thenReturn(ImmutableMap.of("hello.world", "Hello, world (/\\,./;'\"1!2@3#4$5%6^7&8*9(0)-_=+)",
                                        "hello.country", "Hello, wherever you are"));
        when(i18nResolver.getAllTranslationsForPrefix("hello", Locale.FRENCH))
            .thenReturn(ImmutableMap.of("hello.world", "Bonjour tout le monde (/\\,./;'\"1!2@3#4$5%6^7&8*9(0)-_=+)",
                                        "hello.country", "Bonjour, France"));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>"
                              + "<ModulePrefs title='Supported Locales'>"
                              + "<Locale>"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Hello, world (/\\,./;&apos;&quot;1!2@3#4$5%6^7&amp;8*9(0)-_=+)</msg>"
                              + "<msg name=\"hello.country\">Hello, wherever you are</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "<Locale lang=\"fr\">"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Bonjour tout le monde (/\\,./;&apos;&quot;1!2@3#4$5%6^7&amp;8*9(0)-_=+)</msg>"
                              + "<msg name=\"hello.country\">Bonjour, France</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "</ModulePrefs>"
                              + "</Module>")));
    }

    @Test
    public void assertValidSupportedLocalesWithMultipleLocalesIsReplaced() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales(\"hello\")"
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.FRENCH, Locale.GERMANY));
        when(i18nResolver.getAllTranslationsForPrefix("hello", new Locale("")))
            .thenReturn(ImmutableMap.of("hello.world", "Hello, world",
                                        "hello.country", "Hello, wherever you are"));
        when(i18nResolver.getAllTranslationsForPrefix("hello", Locale.FRENCH))
            .thenReturn(ImmutableMap.of("hello.world", "Bonjour tout le monde",
                                        "hello.country", "Bonjour, France"));
        when(i18nResolver.getAllTranslationsForPrefix("hello", Locale.GERMANY))
            .thenReturn(ImmutableMap.of("hello.world", "Hallo Welt",
                                        "hello.country", "Hallo, Deutschland"));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>"
                              + "<ModulePrefs title='Supported Locales'>"
                              + "<Locale>"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Hello, world</msg>"
                              + "<msg name=\"hello.country\">Hello, wherever you are</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "<Locale lang=\"fr\">"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Bonjour tout le monde</msg>"
                              + "<msg name=\"hello.country\">Bonjour, France</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "<Locale lang=\"de\" country=\"DE\">"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Hallo Welt</msg>"
                              + "<msg name=\"hello.country\">Hallo, Deutschland</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "</ModulePrefs>"
                              + "</Module>")));
    }

    @Test
    public void assertSupportedLocalesWithTypoIsNotReplaced() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supporteLocales(\"hello\")" // note intentional typo (missing 'd')
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.FRENCH));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(supportedLocalesXml)));
    }

    @Test
    public void assertSupportedLocalesWithNoArgumentsIsNotReplaced() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales()"
            + "</ModulePrefs>"
            + "</Module>";
         InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.FRENCH));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(supportedLocalesXml)));
    }

    @Test
    public void assertSupportedLocalesWithMissingQuoteIsNotReplaced() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales(\"hello)"
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.FRENCH));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING), is(equalTo(supportedLocalesXml)));
    }

    @Test
    public void assertOnlyFirstSupportedLocaleDirectiveIsReplaced() throws IOException
    {
        /*
         * This test used to check that multiple instances of #supportedLocales with different prefix arguments would
         * all be replaced with separate <Locale> elements.  It turns out this doesn't work: Shindig doesn't support
         * multiple <Locale> elements for a single locale, and will only use one of them at runtime.  So now the
         * test checks that only the first instance is replaced.  This still isn't ideal, since subsequent uses will
         * just silently fail. AG-1129
         */
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales(\"hello\")"
            + "#supportedLocales(\"goodbye\")"
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Set<Locale> locales = ImmutableSet.of(Locale.FRENCH);
        when(localeResolver.getSupportedLocales()).thenReturn(locales);
        when(i18nResolver.getAllTranslationsForPrefix("hello", new Locale("")))
            .thenReturn(ImmutableMap.of("hello.world", "Hello, world",
                                        "hello.country", "Hello, wherever you are"));
        when(i18nResolver.getAllTranslationsForPrefix("hello", Locale.FRENCH))
            .thenReturn(ImmutableMap.of("hello.world", "Bonjour tout le monde",
                                        "hello.country", "Bonjour, France"));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>"
                              + "<ModulePrefs title='Supported Locales'>"
                              + "<Locale>"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Hello, world</msg>"
                              + "<msg name=\"hello.country\">Hello, wherever you are</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "<Locale lang=\"fr\">"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Bonjour tout le monde</msg>"
                              + "<msg name=\"hello.country\">Bonjour, France</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "#supportedLocales(\"goodbye\")"
                              + "</ModulePrefs>"
                              + "</Module>")));
    }

    @Test
    public void assertMultipleValidPrefixesAreReplaced() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales(\"hello,goodbye\")"
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.of(Locale.FRENCH));
        when(i18nResolver.getAllTranslationsForPrefix("hello", new Locale("")))
            .thenReturn(ImmutableMap.of("hello.world", "Hello, world",
                                        "hello.country", "Hello, wherever you are"));
        when(i18nResolver.getAllTranslationsForPrefix("goodbye", new Locale("")))
            .thenReturn(ImmutableMap.of("goodbye.world", "Goodbye, world"));
        when(i18nResolver.getAllTranslationsForPrefix("hello", Locale.FRENCH))
            .thenReturn(ImmutableMap.of("hello.world", "Bonjour tout le monde",
                                        "hello.country", "Bonjour, France"));
        when(i18nResolver.getAllTranslationsForPrefix("goodbye", Locale.FRENCH))
            .thenReturn(ImmutableMap.of("goodbye.world", "Adieu tout le monde"));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>"
                              + "<ModulePrefs title='Supported Locales'>"
                              + "<Locale>"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Hello, world</msg>"
                              + "<msg name=\"hello.country\">Hello, wherever you are</msg>"
                              + "<msg name=\"goodbye.world\">Goodbye, world</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "<Locale lang=\"fr\">"
                              + "<messagebundle>"
                              + "<msg name=\"hello.world\">Bonjour tout le monde</msg>"
                              + "<msg name=\"hello.country\">Bonjour, France</msg>"
                              + "<msg name=\"goodbye.world\">Adieu tout le monde</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "</ModulePrefs>"
                              + "</Module>")));
    }

    @Test
    public void assertUnicodePropertyIsEncoded() throws IOException
    {
        String supportedLocalesXml =
            "<Module>"
            + "<ModulePrefs title='Supported Locales'>"
            + "#supportedLocales(\"snowman\")"
            + "</ModulePrefs>"
            + "</Module>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(localeResolver.getSupportedLocales()).thenReturn(ImmutableSet.<Locale>of());
        when(i18nResolver.getAllTranslationsForPrefix("snowman", new Locale("")))
            .thenReturn(ImmutableMap.of("snowman", "\u2603"));

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>"
                              + "<ModulePrefs title='Supported Locales'>"
                              + "<Locale>"
                              + "<messagebundle>"
                              + "<msg name=\"snowman\">&#9731;</msg>"
                              + "</messagebundle>"
                              + "</Locale>"
                              + "</ModulePrefs>"
                              + "</Module>")));
    }

    @Test
    public void assertPrefixWithSpecialCharactersIsNotReplaced() throws IOException
    {
        String supportedLocalesXml =
            "<xml><nonsense><blah>\n" +
            "#supportedLocales(\"\\./;'1!2@3#4$5%6^7&8*9(0)-_=+\")\n" +
            "</blah></nonsense></xml>";
        InputStream in = new ByteArrayInputStream(supportedLocalesXml.getBytes(ENCODING));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Set<Locale> locales = ImmutableSet.of(Locale.FRENCH);
        when(localeResolver.getSupportedLocales()).thenReturn(locales);

        gadgetProcessor.process(in, out);
        assertThat(out.toString(ENCODING),
                   is(equalTo(supportedLocalesXml)));
    }

    @Test
    public void assertThatOAuthIsReplaced() throws Exception
    {
        String unprocessedSpec =
            "<Module>\n"+
            "<ModulePrefs title=\"OAuth Contacts\">\n" +
            "#oauth\n" +
            "</ModulePrefs>";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(new ByteArrayInputStream(unprocessedSpec.getBytes(ENCODING)), out);
        
        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>\n"+
                              "<ModulePrefs title=\"OAuth Contacts\">\n" +
                              "<OAuth>" +
                              "<Service>" +
                              "<Access url=\"http://example.org/baseUrl/plugins/servlet/oauth/access-token\" method=\"POST\" />" +
                              "<Request url=\"http://example.org/baseUrl/plugins/servlet/oauth/request-token\" method=\"POST\" />" +
                              "<Authorization url=\"http://example.org/baseUrl/plugins/servlet/oauth/authorize?oauth_callback=" + Uri.encodeUriComponent("http://oauth.gmodules.com/gadgets/oauthcallback") + "\" />" +
                              "</Service>" +
                              "</OAuth>\n" +
                              "</ModulePrefs>")));
    }

    @Test
    public void assertThatOAuthIsReplacedWhenBaseUrlHasSpecialCharacters() throws Exception
    {
        when(applicationProperties.getBaseUrl()).thenReturn(MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS);
        String unprocessedSpec =
            "<Module>\n"+
            "<ModulePrefs title=\"OAuth Contacts\">\n" +
            "#oauth\n" +
            "</ModulePrefs>";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        gadgetProcessor.process(new ByteArrayInputStream(unprocessedSpec.getBytes(ENCODING)), out);

        assertThat(out.toString(ENCODING),
                   is(equalTo("<Module>\n"+
                              "<ModulePrefs title=\"OAuth Contacts\">\n" +
                              "<OAuth>" +
                              "<Service>" +
                              "<Access url=\"" + MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS + "/plugins/servlet/oauth/access-token\" method=\"POST\" />" +
                              "<Request url=\"" + MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS + "/plugins/servlet/oauth/request-token\" method=\"POST\" />" +
                              "<Authorization url=\"" + MOCK_APP_BASE_URL_WITH_SPECIAL_CHARACTERS + "/plugins/servlet/oauth/authorize?oauth_callback=" + Uri.encodeUriComponent("http://oauth.gmodules.com/gadgets/oauthcallback") + "\" />" +
                              "</Service>" +
                              "</OAuth>\n" +
                              "</ModulePrefs>")));
    }

    private String appendInclude(String resourceTestString)
    {
        return resourceTestString + "#includeResources()";
    }
}
