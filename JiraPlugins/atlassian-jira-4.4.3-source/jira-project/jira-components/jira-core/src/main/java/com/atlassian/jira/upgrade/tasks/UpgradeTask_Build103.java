package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

public class UpgradeTask_Build103 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build103.class);

    private final GenericDelegator delegator;
    private static final String OLD_CUSTOMFIELD_SELECT_PARAM = "com.atlassian.jira.issue.search.parameters.lucene.CustomFieldSelectParameter";

    public UpgradeTask_Build103(GenericDelegator delegator)
    {
        this.delegator = delegator;
    }

    public String getBuildNumber()
    {
        return "103";
    }

    public String getShortDescription()
    {
        return "Upgrades the search requests from 2.5.3 format to 3.0 format (a little late)";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        List requests = delegator.findAll("SearchRequest");

        for (Iterator iterator1 = requests.iterator(); iterator1.hasNext();)
        {
            GenericValue searchRequestGv = (GenericValue) iterator1.next();
            String xml = searchRequestGv.getString("request");
            if (StringUtils.contains(xml, OLD_CUSTOMFIELD_SELECT_PARAM))
            {
                xml = StringUtils.replace(xml,
                                    OLD_CUSTOMFIELD_SELECT_PARAM,
                                    "com.atlassian.jira.issue.search.parameters.lucene.StringParameter");
                searchRequestGv.setString("request", xml);
                searchRequestGv.store();
                log.info("Upgraded search request " + searchRequestGv.getString("name"));
            }
        }
    }
}


