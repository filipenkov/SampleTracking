package com.atlassian.upm.log;

import com.atlassian.upm.api.log.AuditLogEntry;
import com.atlassian.upm.api.log.EntryType;
import com.sun.syndication.feed.atom.Feed;

import java.util.Set;

/**
 * Provides a log of all events that change the state of the plugin system. An event is stored as a triple
 * of date, user name, and message. The log can be retrieved as a collection of events, or an atom feed.
 * <p/>
 * This is the internal version of {@code PluginLogService}. {@code PluginLogService} can be accessed via UPM's API,
 * whereas {@link AuditLogService} can only be used internally.
 * <p/>
 * Implementations should be thread safe. Multiple threads may assume they can call the interface methods
 * safely, assuming no additional atomicity requirements are required between calls. Atomicity may be
 * achieved by synchronizing on the service object and performing successive operations in that block.
 */
public interface AuditLogService
{
    /**
     * Log an internationalized message
     *
     * @param key the i18n key
     * @param params i18n parameters
     */
    void logI18nMessage(String key, String... params);

    /**
     * Log an internationalized message
     *
     * @param key the i18n key
     * @param username the user to log the message as
     * @param params i18n parameters
     * @since 2.0
     */
    void logI18nMessageWithUsername(String key, String username, String... params);


    /**
     * @return all log entries
     */
    Iterable<AuditLogEntry> getLogEntries();

    /**
     * Returns {@code maxResults} number of plugin log entries, starting at {@code startIndex}.
     *
     * @param maxResults the maximum number of plugin log entries to return
     * @param startIndex the starting index of plugin log entries
     * @return  {@code maxResults} number of plugin log entries, starting at {@code startIndex}.
     */
    Iterable<AuditLogEntry> getLogEntries(Integer maxResults, Integer startIndex);


    /**
     * Returns {@code maxResults} number of plugin log entries with the specified types, starting at {@code startIndex}.
     *
     * @param maxResults the maximum number of plugin log entries to return
     * @param startIndex the starting index of plugin log entries
     * @param entryTypes the entry types
     * @return {@code maxResults} number of plugin log entries, starting at {@code startIndex}.
     */
    Iterable<AuditLogEntry> getLogEntries(Integer maxResults, Integer startIndex, Set<EntryType> entryTypes);

    /**
     * @return an atom feed of the log entries
     */
    Feed getFeed();

    /**
     * @return an atom feed of the log entries with paging
     */
    Feed getFeed(Integer maxResults, Integer startIndex);

    /**
     * Purge all entries from the log
     */
    void purgeLog();

    /**
     * Retrieve the maximum number of entries that the log will store
     *
     * @return the maximum number of entries that the log will store
     */
    int getMaxEntries();

    /**
     * Set the maximum number of entries that the log will store
     *
     * @param maxEntries the maximum number of entries to store in the log
     */
    void setMaxEntries(int maxEntries);

    /**
     * Retrieve the number of days an entry should live before being purged
     *
     * @return the number of days an entry should live before being purged
     */
    int getPurgeAfter();

    /**
     * Set the number of days an entry should live before being purged
     *
     * @param purgeAfter the number of days an entry should live before being purged
     */
    void setPurgeAfter(int purgeAfter);
}
