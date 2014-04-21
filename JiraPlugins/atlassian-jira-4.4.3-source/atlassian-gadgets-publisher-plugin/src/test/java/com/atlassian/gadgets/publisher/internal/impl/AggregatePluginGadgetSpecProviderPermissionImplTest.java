package com.atlassian.gadgets.publisher.internal.impl;

import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.publisher.spi.PluginGadgetSpecProviderPermission;
import com.atlassian.plugin.Plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AggregatePluginGadgetSpecProviderPermissionImplTest
{
    final PluginGadgetSpec gadgetSpec = 
        new PluginGadgetSpec(mock(Plugin.class), "module:key", "mygadget.xml", ImmutableMap.<String, String>of());
    
    @Mock PluginGadgetSpecProviderPermission p1;
    @Mock PluginGadgetSpecProviderPermission p2;
    @Mock PluginGadgetSpecProviderPermission p3;
    
    PluginGadgetSpecProviderPermission permission;
    
    @Before
    public void setUp()
    {
        permission = new AggregatePluginGadgetSpecProviderPermissionImpl(ImmutableList.of(p1, p2, p3));
    }
    
    @Test
    public void assertThatIfAllPermissionsReturnPassThenTheAggregatePermissionIsPass()
    {
        whenVotingReturn(Vote.PASS, Vote.PASS, Vote.PASS);
        
        assertThat(permission.voteOn(gadgetSpec), is(equalTo(Vote.PASS)));
    }
    
    @Test
    public void assertThatIfAnyPermissionsReturnDenyThenTheAggregatePermissionIsDeny()
    {
        whenVotingReturn(Vote.PASS, Vote.DENY, Vote.PASS);

        assertThat(permission.voteOn(gadgetSpec), is(equalTo(Vote.DENY)));
    }

    @Test
    public void assertThatIfAnyPermissionReturnsAllowAndAllOthersReturnPassThenTheAggregatePermissionIsAllow()
    {
        whenVotingReturn(Vote.PASS, Vote.ALLOW, Vote.PASS);

        assertThat(permission.voteOn(gadgetSpec), is(equalTo(Vote.ALLOW)));
    }
    
    @Test
    public void assertThatIfAnyPermissionReturnsAllowAndOneBeforeItReturnsDenyThenTheAggregatePermissionIsDeny()
    {
        whenVotingReturn(Vote.DENY, Vote.ALLOW, Vote.PASS);

        assertThat(permission.voteOn(gadgetSpec), is(equalTo(Vote.DENY)));
    }

    @Test
    public void assertThatIfAnyPermissionReturnsAllowAndOneAfterItReturnsDenyThenTheAggregatePermissionIsDeny()
    {
        whenVotingReturn(Vote.PASS, Vote.ALLOW, Vote.DENY);

        assertThat(permission.voteOn(gadgetSpec), is(equalTo(Vote.DENY)));
    }

    private void whenVotingReturn(Vote v1, Vote v2, Vote v3)
    {
        when(p1.voteOn(gadgetSpec)).thenReturn(v1);
        when(p2.voteOn(gadgetSpec)).thenReturn(v2);
        when(p3.voteOn(gadgetSpec)).thenReturn(v3);
    }
}
