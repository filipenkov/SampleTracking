package com.atlassian.upm.log;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.log.AuditLogEntry;
import com.atlassian.upm.api.log.EntryType;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.test.MapBackedPluginSettings;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginSettingsAuditLogServiceTest
{
    private AuditLogService auditLogService;
    private Map settingsMap;
    @Mock PluginSettingsFactory pluginSettingsFactory;
    @Mock UserManager userManager;
    @Mock I18nResolver i18nResolver;
    private ApplicationProperties applicationProperties;
    private static final String AUDIT_LOG_KEY = PluginSettingsAuditLogService.KEY_PREFIX + PluginSettingsAuditLogService.UPM_AUDIT_LOG;
    private static final String LEGACY_AUDIT_LOG_KEY = PluginSettingsAuditLogService.KEY_PREFIX + "upm_audit_log";
    private static final String USERNAME = "admin";
    private static final String SAMPLE_I18N_KEY = "upm.auditLog.install.plugin.success";
    private static final String ENABLED_I18N_KEY = "upm.auditLog.enable.plugin.success";
    private static final String DISABLED_I18N_KEY = "upm.auditLog.disable.plugin.success";
    private static final String SAMPLE_LOG_ENTRY = "Message";
    private static final String SAMPLE_PARAM = "Param";

    Date mockDate;
    
    @Before
    public void setUp()
    {
        mockDate = new Date();
        settingsMap = new HashMap();
        applicationProperties = getStandardApplicationProperties();
        UpmUriBuilder upmUriBuilder = new UpmUriBuilder(applicationProperties);
        when(userManager.getRemoteUsername()).thenReturn(USERNAME);
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(new MapBackedPluginSettings(settingsMap));
        when(i18nResolver.getText(SAMPLE_I18N_KEY, SAMPLE_PARAM)).thenReturn(SAMPLE_LOG_ENTRY);
        auditLogService = new PluginSettingsAuditLogService(i18nResolver, applicationProperties, userManager, pluginSettingsFactory, upmUriBuilder, new MockClock());
    }

    @Test
    public void testThatLogEntriesAreStored()
    {
        purge();
        when(i18nResolver.getText(SAMPLE_I18N_KEY, new String[]{})).thenReturn(SAMPLE_LOG_ENTRY);
        auditLogService.logI18nMessage(SAMPLE_I18N_KEY, SAMPLE_PARAM);
        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertThat(size(entries), is(equalTo(1)));
        assertThat(settingsMap.size(), is(equalTo(1)));
        assertThat(getOnlyElement(entries), hasUsernameAndMessageContaining(USERNAME, SAMPLE_LOG_ENTRY));
    }

    @Test
    public void testThatLogEntriesCanBePurged()
    {
        purge();
        auditLogService.logI18nMessage(SAMPLE_I18N_KEY, SAMPLE_PARAM);
        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertThat(size(entries), is(equalTo(1)));
        auditLogService.purgeLog();
        assertThat(size(auditLogService.getLogEntries()), is(equalTo(0)));
    }

    @Test
    public void testThatMaxEntriesCapsNumberOfLogEntries() throws InterruptedException
    {
        purge();

        final String messageToKeep = ENABLED_I18N_KEY;
        String messageToPurge = DISABLED_I18N_KEY;
        when(i18nResolver.getText(messageToKeep, new String[]{})).thenReturn(messageToKeep);
        when(i18nResolver.getText(messageToPurge, new String[]{})).thenReturn(messageToPurge);
        auditLogService.setMaxEntries(1);
        auditLogService.logI18nMessage(messageToPurge);
        // UPM-858 - guarantee timestamp of next entry is later
        mockDate = new Date(mockDate.getTime() + 1);
        auditLogService.logI18nMessage(messageToKeep);
        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertThat(getOnlyElement(entries), hasUsernameAndMessageContaining(USERNAME, messageToKeep));
    }

    @Test
    public void testThatAnAtomFeedIsRetrievable()
    {
        purge();
        when(i18nResolver.getText("upm.auditLog.anonymous")).thenReturn("anonymous");
        auditLogService.logI18nMessage(SAMPLE_I18N_KEY, SAMPLE_PARAM);
        Feed feed = auditLogService.getFeed();
        Entry entry = getOnlyElement(getFeedEntries(feed));
        assertThat(entry, allOf(hasAuthor(equalTo(USERNAME)), hasTitle(containsString(SAMPLE_LOG_ENTRY))));
    }

    @Test
    public void testThatAnAtomFeedIsRetrievableWithAnonUser()
    {
        purge();
        when(i18nResolver.getText("upm.auditLog.anonymous")).thenReturn("anonymous");
        when(userManager.getRemoteUsername()).thenReturn("anonymous");
        auditLogService.logI18nMessage(SAMPLE_I18N_KEY, SAMPLE_PARAM);
        Feed feed = auditLogService.getFeed();
        Entry entry = getOnlyElement(getFeedEntries(feed));
        assertThat(entry, allOf(hasAuthor(equalTo("anonymous")), hasTitle(containsString(SAMPLE_LOG_ENTRY))));
    }

    @SuppressWarnings("unchecked")
    private Iterable<Entry> getFeedEntries(Feed feed)
    {
        return feed.getEntries();
    }

    @Test
    public void testThatAnAtomFeedIsRetrievableWithCorrectPageLength() throws URISyntaxException
    {
        primeAuditLogWithMessages("Page one", "Page two");
        Entry firstPageEntry = getOnlyElement(getFeedEntries(auditLogService.getFeed(1, 0)));
        // In the feed, the order is reversed.
        assertThat(firstPageEntry, allOf(hasAuthor(equalTo(USERNAME)), hasTitle(containsString("Page two"))));
    }

    @Test
    public void testThatAnAtomFeedIsRetrievableWithCorrectPageOffset() throws URISyntaxException
    {
        primeAuditLogWithMessages("Page one", "Page two");
        Entry secondPageEntry = getOnlyElement(getFeedEntries(auditLogService.getFeed(1, 1)));
        // In the feed, the order is reversed.
        assertThat(secondPageEntry, allOf(hasAuthor(equalTo(USERNAME)), hasTitle(containsString("Page one"))));
    }

    private void primeAuditLogWithMessages(String... messages)
    {
        purge();
        when(i18nResolver.getText("upm.auditLog.anonymous")).thenReturn("anonymous");

        for (String entry : messages)
        {
            when(i18nResolver.getText(SAMPLE_I18N_KEY + entry, SAMPLE_PARAM)).thenReturn(entry);
            auditLogService.logI18nMessage(SAMPLE_I18N_KEY + entry, SAMPLE_PARAM);
            // UPM-858 - guarantee timestamp of next entry is later
            mockDate = new Date(mockDate.getTime() + 1);
        }
    }

    @Test
    public void testThatPurgeAfterCanBeChanged()
    {
        int purgeAfterDays = 321;
        auditLogService.setPurgeAfter(purgeAfterDays);
        assertThat(auditLogService.getPurgeAfter(), is(equalTo(purgeAfterDays)));
    }

    private void purge()
    {
        auditLogService.purgeLog();
        checkState(!auditLogService.getLogEntries().iterator().hasNext());
    }

    private Matcher<AuditLogEntry> hasUsernameAndMessageContaining(String username, String contents)
    {
        return new EntryWithContents(username, contents);
    }

    @Test
    public void testThatLogEventIsAddedWhenUserIsNull()
    {
        purge();
        when(userManager.getRemoteUsername()).thenReturn(null);
        when(i18nResolver.getText("upm.auditLog.anonymous")).thenReturn("anonymous");
        auditLogService.logI18nMessage(SAMPLE_I18N_KEY, SAMPLE_PARAM);

        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertNotNull(getOnlyElement(entries));
        assertEquals("anonymous", getOnlyElement(entries).getUsername());
    }

    @Test
    public void testThatEntryWithTypeCanBeFetchedWithType()
    {
        purge();
        settingsMap.put(AUDIT_LOG_KEY,
                        ImmutableList.of("{\"username\":\"admin\",\"date\":" + new Date().getTime() + ",\"i18nKey\":\"i18n.msg.key\","
                                         + "\"entryType\":\"UNCLASSIFIED_EVENT\",\"params\":[\"Param\"]}"));
        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertNotNull(getOnlyElement(entries));
        assertEquals(EntryType.UNCLASSIFIED_EVENT, getOnlyElement(entries).getEntryType());
    }

    @Test
    public void testThatLegacyEntryWithNoMatchingTypeBecomesUnclassified() throws Exception
    {
        purge();
        settingsMap.put(LEGACY_AUDIT_LOG_KEY, ImmutableList.of("{\"username\":\"admin\",\"date\":" + new Date().getTime() + ",\"i18nKey\":\"i18n.msg.key\",\"params\":[\"Param\"]}"));
        AuditLogUpgradeTask auditLogUpgradeTask = new AuditLogUpgradeTask(pluginSettingsFactory, mock(PluginAccessorAndController.class));
        auditLogUpgradeTask.doUpgrade();

        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertEquals(EntryType.UNCLASSIFIED_EVENT, getOnlyElement(entries).getEntryType());
    }

    @Test
    public void testThatLegacyEntryMatchingTypeWithMultipleI18nEntriesCanBeFetched() throws Exception
    {
        purge();
        settingsMap.put(LEGACY_AUDIT_LOG_KEY, ImmutableList.of("{\"username\":\"admin\",\"date\":" + new Date().getTime() + ",\"i18nKey\":\"upm.auditLog.upgrade.plugin.failure\",\"params\":[\"Param\"]}"));
        AuditLogUpgradeTask auditLogUpgradeTask = new AuditLogUpgradeTask(pluginSettingsFactory, mock(PluginAccessorAndController.class));
        auditLogUpgradeTask.doUpgrade();
        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertEquals(EntryType.PLUGIN_UPDATE, getOnlyElement(entries).getEntryType());
    }

    @Test
    public void testThatTypeWithMultipleI18nEntriesCanBeFetched() throws Exception
    {
        purge();
        settingsMap.put(AUDIT_LOG_KEY,
                ImmutableList.of("{\"username\":\"admin\",\"date\":" + new Date().getTime() + ",\"i18nKey\":\"upm.auditLog.update.plugin.failure\","
                        + "\"entryType\":\"PLUGIN_UPDATE\",\"params\":[\"Param\"]}"));
        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertEquals(EntryType.PLUGIN_UPDATE, getOnlyElement(entries).getEntryType());
    }

    @Test
    public void testThatInvalidLogInPluginSettingsDoesNotThrowAnExceptionAndPurgesAuditLog()
    {
        purge();
        settingsMap.put(AUDIT_LOG_KEY, "invalid_log");
        assertThat(auditLogService.getLogEntries(), Matchers.<AuditLogEntry>emptyIterable());
    }

    @Test
    public void testThatOnlyPluginEnabledEntryTypeIsIncludedWithPluginEnabledEntryTypeFilter()
    {
        purge();
        auditLogService.logI18nMessage(ENABLED_I18N_KEY, SAMPLE_PARAM);
        auditLogService.logI18nMessage(DISABLED_I18N_KEY, SAMPLE_PARAM);

        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries(null, null, ImmutableSet.of(EntryType.PLUGIN_ENABLE));
        assertNotNull(getOnlyElement(entries));
        assertEquals(EntryType.PLUGIN_ENABLE, getOnlyElement(entries).getEntryType());
    }

    @Test
    public void testThatAllEntryTypesAreIncludedByDefault()
    {
        purge();
        auditLogService.logI18nMessage(ENABLED_I18N_KEY, SAMPLE_PARAM);
        auditLogService.logI18nMessage(DISABLED_I18N_KEY, SAMPLE_PARAM);

        Iterable<AuditLogEntry> entries = auditLogService.getLogEntries();
        assertThat(entries, is(Matchers.<AuditLogEntry>iterableWithSize(2)));
    }

    private final class EntryWithContents extends TypeSafeDiagnosingMatcher<AuditLogEntry>
    {
        private final String username;
        private final String contents;

        public EntryWithContents(String username, String contents)
        {
            this.username = username;
            this.contents = contents;
        }

        public void describeTo(Description description)
        {
            description.appendText("entry with username ").appendValue(username)
                .appendText(" and message containing ").appendValue(contents);
        }

        protected boolean matchesSafely(AuditLogEntry item, Description mismatchDescription)
        {
            String message = item.getMessage(i18nResolver);
            boolean match = message.contains(username) && message.contains(contents);
            if (!match)
            {
                mismatchDescription.appendText("was ").appendText(message);
            }
            return match;
        }

    }
    
    private Matcher<Entry> hasTitle(Matcher<? super String> titleMatcher)
    {
        return new FeedEntryWithTitle(titleMatcher);
    }
    
    private Matcher<Entry> hasAuthor(Matcher<? super String> authorMatcher)
    {
        return new FeedEntryWithAuthor(authorMatcher);
    }
    
    private final class FeedEntryWithTitle extends TypeSafeDiagnosingMatcher<Entry>
    {
        private final Matcher<? super String> titleMatcher;

        public FeedEntryWithTitle(Matcher<? super String> titleMatcher)
        {
            this.titleMatcher = titleMatcher;
        }

        public void describeTo(Description description)
        {
            description.appendText("entry with title ").appendValue(titleMatcher);
        }

        protected boolean matchesSafely(Entry feedEntry, Description mismatchDescription)
        {
            String entryTitle = feedEntry.getTitle();
            boolean match = titleMatcher.matches(entryTitle);

            if (!match)
            {
                mismatchDescription.appendText("was an entry with title ")
                                   .appendValue(entryTitle);
            }
            return match;
        }

    }    
    
    private final class FeedEntryWithAuthor extends TypeSafeDiagnosingMatcher<Entry>
    {
        private final Matcher<? super String> authorMatcher;

        public FeedEntryWithAuthor(Matcher<? super String> username)
        {
            this.authorMatcher = username;
        }

        public void describeTo(Description description)
        {
            description.appendText("entry with author ").appendValue(authorMatcher);
        }

        protected boolean matchesSafely(Entry feedEntry, Description mismatchDescription)
        {
            List<Person> entryAuthors = feedEntry.getAuthors();
            boolean match = hasMatchingAuthor(entryAuthors);

            if (!match)
            {
                mismatchDescription.appendText("was an entry with authors ")
                                   .appendValue(entryAuthors);
            }
            
            return match;
        }

        /**
         * @return true if any author in <code>entryAuthors</code> matches through <code>authorMatcher</code>
         */
        private boolean hasMatchingAuthor(List<Person> entryAuthors)
        {
            Collection<Person> filteredList = Collections2.filter(entryAuthors, new Predicate<Person>()
            {
                public boolean apply(Person input)
                {
                    return authorMatcher.matches(input.getName());
                }
            });

            return !filteredList.isEmpty();
        }
    }
    
    class MockClock implements PluginSettingsAuditLogService.Clock
    {
        public long currentTimeMillis()
        {
            return mockDate.getTime();
        }
    }
}
