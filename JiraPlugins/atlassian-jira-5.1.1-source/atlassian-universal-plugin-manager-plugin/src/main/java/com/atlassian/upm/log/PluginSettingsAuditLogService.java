package com.atlassian.upm.log;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.log.AuditLogEntry;
import com.atlassian.upm.api.log.EntryType;
import com.atlassian.upm.impl.NamespacedPluginSettings;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.filter;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Provides a log of all events that change the state of the plugin system. An event is stored as a triple
 * of date, user name, and message. The log can be retrieved as a collection of events, or an atom feed.
 * <p/>
 * This class is thread safe. Multiple threads may call its public methods safely, assuming no additional
 * atomicity requirements are required between calls. Atomicity may be achieved by synchronizing on the
 * service object and performing successive operations in that block.
 */
public class PluginSettingsAuditLogService implements AuditLogService
{
    static final String KEY_PREFIX = PluginSettingsAuditLogService.class.getName() + ":log:";
    static final String UPM_AUDIT_LOG = "upm_audit_log_v2"; //UPM-1350 has "_v2" suffix in UPM 1.6.2+
    private static final String UPM_AUDIT_LOG_MAX_ENTRIES = "upm_audit_log_max_entries";
    private static final String UPM_AUDIT_LOG_PURGE_AFTER = "upm_audit_log_purge_after";
    private static final int DEFAULT_MAX_ENTRIES = 5000;
    private static final int DEFAULT_PURGE_AFTER = 90;
    private static final Logger log = LoggerFactory.getLogger(PluginSettingsAuditLogService.class.getName());
    private final I18nResolver i18nResolver;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final ObjectMapper mapper;
    private final Predicate<AuditLogEntry> purgePolicy;
    private final UpmUriBuilder uriBuilder;
    private final Clock clock;
    private volatile Date lastModified = new Date();

    public PluginSettingsAuditLogService(I18nResolver i18nResolver, ApplicationProperties applicationProperties,
        UserManager userManager, PluginSettingsFactory pluginSettingsFactory,
        UpmUriBuilder uriBuilder, Clock clock)
    {
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.userManager = checkNotNull(userManager, "userManager");
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory, "pluginSettingsFactory");
        this.purgePolicy = new Predicate<AuditLogEntry>()
        {
            public boolean apply(AuditLogEntry input)
            {
                DateTime nDaysAgo = new DateTime().minusDays(getPurgeAfter());
                return input.getDate().after(nDaysAgo.toDate());
            }
        };
        this.mapper = new ObjectMapper(new MappingJsonFactory());
        this.uriBuilder = uriBuilder;
        this.clock = checkNotNull(clock);
    }

    public PluginSettingsAuditLogService(I18nResolver i18nResolver, ApplicationProperties applicationProperties,
            UserManager userManager, PluginSettingsFactory pluginSettingsFactory,
            UpmUriBuilder uriBuilder)
    {
        this(i18nResolver, applicationProperties, userManager, pluginSettingsFactory, uriBuilder, new SystemClock());
    }
    
    public void logI18nMessage(final String key, final String... params)
    {
        logI18nMessageWithUsername(key, checkAnonymous(userManager.getRemoteUsername()), params);
    }

    public void logI18nMessageWithUsername(final String key, final String username, final String... params)
    {
        lastModified = new Date(clock.currentTimeMillis());
        AuditLogEntry entry = new AuditLogEntryImpl(username, lastModified, key, EntryType.valueOfI18n(key), params);
        try
        {
            saveEntryAndPurge(entry);
        }
        catch (IOException e)
        {
            throw new AuditLoggingException("Failed to log message: " + entry, e);
        }
        log.info(entry.getMessage(i18nResolver));
    }

    private String checkAnonymous(String username)
    {
        if (username == null)
        {
            return i18nResolver.getText("upm.auditLog.anonymous");
        }
        else
        {
            return username;
        }
    }

    /**
     * Fetch the log entries. May trigger a purge of old messages, according to this log service's
     * purge policy.
     *
     * @return an {@code Iterable} of the entries in the log
     */
    public synchronized Iterable<AuditLogEntry> getLogEntries()
    {
        return getLogEntries(null, null);
    }

    /**
     * Fetch the specified number of log entries. May trigger a purge of old messages, according to this log service's
     * purge policy.
     *
     * @param maxResults the maximum number of results
     * @param startIndex the starting index
     * @return an {@code Iterable} of the entries in the log
     */
    public synchronized Collection<AuditLogEntry> getLogEntries(Integer maxResults, Integer startIndex)
    {
        return getFeedData(maxResults, startIndex).entries;
    }

    public synchronized Iterable<AuditLogEntry> getLogEntries(Integer maxResults, Integer startIndex, Set<EntryType> entryTypes)
    {
        return getFeedData(maxResults, startIndex, entryTypes).entries;
    }

    private synchronized FeedData getFeedData(Integer maxResults, Integer startIndex)
    {
        return getFeedData(maxResults, startIndex, ImmutableSet.of(EntryType.values()));
    }

    /**
     * Get the given feed entries and associated pagination data for the given pagination request.
     * In addition to returning the paged list of entries, this method also returns the starting index and max results
     * of the response, even if the parameters to this method are null.
     *
     * @param maxResults the maximum number of results
     * @param startIndex the starting index
     * @param entryTypes the entry types
     * @return the feed entries and associated pagination data.
     */
    private synchronized FeedData getFeedData(Integer maxResults, Integer startIndex, Set<EntryType> entryTypes)
    {
        ImmutableList.Builder<AuditLogEntry> entries = ImmutableList.builder();
        ArrayList<AuditLogEntry> log = new ArrayList<AuditLogEntry>(filter(purgeEntriesAndTransform(
            getSavedEntriesAsStrings()), new EntryWithTypes(entryTypes)));

        int totalEntries = log.size();

        if (startIndex == null)
        {
            startIndex = 0;
        }

        if (maxResults == null)
        {
            maxResults = totalEntries;
        }

        try
        {
            entries.addAll(log.subList(startIndex, min(maxResults + startIndex, totalEntries)));
        }
        catch (IllegalArgumentException e)
        {
            //if we page too far let's return an empty list instead of an exception.
        }

        return new FeedData(startIndex, maxResults, totalEntries, entries.build());
    }

    /**
     * A private class to encapsulate all data about a given feed request and response.
     */
    private class FeedData
    {
        /**
         * The starting index of the returned entry list.
         */
        private int startIndex;

        /**
         * The maximum number of entries to be in the returned entry list.
         */
        private int maxResults;

        /**
         * The total number of entries registered in the audit log (prior to paging).
         */
        private int totalEntries;

        /**
         * The entries.
         */
        private Collection<AuditLogEntry> entries;

        FeedData(int startIndex, int maxResults, int totalEntries, Collection<AuditLogEntry> entries)
        {
            this.startIndex = startIndex;
            this.maxResults = maxResults;
            this.totalEntries = totalEntries;
            this.entries = entries;
        }
    }

    private static class EntryWithTypes implements Predicate<AuditLogEntry>
    {
        private final Set<EntryType> entryTypes;

        public EntryWithTypes(Set<EntryType> entryTypes)
        {
            this.entryTypes = ImmutableSet.copyOf(entryTypes);
        }

        public boolean apply(AuditLogEntry entry)
        {
            return entryTypes.contains(entry.getEntryType());
        }
    }

    /**
     * Fetch the log entries as an atom feed. May trigger a purge of old messages, according to this
     * log service's purge policy.
     *
     * @return a {@code Feed} containing all the entries in the log
     */
    public synchronized Feed getFeed()
    {
        return getFeed(null, null);
    }

    /**
     * Fetch the log entries as an atom feed. May trigger a purge of old messages, according to this
     * log service's purge policy. Includes paging options.
     *
     * @param maxResults maximum number of log results
     * @param startIndex starting index of log results
     * @return a {@code Feed} containing all the entries in the log
     */
    public synchronized Feed getFeed(Integer maxResults, Integer startIndex)
    {
        Feed feed = new Feed();
        feed.setTitle("Plugin management log for " + applicationProperties.getDisplayName() + " (" + applicationProperties.getBaseUrl() + ")");
        feed.setModified(lastModified);
        addLink(feed, applicationProperties.getBaseUrl(), "base");
        addAuditLogEntries(feed, maxResults, startIndex);
        return feed;
    }

    public void purgeLog()
    {
        saveEntries(Collections.<AuditLogEntry>emptyList());
    }

    public int getMaxEntries()
    {
        String maxEntries = (String) getPluginSettings().get(UPM_AUDIT_LOG_MAX_ENTRIES);
        if (maxEntries == null)
        {
            return DEFAULT_MAX_ENTRIES;
        }
        return Integer.valueOf(maxEntries);
    }

    public void setMaxEntries(int maxEntries)
    {
        if (maxEntries > 0)
        {
            getPluginSettings().put(UPM_AUDIT_LOG_MAX_ENTRIES, Integer.toString(maxEntries));
        }
    }

    public int getPurgeAfter()
    {
        String purgeAfter = (String) getPluginSettings().get(UPM_AUDIT_LOG_PURGE_AFTER);
        if (purgeAfter == null)
        {
            return DEFAULT_PURGE_AFTER;
        }
        return Integer.valueOf(purgeAfter);
    }

    public void setPurgeAfter(int purgeAfter)
    {
        if (purgeAfter > 0)
        {
            getPluginSettings().put(UPM_AUDIT_LOG_PURGE_AFTER, Integer.toString(purgeAfter));
        }
    }

    @SuppressWarnings("unchecked")
    private void addAuditLogEntries(Feed feed, Integer maxResults, Integer startIndex)
    {
        FeedData feedData = getFeedData(maxResults, startIndex);

        addTotalEntriesMarkup(feed, feedData.totalEntries);
        addStartIndexMarkup(feed, feedData.startIndex);

        //add links to next and previous pages if any exist, and to first/last pages
        int nextPageStartIndex = feedData.startIndex + feedData.maxResults;
        int previousPageStartIndex = max(feedData.startIndex - feedData.maxResults, 0);
        int firstPageStartIndex = 0;
        int lastPageStartIndex = (int) Math.floor((feedData.totalEntries - 1) / feedData.maxResults) * feedData.maxResults;

        if (nextPageStartIndex < feedData.totalEntries)
        {
            addLink(feed, uriBuilder.buildAuditLogFeedUri(feedData.maxResults, nextPageStartIndex), "next");
            addLink(feed, uriBuilder.buildAuditLogFeedUri(feedData.maxResults, lastPageStartIndex), "last");
        }

        if (feedData.startIndex > 0)
        {
            addLink(feed, uriBuilder.buildAuditLogFeedUri(feedData.maxResults, firstPageStartIndex), "first");
            addLink(feed, uriBuilder.buildAuditLogFeedUri(feedData.maxResults, previousPageStartIndex), "previous");
        }

        //transform AuditLogEntry elements to rome Entry elements and add to the feed
        feed.getEntries().addAll(Collections2.transform(feedData.entries, auditLogEntryToFeedEntryFn()));
    }

    private void addTotalEntriesMarkup(Feed feed, int totalEntries)
    {
        addForeignMarkup(feed, "totalEntries", String.valueOf(totalEntries));
    }

    private void addStartIndexMarkup(Feed feed, int startIndex)
    {
        addForeignMarkup(feed, "startIndex", String.valueOf(startIndex));
    }

    @SuppressWarnings("unchecked")
    private void addForeignMarkup(Feed feed, String name, String value)
    {
        final Element elem = new Element(name);
        elem.setText(String.valueOf(value));
        ((List) feed.getForeignMarkup()).add(elem);
    }

    private Function<AuditLogEntry, Entry> auditLogEntryToFeedEntryFn()
    {
        return new Function<AuditLogEntry, Entry>()
        {
            public Entry apply(final AuditLogEntry from)
            {
                Entry entry = new Entry();
                entry.setUpdated(from.getDate());
                entry.setTitle(from.getTitle(i18nResolver));
                String username = from.getUsername();
                Person person = generatePerson(username);
                entry.setAuthors(ImmutableList.of(person));
                return entry;
            }
        };
    }

    private Person generatePerson(String username)
    {
        String product = applicationProperties.getDisplayName();
        Person person = new Person();

        // don't want to get a default UserProfile when the "user" is anon or the product,
        // and anyway there could be a user with the username "anonymous" or "Confluence" etc
        if (i18nResolver.getText("upm.auditLog.anonymous").equals(username) || product.equals(username))
        {
            person.setName(username);
        }
        else
        {
            UserProfile userProfile = userManager.getUserProfile(username);
            final String userFullname = userProfile == null ? null : userProfile.getFullName();
            person.setName((userFullname != null) ? userFullname : username);
            URI userUri = uriBuilder.buildAbsoluteProfileUri(userProfile);
            if (userUri != null)
            {
                person.setUrl(userUri.toString());
            }
        }
        return person;
    }

    @SuppressWarnings("unchecked")
    private void addLink(Feed feed, String url, String rel)
    {
        Link link = new Link();
        link.setHref(url);
        link.setRel(rel);
        feed.getOtherLinks().add(link);
    }

    private void addLink(Feed feed, URI uri, String rel)
    {
        addLink(feed, uri.toString(), rel);
    }

    private void saveEntryAndPurge(final AuditLogEntry entry) throws IOException
    {
        List<String> entries = getSavedEntriesAsStrings();
        entries.add(mapper.writeValueAsString(entry));
        Iterable<AuditLogEntry> purgedEntries = purgeEntriesAndTransform(entries);
        saveEntries(purgedEntries);
    }

    @SuppressWarnings("unchecked")
    private List<String> getSavedEntriesAsStrings()
    {
        Object entries = getPluginSettings().get(UPM_AUDIT_LOG);
        if (entries == null)
        {
            return new ArrayList<String>();
        }
        else if (!(entries instanceof List))
        {
            log.error("Invalid audit log storage has been detected: " + entries);
            purgeLog();
            return new ArrayList<String>();
        }
        else
        {
            return (List<String>) entries;
        }
    }

    private void saveEntries(Iterable<AuditLogEntry> stringEntries)
    {
        Iterable<String> entries = Iterables.transform(stringEntries, new Function<AuditLogEntry, String>()
        {
            public String apply(AuditLogEntry from)
            {
                try
                {
                    return mapper.writeValueAsString(from);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to save AuditLogEntry to JSON: " + from, e);
                }
            }
        });
        List<String> result = new ArrayList<String>();
        int count = 0;
        int maxEntries = getMaxEntries();
        for (String s : entries)
        {
            if (++count > maxEntries)
            {
                break;
            }
            result.add(s);
        }
        // Save only up to maxEntries entries
        // For some reason, confluence acts strangely if you throw a subList in here. I can't even
        // define the broken behavior properly, nor do I think I can reproduce it outside of UPM, so
        // I'm not filing a bug
        getPluginSettings().put(UPM_AUDIT_LOG, result);
    }

    private Collection<AuditLogEntry> purgeEntriesAndTransform(List<String> stringEntries)
    {
        Collection<AuditLogEntry> entries = Collections2.transform(stringEntries, new Function<String, AuditLogEntry>()
        {
            public AuditLogEntry apply(String from)
            {
                try
                {
                    return mapper.readValue(from, AuditLogEntryImpl.class);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to parse AuditLogEntry from JSON string: " + from, e);
                }
            }
        });
        // Purge old entries
        entries = Collections2.filter(entries, purgePolicy);
        return Ordering.from(auditLogDateComparator).reverse().sortedCopy(entries);

    }

    public static Comparator<AuditLogEntry> auditLogDateComparator = new Comparator<AuditLogEntry>()
    {
        public int compare(AuditLogEntry entry1, AuditLogEntry entry2)
        {
            return entry1.compareTo(entry2);
        }
    };

    private PluginSettings getPluginSettings()
    {
        //never cache our plugin settings
        return new NamespacedPluginSettings(pluginSettingsFactory.createGlobalSettings(), KEY_PREFIX);
    }
    
    /**
     * Pluggable current time service; Joda-Time does not provide one and we're not importing
     * atlassian-core-utils.
     */
    public static interface Clock
    {
        long currentTimeMillis();
    }
    
    public static class SystemClock implements Clock
    {
        public long currentTimeMillis()
        {
            return System.currentTimeMillis();
        }
    }
}
