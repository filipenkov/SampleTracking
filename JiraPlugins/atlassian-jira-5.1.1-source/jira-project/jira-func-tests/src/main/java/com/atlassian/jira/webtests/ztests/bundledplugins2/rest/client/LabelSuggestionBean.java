package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @since v5.0
 */
@JsonIgnoreProperties( ignoreUnknown = true)
public class LabelSuggestionBean
{
    public String label;
}
