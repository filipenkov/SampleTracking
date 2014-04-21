package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.operator.Operator;
import electric.xml.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Converts XML parameters for the WorkRatioParameter into JQL clauses.
 *
 * Note that when a "minimum" and "maximum" value are specified, the resultant XML contains two distinct parameter
 * elements: 1 for minimum and 1 for maximum. So, the clauses have to be generated separately, but that is okay because
 * the produced clause will be ANDed with clauses from other parameters anyway.
 *
 * @since v4.0
 */
public class WorkRatioClauseXmlHandler implements ClauseXmlHandler
{
    private static final Logger log = Logger.getLogger(WorkRatioClauseXmlHandler.class);
    private static final String WORK_RATIO_ELEMENT_NAME = "workratio";
    private static final String NAME_ATTR = "name";
    private static final String VALUE_ATTR = "value";
    private static final String MIN_SUFFIX = "min";
    private static final String MAX_SUFFIX = "max";

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String xmlFieldId = el.getName();

        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);
        if (clauseName == null)
        {
            log.warn("Trying to generate a work ratio clause for element with name '" + xmlFieldId + "' and can not find a clause name to map to.");
            return new FailedConversionResult(xmlFieldId);
        }

        final String nameAttr = el.getAttributeValue(NAME_ATTR);
        if (StringUtils.isBlank(nameAttr))
        {
            log.warn("Malformed workratio parameter: no name attribute specified in element '" + el.toString() + "'.");
            return new FailedConversionNoValuesResult(xmlFieldId);
        }

        final String[] nameParts = nameAttr.split(":");
        if (nameParts.length != 2)
        {
            log.warn("Malformed workratio parameter: unexpected number of parts in name attribute '" + nameAttr + "'.");
            return new FailedConversionNoValuesResult(xmlFieldId);
        }

        final String suffix = nameParts[1];
        final boolean isMin;
        if (MIN_SUFFIX.equals(suffix))
        {
            isMin = true;
        }
        else if (MAX_SUFFIX.equals(suffix))
        {
            isMin = false;
        }
        else
        {
            log.warn("Malformed workratio parameter: unknown name suffix '" + suffix + "'.");
            return new FailedConversionNoValuesResult(xmlFieldId);
        }

        final String valueStr = el.getAttributeValue(VALUE_ATTR);
        Long value = null;
        if (!StringUtils.isBlank(valueStr))
        {
            try
            {
                value = new Long(valueStr);
            }
            catch (NumberFormatException e)
            {
                // we will log and return null later
            }
        }

        if (value == null)
        {
            log.warn("Malformed workratio parameter: bad value '" + valueStr + "'.");
            return new FailedConversionNoValuesResult(xmlFieldId);
        }

        final Clause clause;
        if (isMin)
        {
            clause = JqlQueryBuilder.newClauseBuilder().addNumberCondition(clauseName, Operator.GREATER_THAN_EQUALS, value).buildClause();
        }
        else
        {
            clause = JqlQueryBuilder.newClauseBuilder().addNumberCondition(clauseName, Operator.LESS_THAN_EQUALS, value).buildClause();
        }

        if (WORK_RATIO_ELEMENT_NAME.equals(xmlFieldId))
        {
            return new FullConversionResult(clause);
        }
        else
        {
            return new BestGuessConversionResult(clause, xmlFieldId, clauseName);
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }
}
