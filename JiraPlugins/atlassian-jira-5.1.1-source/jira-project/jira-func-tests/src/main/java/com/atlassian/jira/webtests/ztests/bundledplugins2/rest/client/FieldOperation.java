package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since v5.0
 */
public class FieldOperation
{
    @JsonProperty
    private String oper;

    @JsonProperty
    private String[] val;

    public FieldOperation(String oper, String... v)
    {
        this.oper = oper;
        this.val = v;
    }
}
