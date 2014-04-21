package com.atlassian.jira.plugin.ext.bamboo.applinks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.event.api.EventPublisher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManagerImpl.JBAM_ASSOCIATIONS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BambooApplicationLinkManagerImplTest
{
    private static final String PROJECT1 = "ONE";
    private static final String PROJECT2 = "TWO";

    private BambooApplicationLinkManager applinkMgr;
    private List<String> associations;

    @Mock EventPublisher eventPublisher;
    @Mock ApplicationLinkService applinkService;
    @Mock ApplicationLink applink;
    @Mock ApplicationLink applink2;

    @Before
    public void setup() throws Exception
    {
        associations = new ArrayList<String>();

        when(applink.getId()).thenReturn(new ApplicationId(UUID.randomUUID().toString()));
        when(applink.putProperty(eq(JBAM_ASSOCIATIONS), anyObject())).thenAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                associations = (List<String>) invocation.getArguments()[1];
                return null;
            }
        });
        when(applink.getProperty(JBAM_ASSOCIATIONS)).thenAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return getAssociations();
            }
        });
        when(applink.removeProperty(JBAM_ASSOCIATIONS)).thenAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                associations = new ArrayList<String>();
                return null;
            }
        });

        when(applink2.getId()).thenReturn(new ApplicationId(UUID.randomUUID().toString()));

        when(applinkService.getApplicationLink(applink.getId())).thenReturn(applink);
        when(applinkService.getApplicationLink(applink2.getId())).thenReturn(applink2);
        when(applinkService.getPrimaryApplicationLink(BambooApplicationType.class)).thenReturn(applink);

        applinkMgr = new BambooApplicationLinkManagerImpl(applinkService, eventPublisher);
    }

    @Test
    public void projectIsAssociatedWithPrimaryApplinkByDefault()
    {
        assertThat(applinkMgr.getApplicationLink(PROJECT1), is(equalTo(applink)));
    }

    @Test
    public void projectIsAssociatedWithCorrectApplink()
    {
        applinkMgr.associate(PROJECT1, applink2.getId());
        assertThat(applinkMgr.getApplicationLink(PROJECT1), is(equalTo(applink2)));
    }

    @Test
    public void associateFirstProject()
    {
        applinkMgr.associate(PROJECT1, applink.getId());
        List<String> associations = (List<String>) applink.getProperty(JBAM_ASSOCIATIONS);
        assertThat(associations.size(), is(equalTo(1)));
        assertThat(associations, hasItems(PROJECT1));
    }

    @Test
    public void associateSecondProject()
    {
        applinkMgr.associate(PROJECT1, applink.getId());
        applinkMgr.associate(PROJECT2, applink.getId());
        List<String> associations = (List<String>) applink.getProperty(JBAM_ASSOCIATIONS);
        assertThat(associations.size(), is(equalTo(2)));
        assertThat(associations, hasItems(PROJECT1, PROJECT2));
    }

    @Test
    public void associateSecondProjectAfterUnassociatingFirstProject()
    {
        applinkMgr.associate(PROJECT1, applink.getId());
        applinkMgr.unassociateAll(applink.getId());
        applinkMgr.associate(PROJECT2, applink.getId());
        List<String> associations = (List<String>) applink.getProperty(JBAM_ASSOCIATIONS);
        assertThat(associations.size(), is(equalTo(1)));
        assertThat(associations, hasItems(PROJECT2));
    }

    private List<String> getAssociations()
    {
        return associations;
    }
}
