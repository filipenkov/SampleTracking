package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>This interface represents a particular type of {@link CustomField}. It encapsulates all logic dealing with values
 * of a Custom Field, such as creation, update and removal of values, storage & retrieval of defaults and validation of
 * values. </p>
 *
 * <p>A value instance of a custom field is represented as an {@link Object}, from hereon in referred to as a
 * <em><strong>Transport Object</strong></em>. These may be of singular types (eg. {@link Number}, {@link String}) or
 * Multi-Dimensional (eg. {@link Collection} of Strings, {@link Map} of Date Objects, {@link
 * CustomFieldParams} of {@link Option}). Most methods in the interface expect/returns Transfer Objects (e.g.
 * Persistence Methods ({@link #createValue}, {@link #updateValue}) and Transfer Object Getters
 * {@link #getValueFromIssue} and {@link #getValueFromCustomFieldParams}.</p>
 *
 * <p> However, two special conversion methods ({@link #getSingularObjectFromString} & {@link
 * #getStringFromSingularObject}) act on the <strong>Singular Object</strong> level. Thus, even when the
 * Transfer Object type is a Collection of Number, getSingularObjectFromString would still return a single Number
 * object. </p>
 *
 * <p>Implementing classes should <strong>clearly document</strong> the exact structure of the Transport Object in the
 * Class javadoc header. If the Transport Object is Multi-Dimensional, the type of the Singular Object should also be
 * specified. This is especially relevant for complex custom field types (See {@link com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType} for
 * example)</p>
 *
 * @see CustomField
 * @see CustomFieldParams
 * @since JIRA 3.0
 */
public interface CustomFieldType
{
    String DEFAULT_VALUE_TYPE = "DefaultValue";
    // ---------------------------------------------------------------------------------------------  Descriptor Methods

    /**
     * Initialises the CustomFieldType with the given descriptor.
     *
     * @param customFieldTypeModuleDescriptor CustomFieldTypeModuleDescriptor
     */
    void init(CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor);

    /**
     * Returns the full key of the CustomFieldType. Typically, this will be prefixed with
     * "com.atlassian.jira.plugin.system.customfieldtypes:"
     *
     * @return CustomFieldType Key prefixed with the Package
     */
    public String getKey();

    public String getName();
    public String getDescription();
    public CustomFieldTypeModuleDescriptor getDescriptor();



    // ---------------------------------------------------------------------------------- Single Value Object Converters

    /**
     * Returns the {@link String} representation of a single value within the CustomFieldType. This is the value that
     * will is passed to the presentation tier for editing. For single CustomFieldTypes the <em>Singular Object</em> is
     * the same as a <em>Transport Object</em>. However, for multi-dimensional CustomFieldTypes, the Singular Object is
     * the Object contained within the {@link Collection} or {@link CustomFieldParams}
     *
     * @param singularObject the object
     * @return String representation of the Object
     */
    public String getStringFromSingularObject(Object singularObject);

    /**
     * Returns a Singular Object, given the string value as passed by the presentation tier.
     * Throws FieldValidationException if the string is an invalid representation of the Object.
     *
     * @param string the String
     * @return singularObject instance
     * @throws FieldValidationException if the string is an invalid representation of the Object.
     */
    public Object getSingularObjectFromString(String string) throws FieldValidationException;



    // -------------------------------------------------------------------------------- Custom Field Persistence Methods

    /**
     * Performs additional tasks when a CustomField of this type is being removed {@link CustomField#remove}.
     * This includes removal of values & options.
     *
     * @param field The custom field that is being removed, so any data stored for
     * any issues for that field can be deleted.
     * @return {@link Set} of issue ids that has been affected
     */
    public Set<Long> remove(CustomField field);



    // -----------------------------------------------------------------------------------------------------  Validation

    /**
     * Ensures that the {@link CustomFieldParams} of Strings is a valid representation of the Custom Field values.
     * Any errors should be added to the {@link ErrorCollection} under the appropriate key as required.
     *
     * @param relevantParams parameter object of Strings
     * @param errorCollectionToAddTo errorCollection to which any erros should be added (never null)
     * @param config FieldConfig
     */
    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config);



    // --------------------------------------------------------------------------------------------- Persistance Methods

    // These methods take the <em>Transport Object</em> as a parameter.
    public void createValue(CustomField field, Issue issue, Object value);
    public void updateValue(CustomField field, Issue issue, Object value);



    // ---------------------------------------------------------------------------------- Transfer Object Getter methods

    /**
     * Retrieves the Object representing the CustomField value instance from the CustomFieldParams of Strings. This
     * return type <strong>must</strong> exactly match that of the value Object parameter in the persistance methods
     * ({@link #createValue}, {@link #updateValue}) and Object returned from {@link
     * #getValueFromIssue}.
     *
     * @param parameters CustomFieldParams of <b>String</b> objects. Will contain one value for Singular field types.
     * @return <i>Transport Object</i> matching the Object parameter of {@link #createValue}, {@link #updateValue}
     * @throws FieldValidationException if the String value fails to convert into Objects
     * @see #createValue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue, Object)
     * @see #updateValue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue, Object)
     * @see #getValueFromIssue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue)
     */
    public Object getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException;

    /**
     * Return the String value object from the CustomFieldParams. The object may be a single String (e.g. TextCFType,
     * List of Strings (e.g. MultiSelectCFType) or CustomFieldParams of Strings (e.g. CascadingSelectCFType)
     *
     * @param parameters - CustomFieldParams containing String values
     * @return String value object from the CustomFieldParams
     */
    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters);

    /**
     * Retrieves the Object representing the <strong>current</strong> CustomField value for the given issue. See
     * {@link #getValueFromCustomFieldParams} for more detailed notes.
     *
     * @param field Custom field for which to retrieve the value
     * @param issue Issue from which to retrieve the value
     * @return <i>Transport Object</i> matching the Object parameter of {@link #createValue}, {@link #updateValue}
     * @see #getValueFromCustomFieldParams(com.atlassian.jira.issue.customfields.view.CustomFieldParams)
     */
    public Object getValueFromIssue(CustomField field, Issue issue);



    // -------------------------------------------------------------------------------------------------- Default Values

    /**
     * Retrieves the Object representing the <strong>default</strong> CustomField value for the Custom Field. See
     * {@link #getValueFromCustomFieldParams} for more detailed notes.
     *
     * @param fieldConfig CustomField for default value
     * @return <i>Transport Object</i> matching the Object parameter of {@link #createValue}, {@link #updateValue}
     */
    public Object getDefaultValue(FieldConfig fieldConfig);

    /**
     * Sets the default value for a Custom Field
     *
     * @param fieldConfig CustomField for which the default is being stored
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     */
    public void setDefaultValue(FieldConfig fieldConfig, Object value);



    // --------------------------------------------------------------------------------------------------  Miscellaneous

    /**
     * Returns a values to be stored in the change log, example is the id of the changed item.
     *
     * @since 3.1 Implementions can return null. This should only occur when no change log is desired
     *
     * @param field CustomField that the value belongs to
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return Change log value.
     */
    public String getChangelogValue(CustomField field, Object value);

    /**
     * Returns a String of representing values to be stored in the change log, an example is the name of a version
     * field that a version id will resolve to within JIRA.
     *
     * @since 3.4 Implementions can return null. This should only occur when no change log is desired or when the
     * value returned from the getChangelogValue method is an acurate representation of the data's value.
     *
     * @param field CustomField that the value belongs to
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return Change log string.
     */
    public String getChangelogString(CustomField field, Object value);

    /**
     * The custom field may wish to pass parameters to the velocity context beyond the getValueFromIssue methods
     * (eg managers).
     * <p />
     * The values are added to the context for all velocity views (edit, search, view, xml)
     * <p />
     *
     * @param issue The issue currently in context (Note: this will be null in cases like 'default value')
     * @param field CustomField
     * @param fieldLayoutItem FieldLayoutItem
     * @return  A {@link Map} of parameters to add to the velocity context, or an empty Map otherwise (never null)
     */
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem);

    /**
     * Returns a List of {@link FieldConfigItemType} objects.
     * This opens up possibilties for configurable custom fields
     * //todo: generics
     * @return List of {@link FieldConfigItemType}
     */
    public List getConfigurationItemTypes();

    /**
     * Returns a list of indexers that will be used for the field.
     *
     * @param customField the custom field to get the related indexers of.
     * @return List of instantiated and initialised {@link FieldIndexer} objects. Null if no related indexers.
     */
    List<FieldIndexer> getRelatedIndexers(CustomField customField);

    /**
     * This is a mirror of the method from the RenderableField interface and is needed to bridge the gap between
     * CustomFields and CustomFieldTypes.
     * @return true if the field is configurable for use with the renderers, a text based field, false otherwise.
     */
    public boolean isRenderable();

    /**
     * Used to compare 2 field values and work out whether a change item should be generated
     * @param v1 current value
     * @param v2 new value
     * @return true if teh change item should be generated, false otherwise
     */
    boolean valuesEqual(Object v1, Object v2);

    /**
     * Allow the custom field type perform a specific check as to its availability for bulk editing.
     * @param bulkEditBean BulkEditBean
     * @return null if available for bulk edit or appropriate unavailable message
     */
    String availableForBulkEdit(BulkEditBean bulkEditBean);
}
