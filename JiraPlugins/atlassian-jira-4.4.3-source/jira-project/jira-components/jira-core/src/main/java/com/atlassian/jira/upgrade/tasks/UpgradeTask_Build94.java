package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.util.SearchRequestConverter;
import com.atlassian.jira.upgrade.util.UsersGroupParamConverter;

public class UpgradeTask_Build94 extends AbstractSearchRequestUpgradeTask
{
    protected final SearchRequestConverter converter;

    public UpgradeTask_Build94(OfBizDelegator delegator)
    {
        super(delegator);
        converter = new UsersGroupParamConverter();
    }

    public String getBuildNumber()
    {
        return "94";
    }

    public String getShortDescription()
    {
        return "Upgrade search request XML new format which allows dynamically loading of the group parameter every time";
    }

    protected SearchRequestConverter getConverter()
    {
        return converter;
    }
}
