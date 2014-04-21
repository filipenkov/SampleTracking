package org.jcvi.jira.plugins.config;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import org.apache.log4j.Logger;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;
import org.jcvi.jira.plugins.customfield.shared.config.ConfigurationParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * User: pedworth
 * Date: 11/16/11
 * <p>Provides an interface to GenericConfigManager, which can be used instead
 * of a PropertiesSet to store the configuration. The main advantage is that
 * there is no length limit. (PropertiesSets are limited to 255 chars per
 * entry)</p>
 */
public class ConfigManagerStore {
    private static final Logger log = Logger.getLogger(ConfigManagerStore.class);

    private final GenericConfigManager configManager;
    private final String configurationTypeId;
    private final String fieldId;
    private final ConfigurationParameter[] allFields;

    public ConfigManagerStore(GenericConfigManager manager,
                              FieldConfig config,
                              String configStoreId,
                              ConfigurationParameter[] params) {

        this.configManager       = manager;
        if (config != null) {
            this.fieldId             = config.getId().toString();
        } else {
                        //fake a stack-trace
            try {
                throw new IllegalStateException();
            } catch (IllegalStateException ise) {
                log.error("Null fieldId",ise);
            }
            this.fieldId = null;
        }
        this.configurationTypeId = configStoreId;
        this.allFields           = params;

        if (configurationTypeId.contains(":")) {
          throw new AssertionError(
           "The value returned by getSettingsStorageKey must not contain ':'");
        }
    }

    public ConfigManagerStore(GenericConfigManager manager,
                              FieldConfig config,
                              CFConfigItem configType) {
        this(manager,
             config,
             configType.getSettingsStorageKey(),
             configType.getConfigurableProperties());
    }

    public String retrieveStoredValue(ConfigurationParameter configField) {
        String key = getStorageField(configField.getStorageKey());
        try {
            Object dbValue = configManager.retrieve(key,fieldId);
            if (dbValue != null) {
                if (dbValue instanceof String) {
                    return (String)dbValue;
                } else {
                    log.error("Incorrect value stored for configuration field: "+
                            key+" the value was "+dbValue.toString());
                }
            }
            //not null and not a string, drop through
        } catch (FieldValidationException e) {
            log.error("Incorrect value stored for configuration field: "+key, e);
        }
        return null;
    }

    public void storeValue(ConfigurationParameter configField, String value) {
        String key = getStorageField(configField.getStorageKey());
        configManager.update(key,fieldId,value);
    }

    public Map<ConfigurationParameter,String> getAllContextsValues() {
        Map<ConfigurationParameter,String> fields
                            = new HashMap<ConfigurationParameter,String>();
        for(ConfigurationParameter prop : allFields) {
            String value = retrieveStoredValue(prop);
            if (value != null) {
                fields.put(prop,value);
            }
        }
        return fields;
    }

    private String getStorageField(String fieldName) {
        if (fieldName.indexOf(":") >= 0) {
            throw new AssertionError("FieldNames must not contain ':'");
        }
        return configurationTypeId+":"+fieldName;
    }
}
