package com.atlassian.upm.log;

import com.atlassian.sal.api.ApplicationProperties;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApplicationLifecycleLoggerImpl implements ApplicationLifecycleLogger
{
    private final AuditLogService auditLog;
    private final ApplicationProperties applicationProperties;

    public ApplicationLifecycleLoggerImpl(AuditLogService auditLog, ApplicationProperties applicationProperties)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.auditLog = checkNotNull(auditLog, "auditLog");
    }

    @Override
    public void onStart()
    {
        auditLog.logI18nMessageWithUsername("upm.auditLog.upm.startup", applicationProperties.getDisplayName());
    }
}
