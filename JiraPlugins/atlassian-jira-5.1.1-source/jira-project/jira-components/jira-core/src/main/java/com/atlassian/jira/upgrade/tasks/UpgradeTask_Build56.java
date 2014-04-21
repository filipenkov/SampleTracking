/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import org.ofbiz.core.entity.GenericEntity;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpgradeTask_Build56 extends AbstractReindexUpgradeTask
{
    private static final String UPGRADE_SYSTEM_PROP = "jira.upgrade.build56.override";
    public static final String ERROR_MESSAGE_STATUSES = "Could not upgrade due to non default statuses. Please contact " + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.support.mail.to") + " for more information.";
    public static final String ERROR_MESSAGE_WORKFLOW = "Could not upgrade due workflow inconsistencies.";
    private static final String STATUS_UNASSIGNED = "1";
    private static final String STATUS_ASSIGNED = "2";

    private final OfBizDelegator delegator;


    public UpgradeTask_Build56(OfBizDelegator delegator)
    {
        super();
        this.delegator = delegator;
    }

    public String getBuildNumber()
    {
        return "56";
    }

    public String getShortDescription()
    {
        return "Rename unassigned to open, move assigned issues to open and remove assigned status";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Check that unassigned and assigned status are still the same as the defaults
        GenericValue unassigned = ManagerFactory.getConstantsManager().getStatus(STATUS_UNASSIGNED);
        GenericValue assigned = ManagerFactory.getConstantsManager().getStatus(STATUS_ASSIGNED);

        if (!"Unassigned".equals(unassigned.getString("name")) || assigned == null || !"Assigned".equals(assigned.getString("name")))
        {
            // Check that the upgrade has not been done already
            if (!"Open".equals(unassigned.getString("name")))
            {
                if (!Boolean.getBoolean(UPGRADE_SYSTEM_PROP))
                {
                    throw new Exception("Could not upgrade due to non default statuses ('" + unassigned.getString("name") + "' instead of 'Unassigned'; '" + assigned.getString("name") + "' instead of 'Assigned'). If these are logically equivalent you may force the upgrade by restarting JIRA with -D" + UPGRADE_SYSTEM_PROP + "=true. Otherwise please contact " + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.support.mail.to") + " for more information.");
                }
            }
        }

        // Rename 'unassigned' status to 'open'
        unassigned.set("name", "Open");
        unassigned.set("description", "The issue is open and ready for the assignee to start work on it.");
        unassigned.set("iconurl", "/images/icons/status_open.gif");
        unassigned.store();

        // Move assigned issues to open status
        GenericEntity workflowEntry;
        GenericEntity currentStep;
        List byAnd = delegator.findByAnd("Issue", EasyMap.build("status", STATUS_ASSIGNED));
        List storing = new ArrayList();
        for (Iterator iterator = byAnd.iterator(); iterator.hasNext();)
        {
            GenericValue issue = (GenericValue) iterator.next();
            issue.set("status", STATUS_UNASSIGNED);
            storing.add(issue);
            List workflowEntries = delegator.findByAnd("OSWorkflowEntry", EasyMap.build("id", issue.getLong("workflowId")));
            for (int i = 0; i < workflowEntries.size(); i++)
            {
                workflowEntry = (GenericEntity) workflowEntries.get(i);
                if (workflowEntry != null)
                {
                    List currentSteps = delegator.findByAnd("OSCurrentStep", EasyMap.build("entryId", workflowEntry.getLong("id")));
                    for (int j = 0; j < currentSteps.size(); j++)
                    {
                        currentStep = (GenericEntity) currentSteps.get(j);
                        if (currentStep != null)
                        {
                            currentStep.set("stepId", new Integer(1));
                            storing.add(currentStep);
                        }
                    }
                }
            }
        }

        delegator.storeAll(storing);

        // Remove the assigned status
        if (assigned != null)
            assigned.remove();

        //one status has been removed, another renamed - flush the caches
        ManagerFactory.getConstantsManager().refreshStatuses();

        // Reindex all the data
        super.doUpgrade(setupMode);
    }
}
