/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it;

import com.atlassian.jira.functest.framework.backdoor.ServicesControl;
import com.atlassian.jira.plugins.mail.handlers.CVSLogHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.FullCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.NonQuotedCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.RegexCommentHandler;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestUpgradeTasks extends BaseJiraWebTest
{
    private static final Map<String, String> expectedHandlers = ImmutableMap.<String, String>builder()
            .put("Gmail IMAP", CreateOrCommentHandler.class.getName())
            .put("Gmail POP", CreateIssueHandler.class.getName())
            .put("File Service", NonQuotedCommentHandler.class.getName())
            .put("File Service with Subdir", FullCommentHandler.class.getName())
            .put("Gmail POP RegEx", RegexCommentHandler.class.getName())
            .put("Gmail IMAP CVS", CVSLogHandler.class.getName())
            .build();

    public static final Function<ServicesControl.ServiceBean, String> SERVICE_NAME = new Function<ServicesControl.ServiceBean, String>()
    {
        @Override
        public String apply(ServicesControl.ServiceBean from)
        {
            return from.name;
        }
    };

	@Before
	public void restore() {
		backdoor.restoreData("gmailpopimapdefinedin44.xml");
	}

    @Test
    public void testMailHandlersFixedToUseHandlersFromThePlugin() throws Exception
    {
        final List<ServicesControl.ServiceBean> services = backdoor.services().getServices();
        final ImmutableMap<String, ServicesControl.ServiceBean> byName = Maps.uniqueIndex(services, SERVICE_NAME);
        for (Map.Entry<String, String> expected : expectedHandlers.entrySet())
        {
            final ServicesControl.ServiceBean serviceBean = byName.get(expected.getKey());
            assertNotNull("Expected service not found: " + expected.getKey(), serviceBean);
            assertTrue(serviceBean.usable);
            assertNotNull(serviceBean.params);
            assertEquals(serviceBean.params.get(AbstractMessageHandlingService.KEY_HANDLER), expected.getValue());
        }

    }

    private static final List<String> expectedMailFetcherServices = ImmutableList.of("Gmail IMAP", "Gmail POP", "Gmail POP RegEx", "Gmail IMAP CVS");

    @Test
    public void testMailFetcherServiceUsedInsteadOfObsoleteImapAndPop() throws Exception
    {
        Set<String> left = Sets.newHashSet(expectedMailFetcherServices);
        for (ServicesControl.ServiceBean s : backdoor.services().getServices())
        {
            if (left.remove(s.name))
            {
                assertEquals(MailFetcherService.class.getName(), s.serviceClass);
            }
        }
        assertEquals("Some services have not been found", Collections.<String>emptySet(), left);
    }

    @Test
    public void testBackupServiceNotBrokenInTheProcess() throws Exception
    {
        final ServicesControl.ServiceBean backupService = backdoor.services().getService(10001);
        assertEquals("Backup Service", backupService.name);
        assertEquals("com.atlassian.jira.service.services.export.ExportService", backupService.serviceClass);
        assertTrue(backupService.usable);
        assertNotNull(backupService.params);
        assertEquals(backupService.params, ImmutableMap.of("USE_DEFAULT_DIRECTORY", "true"));
    }

    @Test
    public void testServiceProviderTokenRemoverNotBrokenInTheProcess() throws Exception
    {
        final List<ServicesControl.ServiceBean> services = backdoor.services().getServices();
        final ServicesControl.ServiceBean backupService = Iterables.find(services, Predicates.compose(Predicates.equalTo("Service Provider Token Remover"), SERVICE_NAME));
        assertNotNull(backupService);
        assertEquals("com.atlassian.sal.jira.scheduling.JiraPluginSchedulerService", backupService.serviceClass);
        assertTrue(backupService.usable);
        assertNotNull(backupService.params);
        assertEquals(backupService.params, ImmutableMap.of(
                "pluginJobName", "Service Provider Token Remover",
                "repeatInterval", "28800000",
                "initiallyFired", "true"));
    }

    @Test
    public void testMailQueueServiceNotBrokenInTheProcess() throws Exception
    {
        final ServicesControl.ServiceBean backupService = backdoor.services().getService(10000);
        assertEquals("Mail Queue Service", backupService.name);
        assertEquals("com.atlassian.jira.service.services.mail.MailQueueService", backupService.serviceClass);
        assertTrue(backupService.usable);
        assertNull(backupService.params);
    }
}
