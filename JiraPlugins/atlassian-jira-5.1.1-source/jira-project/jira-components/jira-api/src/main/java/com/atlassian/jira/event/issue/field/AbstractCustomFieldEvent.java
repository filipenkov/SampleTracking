package com.atlassian.jira.event.issue.field;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Abstract event that captures the data relevant to custom field events.
 *
 * @since v5.1
 */
public class AbstractCustomFieldEvent
{
    private final Long id;
    private final String customFieldId;
    private final String fieldType;

    public AbstractCustomFieldEvent(CustomField customField)
    {
        if (null != customField)
        {
            id = customField.getIdAsLong();
            customFieldId = customField.getId();
            fieldType = customField.getCustomFieldType() != null ? customField.getCustomFieldType().getName() : null;
        }
        else
        {
            id = null;
            customFieldId = null;
            fieldType = null;
        }
    }

    public AbstractCustomFieldEvent(Long id, String customFieldId, String fieldType)
    {
        this.id = id;
        this.customFieldId = customFieldId;
        this.fieldType = fieldType;
    }

    /**
     * Returns the ID of the custom field that this event relates to, as a number. Note that the custom field's full ID
     * is returned by {@link #getCustomFieldId()}.
     *
     * @return a Long containing the numeric id of the custom field
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the ID of the custom field that this event relates to. The custom field's string ID will have the form
     * "customfield_XXXXX", where XXXXX is the value of {@link #getId()}.
     *
     * @return a String containing the ID of the custom field
     */
    public String getCustomFieldId()
    {
        return customFieldId;
    }

    public String getFieldType()
    {
        return fieldType;
    }
}
