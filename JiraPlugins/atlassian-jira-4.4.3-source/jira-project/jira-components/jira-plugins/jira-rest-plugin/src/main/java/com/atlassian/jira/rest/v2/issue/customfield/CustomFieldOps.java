package com.atlassian.jira.rest.v2.issue.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.rest.api.field.FieldBean;

import java.util.Map;

/**
 * This interface specifies the methods that exist for custom field-related functionality in the JIRA REST plugin.
 *
 * @since v4.2
 */
public interface CustomFieldOps
{
    /**
     * Returns a Map containing an entry for each custom field in the given Issue. Each entry contains the custom field
     * name as the key, and a CustomFieldBean as the value. If this CustomFieldOps is now able to marshall a given
     * custom field type, or if it encounters an error while doing so, the CustomFieldBean's value property will contain
     * a null reference.
     *
     * @param issue the Issue
     * @return a Map<String, CustomFieldBean>
     */
    Map<String, FieldBean> getCustomFields(Issue issue);
}
