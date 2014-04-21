package com.atlassian.upm.log;

import java.util.Set;

import com.atlassian.upm.api.log.AuditLogEntry;
import com.atlassian.upm.api.log.EntryType;
import com.atlassian.upm.api.log.PluginLogService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serves as a wrapper around {@link AuditLogService} to expose specific functionality
 * through UPM's API. Delegates to {@link AuditLogService} for the methods which should be exposed,
 * and hides the methods that should not.
 */
public class PluginLogServiceImpl implements PluginLogService
{
    private final AuditLogService auditLog;

    public PluginLogServiceImpl(AuditLogService auditLog)
    {
        this.auditLog = checkNotNull(auditLog, "auditLog");
    }
    
    public Iterable<AuditLogEntry> getLogEntries()
    {
        return getLogEntries(null, null);
    }

    public Iterable<AuditLogEntry> getLogEntries(Integer maxResults, Integer startIndex)
    {
        return auditLog.getLogEntries(maxResults, startIndex);
    }

    public Iterable<AuditLogEntry> getLogEntries(Set<EntryType> entryTypes)
    {
        return getLogEntries(null, null, entryTypes);
    }

    public Iterable<AuditLogEntry> getLogEntries(Integer maxResults, Integer startIndex, Set<EntryType> entryTypes)
    {
        return auditLog.getLogEntries(maxResults, startIndex, entryTypes);
    }
}
