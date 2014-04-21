package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.util.UpgradeUtils;
import org.apache.commons.collections.Transformer;

public class UpgradeTask_Build100 extends AbstractUpgradeTask
{
    private final UpgradeUtils upgradeUtils;
    private final CustomFieldManager customFieldManager;

    public UpgradeTask_Build100(UpgradeUtils upgradeUtils, CustomFieldManager customFieldManager)
    {
        this.upgradeUtils = upgradeUtils;
        this.customFieldManager = customFieldManager;
    }

    public String getBuildNumber()
    {
        return "100";
    }

    public String getShortDescription()
    {
        return "Changing the cofig tables to use new fields";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        upgradeUtils.transformTableColumn("FieldConfigScheme", "customfield", "fieldid", new CustomFieldIdExpander());
        upgradeUtils.clearColumn("FieldConfigScheme", "customfield");
        upgradeUtils.transformTableColumn("FieldConfiguration", "customfield", "fieldid", new CustomFieldIdExpander());
        upgradeUtils.clearColumn("FieldConfiguration", "customfield");

        customFieldManager.refresh();   
    }

    private static class CustomFieldIdExpander implements Transformer
    {
        public Object transform(Object input)
        {
            if (input != null)
            {
                Long customFieldId = (Long) input;
                return "customfield_" + customFieldId;
            }
            else
            {
                return null;
            }
        }
    }
}
