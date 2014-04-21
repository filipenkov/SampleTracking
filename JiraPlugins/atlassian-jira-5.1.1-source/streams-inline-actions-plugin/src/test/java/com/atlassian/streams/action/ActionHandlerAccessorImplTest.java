package com.atlassian.streams.action;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.streams.action.modules.ActionHandlersModuleDescriptor;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionHandlerAccessorImplTest
{
    private static final String KEY = "com.atlassian.streams.bamboo.inlineactions:actionHandlers";
    
    private ActionHandlerAccessor actionHandlerAccessor;
    
    @Mock PluginAccessor pluginAccessor;
    @Mock ActionHandlersModuleDescriptor actionHandler;
    
    @Before
    public void setup()
    {
        actionHandlerAccessor = new ActionHandlerAccessorImpl(pluginAccessor);

        when(actionHandler.getCompleteKey()).thenReturn(KEY);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(ActionHandlersModuleDescriptor.class)).thenReturn(ImmutableList.of(actionHandler));
    }
    
    @Test
    public void testContainsModuleKey()
    {
        assertThat(actionHandlerAccessor.getActionHandlerModuleKeys(), hasItem(KEY));
    }
}
