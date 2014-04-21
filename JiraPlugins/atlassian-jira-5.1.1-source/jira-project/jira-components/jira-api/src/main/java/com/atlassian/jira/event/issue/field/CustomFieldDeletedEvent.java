package com.atlassian.jira.event.issue.field;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Event indicating a custom field has been deleted.
 *
 * @since v5.1
 */
public class CustomFieldDeletedEvent extends AbstractCustomFieldEvent
{
    public CustomFieldDeletedEvent(CustomField customField)
    {
        super(customField);
    }
}
