package com.atlassian.jira.plugins.importer.upgrade;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.plugins.importer.customfields.SupportedCustomFieldPredicate;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FixCustomFieldsUpgradeTask implements PluginUpgradeTask {
    private static final Logger log = Logger.getLogger(FixCustomFieldsUpgradeTask.class);

    private final CustomFieldManager customFieldManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final JiraContextTreeManager jiraContextTreeManager;

    public FixCustomFieldsUpgradeTask(CustomFieldManager customFieldManager, FieldConfigSchemeManager fieldConfigSchemeManager) {
        this.customFieldManager = customFieldManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.jiraContextTreeManager = ComponentManager.getComponentInstanceOfType(JiraContextTreeManager.class);
    }

    @Override
    public int getBuildNumber() {
        return 1;
    }

    @Override
    public String getShortDescription() {
        return "Fix Custom Fields that were broken by previous version of this plugin";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        Logger.getLogger("com.atlassian.jira.plugins.importer.upgrade").setLevel(Level.INFO);
        UpgradeUtils.logUpgradeTaskStart(this, log);

        final Iterable<CustomField> customFields = Iterables.filter(customFieldManager.getCustomFieldObjects(), new SupportedCustomFieldPredicate());
        for(CustomField customField : customFields) {
            final List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
            if (schemes.isEmpty() || schemes.size() > 1) {
                continue;
            }
            final FieldConfigScheme scheme = Iterables.getOnlyElement(schemes);
            if(!scheme.isBasicMode()) {
                final Map<String, FieldConfig> issueTypes = Maps.newHashMap(scheme.getConfigs());
                if (issueTypes.isEmpty()) {
                    continue;
                }
                final FieldConfig config = Iterables.getFirst(issueTypes.values(), null);
                for(String issueTypeId : issueTypes.keySet()) {
                    issueTypes.put(issueTypeId, config);
                }

                log.warn(String.format("Custom field '%s' has too many configurations associated with a scheme, fixing it.", customField.getName()));

                final FieldConfigScheme configScheme = new FieldConfigScheme.Builder(scheme).setConfigs(issueTypes).toFieldConfigScheme();
                fieldConfigSchemeManager.updateFieldConfigScheme(configScheme, Collections.singletonList(jiraContextTreeManager.getRootNode()), customField);
            }
        }
        customFieldManager.refresh();

        UpgradeUtils.logUpgradeTaskEnd(this, log);
        return null;
    }

    @Override
    public String getPluginKey() {
        return "com.atlassian.jira.plugins.jira-importers-plugin";
    }
}
