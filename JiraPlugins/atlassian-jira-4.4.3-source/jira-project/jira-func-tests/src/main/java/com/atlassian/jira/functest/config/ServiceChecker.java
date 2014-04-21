package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.config.mail.MailChecker;
import com.atlassian.jira.functest.config.service.ConfigService;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Iterator;
import java.util.List;

/**
 * Test to make sure that the debug service is not configured. The debug service is one we use in tests. It is
 * basically a service that can be configured to take a long time to run (i.e. it sleeps) but actually does nothing.
 * If we have one of these in our tests the debug service can delay other services from running (e.g. backup service)
 * which can lead to that other services deadlocking JIRA (e.g. if it runs during a restore) if they are delayed into the
 * next restore. This actually happens.
 *
 * @since v4.4
 */
public class ServiceChecker implements ConfigurationCheck
{
    private static final String CHECK_SERVICE = "service";
    private static final List<String> IGNORED_SERVICES;

    static
    {
        CollectionBuilder<String> builder = CollectionBuilder.newBuilder(MailChecker.SERVICES);
        builder.add(BackupChecker.BACKUP_SERVICE).add("MailQueueService").add("JiraPluginSchedulerService");

        IGNORED_SERVICES = builder.asList();
    }

    @Override
    public Result checkConfiguration(JiraConfig config, CheckOptions options)
    {
        final CheckResultBuilder builder = new CheckResultBuilder();
        final List<ConfigService> services = config.getServices();
        for (ConfigService service : services)
        {
            if (!isIgnored(service))
            {
                final String name = service.getName() != null ? service.getName() : "<unknown>";
                if (options.checkEnabled(CHECK_SERVICE))
                {
                    builder.error("Service '" + name + "' exits.", CHECK_SERVICE);
                }
            }
        }
        return builder.buildResult();
    }

    @Override
    public void fixConfiguration(JiraConfig config, CheckOptions options)
    {
        final List<ConfigService> services = config.getServices();
        for (final Iterator<ConfigService> iterator = services.iterator(); iterator.hasNext();)
        {
            ConfigService service = iterator.next();
            if (!isIgnored(service))
            {
                if (options.checkEnabled(CHECK_SERVICE))
                {
                    iterator.remove();
                }
            }
        }
    }

    private boolean isIgnored(ConfigService service)
    {
        for (String ignoredService : IGNORED_SERVICES)
        {
            if (service.getClazz().contains(ignoredService))
            {
                return true;
            }
        }
        return false;
    }
}
