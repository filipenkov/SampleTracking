package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collection;

/**
 * Handles the IssueConstants old SearchParameters and is able to convert them to the equivalent JQL clauses.
 *
 * This actually can handle ANY valid document constant since that is the way the old SearchParameter was coded,
 * so in case any plugin developers were using this Parameter type in a non-standard way we need to handle
 * document constants past what we used within JIRA. See the old isValid method on the Parameters.
 *
 * Handles:
 * - com.atlassian.jira.issue.search.parameters.lucene.PriorityParameter
 * - com.atlassian.jira.issue.search.parameters.lucene.ResolutionParameter
 * - com.atlassian.jira.issue.search.parameters.lucene.StatusParameter
 * - com.atlassian.jira.issue.search.parameters.lucene.IssueConstantsParameter
 *
 * @since v4.0
 */
public class ConstantsClauseXmlHandler extends AbstractSimpleClauseXmlHandler implements ClauseXmlHandler
{
    private final Collection<String> supportedConstantNames;

    public ConstantsClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        super(fieldFlagOperandRegistry);
        supportedConstantNames = CollectionBuilder.newBuilder(SystemSearchConstants.forPriority().getIndexField(),
                SystemSearchConstants.forStatus().getIndexField(),
                SystemSearchConstants.forResolution().getIndexField(),
                SystemSearchConstants.forIssueType().getIndexField()).asCollection();
    }

    protected boolean xmlFieldIdSupported(final String xmlFieldId)
    {
        return supportedConstantNames.contains(xmlFieldId);
    }

    public boolean isSafeToNamifyValue()
    {
        return true;
    }
}
