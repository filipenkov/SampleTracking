package org.jcvi.jira.plugins.customfield.additionalsearch.config;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;

/**
 * Part of the Custom Field configuration screen.
 *
 * The object from this class represents a collection of properties
 * that can be configured for this custom field. Each property is
 * represented by a SearchConfigParameter object.
 *
 * It is used with the standard configuration velocity template to output
 * the names and values of the config.
 * @see CFConfigItem
 */
public class AdditionalSearchFieldConfigItem extends CFConfigItem {
    public AdditionalSearchFieldConfigItem(CustomFieldTypeModuleDescriptor descriptor, GenericConfigManager configManager) {
        super(descriptor,configManager);
    }

    @Override
    public AdditionalSearchFieldConfigParameters[] getConfigurableProperties() {
        return AdditionalSearchFieldConfigParameters.values();
    }

    @Override
    public String getName() {
        return "CFConfigItem.getName()";
        //DOC return "Field Copy Configuration";
    }

    // --------------------------------------------------------------------
    //                          config page
    // --------------------------------------------------------------------
    // The target for the link on the Configure Custom Field page
    @Override
    protected String getActionAlias() {
        return "AdditionalSearchFieldConfig";
    }

    // --------------------------------------------------------------------
    //                          velocity context
    // --------------------------------------------------------------------
    //
    //key (to get the configuration object in velocity use ${key})
    public String getObjectKey() {
        return "config";
    }

    //Used on the select configuration page, the one with set default as
    //an option on it
    @Override
    public String getDisplayNameKey() {
        return "CFConfigItem.getDisplayNameKey()";
        //DOC return "Additional Search Field Configuration";
    }
}
