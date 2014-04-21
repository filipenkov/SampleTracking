package com.atlassian.jira.webtests.ztests.workflow;

/**
 * @since v3.13
 */
public class ExpectedChangeHistoryItem
{
    private String fieldName;
    private String oldValue;
    private String newValue;

    public ExpectedChangeHistoryItem(String fieldName, String oldValue, String newValue)
    {
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }
}
