package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents a custom field with a specified string value.
 *
 * @since v4.0
 */
public class StringRangeParameterClauseXmlHandler implements ClauseXmlHandler
{
    private final String LESS_THAN = "<=";
    private final String GREATER_THAN = ">=";
    private static final Logger log = Logger.getLogger(StringRangeParameterClauseXmlHandler.class);

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String xmlFieldId = el.getName();
        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);

        if (clauseName == null)
        {
            log.warn("Trying to generate a clause for field with id '" + xmlFieldId + "' and no corresponding clause name could be found.");
            return new FailedConversionResult(xmlFieldId);
        }

        final String value = el.getAttributeValue("value");
        final String operator = el.getAttributeValue("operator");
        if (StringUtils.isBlank(value) || StringUtils.isBlank(operator))
        {
            return new FailedConversionNoValuesResult(xmlFieldId);
        }
        else
        {
            final Clause clause;
            if (operator.equals(LESS_THAN))
            {
                clause = new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, value);
            }
            else if (operator.equals(GREATER_THAN))
            {
                clause = new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, value);
            }
            else
            {
                // Did not understand the operator so what can we do?
                return new FailedConversionNoValuesResult(xmlFieldId);
            }
            return new FullConversionResult(clause);
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }
}
