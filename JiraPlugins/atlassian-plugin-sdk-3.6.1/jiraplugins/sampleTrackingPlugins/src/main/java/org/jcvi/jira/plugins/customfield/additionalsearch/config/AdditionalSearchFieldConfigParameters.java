package org.jcvi.jira.plugins.customfield.additionalsearch.config;

import com.atlassian.jira.issue.fields.NavigableField;
import org.jcvi.jira.plugins.customfield.shared.config.ConfigInputType;
import org.jcvi.jira.plugins.customfield.shared.config.ConfigurationParameter;
import org.jcvi.jira.plugins.customfield.shared.config.SelectOption;
import org.jcvi.jira.plugins.utils.JIRAFieldUtils;

import java.util.Collection;

/**
 * Part of the Custom Field configuration screen.
 *
 * The parameters
 * "FIELD_TO_COPY"  the field to copy the value from
 */
public enum AdditionalSearchFieldConfigParameters implements ConfigurationParameter {
    //UnusedDeclaration suppressed as they are used, but via enum.values()
    FIELD_TO_COPY(ConfigInputType.TEXTAREA, "FIELD_TO_COPY","Field to add Search to") {
        public SelectOption[] getPossibleValues() {
               Collection<NavigableField> fields = JIRAFieldUtils.getFields();
               SelectOption[] options = new SelectOption[fields.size()];
               int i = 0;
               for (NavigableField field : fields) {
                   options[i++] = new SelectOption(field.getNameKey(),field.getName());
               }
               return options;
           }
    },
    MESSAGE(ConfigInputType.MESSAGE,   "NONE","Source Issue Selection Order") {
        public SelectOption[] getPossibleValues() {
            return null;
        }
        @Override
        public boolean isDisplayedInView() {
            return false;
        }
    },
    FIRST(ConfigInputType.SELECT,   "FIRST","First Choice") {
        public SelectOption[] getPossibleValues() {
            return new SelectOption[] {
                new SelectOption("self","Self - Copy from the same Issue"),
                new SelectOption("parent","Parent - If the Issue is a sub-task copy from the parent"),
            };
        }
    },
    MESSAGE2(ConfigInputType.MESSAGE,   "NONE2","Second Choice, used if the first choice source has no value for the field") {
        public SelectOption[] getPossibleValues() {
            return null;
        }
        @Override
        public boolean isDisplayedInView() {
            return false;
        }
    },
    SECOND(ConfigInputType.SELECT,   "SECOND","Second Choice") {
        public SelectOption[] getPossibleValues() {
            return new SelectOption[] {
                new SelectOption("none","None - only use the first choice"),
                new SelectOption("self","Self"),
                new SelectOption("parent","Parent")
            };
        }
    };


    private final String key, name;
    private final ConfigInputType configInputType;
    private AdditionalSearchFieldConfigParameters(ConfigInputType inputType,
                                                  String storageKey,
                                                  String displayName) {
        this.key = storageKey;
        this.name = displayName;
        this.configInputType = inputType;
    }

    @Override
    public String getStorageKey() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return name;
    }
    


    @Override
    public String getSummary(String value) {
        //todo: convert the value into the fields user friendly name
        return value;
    }

    @Override
    public boolean isDisplayedInView() {
        return true;
    }
    
    public ConfigInputType getInputType()
    {
    	return configInputType;
    }

    public abstract SelectOption[] getPossibleValues();
}
