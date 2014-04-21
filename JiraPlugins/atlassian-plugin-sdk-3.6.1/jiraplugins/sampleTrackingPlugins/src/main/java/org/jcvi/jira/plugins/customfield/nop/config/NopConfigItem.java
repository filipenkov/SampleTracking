package org.jcvi.jira.plugins.customfield.nop.config;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;

/**
 * Provides the customization of JIRAs configuration system
 */
public class NopConfigItem extends CFConfigItem {
    public NopConfigItem(CustomFieldTypeModuleDescriptor descriptor, GenericConfigManager configManager) {
        super(descriptor, configManager);
    }

    @Override
    public NopConfigurationParameters[] getConfigurableProperties() {
        return NopConfigurationParameters.values();
    }

    @Override
    public String getName() {
        return "Nop Configuration Type";
    }

    // --------------------------------------------------------------------
    //                          config page
    // --------------------------------------------------------------------
    // The target for the link on the Configure Custom Field page
    @Override
    protected String getActionAlias() {
        //use the standard page
        return "EditCFConfig";
    }

    // --------------------------------------------------------------------
    //                          velocity context
    // --------------------------------------------------------------------
    //
    //key (to get the configuration object in velocity use ${key})
    public String getObjectKey() {
        return "config";
    }

    @Override
    public String getDisplayNameKey() {
        return "Nop Configuration Item";
    }
}
