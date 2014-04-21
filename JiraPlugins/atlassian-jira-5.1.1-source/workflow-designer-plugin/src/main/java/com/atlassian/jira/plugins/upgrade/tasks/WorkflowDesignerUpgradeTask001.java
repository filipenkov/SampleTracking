package com.atlassian.jira.plugins.upgrade.tasks;

import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.workflows.layout.persistence.WorkflowLayoutPropertyKeyBuilder;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.opensymphony.module.propertyset.PropertySet;
import com.sysbliss.jira.plugins.workflow.BuildInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;

import static com.atlassian.jira.workflows.layout.persistence.WorkflowLayoutPropertyKeyBuilder.WorkflowState;

/**
 * Upgrades the workflow designer data to build number 1.
 *
 * <p>The upgrade consists in migrating all the existing workflow layouts from property entries that followed the
 * formats:
 * <ul>
 *      <li><code>jira.jwd.layout:<strong>workflow-name</strong></code> and;</li>
 *      <li><code>jira.jwd.draft.layout:<strong>workflow-name</strong></code></li>
 * </ul>
 *
 * to the formats:

 * <ul>
 *      <li><code>jira.workflow.layout:<strong>md5sum(workflow-name)</strong></code> and;</li>
 *      <li><code>jira.workflow.draft.layout:<strong>msd5sum(workflow-name)</strong></code></li>
 * </ul>
 * </p>
 * <p>
 * We now append the <em>md5sum</em> of the workflow name in order to ensure that the property key will fit in the
 * database column used by JIRA to store the property name.
 * </p>
 * @since v5.1
 */
public class WorkflowDesignerUpgradeTask001 implements PluginUpgradeTask
{
    private static final Logger log = Logger.getLogger(WorkflowDesignerUpgradeTask001.class);
    private final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration;

    public WorkflowDesignerUpgradeTask001(final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration)
    {
        this.legacyWorkflowLayoutPropertyKeyMigration = legacyWorkflowLayoutPropertyKeyMigration;
    }

    public int getBuildNumber()
    {
        return 1;
    }

    public String getShortDescription()
    {
        return "Migrating workflow layouts to a new storage format.";
    }

    public Collection<Message> doUpgrade() throws Exception
    {
        legacyWorkflowLayoutPropertyKeyMigration.perform();
        return null;
    }

    public String getPluginKey()
    {
        return BuildInfo.PLUGIN_KEY;
    }

    public static class LegacyWorkflowLayoutPropertyKeyMigration
    {
        private final PropertySet workflowDesignerPropertySet;

        public LegacyWorkflowLayoutPropertyKeyMigration(final JiraPropertySetFactory jiraPropertySetFactory)
        {
            workflowDesignerPropertySet = jiraPropertySetFactory.buildNoncachingPropertySet(BuildInfo.PLUGIN_KEY);
        }

        public void perform()
        {
            try
            {
                final Collection<String> liveKeys = workflowDesignerPropertySet.getKeys(LegacyLayoutKeys.Live.prefix());
                migrate(liveKeys, LegacyLayoutKeys.Live.prefix(), LegacyLayoutKeys.Live.state());

                final Collection<String> draftKeys = workflowDesignerPropertySet.getKeys(LegacyLayoutKeys.Draft.prefix());
                migrate(draftKeys, LegacyLayoutKeys.Draft.prefix(), LegacyLayoutKeys.Draft.state());
            }
            catch (final Exception anException)
            {
                log.error
                        (
                                "An error occurred while trying to migrate the existing workflow layouts to the new "
                                        + "md5sum based storage format.",
                                 anException
                        );
            }
        }

        private void migrate(final Iterable<String> legacyLayoutPropertyKeys, final String keyPrefix, final WorkflowState workflowState)
        {
            for (final String legacyLayoutKey : legacyLayoutPropertyKeys)
            {
                final String workflowName = StringUtils.removeStart(legacyLayoutKey, keyPrefix);
                final String newLayoutKey =
                        WorkflowLayoutPropertyKeyBuilder.newBuilder().
                                setWorkflowName(workflowName).
                                setWorkflowState(workflowState).
                                build();
                workflowDesignerPropertySet.setText(newLayoutKey, workflowDesignerPropertySet.getText(legacyLayoutKey));
                workflowDesignerPropertySet.remove(legacyLayoutKey);
            }
        }

        private static class LegacyLayoutKeys
        {
            private static class Live
            {
                private static String prefix() {return "jira.jwd.layout:";}

                private static WorkflowState state() {return WorkflowState.LIVE;}
            }

            private static class Draft
            {
                private static String prefix() {return "jira.jwd.draft.layout:";}

                private static WorkflowState state() {return WorkflowState.DRAFT;}
            }
        }
    }
}
