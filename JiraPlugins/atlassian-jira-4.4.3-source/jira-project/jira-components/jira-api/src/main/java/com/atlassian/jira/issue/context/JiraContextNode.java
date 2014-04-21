package com.atlassian.jira.issue.context;

import com.atlassian.bandana.BandanaContext;

import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

/**
 * A constrcurted {@link IssueContext} with the ability to climb nodes
 */
public interface JiraContextNode extends BandanaContext, IssueContext, Comparable<JiraContextNode>
{
    String FIELD_PROJECT_CATEGORY = "projectcategory";
    String FIELD_PROJECT = "project";
    String FIELD_ISSUE_TYPE = "issuetype";
    // TODO: Is this actually used anywhere?
    String FIELD_CONFIG = "fieldconfiguration";

    public GenericValue getProjectCategory();

    public boolean isInContext(IssueContext issueContext);

    /**
     * Copy the supplied parameters and add new ones.
     * @param props to copy from
     * @return the copied map
     */
    Map<String, Object> appendToParamsMap(Map<String, Object> props);

    /**
     * Appears to be unused.
     * @deprecated
     */
    @Deprecated
    List<JiraContextNode> getChildContexts();
}
