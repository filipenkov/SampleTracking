package com.atlassian.jira.plugins.mail.upgrade;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugins.mail.handlers.CVSLogHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.FullCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.NonQuotedCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.RegexCommentHandler;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Updates existing services that use the old mail handlers to use the ones from the plugin.
 *
 * @since v5.0
 */
public class UpgradeTask_1_MoveMailHandlers implements PluginUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_1_MoveMailHandlers.class);

    // Copied from OfBizServiceConfigStore - keep in sync or this breaks
    private static final String ENTITY_NAME = "ServiceConfig";
    private static final String SERVICE_CONFIG_NAME = "name";
    private static final String SERVICE_CONFIG_CLAZZ = "clazz";

    public static final Map<String, String> handlerTranslation = ImmutableMap.<String, String>builder()
            .put("com.atlassian.jira.service.util.handler.CreateOrCommentHandler", CreateOrCommentHandler.class.getName())
            .put("com.atlassian.jira.service.util.handler.CreateIssueHandler", CreateIssueHandler.class.getName())
            .put("com.atlassian.jira.service.util.handler.CVSLogHandler", CVSLogHandler.class.getName())
            .put("com.atlassian.jira.service.util.handler.FullCommentHandler", FullCommentHandler.class.getName())
            .put("com.atlassian.jira.service.util.handler.NonQuotedCommentHandler", NonQuotedCommentHandler.class.getName())
            .put("com.atlassian.jira.service.util.handler.RegexCommentHandler", RegexCommentHandler.class.getName())
            .build();

    private static final Set<String> obsoleteServices = ImmutableSet.of(
            "com.atlassian.jira.service.services.imap.ImapService",
            "com.atlassian.jira.service.services.pop.PopService");


    private final ServiceManager serviceManager;
    private final OfBizDelegator genericDelegator;

    public UpgradeTask_1_MoveMailHandlers(ServiceManager serviceManager, OfBizDelegator delegator)
    {
        this.serviceManager = serviceManager;
        this.genericDelegator = delegator;
    }

    @Override
    public int getBuildNumber()
    {
        return 1;
    }

    @Override
    public String getShortDescription()
    {
        return "Updates existing services that use the old mail handlers to use the ones from the plugin.";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception
    {
        boolean refreshNeeded = false;
        final Collection<GenericValue> serivceConfigGVs = genericDelegator.findAll(ENTITY_NAME);
        try
        {
            for (GenericValue configGV : serivceConfigGVs)
            {
                final String serviceClass = configGV.getString(SERVICE_CONFIG_CLAZZ);
                final Object serviceName = configGV.get(SERVICE_CONFIG_NAME);
                if (obsoleteServices.contains(serviceClass))
                {
                    log.info(String.format("Upgrading service '%s' - fixing service to %s", serviceName, MailFetcherService.class.getName()));
                    configGV.setString(SERVICE_CONFIG_CLAZZ, MailFetcherService.class.getName());
                    configGV.store();
                    refreshNeeded = true;
                }

                final PropertySet ps = OFBizPropertyUtils.getPropertySet(configGV);
                if (ps != null && ps.exists(AbstractMessageHandlingService.KEY_HANDLER) && ps.getType(AbstractMessageHandlingService.KEY_HANDLER) == PropertySet.STRING)
                {
                    final String handler = ps.getString(AbstractMessageHandlingService.KEY_HANDLER);
                    if (handlerTranslation.containsKey(handler))
                    {
                        final String newHandler = handlerTranslation.get(handler);
                        log.info(String.format("Upgrading service '%s' - fixing mail handler to %s", serviceName, newHandler));
                        ps.setString(AbstractMessageHandlingService.KEY_HANDLER, newHandler);
                        configGV.store();
                        refreshNeeded = true;
                    }
                }
            }
        }
        finally
        {
            if (refreshNeeded)
            {
                serviceManager.refreshAll();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String getPluginKey()
    {
        return "com.atlassian.jira.jira-mail-plugin";
    }
}
