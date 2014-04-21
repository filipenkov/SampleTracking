package com.atlassian.crowd.embedded.admin.condition;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Web Fragment condition that helps show hide the migrate users link.
 * Returns true if there is > 1 internal or delegated auth directory.
 *
 * @since v1.4.3
 */
public class EnableUserMigrationCondition implements Condition
{
    private final CrowdDirectoryService crowdDirectoryService;

    public EnableUserMigrationCondition(final CrowdDirectoryService crowdDirectoryService)
    {
        this.crowdDirectoryService = crowdDirectoryService;
    }

    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        int count = 0;
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (directory.getType() == DirectoryType.INTERNAL || directory.getType() == DirectoryType.DELEGATING)
            {
                count++;
            }
        }
        return count > 1;
    }
}
