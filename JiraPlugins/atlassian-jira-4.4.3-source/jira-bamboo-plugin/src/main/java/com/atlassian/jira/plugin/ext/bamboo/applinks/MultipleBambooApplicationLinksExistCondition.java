package com.atlassian.jira.plugin.ext.bamboo.applinks;

import java.util.Map;

import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import org.apache.log4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Condition that passes if multiple Bamboo application links exist.
 */
public class MultipleBambooApplicationLinksExistCondition implements Condition
{
    private static final Logger log = Logger.getLogger(MultipleBambooApplicationLinksExistCondition.class);
    private static final Class<BambooApplicationType> BAMBOO_TYPE = BambooApplicationType.class;

    private final BambooApplicationLinkManager applinkManager;

    public MultipleBambooApplicationLinksExistCondition(BambooApplicationLinkManager applinkManager)
    {
        this.applinkManager = checkNotNull(applinkManager, "applinkManager");
    }

    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        return applinkManager.getApplicationLinkCount() > 1;
    }
}
