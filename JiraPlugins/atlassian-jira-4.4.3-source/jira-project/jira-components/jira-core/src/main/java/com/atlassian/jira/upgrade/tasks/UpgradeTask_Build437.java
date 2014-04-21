package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.OfBizPortalPageStore;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

public class UpgradeTask_Build437 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build437.class);

    private final OfBizDelegator delegator;
    private final PortalPageStore portalPageStore;

    public UpgradeTask_Build437(final OfBizDelegator delegator, final PortalPageStore portalPageStore)
    {
        this.delegator = delegator;
        this.portalPageStore = portalPageStore;
    }

    public String getBuildNumber()
    {
        return "437";
    }

    public String getShortDescription()
    {
        return "Initialising dashboard version numbers for optimistic locking.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        //find all portalpages that have no version set yet.
        final OfBizListIterator iterator = delegator.findListIteratorByCondition(OfBizPortalPageStore.Table.NAME,
                new EntityExpr(OfBizPortalPageStore.Column.VERSION, EntityOperator.EQUALS, null));
        final List<Long> portalPageIds = new ArrayList<Long>();
        try
        {
            GenericValue portalPageGv = iterator.next();
            while (portalPageGv != null)
            {
                portalPageIds.add(portalPageGv.getLong(OfBizPortalPageStore.Column.ID));
                portalPageGv = iterator.next();
            }
        }
        finally
        {
            iterator.close();
        }

        try
        {
            //set version to 0 for all dashboard pages returned by the previous query.
            final int rowsUpdated = delegator.bulkUpdateByPrimaryKey(OfBizPortalPageStore.Table.NAME,
                    MapBuilder.<String, Object>newBuilder().add(OfBizPortalPageStore.Column.VERSION, 0L).toMap(),
                    portalPageIds);
            log.info("Initialised " + rowsUpdated + " dashboard versions to 0.");
        }
        finally
        {
            portalPageStore.flush();
        }
    }
}