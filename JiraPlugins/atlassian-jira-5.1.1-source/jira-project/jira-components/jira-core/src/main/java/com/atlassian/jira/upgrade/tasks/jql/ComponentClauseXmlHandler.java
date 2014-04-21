package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents a component and some values
 * provided the "old-style" component parameter XML.
 *
 * @since v4.0
 */
public class ComponentClauseXmlHandler extends AbstractSimpleClauseXmlHandler implements ClauseXmlHandler
{
    public ComponentClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        super(fieldFlagOperandRegistry);
    }

    protected boolean xmlFieldIdSupported(final String xmlFieldId)
    {
        return SystemSearchConstants.forComponent().getIndexField().equals(xmlFieldId);
    }

    public boolean isSafeToNamifyValue()
    {
        return true;
    }
}