package org.jcvi.jira.plugins.customfield.velocity;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.jcvi.jira.plugins.customfield.shared.CFType;
import org.jcvi.jira.plugins.customfield.velocity.config.VelocityConfigItem;

import java.util.HashMap;
import java.util.Map;

//Suppressed as this class is dynamically loaded
@SuppressWarnings({"UnusedDeclaration"})
class VelocityFieldType extends CFType { //package protected, inherited from parent
    private static final String REVERSE = "reverse";
    private final FieldVisibilityManager fieldVisibilityManager;


    private final JiraAuthenticationContext auth;

    public VelocityFieldType(CustomFieldValuePersister persist,
                                   GenericConfigManager configManager,
                                   FieldVisibilityManager fieldVisibility,
                                   JiraAuthenticationContext authenticationContext) {
        super(persist, configManager);
        this.fieldVisibilityManager = fieldVisibility;
        this.auth = authenticationContext;
    }

    /**
     * Required by the implementations of createValue,updateValue and
     * getValueFromIssue in AbstractSingleFieldType
     * @return  The type of data to store at the persistence/db level
     */
    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }

    //---------------------------------------------------------------------
    //                      Custom Field's Properties
    //---------------------------------------------------------------------
    /**
     * Adds variables to the velocity environment, the map's keys and values
     * become variable names and values in the velocity context.<p />
     * This method adds:
     * <table>
     * <hr><th>Name</th><th>Value</th></hr>
     *     <tr>
     *         <td>reverse</td><td>VelocityReverser</td>
     *     </tr>
     * </table>
     * The values are added to the context for all velocity views
     * (edit, search, view, xml, edit-config) <p/>
     * Not all of the items can be added though to the edit-config,
     * as the issue is null and there is no way to get the
     * fieldConfig. $action.configBean.value should be used instead
     *
     * @param issue The issue currently in context (Note: this will be null in cases like 'default value')
     * @param field CustomField
     * @param fieldLayoutItem FieldLayoutItem
     * @return  A {@link java.util.Map} of parameters to add to the velocity context, or an empty Map otherwise (never null)
     */
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> existingContext = super.getVelocityParameters(issue,field,fieldLayoutItem);

        //issue is null for some config pages
        //field shouldn't be but may as well be paranoid
        if (issue != null && field != null) {
            //add some other config
            Map<String, String>configuration = new HashMap<String,String>();
            //todo: do we really want everything in the env, doesn't this just duplicate the stuff from ctx for most of it?
            Map<String, Object> newContext = VelocityContextFactory.getContext(configuration, issue, field, auth);
            existingContext.putAll(newContext);
            //store the config and issue in the metaVelocity
            //so that we have access to them when called from
            //the velocity template
            MetaVelocity velocity = new MetaVelocity
                    (genericConfigManager,issue, field, getVelocityConfigItemType());
            existingContext.put(REVERSE, velocity);
        }
        //todo: is there anything that the config page needs adding to the environment?

        return existingContext;
    }

    @Override
    protected FieldConfigItemType getConfigurationItemType() {
        return getVelocityConfigItemType();
    }

    protected VelocityConfigItem getVelocityConfigItemType() {
        return new VelocityConfigItem(getDescriptor(), genericConfigManager);
    }

    /**
     * <p>Returns a single
     * {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}
     * object: VelocityFieldIndexer</p>
     * @param customField the custom field to get the indexer for.
     * @return a FieldIndexer, null if none are wanted
     */
    protected FieldIndexer getRelatedIndexer(CustomField customField) {
        return new VelocityFieldIndexer(genericConfigManager,
                                        fieldVisibilityManager,
                                        customField,
                                        getVelocityConfigItemType());
    }
}
