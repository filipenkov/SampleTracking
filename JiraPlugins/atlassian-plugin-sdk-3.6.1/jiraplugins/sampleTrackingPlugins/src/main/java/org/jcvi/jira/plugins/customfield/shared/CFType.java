package org.jcvi.jira.plugins.customfield.shared;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>CustomFieldType object define the components make a custom field type
 * and provide implementations for interacting with the system.</p>
 *
 * <h3>Configuration</h3>
 * <p>These methods will probably need overriding in any child class.
 * They are implemented here with nop behaviours to save adding them
 * if they are not used.</p>
 *
 * <h4>FieldConfigItemType</h4>
 * <p>For one or less ConfigurationItems override
 * <ul>
 *       <li>getConfigurationItemType</li>
 *       <li>isDefaultConfigurationItemUsed</li>
 * </ul>
 * For multiple ConfigurationItems override
 * <ul><li>getConfigurationItemTypes</li></ul>
 * </p>
 * <h4>FieldIndexers</h4>
 * <p>For one or less FieldIndexers override
 * <ul><li>getRelatedIndexer</li></ul>
 * For multiple FieldIndexers override
 * <ul><li>getRelatedIndexers</li></ul>
 * </p>
 * <h3>Intercept points</h3>
 * <p>The following methods intercept the data on its way to and from
 * external systems, e.g. the HTTP form, the database/persister,
 * the configuration store and the indexing system.</p>
 * <table>
 *     <hr><td>Method</td><td>Direction</td><td>System</td></hr>
 *     <tr><td>getVelocityParameters</td><td>to</td><td rowspan="2">HTTP form</td></tr>
 *     <tr><td>getValueFromCustomFieldParam</td><td>from</td></tr>
 *     <tr><td>createValue/updateValue</td><td>to</td><td rowspan="2">Persister</td></tr>
 *     <tr><td>getValueFromIssue</td><td>from</td></tr>
 *     <tr><td>setDefaultValue</td><td>to</td><td rowspan="2">Config store (only one property though)</td></tr>
 *     <tr><td>getDefaultValue</td><td>from</td></tr>
 *     <tr><td>??</td><td>to</td><td rowspan="2">Lucene</td></tr>
 *     <tr><td>??</td><td>from</td></tr>
 * </table>
 */
public abstract class CFType extends AbstractSingleFieldType {
    //SortableCustomField is already implemented by TextCFType
    protected CFType(CustomFieldValuePersister persist,
                  GenericConfigManager configManager) {
        super(persist,
              configManager);
    }

    //---------------------------------------------------------------------
    //                      FieldConfigItemType
    //---------------------------------------------------------------------
    /**
     * <p>Returns a single
     * {@link com.atlassian.jira.issue.fields.config.FieldConfigItemType}
     * object.</p>
     * <p>This is a simplified method signature that is easier to
     * implement than getConfigurationItemTypes. It doesn't require
     * the null checks involved with using lists and the addition /
     * removal of DefaultConfigurationItem is handled automatically
     * based on isDefaultConfigurationItemUsed</p>
     * <p>If getConfigurationItemTypes is overridden then this
     * method is ignored.</p>
     * @return a FieldConfigItemType, null if none are wanted
     */
    protected FieldConfigItemType getConfigurationItemType() {
        return null; //nop
    }

    /**
     * <p>This method controls the addition / removal of the
     * DefaultConfigurationItem when getSingleConfigurationItemTypes is
     * used.</p>
     * <p>If getConfigurationItemTypes is overridden then this method
     * is ignored.</p>
     * @return Should the configuration item for setting a default value be included in the
     * configurationItemTypes for this field?
     */
    protected boolean isDefaultConfigurationItemUsed() {
        return true; //by default the 'DefaultConfigurationItem' is added
    }

    /**
     * <p>FieldConfigItemType object define items/options on the configure
     * custom-field page.</p>
     * <p>This method should be overridden if more than one
     * FieldConfigItemType needs to be added.</p>
     * <p>Implementation notes:
     * <ul>
     * <li>Either super.getConfigurationItemTypes,
     * getExistingFieldConfigItemTypes or getExistingFieldConfigItemTypes
     * should be called first to get any FieldConfigItemType objects
     * added by parent implementations, (e.g. DefaultValueConfigItem)</li>
     * <li>super.getConfigurationItemTypes can return null</li>
     * <li>getConfigurationItemType is ignored
     * getExistingFieldConfigItemTypes is called.</li>
     * <li>isDefaultConfigurationItemUsed is ignored
     * getSingleConfigurationItemTypes or getExistingFieldConfigItemTypes
     * are called.</li>
     * </ul>
     * </p>
     * @return A List of FieldConfigItemType objects or null
    */
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        return getSingleConfigurationItemTypes();
    }

    //---------------------------------------------------------------------
    //                      FieldIndexer
    //---------------------------------------------------------------------
    /**
     * <p>Returns a single
     * {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}
     * object.</p>
     * <p>This is a simplified method signature that is easier to
     * implement than getRelatedIndexer. It doesn't require
     * the null checks involved with using lists.</p>
     * <p>If getRelatedIndexers is overridden then this method is
     * ignored.</p>
     * @param customField the custom field to get the indexer for.
     * @return a FieldIndexer, null if none are wanted
     */
    protected FieldIndexer getRelatedIndexer(CustomField customField) {
        return null; //nop
    }

    /**
     * <p>FieldIndexer objects are used to add entries to the Lucine index</p>
     * <p>This method should be overridden if more than one indexer needs
     * to be returned.</p>
     * <p>Implementation Notes:
     * <ul>
     * <li>super.getRelatedIndexers or getSingleRelatedIndexers should
     * be called first to include any FieldIndexers defined by the
     * parent classes.</li>
     * <li>super.getRelatedIndexers can return null.</li>
     * <li>getRelatedIndexer is ignored unless getSingleRelatedIndexers
     * is called.</li>
     * </ul>
     * </p>
     * @return A list of FieldIndexer objects or null
     */
    @Override
    public List<FieldIndexer> getRelatedIndexers(CustomField field) {
        return getSingleRelatedIndexers(field);
    }

    //---------------------------------------------------------------------
    //                      Templates
    //---------------------------------------------------------------------
    /**
     * Adds variables to the velocity environment, the map's keys and values
     * become variable names and values in the velocity context.<p />
     * This method adds:
     * <table>
     * <hr><th>Name</th><th>Value</th></hr>
     * <tr>
     * <td></td><td></td>
     * </tr>
     * </table>
     * AbstractCustomFieldType adds
     * <table>
     * <hr><th>Name</th><th>Value</th></hr>
     * <tr>
     * <td>issueGv</td><td>The issue object, if it isn't null</td>
     * </tr>
     * </table>
     * <p>This is called from CustomFieldUtils.buildParams, which adds the
     * other parameters including 'value'</p>
     * The values are added to the context for all velocity views
     * (edit, search, view, xml, edit-config) <p/>
     * Not all of the items can be added though to the edit-config,
     * as the issue is null and there is no way to get the
     * fieldConfig. $action.configBean.value should be used instead
     *
     * @param issue             The issue currently in context (Note: this
     *                          will be null in cases like 'default value')
     * @param field             CustomField, Not Null
     * @param fieldLayoutItem   FieldLayoutItem, possibly Null
     * @return  A {@link java.util.Map} of parameters to add to the velocity context, or an empty Map otherwise (never null)
     */
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> existingContext = super.getVelocityParameters(issue,field,fieldLayoutItem);

        //issue is null for some config pages
        //field shouldn't be but may as well be paranoid
        if (issue != null && field != null) {
            //existingContext.put(REVERSE, velocity);
        } else {
            //config page additions
        }
        return existingContext;
    }

    //---------------------------------------------------------------------
    //                      Persistence <-> Carrier
    //---------------------------------------------------------------------
    //DB<->Carrier
    @Override
    protected Object getDbValueFromObject(Object o) {
        //null op, converting a string to a string
        //just in case asString converts nulls and handles
        //if o isn't actually a string
        return asString(o);
    }

    //Part DB/Carrier, part Carrier/GUI
    //this is directly called to set $value in templates
    @Override
    protected Object getObjectFromDbValue(Object o) throws FieldValidationException {
        //null op, converting a string to a string
        //just in case asString converts nulls and handles
        //if o isn't actually a string
        return asString(o);
    }

    //---------------------------------------------------------------------
    //                      GUI <-> Carrier
    //---------------------------------------------------------------------

    //GUI value <-> Carrier
    public String getStringFromSingularObject(final Object o) {
        //The $value for EDIT comes from here
        //but $value for VIEW doesn't!
        return asString(o);
    }

    public Object getSingularObjectFromString(String s) throws FieldValidationException {
        //convert the empty string to null?
        //none of the other implementations seem to
        return s;
    }
    //---------------------------------------------------------------------
    //                      Support Methods
    //---------------------------------------------------------------------
    /**
     * Returns a List of {@link com.atlassian.jira.issue.fields.config.FieldConfigItemType} objects.
     * The list consists of the values from the parent implementations and the value from
     * getConfigurationItemType() and optionally the DefaultValueConfigItem. The default is
     * included iff isDefaultConfigItemUsed;
     * @return List of {@link com.atlassian.jira.issue.fields.config.FieldConfigItemType}
     * todo: does null indicate something different than an empty List?
     */
    protected List<FieldConfigItemType> getSingleConfigurationItemTypes() {
        //getExistingFieldConfigItemTypes handles inserting or removing the
        //DefaultValueConfigItem as necessary
        List<FieldConfigItemType> configTypes = getExistingFieldConfigItemTypes();

        FieldConfigItemType configItem = getConfigurationItemType();
        if (configItem == null) {
            //nothing to do
            return configTypes;
        }
        //check the list isn't null
        if (configTypes == null) {
            configTypes = new ArrayList<FieldConfigItemType>(1);
        }
        //add and return
        configTypes.add(configItem);
        return configTypes;
    }

    /**
     * Returns a list of indexers that will be used for the field.
     *
     * @param customField the custom field to get the related indexers of.
     * @return List of instantiated and initialised
     * {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}
     * objects. Null if no related indexers.
     */
    protected List<FieldIndexer> getSingleRelatedIndexers(CustomField customField) {
        List<FieldIndexer> indexers = super.getRelatedIndexers(customField);
        FieldIndexer customIndexer = getRelatedIndexer(customField);
        if (customIndexer == null) {
            //if there is no indexer to add return the current list,
            //which could be null
            return indexers;
        }

        //ensure that there is a list to add the indexer to
        if (indexers == null) {
            indexers = new ArrayList<FieldIndexer>(1);
        }
        indexers.add(customIndexer);
        return indexers;
    }

    /**
     * To get the FieldConfigItemType objects defined by the parent
     * implementations of the CustomFieldType passed in
     * @return a list of FieldConfigItems from the passed in CustomFieldType.
     * Including the DefaultValueConfigItem iff isDefaultConfigurationItemUsed
     * is true.
     */
    private List<FieldConfigItemType> getExistingFieldConfigItemTypes() {
        List<FieldConfigItemType> configTypes = super.getConfigurationItemTypes();
        //possible situations,
        //1.parent list includes default         & we want default
        //2.parent list doesn't includes default & we want default
        //3.parent list includes default         & we don't want default
        //4.parent list doesn't includes default & we don't want default
        if (isDefaultConfigurationItemUsed()) {
            //case 1 or 2 (we want the default)
            if (containsDefaultValueConfigItem(configTypes)) {
                //case 1, already there
                return configTypes;
            } else {
                //case 2 not in the list, add it
                return addDefaultValueConfigItem(configTypes);
            }
        } else {
            //case 3 or 4
            return removeDefaultValueConfigItem(configTypes);
        }
    }

    //returns the passed in List with the item added.
    //it only returns a new List if the one passed in was null
    private List<FieldConfigItemType> addDefaultValueConfigItem(List<FieldConfigItemType> configTypes) {
        if (configTypes == null) {
            //configTypes could be null
            configTypes = new ArrayList<FieldConfigItemType>(1);
        }
        configTypes.add(new DefaultValueConfigItem());
        return configTypes;
    }

    /**
     * Removes any DefaultValueConfigItems from the list passed in. This handles the
     * cases where:
     * <ul>
     * <li>parent list includes default         & we don't want default</li>
     * <li>parent list doesn't includes default & we don't want default</li>
     * </ul>
     * The list passed in is not altered, a new list that is guaranteed to not include
     * any DefaultValueConfigItems is returned. If no list was passed in (null) then
     * no list is returned (null)
     * @param configTypes   The possibly null List of FieldConfigItemType objects to be
     * filtered
     * @return A new List of FieldConfigItemType objects, none of which are of type
     * DefaultValueConfigItem. The list could be empty or null.
     */
    private List<FieldConfigItemType> removeDefaultValueConfigItem(List<FieldConfigItemType> configTypes) {
        if (configTypes == null) {
            return null;
        }
        //The values of configTypes are filtered into this List
        List<FieldConfigItemType> configItemsNotIncludingDefault = new ArrayList<FieldConfigItemType>();
        //find default in the passed in list
        for(FieldConfigItemType currentItem : configTypes) {
            if (!(currentItem instanceof DefaultValueConfigItem)) {
                configItemsNotIncludingDefault.add(currentItem);
            }
        }
        return configItemsNotIncludingDefault;
    }

    /**
     * Checks if the list of fieldConfigItemTypes contains at least one item of type DefaultValueConfigItem.
     * @param configTypes   The possibly null List of FieldConfigItemType objects to be
     * filtered
     * @return A new List of FieldConfigItemType objects, none of which are of type
     * DefaultValueConfigItem. The list could be empty or null.
     */
    private boolean containsDefaultValueConfigItem(List<FieldConfigItemType> configTypes) {
        if (configTypes == null) {
            return false;
        }
        //find default in the passed in list
        for(FieldConfigItemType currentItem : configTypes) {
            if (currentItem instanceof DefaultValueConfigItem) {
                //found a DefaultValueConfigItem
                return true;
            }
        }
        //looped through all of the elements of configTypes without finding any of type DefaultValueConfig.
        return false;
    }

    /**
     * Strings pass through, all other inputs are converted to strings
     * including null which becomes the empty string.
     * @param in The object to test / convert
     * @return A string, not null
     */
    private String asString(Object in) {
        if (in == null) {
            return "";
        }
        if (in instanceof String) {
            return (String)in;
        }
        return in.toString();
    }
}
