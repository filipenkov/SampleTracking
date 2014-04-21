package com.atlassian.jira.event.issue.field;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Event indicating a custom field has been updated.
 *
 * @since v5.1
 */
public class CustomFieldUpdatedEvent extends AbstractCustomFieldEvent
{
    public CustomFieldUpdatedEvent(CustomField customField)
    {
        super(customField);
    }

    public CustomFieldUpdatedEvent(Long id, String customFieldId, String fieldType)
    {
        super(id, customFieldId, fieldType);
    }
}
