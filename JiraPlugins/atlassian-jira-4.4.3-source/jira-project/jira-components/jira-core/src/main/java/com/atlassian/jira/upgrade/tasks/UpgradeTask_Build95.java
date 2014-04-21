package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.util.SearchRequestConverter;
import com.atlassian.jira.upgrade.util.XsltSearchRequestTransformer;

public class UpgradeTask_Build95 extends AbstractSearchRequestUpgradeTask
{
    protected final SearchRequestConverter converter;

    public UpgradeTask_Build95(OfBizDelegator delegator)
    {
        super(delegator);
        converter = new XsltSearchRequestTransformer("upgrade_build95_searchrequest.xsl");
    }

    public String getBuildNumber()
    {
        return "95";
    }

    public String getShortDescription()
    {
        return "Upgrade search request XML to use a single AbsoluteDateRangeParameter.class object rather than multiple date params.";
    }

    protected SearchRequestConverter getConverter()
    {
        return converter;
    }
}
