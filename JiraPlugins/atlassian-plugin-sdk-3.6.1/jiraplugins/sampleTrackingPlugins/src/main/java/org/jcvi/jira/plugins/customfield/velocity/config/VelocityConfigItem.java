package org.jcvi.jira.plugins.customfield.velocity.config;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;
import org.jcvi.jira.plugins.customfield.velocity.TemplateType;

/**
 * Created by IntelliJ IDEA.
 * User: pedworth
 * Date: 8/18/11
 */
public class VelocityConfigItem extends CFConfigItem {
    //empty constructor
    public VelocityConfigItem(CustomFieldTypeModuleDescriptor descriptor,
                              GenericConfigManager configManager) {
        super(descriptor,configManager);
    }

    @Override
    public TemplateType[] getConfigurableProperties() {
        return TemplateType.values();
    }

    @Override
    public String getName() {
        return "Velocity Field";
    }

    // --------------------------------------------------------------------
    //                          config page
    // --------------------------------------------------------------------
    // The target for the link on the Configure Custom Field page
    @Override
    protected String getActionAlias() {
        return "EditVelocityConfig";
    }

    // --------------------------------------------------------------------
    //                          velocity context
    // --------------------------------------------------------------------
    //
    //key (to get the configuration object in velocity use ${key})
    public String getObjectKey() {
        return "config";
    }

    /**
     * This is used to generate the LEFT column and part of the title and
     * contents of the link in the RIGHT column.
     * @return a String identifying this form of configuration information
     */
    @Override
    public String getDisplayNameKey() {
        return "Templates";
    }

    // --------------------------------------------------------------------
    //                          config summary view
    // --------------------------------------------------------------------

    //use CFConfigItem
//    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem) {
//        String result = "";
//        JiraConfig config = new JiraConfig(fieldConfig);
//
//        for(TemplateType template : TemplateType.values()) {
//            result += "<p>"+template.getDisplayName()+": ";
//            String value = config.retrieveStoredValue(template.name());
//            if (value != null && value.trim().length() > 0) {
//                result += SET;
//            } else {
//                result += NOT_SET;
//            }
//        }
//        return result;
//    }

}
