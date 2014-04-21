package com.atlassian.jira.web.component;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Collection;
import java.util.Map;

/**D
 * Tests DashboardPageConfigUrlFactoryImpl duh.
 *
 * @since v3.13
 */
public class TestDashboardPageConfigUrlFactoryImpl extends ListeningTestCase
{
    /**
     * Checks for no errant leading slashes because these are added when sticking baseurl on.
     */
    @Test
    public void testSlashage()
    {
        final Long currentPageId = 234L;
        DashboardPageConfigUrlFactoryImpl dashboardPageConfigUrlFactory = new DashboardPageConfigUrlFactoryImpl(currentPageId);
        final Map config = EasyMap.build("monkey", "chimp", "icecream", "chocolate");
        DashboardPageConfigUrlFactory.PortletConfigurationAdaptor accessor = new DashboardPageConfigUrlFactory.PortletConfigurationAdaptor()
        {
            public Collection getKeys()
            {
                return config.keySet();
            }

            public String getPropertyAsString(String key)
            {
                return (String) config.get(key);
            }

            public String getPortletId()
            {
                return "12345";
            }
        };
        String runPortletUrl = dashboardPageConfigUrlFactory.getRunPortletUrl(accessor);
        assertFalse(runPortletUrl.startsWith("/"));
        assertTrue(runPortletUrl.indexOf("monkey=chimp") != -1);
        assertTrue(runPortletUrl.indexOf("icecream=chocolate") != -1);
    }
}
