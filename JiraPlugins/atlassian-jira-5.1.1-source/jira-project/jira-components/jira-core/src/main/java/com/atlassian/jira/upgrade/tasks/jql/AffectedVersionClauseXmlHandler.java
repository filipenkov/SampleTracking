package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents an affected version and some values
 * provided the "old-style" affected version parameter XML.
 *
 * @since v4.0
 */
public class AffectedVersionClauseXmlHandler extends AbstractSimpleClauseXmlHandler implements ClauseXmlHandler
{
    public AffectedVersionClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        super(fieldFlagOperandRegistry);
    }

    protected boolean xmlFieldIdSupported(final String xmlFieldId)
    {
        return SystemSearchConstants.forAffectedVersion().getIndexField().equals(xmlFieldId);
    }

    public boolean isSafeToNamifyValue()
    {
        return true;
    }
}