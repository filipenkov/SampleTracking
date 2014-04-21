package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

/**
 * Forces any resolution fieldLayoutItems that have required set to true to be false instead.
 */
public class UpgradeTask_Build104 extends AbstractUpgradeTask
{
    private GenericDelegator delegator;

    public UpgradeTask_Build104(GenericDelegator delegator)
    {
        this.delegator = delegator;
    }

    public String getShortDescription()
    {
        return "Forces any resolution fieldLayoutItems that have required set to true to be false instead.";
    }

    public String getBuildNumber()
    {
        return "104";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        boolean modified = false;
        // get all the field layout items for the resolution field
        List resolutions = delegator.findByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", IssueFieldConstants.RESOLUTION));
        for (Iterator iterator = resolutions.iterator(); iterator.hasNext();)
        {
            GenericValue fieldLayoutItem = (GenericValue) iterator.next();
            if("true".equals(fieldLayoutItem.getString("isrequired")))
            {
                fieldLayoutItem.set("isrequired", "false");
                fieldLayoutItem.store();
                modified = true;
            }
        }
        if(modified)
        {
            // clear the cache if we modified any fieldLayoutItems
            ComponentAccessor.getFieldLayoutManager().refresh();
        }
    }
}
