package com.atlassian.upm.conditions;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.upm.permission.UpmVisibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determine if a web item should be displayed
 */
public class IsPermittedToView implements Condition
{
    private static final Logger log = LoggerFactory.getLogger(IsPermittedToView.class);
    private String page;
    private UpmVisibility visibility;

    public IsPermittedToView(UpmVisibility visibility)
    {
        this.visibility = visibility;
    }

    public void init(final Map<String, String> paramMap) throws PluginParseException
    {
        page = paramMap.get("page");
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        if ("audit-log".equals(page))
        {
            return visibility.isAuditLogVisible();
        }
        else if ("update-check".equals(page))
        {
            return visibility.isCompatibilityVisible();
        }
        else if ("plugins".equals(page))
        {
            return visibility.isManageExistingVisible();
        }

        log.warn("Permission requested for unknown page '" + page + ".'");
        return false;
    }
}