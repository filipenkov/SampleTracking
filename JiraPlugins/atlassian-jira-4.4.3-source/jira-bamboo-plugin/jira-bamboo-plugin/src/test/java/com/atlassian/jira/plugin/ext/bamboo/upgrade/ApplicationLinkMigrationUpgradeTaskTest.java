package com.atlassian.jira.plugin.ext.bamboo.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.LegacyBambooServer;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager.LegacyBambooServerManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationLinkMigrationUpgradeTaskTest
{
    @Mock private MutatingApplicationLinkService mutatingLinkService;
    @Mock private LegacyBambooServerManager legacyServerManager;
    @Mock private BambooApplicationLinkManager applinkManager;
    @Mock private AuthenticationConfigurationManager authConfigManager;
    @Mock private TypeAccessor typeAccessor;
    @Mock private LegacyBambooServer server1;
    @Mock private LegacyBambooServer server2;
    @Mock private BambooApplicationType applicationType;

    private ApplicationLinkMigrationUpgradeTask upgradeTask;
    private Map<String, ApplicationLinkDetails> registeredApplinks;

    @Before
    public void setUp()
    {
        when(server1.getId()).thenReturn(1);
        when(server1.getName()).thenReturn("TST01");
        when(server1.getDescription()).thenReturn("Bamboo test instance #01");
        when(server1.getHost()).thenReturn("http://bamboo01.atlassian.com");
        when(server1.getUsername()).thenReturn("myusername");
        when(server1.getPassword()).thenReturn("mypassword");
        when(server1.getAssociatedProjectKeys()).thenReturn(ImmutableSet.of("PROJECT01_TST01", "PROJECT02_TST01", "PROJECT03_TST01"));

        when(server2.getId()).thenReturn(2);
        when(server2.getName()).thenReturn("TST02");
        when(server2.getDescription()).thenReturn("Bamboo test instance #02");
        when(server2.getHost()).thenReturn("http://bamboo02.atlassian.com");
        when(server2.getUsername()).thenReturn("myusername");
        when(server2.getPassword()).thenReturn(null);
        when(server2.getAssociatedProjectKeys()).thenReturn(ImmutableSet.of("PROJECT01_TST02", "PROJECT02_TST02", "PROJECT03_TST02"));

        when(legacyServerManager.getServers()).thenReturn(ImmutableList.<LegacyBambooServer>of(server1, server2));
        when(applinkManager.getApplicationLinks()).thenReturn(ImmutableList.<ApplicationLink>of());
        when(typeAccessor.getApplicationType(BambooApplicationType.class)).thenReturn(applicationType);

        registeredApplinks = new HashMap<String, ApplicationLinkDetails>();
        when(mutatingLinkService.addApplicationLink(any(ApplicationId.class),
                                                    any(ApplicationType.class),
                                                    any(ApplicationLinkDetails.class))).thenAnswer(new Answer<ApplicationLink>()
        {
            public ApplicationLink answer(InvocationOnMock invocation) throws Throwable
            {
                Object[] args = invocation.getArguments();
                ApplicationLinkDetails details = (ApplicationLinkDetails) args[2];
                registeredApplinks.put(details.getName(), details);
                return null;
            }
        });

        upgradeTask = new ApplicationLinkMigrationUpgradeTask(mutatingLinkService, legacyServerManager, applinkManager, authConfigManager, typeAccessor);
    }

    @Test
    public void testApplicationDetailsContainsCorrectData() throws Exception
    {
        upgradeTask.doUpgrade();
        ApplicationLinkDetails applicationLinkDetails = registeredApplinks.get("TST01");

        assertEquals("TST01", applicationLinkDetails.getName());
        assertEquals("http://bamboo01.atlassian.com", applicationLinkDetails.getDisplayUrl().toASCIIString());
        assertEquals("http://bamboo01.atlassian.com", applicationLinkDetails.getRpcUrl().toASCIIString());
    }

    @Test
    public void testTwoApplicationLinksRegistered() throws Exception
    {
        upgradeTask.doUpgrade();
        assertEquals(2, registeredApplinks.entrySet().size());
    }
}
