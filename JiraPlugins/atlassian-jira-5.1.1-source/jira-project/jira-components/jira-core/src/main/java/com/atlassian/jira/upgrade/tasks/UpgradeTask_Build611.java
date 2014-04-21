package com.atlassian.jira.upgrade.tasks;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.jira.bc.portal.GadgetApplinkUpgradeUtil;
import com.atlassian.jira.bc.whitelist.WhitelistManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adds whitelist entries for all external gadgets.
 *
 * @since v4.3
 */
public class UpgradeTask_Build611 extends AbstractUpgradeTask
{
    private final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil;
    private final WhitelistManager whitelistManager;


    public UpgradeTask_Build611(final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil, final WhitelistManager whitelistManager)
    {
        super(false);
        this.gadgetApplinkUpgradeUtil = gadgetApplinkUpgradeUtil;
        this.whitelistManager = whitelistManager;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Map<URI,List<ExternalGadgetSpec>> externalGadgets = gadgetApplinkUpgradeUtil.getExternalGadgetsRequiringUpgrade();
        final List<String> rules = new ArrayList<String>();
        //always add WAC to ensure we get the marketing gadget
        rules.add("http://www.atlassian.com/*");
        if(!externalGadgets.isEmpty())
        {
            for (URI uri : externalGadgets.keySet())
            {
                rules.add(uri.normalize().toASCIIString().toLowerCase() + "/*");
            }
        }
        whitelistManager.updateRules(rules, false);
    }

    @Override
    public String getShortDescription()
    {
        return "Configuring whitelist entries for all external gadgets";
    }

    @Override
    public String getBuildNumber()
    {
        return "611";
    }
}