/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class UpgradeTask_Build133 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build133.class);
    private OfBizDelegator delegator;

    public UpgradeTask_Build133(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    public String getBuildNumber()
    {
        return "133";
    }

    public String getShortDescription()
    {
        return "Remove bad portlet properties";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        OfBizListIterator listIterator = null;

        try
        {
            // Retrieve all portlet configurations through the findListIteratorByCondition method
            listIterator = delegator.findListIteratorByCondition("PortletConfiguration", null);
            GenericValue portletConfig = (GenericValue) listIterator.next();

            while (portletConfig != null)
            {
                log.debug("Removing bad PortletConfig properties for portletconf with id: " + portletConfig.getLong("id"));
                PropertySet ofbizPs = OFBizPropertyUtils.getPropertySet(portletConfig);
                if (ofbizPs.exists("template"))
                    ofbizPs.remove("template");
                if (ofbizPs.exists("description"))
                    ofbizPs.remove("description");

                portletConfig = (GenericValue) listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                // Close the iterator
                listIterator.close();
            }

        }
    }
}