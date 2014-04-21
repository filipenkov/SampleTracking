package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.EasyList;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Generate an initial vote history on upgrade
 *
 * @since v4.4
 */
public class UpgradeTask_Build638 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build638.class);
    public static final String ISSUE_ENTITY = "Issue";
    public static final String VOTE_HISTORY_ENTITY = "VoteHistory";

    OfBizDelegator ofBizDelegator;

    public UpgradeTask_Build638(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public String getBuildNumber()
    {
    return "638";
    }

    public String getShortDescription()
    {
        return "Initialising the vote history.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        Timestamp now = new Timestamp(new Date().getTime());
        final List<GenericValue> issues = ofBizDelegator.findByCondition(ISSUE_ENTITY,
                new EntityExpr("votes", EntityOperator.GREATER_THAN, Long.valueOf(0)), EasyList.build("id", "votes"));

        for (GenericValue issue : issues)
        {
            ofBizDelegator.createValue(VOTE_HISTORY_ENTITY,
                    EasyMap.build("issue", issue.getLong("id"), "votes", issue.getLong("votes"), "timestamp", now));
        }
    }

}
