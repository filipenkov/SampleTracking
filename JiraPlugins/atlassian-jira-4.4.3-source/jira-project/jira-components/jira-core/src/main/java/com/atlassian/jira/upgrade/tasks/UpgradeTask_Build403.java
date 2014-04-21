package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Upgrade task to ensure that all colors specified for the priorities are valid.  I.e. start with a #
 *
 * @since v4.0
 */
public class UpgradeTask_Build403 extends AbstractUpgradeTask
{

    private final ConstantsManager constantsManager;
    private static final String VALID_HEX_CHARS = "0123456789ABCDEFabcdef";
    private static final String STATUS_COLOR = "statusColor";

    public UpgradeTask_Build403(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "403";
    }

    @Override
    public String getShortDescription()
    {
        return "Ensuring priority colours are valid html colours";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Collection<GenericValue> priorites = constantsManager.getPriorities();
        for (GenericValue priority : priorites)
        {
            final String color = priority.getString(STATUS_COLOR);
            if (color != null && color.length() == 6 && isHexString(color))
            {
                priority.setString(STATUS_COLOR, "#" + color);
                priority.store();
            }
        }

    }

    private boolean isHexString(String str)
    {
        for (int i = 0; i < str.length(); i++)
        {
            if (!isHexChar(str.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isHexChar(char c)
    {
        return VALID_HEX_CHARS.contains(Character.toString(c));
    }
}
