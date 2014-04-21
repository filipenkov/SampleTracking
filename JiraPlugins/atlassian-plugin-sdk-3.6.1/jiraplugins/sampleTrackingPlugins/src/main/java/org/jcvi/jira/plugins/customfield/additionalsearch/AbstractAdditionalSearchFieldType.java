package org.jcvi.jira.plugins.customfield.additionalsearch;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Field;
import org.jcvi.jira.plugins.config.ConfigManagerStore;
import org.jcvi.jira.plugins.customfield.additionalsearch.config.AdditionalSearchFieldConfigItem;
import org.jcvi.jira.plugins.customfield.additionalsearch.config.AdditionalSearchFieldConfigParameters;
import org.jcvi.jira.plugins.customfield.shared.CFIndexer;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;
import org.jcvi.jira.plugins.customfield.shared.CFType;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapper;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapperUtils;
import org.jcvi.jira.plugins.utils.typemapper.TypeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * CustomFieldType is the core class of a Custom Field's definition.
 * @see CFType
 *
 * This CustomField has no value of its own, it simply copies another fields
 * value. This is useful for adding multiple searchers for the same field.
 */
public abstract class AbstractAdditionalSearchFieldType<TRANSPORT_TYPE> extends CFType {
    private static final Logger log = Logger.getLogger(AbstractAdditionalSearchFieldType.class);

    public static final String CUSTOMFIELD_PREFIX = "customfield_";
    private final Class<TRANSPORT_TYPE> transportClass;

    //--------------------------------------------------------------------------
    // injected managers
    //--------------------------------------------------------------------------
    //used by CFIndexer to determine if a value should be stored
    private final FieldVisibilityManager fieldVisibilityManager; //injected
    //required to access the field that is being copied
    private final CustomFieldManager customFieldManager; //injected


    /**
     *
     * @param persist               Injected manager used to access the backing store
     * @param configManager         Injected manager used to access the
     *                              customField's configuration information
     *                              (in combination with CustomField and
     *                              Issue objects)
     * @param fieldVisibility       Injected manager used to determine if a
     *                              value needs to be stored/indexed for a
     *                              particular issue
     * @param fieldManager          Injected manager used to access the copied
     *                              field
     * @param transportTypeClass    The class of objects that the CustomField
     *                              stores/indexes
     */
    protected AbstractAdditionalSearchFieldType(CustomFieldValuePersister persist,
                                                GenericConfigManager configManager,
                                                FieldVisibilityManager fieldVisibility,
                                                CustomFieldManager fieldManager,
                                                Class<TRANSPORT_TYPE> transportTypeClass) {
        super(persist,
              configManager);
        this.fieldVisibilityManager = fieldVisibility;
        this.customFieldManager = fieldManager;
        this.transportClass = transportTypeClass;
        //Because Atlassian hate programmers the descriptor isn't setup in the
        //constructor, it's set later via the init method
        //String pluginKey = getDescriptor().getPluginKey();
        //this.configurationOptionSet = new AdditionalSearchFieldConfigItem(configManager);
    }

    /**
     * Required by the implementations of createValue,updateValue and
     * getValueFromIssue in AbstractSingleFieldType
     *
     * The available types are defined in PersistenceFieldType. The most
     * common ones are:
     * <ul>
     *   <li>PersistenceFieldType.TYPE_LIMITED_TEXT</li>
     *   <li>PersistenceFieldType.TYPE_DECIMAL</li>
     * </ul>
     * @return  The type of data to store at the persistence/db level
     */
    @Override
    protected abstract PersistenceFieldType getDatabaseType();

    /**
     * @see CFIndexer#getLuceneIndexType()
     * @return  Field.Index.ANALYZED or Field.Index.NOT_ANALYZED
     */
    protected abstract Field.Index getLuceneIndexForIndexer();

    /**
     *  The Lucene index only stores Strings and so this type mapper is used
     *  to convert the data/TRANSPORT_TYPE object into a string for inclusion
     *  in the index.
     */
    protected abstract TypeMapper<TRANSPORT_TYPE,String> getLuceneTypeMapper();

    /**
     *  The TRANSPORT_TYPE of the copied field may not match that of this field.
     *  This mapper is used to convert the value from the copied field into
     *  a type/format suitable to store/index. If the conversion is not possible
     *  the mapper should return NULL and no value will be stored/indexed.
     */
    protected abstract TypeMapper<Object, TRANSPORT_TYPE>  getCopiedFieldValueMapper();

    /**
     * This attaches a JIRA style configuration option to the field.
     * Note: if this needs changing to use multiple FieldConfigItemTypes then
     * {@link #getConfigurationItemTypes} should be overridden instead.
     */
    @Override
    public FieldConfigItemType getConfigurationItemType() {
        return getConfigurationOptionSet();
    }

    //This provides a more specifically typed version of
    //getConfigurationItemType and is in fact used by getConfigurationItemType
    private CFConfigItem getConfigurationOptionSet() {
        //This represents a single set of options that can be configured
        //for this CustomField type. AbstractAdditionalSearchFieldType only has one
        //configurable parameter, the field that should be copied.
        return new AdditionalSearchFieldConfigItem(getDescriptor(),genericConfigManager);
    }

    /**
     * This adds 'copy_value' to the context
     * @param issue             The issue currently in context (Note: this
     *                          will be null in cases like 'default value')
     * @param field             CustomField, Not Null
     * @param fieldLayoutItem   FieldLayoutItem, possibly Null
     * @return  A map containing a String or Double value for 'copy_value'
     */
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> existingContext = super.getVelocityParameters(issue,field,fieldLayoutItem);

        //issue is null for some config pages
        //field shouldn't be but may as well be paranoid
        if (issue != null && field != null) {
            if (issue.getId() == null) {
                //IssueNavigator
            } else {
                Collection<TRANSPORT_TYPE> copiedValues
                                            = getCopyValuesFromIssue(field, issue);
                if (copiedValues != null && !copiedValues.isEmpty()) {
                    //If there is only one value
                    if (copiedValues.size() == 1) {
                        TRANSPORT_TYPE copiedValue = copiedValues.iterator().next();
                        existingContext.put("copy_value",copiedValue);
                    }
                    existingContext.put("copy_values",copiedValues);
                }
            }
        }
        return existingContext;
    }

    /**
     * Returns an indexer for the copied field.
     *
     * The method also calls CFIndexer.getSingleRelatedIndexers to add
     * indexers defined in super classes or by subclasses that override
     * the getRelatedIndexer method
     */
    @Override
    public List<FieldIndexer> getRelatedIndexers(final CustomField thisCustomField) {
        List<FieldIndexer> indexers = new ArrayList<FieldIndexer>();

        //Note this also calls getRelatedIndexer
        List<FieldIndexer> parentIndexers = super.getRelatedIndexers(thisCustomField);
        if (parentIndexers != null && !parentIndexers.isEmpty()) {
            indexers.addAll(parentIndexers);
        }

        //SOURCE FIELD TYPE
        //Initially I wanted to select the indexer based on the type of the field
        //being copied. Annoyingly we don't have enough information to access
        //the configuration at this point. (The domain/[issue & project] is
        //needed to get the specific configuration used).
        //As we don't know the type of the field being copied
        //The indexers must determine the type of field being copied themselves.

        indexers.add(new CFIndexer<TRANSPORT_TYPE>(
                fieldVisibilityManager, //Passed by injection (via CustomFieldTypes)
                thisCustomField,        //Passed by injection (via CustomFieldTypes)
                transportClass,         //The class of objects handled by the indexer
                getLuceneTypeMapper()   //A TypeMapper to convert a TRANSPORT_TYPE
                //object, retrieved via getValues, into a
                //String to store in the Lucene index
        ) {
            //Handles the actual value finding.
            //Looks up the value in the field that is being copied
            @Override
            protected Collection<TRANSPORT_TYPE> getValues(Issue issue) throws FieldValidationException {
                return getCopyValuesFromIssue(thisCustomField, issue);
            }

            @Override
            protected Field.Index getLuceneIndexType() {
                return getLuceneIndexForIndexer();
            }
        });
        return indexers;
    }

    //**************************************************************************
    // The field copying code
    //**************************************************************************

    /**
     * This is the core method. It looks up the configuration of the customField
     * and then uses that to copy the value from another field.
     * @param targetField   The field that the value is being copied to. This
     *                      isn't part of the constructor as it isn't known
     *                      until after the indexer has been created.
     * @param issue         The issue that the value is being requested for.
     *                      The actual value may be from the issue's parent
     *                      rather than the issue itself
     * @return  The value from the field to be copied converted into the
     * TRANSPORT_TYPE
     * @throws FieldValidationException if the configuration is incorrect
     */
    private Collection<TRANSPORT_TYPE> getCopyValuesFromIssue(CustomField targetField,
                                                              Issue issue)
            throws FieldValidationException {
        //check that we are in-scope
        if (!targetField.isAllIssueTypes() &&
                        !inScope(targetField,issue)) {
            return null;
        }

        if (issue == null) {
            //get the default
            //should this return the copied fields default, not practical as
            //we need to know the issue to find the correct configuration.
            //should it have its own default, this would still need access to
            //the configuration.
            //that leaves returning null...
            return null;
        }

        //The configuration depends on the issue requested, not the parent
        ConfigManagerStore configStore =
                getConfigStore(getConfigurationOptionSet(),targetField,issue);

        //lookup the field using the name from the config
        //getFieldToCopy never returns null, throws Exceptions instead
        CustomField fieldToCopy = getFieldToCopy(configStore);

        //check that the field to copy is in scope
        final Issue issueToCopyFrom;
        if (fieldToCopy.isAllIssueTypes() ||
                inScope(fieldToCopy,issue)) {
            log.debug("Field: '"+fieldToCopy+ ":"+issue+"' copying from same issue");
            issueToCopyFrom = issue;
        } else if (inScope(fieldToCopy,issue.getParentObject())) {
            // if the field isn't in scope on the sub-task but it is on the
            // parent then we should be copying from the parent not the sub-task
            issueToCopyFrom = issue.getParentObject();
            log.debug("Field: '"+fieldToCopy+ ":"+issue+"' copying from parent issue");
        } else {
            // the field isn't in scope and so won't have a value
            log.debug("Field: '"+fieldToCopy+ ":"+issue+"' is out of scope on the issue it is to be copied from");
            return null;
        }


        //TypeUtils.toCollection puts the values in a collection, or if they
        //already are cast them to a collection. JIRA isn't very consistent
        //about if getValue returns a single or multiple items
        Collection<Object> valuesCollection
                = TypeUtils.toCollection(fieldToCopy.getValue(issueToCopyFrom));
        log.debug("Field: "+targetField.getName()+" copying from: ("+issueToCopyFrom+","+fieldToCopy.getName()+") found values "+valuesCollection.toString());

        //we need to convert the list into the Transport Type before returning it
        return TypeMapperUtils.mapUnorderedCollection(getCopiedFieldValueMapper(), valuesCollection);
    }

    private boolean inScope(CustomField cf, Issue issue) {
        //check scope
        if (!cf.isAllIssueTypes()) {
            if (issue == null) {
                //no context, and not global
                return false;
            }
            Project project = issue.getProjectObject();
            IssueType issuesType = issue.getIssueTypeObject();
            List<String> issueTypesByID = TypeUtils.singleItem(issuesType.getId());
            if (!cf.isInScope(project,issueTypesByID)) {
                 //not in the global scope or this Issue's scope
                return false;
            }
        }
        //In scope, check if it's visible for this issue type
        return !fieldVisibilityManager.isFieldHidden(cf.getHiddenFieldId(),issue);
    }

    private CustomField getFieldToCopy(ConfigManagerStore configStore) throws FieldValidationException{
        String fieldToLookup = configStore.retrieveStoredValue(
                                AdditionalSearchFieldConfigParameters.FIELD_TO_COPY);
        if (fieldToLookup == null) {
            //nothing we can do if we don't have the name
            throw new FieldValidationException("No source field set");
        }

        CustomField fieldToCopy = getFieldToCopy(fieldToLookup);
        if (fieldToCopy == null) {
            //nothing we can do if we don't have access to the field
            throw new FieldValidationException("Unable to find source field: "+fieldToLookup);
        }
        log.debug("found source: " + fieldToLookup);
        return fieldToCopy;
    }

    private CustomField getFieldToCopy(String name) {
        //Try its ID first
        {
            CustomField fieldToCopy =
                  customFieldManager.getCustomFieldObject(name);
            if (fieldToCopy != null) {
              return fieldToCopy;
            }
        }
        //Is it a customfield number?
        try {
            int idNumber = Integer.parseInt(name,10);
            CustomField fieldToCopy = customFieldManager.getCustomFieldObject(
                                                    CUSTOMFIELD_PREFIX+idNumber);
            if (fieldToCopy != null) {
              return fieldToCopy;
            }
        } catch (NumberFormatException nfe) {
            //if wasn't a number, ignore it and carry on
        }

        //if that doesn't work try its name
        return customFieldManager.getCustomFieldObjectByName(name);
    }

    private ConfigManagerStore getConfigStore(CFConfigItem configItemToUse,
                                              CustomField thisField, Issue issue) {
        FieldConfig relevantConfig = thisField.getRelevantConfig(issue);
        if (relevantConfig == null) {
            //this probably doesn't ever occur as JIRA should have already
            //filtered out the field before this method gets called
            log.debug(thisField.getName()+" does not have any relevantConfig for issue "+issue);
        }
        //Create a manager to access the configuration data.
        //Uses thisField and issue to access the data specific to this
        //instance of the field.
        return new ConfigManagerStore(genericConfigManager,
                            //the config values specific to this
                            //instance of the field and the current issue
                            //a null issue is used when handling defaults
                            //or config that doesn't handle different
                            //configurations per project
                            thisField.getRelevantConfig(issue),
                            //the config properties to access
                            configItemToUse);

    }

    protected FieldVisibilityManager getFieldVisibilityManager() {
        return fieldVisibilityManager;
    }

//    /**
//     * todo: Add a version for number fields
//     * If multiple FieldIndexers are to be used then getRelatedIndexers needs to
//     * be overridden instead.
//     * If only first single indexer is wanted then this method is simpler to implement
//     * than getRelatedIndexers.
//     * If no indexers are wanted then neither method needs to be overridden.
//     *
//     * @param customField the custom field to get the indexer for.
//     * @return an instantiated and initialised FieldIndexer
//     * {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}.
//     * Null if no related indexer.
//     */
//    @Override
//    protected FieldIndexer getRelatedIndexer(CustomField customField) {
//        return new StringIndexer(getFieldVisibilityManager(),customField);
//    }

    //
    public Object getValueFromIssue(CustomField field, Issue issue) {
        if (issue != null && field != null) {
            Object value = getCopyValuesFromIssue(field,issue);
            log.debug(issue.getKey()+":"+field.getName()+ " getValueFromIssue called returning: "+value);
            return value;
        }
        return null;
    }

}
