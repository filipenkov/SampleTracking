package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;


import com.atlassian.jira.rest.api.issue.IssueFields;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public class OperationalUpdateRequest
{
    @JsonProperty
    private IssueFields fields;

    @JsonProperty
    private Map<String, List<Map<String, Object>>> update;

    public OperationalUpdateRequest(Map<String, List<Map<String, Object>>> update)
    {
        this.update = update;
    }

    public void setFields(IssueFields fields)
    {
        this.fields = fields;
    }
}
