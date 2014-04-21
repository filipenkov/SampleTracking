package com.atlassian.upm.log;

import java.io.Serializable;
import java.util.Date;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.log.AuditLogEntry;
import com.atlassian.upm.api.log.EntryType;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an entry in the audit log. Entries are comparable by date.
 */
public class AuditLogEntryImpl implements AuditLogEntry
{
    @JsonProperty private final String username;
    @JsonProperty private final Date date;
    @JsonProperty private final String i18nKey;
    @JsonProperty private final String[] params;
    @JsonProperty private final EntryType entryType;

    @JsonCreator
    AuditLogEntryImpl(@JsonProperty("username") String username,
                      @JsonProperty("date") Date date,
                      @JsonProperty("i18nKey") String i18nKey,
                      @JsonProperty("entryType") EntryType entryType,
                      @JsonProperty("params") String... params)
    {
        this.username = checkNotNull(username, "username");
        this.date = checkNotNull(date, "date");
        this.i18nKey = checkNotNull(i18nKey, "i18nKey");
        this.params = checkNotNull(params, "params");

        //entryType wasn't added until UPM 1.6 - we may need to deduce it for legacy/new audit log entries.
        this.entryType = (entryType == null) ? EntryType.valueOfI18n(i18nKey) : entryType;
    }

    /**
     * @param i18nResolver
     * @return the resolved title of the log entry
     */
    public String getTitle(I18nResolver i18nResolver)
    {
        return i18nResolver.getText(i18nKey, (Serializable[]) params);
    }

    /**
     * @param i18nResolver
     * @return the full message text of the entry, including date and username
     */
    public String getMessage(I18nResolver i18nResolver)
    {
        return date + " " + username + ": " + i18nResolver.getText(i18nKey, (Serializable[]) params);
    }

    /**
     * @return the date this entry was created
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * @return the username of the user that was logged in and created this event
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @return the event message
     */
    public String getI18nKey()
    {
        return i18nKey;
    }

    public EntryType getEntryType()
    {
        return entryType;
    }

    /**
     * Compares events by date
     *
     * @param auditLogEntry
     * @return a comparison value, as per the {@code Comparable} contract, based on the event dates
     */
    public int compareTo(AuditLogEntry auditLogEntry)
    {
        return getDate().compareTo(auditLogEntry.getDate());
    }
}
