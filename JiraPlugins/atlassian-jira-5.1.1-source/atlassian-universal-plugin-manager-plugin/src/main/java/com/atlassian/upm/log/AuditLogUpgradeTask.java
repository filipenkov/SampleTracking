package com.atlassian.upm.log;

import java.util.Collection;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.impl.NamespacedPluginSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.log.PluginSettingsAuditLogService.KEY_PREFIX;
import static com.atlassian.upm.log.PluginSettingsAuditLogService.UPM_AUDIT_LOG;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * New to UPM 1.6 was the exposure of UPM's Audit Log through a new UPM API. Part of this was adding
 * an "entryType" element to {@link com.atlassian.upm.api.log.AuditLogEntry}.
 *
 * However, administrators who upgraded their bundled UPMs to 1.6 saw a benign exception upon system startup.
 * The bundled UPM (1.5 or earlier) could not parse the serialized "entryType" element and threw an exception
 * before UPM 1.6 had a chance to start up. This issue is tracked by UPM-1350.
 *
 * This upgrade task is to work around this issue.
 *
 * @since  1.6.2
 */
public class AuditLogUpgradeTask implements PluginUpgradeTask
{
    private static final Logger log = LoggerFactory.getLogger(AuditLogUpgradeTask.class);
    private static final String UPM_AUDIT_LOG_LEGACY = "upm_audit_log";

    private final PluginSettingsFactory pluginSettingsFactory;
    private final PluginAccessorAndController pluginAccessorAndController;

    public AuditLogUpgradeTask(PluginSettingsFactory pluginSettingsFactory, PluginAccessorAndController pluginAccessorAndController)
    {
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory, "pluginSettingsFactory");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
    }

    /**
     * The build number for this upgrade task. Once this upgrade task has run the plugin manager will store this
     * build number against this plugin type.  After this only upgrade tasks with higher build numbers will be run
     */
    public int getBuildNumber()
    {
        return 1;
    }

    public String getShortDescription()
    {
        return "Upgrades audit log to be compatible with UPM 1.6+";
    }

    public Collection<Message> doUpgrade() throws Exception
    {
        log.info("Running UPM Audit Log upgrade task");
        Object auditLog = getPluginSettings().get(UPM_AUDIT_LOG_LEGACY);
        if (auditLog != null)
        {
            log.debug("Migrating UPM Audit Log to v2");
            getPluginSettings().put(UPM_AUDIT_LOG, auditLog);
            getPluginSettings().remove(UPM_AUDIT_LOG_LEGACY);
        }
        return null;
    }

    /**
     * Identifies the plugin that will be upgraded.
     */
    public String getPluginKey()
    {
        return pluginAccessorAndController.getUpmPluginKey();
    }

    private PluginSettings getPluginSettings()
    {
        return new NamespacedPluginSettings(pluginSettingsFactory.createGlobalSettings(), KEY_PREFIX);
    }
}
