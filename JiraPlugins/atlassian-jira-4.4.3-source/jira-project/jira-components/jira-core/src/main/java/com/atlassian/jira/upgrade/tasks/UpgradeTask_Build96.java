package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.util.SearchRequestConverter;
import com.atlassian.jira.upgrade.util.XsltSearchRequestTransformer;

public class UpgradeTask_Build96 extends AbstractSearchRequestUpgradeTask
{
    protected final SearchRequestConverter converter;

    public UpgradeTask_Build96(OfBizDelegator delegator)
    {
        super(delegator);
        converter = new XsltSearchRequestTransformer("upgrade_build96_searchrequest.xsl");
    }

    public String getBuildNumber()
    {
        return "96";
    }

    public String getShortDescription()
    {
        return "Upgrade search request XML to use a single RelativeDateRangeParameter object rather than multiple date period params.";
    }

    protected SearchRequestConverter getConverter()
    {
        return converter;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        super.doUpgrade(setupMode);
    }
}
