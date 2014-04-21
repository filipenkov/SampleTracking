package com.atlassian.jira.web.bean;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.util.InvolvedPluginsTracker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.google.common.collect.ImmutableList;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static com.atlassian.jira.mock.plugin.elements.MockResourceDescriptorBuilder.i18n;
import static com.atlassian.jira.util.collect.IteratorEnumeration.fromIterator;
import static java.util.Collections.singleton;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestI18nBean extends ListeningTestCase
{
    private static final List<Locale> LOCALES_FOR_ORDER_TEST = Arrays.asList(
            Locale.US,
            Locale.ENGLISH,
            Locale.FRENCH,
            Locale.UK,
            Locale.ROOT,
            Locale.CANADA_FRENCH,
            Locale.FRANCE);

    private static final String AUTOMATIC_IN_ENGLISH = "Automatic";
    private static final String AUTOMATIC_IN_FRENCH = "Automatique";

    private static final String HELLO_IN_ENGLISH = "Hello";
    private static final String HELLO_IN_POLISH = "Witaj";

    private static final String KEY_ORDER_TEST_DOES_NOT_EXIST = "locale.order.test.does.not.exist";
    private static final String KEY_ORDER_TEST_ROOT = "locale.order.test.root";
    private static final String KEY_ORDER_TEST_ROOT_LANG = "locale.order.test.root.lang";
    private static final String KEY_ORDER_TEST_ROOT_LANG_COUNTRY = "locale.order.test.root.lang.country";
    private static final String KEY_ORDER_TEST_ROOT_COUNTRY = "locale.order.test.root.country";
    private static final String KEY_ORDER_TEST_LANG = "locale.order.test.lang";
    private static final String KEY_ORDER_TEST_LANG_COUNTRY = "locale.order.test.lang.country";
    private static final String KEY_ORDER_TEST_COUNTRY = "locale.order.test.country";

    private List<TranslationTransform> translationTransforms = Collections.emptyList();


    @Test
    public void testNonDefaultLocaleGetText() throws Exception
    {
        final String lookupKey = "key.not.in.normal.jira.bundles";
        final AtomicInteger handleGetObjectCalled = new AtomicInteger();
        assertEquals(0, handleGetObjectCalled.get());

        // Create a beans with "en" and "fr_FR" locales
        I18nHelper en = getI18nBean(Locale.ENGLISH, lookupKey, AUTOMATIC_IN_ENGLISH, handleGetObjectCalled);
        assertEquals(1, handleGetObjectCalled.get());
        I18nHelper fr = getI18nBean(Locale.FRANCE, lookupKey, AUTOMATIC_IN_FRENCH, handleGetObjectCalled);
        assertEquals(2, handleGetObjectCalled.get());

        assertEquals(AUTOMATIC_IN_ENGLISH, en.getText(lookupKey));
        assertEquals(2, handleGetObjectCalled.get());

        // Create a bean with French locale
        assertEquals(AUTOMATIC_IN_FRENCH, fr.getText(lookupKey));
        assertEquals(2, handleGetObjectCalled.get());
    }

    private I18nHelper getI18nBean(final Locale locale, final String lookupKey, final String returnValueFromLookup, final AtomicInteger handleGetObjectCalled)
    {
        return new BackingI18n(locale, new MockI18nTranslationMode(), new InvolvedPluginsTracker(), translationTransforms)
        {
            @Override
            public ResourceBundle getDefaultResourceBundle()
            {
                return new MockResourceBundle(lookupKey, handleGetObjectCalled, returnValueFromLookup);
            }

            @Override
            protected PluginAccessor getPluginAccessor()
            {
                return null;
            }
        };
    }

    @Test
    public void testKeyInv2LanguagePack()
    {
        final String lookupKeyInPolishV2 = "key.not.in.normal.jira.bundles.in.polish.v2";

        // for Polish locale, text should be found in the Polish resource bundle (default bundle should not be queried),
        // for English a default bundle should be queried
        final AtomicInteger handleGetObjectCalled = new AtomicInteger();
        assertEquals(0, handleGetObjectCalled.get());
        I18nHelper pl_PL = getI18nBeanWithV2LanguagePack(new Locale("pl", "PL"), lookupKeyInPolishV2, null, handleGetObjectCalled);
        assertEquals(1, handleGetObjectCalled.get());
        I18nHelper en_US = getI18nBeanWithV2LanguagePack(new Locale("en", "US"), lookupKeyInPolishV2, HELLO_IN_ENGLISH, handleGetObjectCalled);
        assertEquals(2, handleGetObjectCalled.get());

        assertEquals(HELLO_IN_POLISH, pl_PL.getText(lookupKeyInPolishV2));
        assertEquals(2, handleGetObjectCalled.get());
        assertEquals(HELLO_IN_ENGLISH, en_US.getText(lookupKeyInPolishV2));
        assertEquals(2, handleGetObjectCalled.get());
    }

    private I18nHelper getI18nBeanWithV2LanguagePack(final Locale locale, final String lookupKey, final String returnValueFromLookup, final AtomicInteger handleGetObjectCalled)
    {
        return new BackingI18n(locale, new MockI18nTranslationMode(), new InvolvedPluginsTracker(), translationTransforms)
        {
            @Override
            public ResourceBundle getDefaultResourceBundle()
            {
                return new MockResourceBundle(lookupKey, handleGetObjectCalled, returnValueFromLookup);
            }

            @Override
            protected PluginAccessor getPluginAccessor()
            {
                return new MockV2LanguagePackPluginAccessor(
                    new Locale("pl", "PL"), "com.atlassian.jira.web.bean.languageTest", getClass().getClassLoader());
            }
        };
    }

    @Test
    public void testUnresolvedKeyIsNotFormatted()
    {
        final String lookupKey = "this.key.won't.exist.in.the.properties.files.purple.monkey.dishwasher";
        final AtomicInteger handleGetObjectCalled = new AtomicInteger(0);

        // JRA-10274: key strings with single quotes get mangled by MessageFormat - do not format the message if
        // the key string lookup did not return anything
        final ResourceBundle bundle = new ResourceBundle()
        {
            @Override
            protected Object handleGetObject(final String key)
            {
                assertEquals(lookupKey, key);
                handleGetObjectCalled.incrementAndGet();
                throw new MissingResourceException("Exception that should be thrown as part of the test", this.getClass().getName(), key);
            }

            @Override
            public Enumeration<String> getKeys()
            {
                return fromIterator(singleton(lookupKey).iterator());
            }
        };

        assertEquals(0, handleGetObjectCalled.get());
        final I18nHelper bean = new BackingI18n(Locale.ENGLISH, new MockI18nTranslationMode(), new InvolvedPluginsTracker(), translationTransforms)
        {
            @Override
            public ResourceBundle getDefaultResourceBundle()
            {
                return bundle;
            }

            @Override
            protected PluginAccessor getPluginAccessor()
            {
                return null;
            }
        };
        assertEquals(1, handleGetObjectCalled.get());
        assertEquals(lookupKey, bean.getText(lookupKey));
        assertEquals(1, handleGetObjectCalled.get());
    }

    @Test
    public void shouldLoadPluginBundlesWithTheSameName()
    {
        final PluginAccessor mockAccessor = createNiceMock(PluginAccessor.class);
        expect(mockAccessor.getEnabledPlugins()).andReturn(ImmutableList.<Plugin>of(mockPluginWithDuplicatedResources())).anyTimes();
        expect(mockAccessor.getEnabledModuleDescriptorsByClass(EasyMock.<Class>anyObject())).andReturn(Collections.emptyList()).anyTimes();
        replay(mockAccessor);
        final I18nHelper tested = createBackingI18nWithMockAccessor(Locale.ENGLISH, mockAccessor);
        // should contain resources from all 3 bundles
        assertEquals("some value", tested.getText("some_prop1"));
        assertEquals("some value", tested.getText("some_prop2"));
        assertEquals("some value", tested.getText("some_prop3"));
    }

    @Test
    public void testRootLocaleAlwaysAccepted()
    {
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ROOT, Locale.ROOT));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ROOT, Locale.ENGLISH));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ROOT, Locale.CANADA));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ROOT, Locale.UK));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ROOT, Locale.FRANCE));
    }


    @Test
    public void testEnglishAcceptedOnlyInEnglishLocales()
    {
        assertFalse(BackingI18n.providedLocaleMatches(Locale.ENGLISH, Locale.ROOT));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ENGLISH, Locale.ENGLISH));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ENGLISH, Locale.US));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ENGLISH, Locale.UK));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.ENGLISH, Locale.CANADA));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.ENGLISH, Locale.FRANCE));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.ENGLISH, Locale.CANADA_FRENCH));
    }

    @Test
    public void testFrenchAcceptedOnlyInFrenchLocales()
    {
        assertFalse(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.ROOT));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.ENGLISH));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.US));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.UK));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.CANADA));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.FRENCH));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.FRANCE));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.FRENCH, Locale.CANADA_FRENCH));
    }


    @Test
    public void testAmericanEnglishAcceptedOnlyInExactMatch()
    {
        assertFalse(BackingI18n.providedLocaleMatches(Locale.US, Locale.ROOT));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.US, Locale.ENGLISH));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.US, Locale.US));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.US, Locale.UK));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.US, Locale.CANADA));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.US, Locale.FRENCH));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.US, Locale.FRANCE));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.US, Locale.CANADA_FRENCH));
    }

    @Test
    public void testCanadianFrenchAcceptedOnlyInExactMatch()
    {
        assertFalse(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.ROOT));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.ENGLISH));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.US));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.UK));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.CANADA));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.FRENCH));
        assertFalse(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.FRANCE));
        assertTrue(BackingI18n.providedLocaleMatches(Locale.CANADA_FRENCH, Locale.CANADA_FRENCH));
    }

    @Test
    public void shouldMimicNormalResourceBundleRules()
    {
        final PluginAccessor mockAccessor = getOrderTestMockAccessor();
        for (Locale locale : LOCALES_FOR_ORDER_TEST)
        {
            assertCorrectPropertyValues(locale, mockAccessor);
        }
    }

    @Test
    public void testIgnoreMatchedSingleQuotesAroundKey() throws Exception
    {
        final PluginAccessor mockAccessor = getOrderTestMockAccessor();
        final I18nHelper helper = createBackingI18nWithMockAccessor(Locale.FRANCE, mockAccessor);

        // Try getUnescapedText first
        assertEquals(null, helper.getUnescapedText(null));
        assertEquals("'", helper.getUnescapedText("'"));
        assertEquals("'xyzzy", helper.getUnescapedText("'xyzzy"));
        assertEquals("xyzzy'", helper.getUnescapedText("xyzzy'"));
        assertEquals("xyzzy", helper.getUnescapedText("'xyzzy'"));
        assertEquals(KEY_ORDER_TEST_DOES_NOT_EXIST, helper.getUnescapedText("'" + KEY_ORDER_TEST_DOES_NOT_EXIST + "'"));
        assertEquals(Locale.FRANCE.toString(), helper.getUnescapedText("'" + KEY_ORDER_TEST_COUNTRY + "'"));

        // Repeat using getText
        assertEquals(null, helper.getText(null));
        assertEquals("'", helper.getText("'"));
        assertEquals("'xyzzy", helper.getText("'xyzzy"));
        assertEquals("xyzzy'", helper.getText("xyzzy'"));
        assertEquals("xyzzy", helper.getText("'xyzzy'"));
        assertEquals(KEY_ORDER_TEST_DOES_NOT_EXIST, helper.getText("'" + KEY_ORDER_TEST_DOES_NOT_EXIST + "'"));
        assertEquals(Locale.FRANCE.toString(), helper.getText("'" + KEY_ORDER_TEST_COUNTRY + "'"));
    }

    private PluginAccessor getOrderTestMockAccessor()
    {
        final PluginAccessor mockAccessor = createNiceMock(PluginAccessor.class);
        expect(mockAccessor.getEnabledPlugins()).andReturn(generateOrderTestPlugins()).anyTimes();
        expect(mockAccessor.getEnabledModuleDescriptorsByClass(EasyMock.<Class>anyObject())).andReturn(Collections.emptyList()).anyTimes();
        replay(mockAccessor);
        return mockAccessor;
    }

    private void assertCorrectPropertyValues(final Locale locale, final PluginAccessor mockAccessor)
    {
        final String name = locale.toString();
        final I18nHelper helper = createBackingI18nWithMockAccessor(locale, mockAccessor);

        assertEquals(name, KEY_ORDER_TEST_DOES_NOT_EXIST, helper.getText(KEY_ORDER_TEST_DOES_NOT_EXIST));
        assertEquals(name, "", helper.getText(KEY_ORDER_TEST_ROOT));
        assertEquals(name, locale.getLanguage(), helper.getText(KEY_ORDER_TEST_ROOT_LANG));
        assertEquals(name, name, helper.getText(KEY_ORDER_TEST_ROOT_LANG_COUNTRY));
        assertEquals(name, hasCountry(locale) ? name : "", helper.getText(KEY_ORDER_TEST_ROOT_COUNTRY));
        assertEquals(name, hasLanguage(locale) ? locale.getLanguage() : KEY_ORDER_TEST_LANG, helper.getText(KEY_ORDER_TEST_LANG));
        assertEquals(name, hasLanguage(locale) ? name : KEY_ORDER_TEST_LANG_COUNTRY, helper.getText(KEY_ORDER_TEST_LANG_COUNTRY));
        assertEquals(name, hasCountry(locale) ? name : KEY_ORDER_TEST_COUNTRY, helper.getText(KEY_ORDER_TEST_COUNTRY));
    }

    private boolean hasLanguage(Locale locale)
    {
        return locale.getLanguage().length() > 0;
    }

    private boolean hasCountry(Locale locale)
    {
        return locale.getCountry().length() > 0;
    }

    private ImmutableList<Plugin> generateOrderTestPlugins()
    {
        final ImmutableList.Builder<Plugin> builder = ImmutableList.builder();
        for (Locale locale : LOCALES_FOR_ORDER_TEST)
        {
            builder.add(mockPluginForOrderTest(locale));
        }
        return builder.build();
    }

    private Plugin mockPluginForOrderTestRoot()
    {
        return new MockPlugin("Order Test Plugin (ROOT)", "order.test.plugin.root", new PluginInformation())
                .addResourceDescriptor(i18n("orderTest", "com.atlassian.jira.web.bean.orderTest"));
    }

    private Plugin mockPluginForOrderTest(Locale locale)
    {
        if (locale == null || Locale.ROOT.equals(locale))
        {
            return mockPluginForOrderTestRoot();
        }
        // Note: We munge the file name to make sure that ResourceBundle's automatic inheritance
        // scheme is not involved, as it will definitely not be when resource bundles live in different plugins
        final String propFileName = "orderTest" + locale.getLanguage() + locale.getCountry();
        return new MockPlugin("Order Test Plugin (" + locale.toString() + ')', "order.test.plugin", new PluginInformation())
                .addResourceDescriptor(i18n("orderTest", "com.atlassian.jira.web.bean." + propFileName));
    }

    private I18nHelper createBackingI18nWithMockAccessor(final Locale locale, final PluginAccessor mockAccessor)
    {
        return new BackingI18n(locale, new MockI18nTranslationMode(), new InvolvedPluginsTracker(), translationTransforms)
        {
            @Override
            protected PluginAccessor getPluginAccessor()
            {
                return mockAccessor;
            }
        };
    }


    private Plugin mockPluginWithDuplicatedResources()
    {
        Plugin answer = new MockPlugin("test plugin", "test", new PluginInformation()).addResourceDescriptor(i18n("thesame", "com.atlassian.jira.web.bean.i18ntest1"));
        answer.addModuleDescriptor(newMockDescWithResource("module1", "thesame", "com.atlassian.jira.web.bean.i18ntest2"));
        answer.addModuleDescriptor(newMockDescWithResource("module2", "thesame", "com.atlassian.jira.web.bean.i18ntest3"));
        return answer;
    }

    private ModuleDescriptor<String> newMockDescWithResource(String key, String resourceName, String location)
    {
        @SuppressWarnings("unchecked") ModuleDescriptor<String> answer = createNiceMock(ModuleDescriptor.class);
        expect(answer.getKey()).andReturn(key).anyTimes();
        expect(answer.getResourceDescriptors()).andReturn(ImmutableList.of(i18n(resourceName, location))).anyTimes();
        replay(answer);
        return answer;
    }

    private static class MockResourceBundle extends ResourceBundle
    {
        private final String lookupKey;
        private final AtomicInteger handleGetObjectCalled;
        private final String returnValueFromLookup;

        public MockResourceBundle(String lookupKey, AtomicInteger handleGetObjectCalled, String returnValueFromLookup)
        {
            this.lookupKey = lookupKey;
            this.handleGetObjectCalled = handleGetObjectCalled;
            this.returnValueFromLookup = returnValueFromLookup;
        }

        @Override
        protected Object handleGetObject(final String key)
        {
            assertEquals(lookupKey, key);
            handleGetObjectCalled.incrementAndGet();
            return returnValueFromLookup;
        }

        @Override
        public Enumeration<String> getKeys()
        {
            return fromIterator(singleton(lookupKey).iterator());
        }
    }
}
