package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.portal.CachingPortletConfigurationStore;
import com.atlassian.jira.portal.FlushablePortletConfigurationStore;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class TestUpgradeTask_Build418 extends LegacyJiraMockTestCase
{

    public void testDoUpgrade() throws Exception
    {
        //create a couple of portlet configs some with legacy keys others without
        final GenericValue portletGV = EntityUtils.createValue("PortletConfiguration",
                EasyMap.build("portalpage", 10000L, "portletId", "INTRODUCTION", "columnNumber", 0L, "position", 0L));
        final GenericValue portletGV1 = EntityUtils.createValue("PortletConfiguration",
                EasyMap.build("portalpage", 10020L, "portletId", "com.atlassian.jira.plugin.system.portlets:introduction",
                        "columnNumber", 0L, "position", 0L));
        final GenericValue portletGV2 = EntityUtils.createValue("PortletConfiguration",
                EasyMap.build("portalpage", 10000L, "portletId", "PROJECTS", "columnNumber", 0L, "position", 0L));
        final GenericValue portletGV3 = EntityUtils.createValue("PortletConfiguration",
                EasyMap.build("portalpage", 10000L, "portletId", "com.someone:somegadget", "columnNumber", 0L, "position", 0L));

        final OfBizDelegator ofBizDelegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        final MockControl mockCachingPortletConfigurationStoreControl = MockClassControl.createControl(CachingPortletConfigurationStore.class);
        final CachingPortletConfigurationStore mockCachingPortletConfigurationStore = (CachingPortletConfigurationStore) mockCachingPortletConfigurationStoreControl.getMock();
        mockCachingPortletConfigurationStore.flush();
        mockCachingPortletConfigurationStoreControl.replay();
        UpgradeTask_Build418 upgradeTask = new UpgradeTask_Build418(ofBizDelegator)
        {
            @Override
            FlushablePortletConfigurationStore getFlushablePortletConfigurationStore()
            {
                return mockCachingPortletConfigurationStore;
            }
        };
        upgradeTask.doUpgrade(false);

        List<GenericValue> gvs = ofBizDelegator.findByAnd("PortletConfiguration", EasyMap.build("id", portletGV.getLong("id")));
        assertEquals("com.atlassian.jira.plugin.system.portlets:introduction", gvs.get(0).getString("portletId"));
        gvs = ofBizDelegator.findByAnd("PortletConfiguration", EasyMap.build("id", portletGV1.getLong("id")));
        assertEquals("com.atlassian.jira.plugin.system.portlets:introduction", gvs.get(0).getString("portletId"));
        gvs = ofBizDelegator.findByAnd("PortletConfiguration", EasyMap.build("id", portletGV2.getLong("id")));
        assertEquals("com.atlassian.jira.plugin.system.portlets:projects", gvs.get(0).getString("portletId"));
        gvs = ofBizDelegator.findByAnd("PortletConfiguration", EasyMap.build("id", portletGV3.getLong("id")));
        assertEquals("com.someone:somegadget", gvs.get(0).getString("portletId"));

        mockCachingPortletConfigurationStoreControl.verify();
    }
}