package com.atlassian.jira.upgrade.tasks;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.config.properties.MockPropertiesManager;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.JiraServiceContainerImpl;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.DebugService;
import com.atlassian.jira.service.services.export.ExportService;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TestUpgradeTask_Build190 extends LegacyJiraMockTestCase
{
    private UpgradeTask_Build190 ut;
    private Mock mockServiceManager;
    private ApplicationProperties ap;

    protected void setUp() throws Exception
    {
        super.setUp();
        ap = new MockApplicationProperties();
        mockServiceManager = new Mock(ServiceManager.class);
        ut = new UpgradeTask_Build190((ServiceManager) mockServiceManager.proxy(), ap);
    }

    protected void tearDown() throws Exception
    {
        ap = null;
        ut = null;
    }

    /**
     * Test that short description is set
     */
    public void testShortDescription()
    {
        assertNotNull(ut.getShortDescription());
    }

    /**
     * Test the build number is 205
     */
    public void testBuildNumber()
    {
        String buildNumber = ut.getBuildNumber();
        assertNotNull(buildNumber);
        assertEquals("190", buildNumber);
    }

    public void testDoUpgrade() throws ObjectConfigurationException
    {
        //try running the UT with no aplication property set for the backup path (should do nothing)
        mockServiceManager.expectNotCalled("getServices");
        ut.doUpgrade(false);
        mockServiceManager.verify();
    }

    public void testDoUpgradeWithApplicationProperty()
    {
        //try running the UT with the application property set but no services defined.
        ap.setString(APKeys.JIRA_PATH_BACKUP, "/tmp/");
        mockServiceManager.expectAndReturn("getServices", Collections.EMPTY_LIST);
        ut.doUpgrade(false);
    }

    public PropertySet testDoUpgradeWithServiceWithPath()
            throws ObjectConfigurationException
    {
        //try running the UT with the application property set and an export service (that has a backup path defined)
        ap.setString(APKeys.JIRA_PATH_BACKUP, "/tmp/");
        Collection serviceList = new ArrayList();
        ExportService es = new ExportService(null);
        PropertySet ps = MockPropertiesManager.getInstance().getPropertySet();
        ps.setString("DIR_NAME", "/some/backup/path");
        es.init(ps);
        JiraServiceContainer jsc = new JiraServiceContainerImpl(es, new Long(1));
        serviceList.add(jsc);
        mockServiceManager.expectAndReturn("getServices", serviceList);
        mockServiceManager.expectNotCalled("editService");
        ut.doUpgrade(false);
        mockServiceManager.verify();
        return ps;
    }

    public void testDoUpgradeWithServiceWithoutPath()
            throws ObjectConfigurationException
    {
        Collection serviceList;
        ExportService es;
        JiraServiceContainer jsc;

        //try running the UT with the application property set and an export service (that has no backup path defined)
        ap.setString(APKeys.JIRA_PATH_BACKUP, "/tmp/");
        serviceList = new ArrayList();
        es = new ExportService(null);

        // Not getting a whole property set here, as it contained some text properties (due to the new license format)
        // A service will ever only have string properties anyway ( see ConfigurableObjectUtil).  For this test, we
        // don't need any properties to initialize the service with at all, since we want to simulate a service without
        // a backup path set.
        PropertySet ps = new MapPropertySet()
        {
            public synchronized boolean exists(final String s)
            {
                return false;
            }

            public synchronized Collection getKeys(final String s, final int i)
            {
                return Collections.EMPTY_SET;
            }
        };
        es.init(ps);
        jsc = new JiraServiceContainerImpl(es, new Long(1));
        serviceList.add(jsc);
        mockServiceManager.expectAndReturn("getServices", serviceList);
        mockServiceManager.expectVoid("editService", new Constraint[] { P.eq(jsc.getId()), P.eq(jsc.getDelay()), new MyMapConstraint("/tmp/") });
        ut.doUpgrade(false);
        mockServiceManager.verify();
    }

    public void testDoUpgradeWithNonExportService()
            throws ObjectConfigurationException
    {
        Collection serviceList;
        DebugService debugService;
        JiraServiceContainer jsc;

        //try running the UT with the application property set and a debug service.
        ap.setString(APKeys.JIRA_PATH_BACKUP, "/tmp/");
        serviceList = new ArrayList();
        debugService = new DebugService();
        PropertySet ps = MockPropertiesManager.getInstance().getPropertySet();
        debugService.init(ps);
        jsc = new JiraServiceContainerImpl(debugService, new Long(1));
        serviceList.add(jsc);
        mockServiceManager.expectAndReturn("getServices", serviceList);
        mockServiceManager.expectNotCalled("editService");
        ut.doUpgrade(false);
        mockServiceManager.verify();
    }

    private class MyMapConstraint implements Constraint
    {

        String dirName = null;

        public MyMapConstraint(String dir_name)
        {
            this.dirName = dir_name;
        }

        public boolean eval(Object object)
        {
            if (object != null && object instanceof Map)
            {
                Map theMap = (Map) object;

                if (theMap.containsKey("DIR_NAME"))
                {
                    String serviceDirPath = ((String[]) theMap.get("DIR_NAME"))[0];
                    if (dirName.compareTo(serviceDirPath) == 0)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}

