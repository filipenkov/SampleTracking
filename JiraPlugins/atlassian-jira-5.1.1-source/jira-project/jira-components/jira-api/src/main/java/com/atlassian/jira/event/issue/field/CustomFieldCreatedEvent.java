package com.atlassian.jira.event.issue.field;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Event indicating a custom field has been created.
 *
 * @since v5.1
 */
public class CustomFieldCreatedEvent extends AbstractCustomFieldEvent
{
    public CustomFieldCreatedEvent(CustomField customField)
    {
        super(customField);
    }
}
