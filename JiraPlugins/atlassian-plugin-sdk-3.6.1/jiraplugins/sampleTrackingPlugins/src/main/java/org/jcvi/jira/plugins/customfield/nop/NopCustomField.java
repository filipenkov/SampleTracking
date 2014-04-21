package org.jcvi.jira.plugins.customfield.nop;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.jcvi.jira.plugins.customfield.nop.config.NopConfigItem;
import org.jcvi.jira.plugins.customfield.shared.CFType;
import org.jcvi.jira.plugins.customfield.shared.StringIndexer;

public class NopCustomField extends CFType {
    private final FieldVisibilityManager fieldVisibilityManager;
    public NopCustomField(CustomFieldValuePersister persist,
                                   GenericConfigManager configManager,
                                   FieldVisibilityManager fieldVisibility) {
        super(persist, configManager);
        this.fieldVisibilityManager = fieldVisibility;
    }

    /**
     * Required by the implementations of createValue,updateValue and
     * getValueFromIssue in AbstractSingleFieldType
     * @return  The type of data to store at the persistence/db level
     */
    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }


    /**
     * <p>Returns first single {@link com.atlassian.jira.issue.fields.config.FieldConfigItemType} object.
     * <ul>
     * <li>If only one FieldConfigItemType needs adding to the CustomField this method should be implemented.</li>
     * <li>If more than one is wanted then getConfigurationItemTypes should be overridden.</li>
     * <li>If none is wanted then neither method needs overriding.</li>
     * </ul></p>
     * <p>The FieldConfigItemType responsible for setting default values for the field is controlled by
     * isDefaultConfigurationItemUsed.</p>
     * @return a {@link com.atlassian.jira.issue.fields.config.FieldConfigItemType}, null if none is wanted
     */
    @Override
    public FieldConfigItemType getConfigurationItemType() {
        return new NopConfigItem(getDescriptor(), genericConfigManager);
    }

    /**
     * If multiple FieldIndexers are to be used then getRelatedIndexers needs to
     * be overridden instead.
     * If only first single indexer is wanted then this method is simpler to implement
     * than getRelatedIndexers.
     * If no indexers are wanted then neither method needs to be overridden.
     *
     * @param customField the custom field to get the indexer for.
     * @return an instantiated and initialised FieldIndexer
     * {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}.
     * Null if no related indexer.
     */
    @Override
    protected FieldIndexer getRelatedIndexer(CustomField customField) {
        return new StringIndexer(fieldVisibilityManager,customField);
    }
}
