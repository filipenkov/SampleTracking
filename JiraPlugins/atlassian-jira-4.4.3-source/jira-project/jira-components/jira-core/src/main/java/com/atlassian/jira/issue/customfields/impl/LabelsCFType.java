package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomFieldParser;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.LabelParserImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.issue.label.OfBizLabelStore;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Labels Custom field
 *
 * @since v4.2
 */
public class LabelsCFType extends AbstractCustomFieldType
        implements SortableCustomField<Set<Label>>, ProjectImportableCustomFieldParser, ProjectImportableCustomField
{
    private static final Logger log = Logger.getLogger(LabelsCFType.class);

    private final JiraAuthenticationContext authContext;
    private final IssueManager issueManager;
    private final GenericConfigManager genericConfigManager;
    private final LabelUtil labelUtil;
    private final LabelManager labelManager;

    public LabelsCFType(final JiraAuthenticationContext authenticationContext,
            final IssueManager issueManager, final GenericConfigManager genericConfigManager,
            final LabelUtil labelUtil, final LabelManager labelManager)
    {
        this.authContext = authenticationContext;
        this.issueManager = issueManager;
        this.genericConfigManager = genericConfigManager;
        this.labelUtil = labelUtil;
        this.labelManager = labelManager;
    }

    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);

        if (issue == null || issue.getId() == null)
        {
            velocityParameters.put("canEdit", Boolean.FALSE);
        }
        else
        {
            final Issue issueFromDb = issueManager.getIssueObject(issue.getId());
            velocityParameters.put("canEdit", issueManager.isEditable(issueFromDb, authContext.getUser()));
            velocityParameters.put("labels", getValueFromIssue(field, issue));
        }

        velocityParameters.put("fieldId", field.getId());
        velocityParameters.put("i18n", authContext.getI18nHelper());
        velocityParameters.put("field", field);
        velocityParameters.put("labelUtil", labelUtil);
        velocityParameters.put("issue", issue);
        velocityParameters.put("labelParser", new LabelParser());

        return velocityParameters;
    }

    public Object getValueFromIssue(CustomField field, Issue issue)
    {
        final Set<Label> labels = labelManager.getLabels(issue.getId(), field.getIdAsLong());
        // We should return null if there are no labels.
        if (labels.isEmpty())
        {
            return null;
        }
        return labels;
    }

    public Object getDefaultValue(final FieldConfig fieldConfig)
    {
        Object databaseValue = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (databaseValue != null)
        {
            try
            {
                final String labelsString = (String) databaseValue;
                return convertStringsToLabels(Collections.singleton(labelsString));
            }
            catch (FieldValidationException e)
            {
                log.error("Invalid default value encountered", e);
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public void setDefaultValue(final FieldConfig fieldConfig, final Object value)
    {
        @SuppressWarnings ("unchecked")
        final Set<Label> labels = (Set<Label>) value;
        genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), convertLabelsToString(labels));
    }

    public String getChangelogValue(final CustomField field, final Object value)
    {
        @SuppressWarnings ("unchecked")
        final Set<Label> newLabels = (Set<Label>) value;
        if (newLabels == null)
        {
            return "";
        }
        return convertLabelsToString(newLabels);
    }

    public String getStringFromSingularObject(final Object singularObject)
    {
        @SuppressWarnings ("unchecked")
        final Label label = (Label) singularObject;
        if (label == null)
        {
            return null;
        }
        return label.getLabel();
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        if (string == null)
        {
            return null;
        }
        final int labelLength = StringUtils.length(string.trim());
        if (LabelParser.isValidLabelName(string) && labelLength <= LabelParser.MAX_LABEL_LENGTH)
        {
            return new Label(null, null, string);
        }
        else
        {
            if (labelLength > LabelParser.MAX_LABEL_LENGTH)
            {
                throw new FieldValidationException(authContext.getI18nHelper().getText("label.service.error.label.toolong", string));
            }
            else
            {
                throw new FieldValidationException(authContext.getI18nHelper().getText("label.service.error.label.invalid", string));
            }
        }
    }

    public Set<Long> remove(final CustomField field)
    {
        return labelManager.removeLabelsForCustomField(field.getIdAsLong());
    }

    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        try
        {
            getValueFromCustomFieldParams(relevantParams);
        }
        catch (FieldValidationException e)
        {
            errorCollectionToAddTo.addError(config.getCustomField().getId(), e.getMessage());
        }
    }

    public void createValue(final CustomField field, final Issue issue, final Object value)
    {
        setLabels(field, issue, value);
    }

    public void updateValue(final CustomField field, final Issue issue, final Object value)
    {
        setLabels(field, issue, value);
    }

    private void setLabels(final CustomField field, final Issue issue, final Object value)
    {
        @SuppressWarnings ("unchecked")
        final Set<Label> labels = (Set<Label>) value;
        final Set<String> labelStrings = new LinkedHashSet<String>();
        if (labels != null)
        {
            for (Label label : labels)
            {
                labelStrings.add(label.getLabel());
            }
        }
        //validation should have already happened by now
        labelManager.setLabels(authContext.getUser(), issue.getId(), field.getIdAsLong(), labelStrings, false, false);
    }

    public Object getValueFromCustomFieldParams(CustomFieldParams customFieldParams) throws FieldValidationException
    {
        if (customFieldParams == null || customFieldParams.isEmpty())
        {
            return null;
        }

        final Collection<String> normalParams = (Collection<String>) customFieldParams.getValuesForKey(null); //single field types should not scope their parameters
        if (normalParams == null || normalParams.isEmpty())
        {
            return null;
        }

        return convertStringsToLabels(normalParams);
    }

    public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        @SuppressWarnings ("unchecked")
        final Collection<String> valuesForNullKey = parameters.getValuesForNullKey();
        if (valuesForNullKey != null)
        {
            final StringBuilder ret = new StringBuilder();
            for (String value : valuesForNullKey)
            {
                ret.append(value).append(LabelsSystemField.SEPARATOR_CHAR);
            }
            return ret.toString().trim();
        }
        return null;
    }

    public int compare(final Set<Label> customFieldObjectValue1, final Set<Label> customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        final String stringValue1 = convertLabelsToString(customFieldObjectValue1);
        final String stringValue2 = convertLabelsToString(customFieldObjectValue2);
        if (stringValue1 == null && stringValue2 == null)
        {
            return 0;
        }

        if (stringValue1 == null)
        {
            return 1;
        }

        if (stringValue2 == null)
        {
            return -1;
        }

        return stringValue1.compareTo(stringValue2);
    }



    private String convertLabelsToString(final Set<Label> newLabels)
    {
        if (newLabels == null)
        {
            return null;
        }
        final StringBuilder ret = new StringBuilder();
        for (Label newLabel : newLabels)
        {
            ret.append(newLabel).append(LabelsSystemField.SEPARATOR_CHAR);
        }
        return ret.toString().trim();
    }

    private Object convertStringsToLabels(final Collection<String> labelStrings)
    {
        final Set<Label> ret = new LinkedHashSet<Label>();
        for (String labelString : labelStrings)
        {
            if (labelString.length() > LabelParser.MAX_LABEL_LENGTH)
            {
                throw new FieldValidationException(authContext.getI18nHelper().getText("label.service.error.label.toolong", labelString));
            }
            ret.addAll(LabelParser.buildFromString(labelString));
        }
        return ret;
    }

    public String getEntityName()
    {
        return OfBizLabelStore.TABLE;
    }

    public ExternalCustomFieldValueImpl parse(final Map attributes) throws ParseException
    {
        // <Label id="10037" fieldid="10000" issue="10000" label="TEST"/>
        Null.not("attributes", attributes);

        final String customFieldIdString = (String) attributes.get("fieldid");
        if (StringUtils.isNotBlank(customFieldIdString))
        {
            com.atlassian.jira.imports.project.parser.LabelParser parser = new LabelParserImpl();
            final ExternalLabel externalLabel = parser.parse(attributes);
            
            //need to convert he label to customfieldvalue represenation
            final ExternalCustomFieldValueImpl label = new ExternalCustomFieldValueImpl(externalLabel.getId(),
                    externalLabel.getCustomFieldId(), externalLabel.getIssueId());
            label.setStringValue(externalLabel.getLabel());

            return label;
        }
        return null;
    }

    public EntityRepresentation getEntityRepresentation(final ExternalCustomFieldValueImpl customFieldValue)
    {
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("id", customFieldValue.getId());
        attributes.put("issue", customFieldValue.getIssueId());
        attributes.put("fieldid", customFieldValue.getCustomFieldId());
        attributes.put("label", customFieldValue.getStringValue());

        return new EntityRepresentationImpl(getEntityName(), attributes);
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return new LabelCustomFieldImporter();
    }

    @Override
    public boolean valuesEqual(final Object v1, final Object v2)
    {
        Collection<?> oldLabels = (Collection<?>) v1;
        Collection<?> newLabels = (Collection<?>) v2;
        if (oldLabels == newLabels)
        {
            return true;
        }
        if (oldLabels == null && newLabels.isEmpty())
        {
            return true;
        }
        if (newLabels == null && oldLabels.isEmpty())
        {
            return true;
        }
        return super.valuesEqual(v1, v2);
    }

    static class LabelCustomFieldImporter implements ProjectCustomFieldImporter
    {
        public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
        {
            //add warnings for invalid labels
            final MessageSet messageSet = new MessageSetImpl();
            final String label = customFieldValue.getValue();
            if (!LabelParser.isValidLabelName(label))
            {
                messageSet.addWarningMessage(i18n.getText("label.project.import.error", label));
                messageSet.addWarningMessageInEnglish("Dropping label '" + label + "' because it contains invalid characters.");
            }
            return messageSet;
        }

        public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
        {
            final String label = customFieldValue.getValue();
            // strip out invalid labels
            if (LabelParser.isValidLabelName(label))
            {
                return new MappedCustomFieldValue(label);
            }
            return new MappedCustomFieldValue(null);
        }
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitLabels(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitLabels(LabelsCFType labelsCustomFieldType);
    }
}
