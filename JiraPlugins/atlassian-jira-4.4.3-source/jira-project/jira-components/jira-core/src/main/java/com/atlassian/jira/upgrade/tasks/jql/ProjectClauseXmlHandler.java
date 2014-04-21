package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents a project and some values
 * provided the "old-style" project parameter XML.
 *
 * @since v4.0
 */
public class ProjectClauseXmlHandler extends AbstractSimpleClauseXmlHandler implements ClauseXmlHandler
{
    public ProjectClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        super(fieldFlagOperandRegistry);
    }

    protected boolean xmlFieldIdSupported(final String xmlFieldId)
    {
        return SystemSearchConstants.forProject().getIndexField().equals(xmlFieldId);
    }

    public boolean isSafeToNamifyValue()
    {
        return true;
    }
}
