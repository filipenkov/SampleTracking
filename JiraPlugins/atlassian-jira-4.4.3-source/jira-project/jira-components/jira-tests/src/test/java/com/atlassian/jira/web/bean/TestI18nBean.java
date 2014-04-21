package com.atlassian.jira.web.bean;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.plugin.util.InvolvedPluginsTracker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.google.common.collect.ImmutableList;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final String AUTOMATIC_IN_ENGLISH = "Automatic";
    private static final String AUTOMATIC_IN_FRENCH = "Automatique";

    private static final String HELLO_IN_ENGLISH = "Hello";
    private static final String HELLO_IN_POLISH = "Witaj";

    @Test
    public void testNonDefaultLocaleGetText() throws Exception
    {
        final String lookupKey = "key.not.in.normal.jira.bundles";
        final AtomicBoolean handleGetObjectCalled = new AtomicBoolean();
        assertFalse(handleGetObjectCalled.get());

        // Create a bean with English locale
        I18nHelper bean = getI18nBean(Locale.ENGLISH, lookupKey, AUTOMATIC_IN_ENGLISH, handleGetObjectCalled);

        // Assert all is well
        assertEquals(AUTOMATIC_IN_ENGLISH, bean.getText(lookupKey));
        assertTrue(handleGetObjectCalled.get());

        // Reset counter
        handleGetObjectCalled.set(false);
        assertFalse(handleGetObjectCalled.get());

        // Create a bean with French locale
        bean = getI18nBean(Locale.FRANCE, lookupKey, AUTOMATIC_IN_FRENCH, handleGetObjectCalled);

        // Assert all is well
        assertEquals(AUTOMATIC_IN_FRENCH, bean.getText(lookupKey));
        assertTrue(handleGetObjectCalled.get());
    }

    private I18nHelper getI18nBean(final Locale locale, final String lookupKey, final String returnValueFromLookup, final AtomicBoolean handleGetObjectCalled)
    {
        return new BackingI18n(locale, new MockI18nTranslationMode(), new InvolvedPluginsTracker())
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
        final AtomicBoolean handleGetObjectCalled = new AtomicBoolean();
        I18nHelper bean = getI18nBeanWithV2LanguagePack(new Locale("pl", "PL"), lookupKeyInPolishV2, null, handleGetObjectCalled);
        assertEquals(HELLO_IN_POLISH, bean.getText(lookupKeyInPolishV2));
        assertFalse(handleGetObjectCalled.get());
        handleGetObjectCalled.set(false);
        bean = getI18nBeanWithV2LanguagePack(new Locale("en", "US"), lookupKeyInPolishV2, HELLO_IN_ENGLISH, handleGetObjectCalled);
        assertEquals(HELLO_IN_ENGLISH, bean.getText(lookupKeyInPolishV2));
        assertTrue(handleGetObjectCalled.get());
    }

    private I18nHelper getI18nBeanWithV2LanguagePack(final Locale locale, final String lookupKey, final String returnValueFromLookup, final AtomicBoolean handleGetObjectCalled)
    {
        return new BackingI18n(locale, new MockI18nTranslationMode(), new InvolvedPluginsTracker())
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
        assertEquals(0, handleGetObjectCalled.get());

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

        final I18nHelper bean = new BackingI18n(Locale.ENGLISH, new MockI18nTranslationMode(), new InvolvedPluginsTracker())
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
        final I18nHelper tested = new BackingI18n(Locale.ENGLISH, new MockI18nTranslationMode(), new InvolvedPluginsTracker())
        {
            @Override
            protected PluginAccessor getPluginAccessor()
            {
                return mockAccessor;
            }
        };
        // should contain resources from all 3 bundles
        assertEquals("some value", tested.getText("some_prop1"));
        assertEquals("some value", tested.getText("some_prop2"));
        assertEquals("some value", tested.getText("some_prop3"));
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
        private final AtomicBoolean handleGetObjectCalled;
        private final String returnValueFromLookup;

        public MockResourceBundle(String lookupKey, AtomicBoolean handleGetObjectCalled, String returnValueFromLookup)
        {
            this.lookupKey = lookupKey;
            this.handleGetObjectCalled = handleGetObjectCalled;
            this.returnValueFromLookup = returnValueFromLookup;
        }

        @Override
        protected Object handleGetObject(final String key)
        {
            assertEquals(lookupKey, key);
            handleGetObjectCalled.set(true);
            return returnValueFromLookup;
        }

        @Override
        public Enumeration<String> getKeys()
        {
            return fromIterator(singleton(lookupKey).iterator());
        }
    }
}
