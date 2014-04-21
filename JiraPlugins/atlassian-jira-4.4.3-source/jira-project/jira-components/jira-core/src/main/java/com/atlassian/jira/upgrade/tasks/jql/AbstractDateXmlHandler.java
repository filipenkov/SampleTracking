package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.operator.Operator;
import electric.xml.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A class that will help with the XML parsing of date based old skool parameters.
 *
 * @since v4.0
 */
abstract class AbstractDateXmlHandler implements ClauseXmlHandler
{
    private static final Logger log = Logger.getLogger(AbstractDateXmlHandler.class);
    private final Collection<String> supportedXmlFieldNames;

    protected AbstractDateXmlHandler(final Collection<String> supportedXmlFieldNames)
    {
        this.supportedXmlFieldNames = notNull("supportedXmlFieldNames", supportedXmlFieldNames);
    }

    public ConversionResult convertXmlToClause(final Element el)
    {
        notNull("el", el);

        final String xmlFieldId = el.getName();
        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);
        if (clauseName == null)
        {
            log.warn("Trying to generate a date clause for field with id '" + xmlFieldId + "' and no corresponding clause name could be found.");
            return new FailedConversionResult(xmlFieldId);
        }

        final Clause fromClause = createClause(clauseName, getLowerBound(clauseName, el), Operator.GREATER_THAN_EQUALS);
        final Clause toClause = createClause(clauseName, getUpperBound(clauseName, el), Operator.LESS_THAN_EQUALS);
        final Clause resultClause = getResultClause(fromClause, toClause);

        if (resultClause == null)
        {
            return new FailedConversionNoValuesResult(xmlFieldId);
        }
        else if (xmlFieldIdSupported(xmlFieldId))
        {
            return new FullConversionResult(resultClause);
        }
        else
        {
            return new BestGuessConversionResult(resultClause, xmlFieldId,  clauseName);
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }

    private Clause getResultClause(final Clause fromClause, final Clause toClause)
    {
        final Clause resultClause;
        if (fromClause == null)
        {
            resultClause = toClause;
        }
        else if (toClause != null)
        {
            resultClause = new AndClause(fromClause, toClause);
        }
        else
        {
            resultClause = fromClause;
        }
        return resultClause;
    }

    private Clause createClause(final String fieldName, final String date, final Operator operator)
    {
        if (StringUtils.isNotBlank(date))
        {
            return JqlQueryBuilder.newBuilder().where().addStringCondition(fieldName, operator, date).buildClause();
        }
        else
        {
            return null;
        }
    }

    protected boolean xmlFieldIdSupported(final String xmlFieldId)
    {
        return supportedXmlFieldNames.isEmpty() || supportedXmlFieldNames.contains(xmlFieldId);
    }

    /**
     * Return the lower date from the parameter. The return should be a value that can be placed directly into a Clause.
     * Can return null to indicate that there is no bound.
     *
     * @param fieldName the field name of the generated clause
     * @param element the element that is being parsed.
     * @return the JQL string representation of the lower date. Can be null to indicate that there is no lower date.
     */
    protected abstract String getLowerBound(final String fieldName, Element element);

    /**
     * Return the upper date from the parameters. The return should be a value that can be converted directly into a
     * Clause. Can return null to indicate that there is no bound.
     *
     * @param fieldName the field name of the generated clause
     * @param element the element that is being parsed.
     * @return the JQL string representation of the upper date. Can be null to indicate that there is no lower date.
     */
    protected abstract String getUpperBound(final String fieldName, Element element);
}
