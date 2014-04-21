package com.atlassian.jira.webtest.framework.model.admin;

/**
 * Represents field in the JIRA administration UI. A field may be either a system field, or a custom field. Fields are
 * used in many places, e.g. in the Custom Field administration, to configure screens etc. 
 *
 * @see SystemAdminField
 * @see CustomAdminField
 * 
 * @since v4.3
 */
public interface AdminField
{

    /**
     * Unique ID of the field
     *
     * @return field ID
     */
    String id();

    /**
     * Name of the field exposed in the UI.
     *
     * @return name of the field
     */
    String fieldName();
}
