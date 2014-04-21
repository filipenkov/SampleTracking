package org.jcvi.jira.plugins.customfield.additionalsearch.config;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import org.jcvi.jira.plugins.customfield.additionalsearch.AbstractAdditionalSearchFieldType;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigAction;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part of the Custom Field configuration screen.
 * The Velocity style Action for the Custom Field configuration page.
 *
 * Objects from this class represent a single entry in a Configuration Scheme
 * on the 'Configure Custom Field' page. The actual 'ConfigurableParameter'
 * can contain multiple 'CFConfigItem's.
 *

 * AdditionalSearchFieldConfigParameters
 *
 * {@see NopConfigAction}
 */
public class AdditionalSearchFieldAction extends CFConfigAction {
    private final CustomFieldManager customFieldManager;

    public AdditionalSearchFieldAction(CustomFieldManager fieldManager) {
        this.customFieldManager = fieldManager;
    }

    @Override
    public AdditionalSearchFieldConfigParameters[] getConfigurableParameters() {
        return AdditionalSearchFieldConfigParameters.values();
    }

    @Override
    public CFConfigItem getConfigItem() {
        return new AdditionalSearchFieldConfigItem(getDescriptor(),getGenericConfigManager());
    }

    @Override
    protected String getSessionKey() {
        //The same session key is used for both Number and Text AdditionalSearchFields
        return AbstractAdditionalSearchFieldType.class.toString();
    }

    //bean method
    public Map<String,String> getCustomFields(long projectId) {
        List<CustomField> allFieldsInProject =
                    customFieldManager.getCustomFieldObjects(projectId,
                    com.atlassian.jira.config.ConstantsManager.ALL_ISSUE_TYPES);
        Map<String,String> fieldNamesAndIDs = new HashMap<String,String>();
        for (CustomField field : allFieldsInProject) {
            fieldNamesAndIDs.put(field.getId(),field.getName());
        }
        return fieldNamesAndIDs;
    }
}
