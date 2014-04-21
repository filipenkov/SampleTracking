package com.atlassian.jira.portal;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.plugin.PluginAccessor;

import java.util.List;

/**
 * Unit test for the {@link DefaultPortletAccessManager}.
 *
 * @since v3.13
 */
public class TestDefaultPortletAccessManager extends MockControllerTestCase
{
    /**
     * This method should just be a simple call through to the plugin accessor.
     */
    @Test
    public void testGetAllPortlets()
    {
        final List /*<Portlet>*/ expectedPortlets = EasyList.build();

        final PluginAccessor pluginAccessor = (PluginAccessor) mockController.getMock(PluginAccessor.class);
        pluginAccessor.getEnabledModulesByClass(Portlet.class);
        mockController.setReturnValue(expectedPortlets);

        PortletAccessManager pam = (PortletAccessManager) mockController.instantiate(DefaultPortletAccessManager.class);

        assertSame(expectedPortlets, pam.getAllPortlets());
    }
}
