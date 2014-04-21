package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Submittable;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.Dialog;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public interface InstallPluginDialog extends Dialog<InstallPluginDialog>, Submittable<InstallPluginDialog>
{
    InstallPluginDialog setFilePath(String absolutePath);
}
