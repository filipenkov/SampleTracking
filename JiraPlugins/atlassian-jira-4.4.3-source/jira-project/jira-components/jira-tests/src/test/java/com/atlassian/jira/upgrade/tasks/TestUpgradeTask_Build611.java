package com.atlassian.jira.upgrade.tasks;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.jira.bc.portal.GadgetApplinkUpgradeUtil;
import com.atlassian.jira.bc.whitelist.WhitelistManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestUpgradeTask_Build611
{
    @Test
    public void testMetaData()
    {
        UpgradeTask_Build611 upgradeTask = new UpgradeTask_Build611(null, null);
        assertEquals("611", upgradeTask.getBuildNumber());
        assertEquals("Configuring whitelist entries for all external gadgets", upgradeTask.getShortDescription());
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        final GadgetApplinkUpgradeUtil mockGadgetApplinkUpgradeUtil = EasyMock.createMock(GadgetApplinkUpgradeUtil.class);
        final WhitelistManager mockWhitelistManager = createMock(WhitelistManager.class);
        expect(mockGadgetApplinkUpgradeUtil.getExternalGadgetsRequiringUpgrade()).andReturn(Collections.<URI, List<ExternalGadgetSpec>>emptyMap());
        expect(mockWhitelistManager.updateRules(CollectionBuilder.list("http://www.atlassian.com/*"), false)).andReturn(CollectionBuilder.list("http://www.atlassian.com/*"));

        EasyMock.replay(mockGadgetApplinkUpgradeUtil);
        replay(mockWhitelistManager);
        UpgradeTask_Build611 upgradeTask = new UpgradeTask_Build611(mockGadgetApplinkUpgradeUtil, mockWhitelistManager);
        upgradeTask.doUpgrade(false);

        EasyMock.verify(mockGadgetApplinkUpgradeUtil);
        verify(mockWhitelistManager);
    }

    @Test
    public void testDoUpgradeWithData() throws Exception
    {
        final GadgetApplinkUpgradeUtil mockGadgetApplinkUpgradeUtil = EasyMock.createMock(GadgetApplinkUpgradeUtil.class);
        final WhitelistManager mockWhitelistManager = createMock(WhitelistManager.class);
        final Map<URI, List<ExternalGadgetSpec>> ret = new LinkedHashMap<URI, List<ExternalGadgetSpec>>();
        ret.put(URI.create("http://extranet.atlassian.com"), null);
        ret.put(URI.create("http://www.google.com"), null);
        ret.put(URI.create("https://www.cows.com"), null);
        expect(mockGadgetApplinkUpgradeUtil.getExternalGadgetsRequiringUpgrade()).andReturn(ret);
        final List<String> rules = CollectionBuilder.list("http://www.atlassian.com/*", "http://extranet.atlassian.com/*", "http://www.google.com/*", "https://www.cows.com/*");
        expect(mockWhitelistManager.updateRules(rules, false)).andReturn(rules);

        EasyMock.replay(mockGadgetApplinkUpgradeUtil);
        replay(mockWhitelistManager);
        UpgradeTask_Build611 upgradeTask = new UpgradeTask_Build611(mockGadgetApplinkUpgradeUtil, mockWhitelistManager);
        upgradeTask.doUpgrade(false);

        EasyMock.verify(mockGadgetApplinkUpgradeUtil);
        verify(mockWhitelistManager);
    }
}
