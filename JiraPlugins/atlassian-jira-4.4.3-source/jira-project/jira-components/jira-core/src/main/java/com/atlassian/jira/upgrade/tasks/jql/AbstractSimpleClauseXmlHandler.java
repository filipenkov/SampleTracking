package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.util.TextUtils;
import electric.xml.Element;
import electric.xml.Elements;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class that can iterate through a block of XML with value blocks inside and produce a Clause from it.
 *
 * @since v4.0
 */
public abstract class AbstractSimpleClauseXmlHandler implements ClauseXmlHandler
{
    private static final Logger log = Logger.getLogger(AbstractSimpleClauseXmlHandler.class);
    private static final String VALUE = "value";
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;

    protected AbstractSimpleClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
    }

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String xmlFieldId = el.getName();
        return convertXmlToClause(el, xmlFieldId);
    }

    protected ConversionResult convertXmlToClause(final Element el, final String xmlFieldId)
    {
        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);
        if (clauseName == null)
        {
            log.warn("Trying to generate a clause for field with id '" + xmlFieldId + "' and no corresponding clause name could be found.");
            return new FailedConversionResult(xmlFieldId);
        }

        final List<String> values = getValuesFromElement(el, xmlFieldId);

        // Not sure why this data would exist, but you know JIRA :)
        if (values.isEmpty())
        {
            log.warn("Trying to generate a clause for field with id '" + xmlFieldId + "' and could not interpret the values contained in the XML.");
            return new FailedConversionNoValuesResult(xmlFieldId);
        }

        final Clause clause = getClauseForValues(clauseName, values);

        if (!xmlFieldIdSupported(xmlFieldId))
        {
            return new BestGuessConversionResult(clause, xmlFieldId, clauseName);
        }
        else
        {
            return new FullConversionResult(clause);
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }

    protected abstract boolean xmlFieldIdSupported(String xmlFieldId);

    Clause getClauseForValues(final String jqlName, final List<String> values)
    {
        final Operator operator;
        final Operand operand;
        if (values.size() == 1)
        {
            operand = getOperandForValue(jqlName, values.get(0));
            // note: since we know that all the FunctionOperands in the registry refer to List functions, we can assume the IN operator
            operator = (operand instanceof FunctionOperand) ? Operator.IN : Operator.EQUALS;
        }
        else
        {
            operator = Operator.IN;
            List<Operand> operands = new ArrayList<Operand>();
            for (String stringValue : values)
            {
                operands.add(getOperandForValue(jqlName, stringValue));
            }
            operand = new MultiValueOperand(operands);
        }

        return JqlQueryBuilder.newClauseBuilder().addCondition(jqlName, operator, operand).buildClause();
    }

    // All the constant values should be id's which should be able to be converted to a long so that we force the
    // query to look for an id first
    Operand getOperandForValue(String jqlName, String value)
    {
        final Operand operandForFlag = fieldFlagOperandRegistry.getOperandForFlag(jqlName, value);
        if (operandForFlag != null)
        {
            return operandForFlag;
        }
        final Long longValue = getValueAsLong(value);
        if (longValue == null)
        {
            return new SingleValueOperand(value);
        }
        else
        {
            return new SingleValueOperand(longValue);
        }
    }

    List<String> getValuesFromElement(final Element el, final String constantName)
    {
        final List<String> values = new ArrayList<String>();

        Elements valueEls = el.getElements(VALUE);
        while (valueEls.hasMoreElements())
        {
            Element valueEl = valueEls.next();

            if (TextUtils.stringSet(valueEl.getTextString()))
            {
                values.add(valueEl.getTextString());
            }
            else
            {
                // This should not happen for Constant values so lets log it and move on
                log.warn("A saved filter parameter for " + constantName + " contains a value tag with no value.");
            }
        }
        return values;
    }

    private Long getValueAsLong(final String singleValueOperand)
    {
        try
        {
            return new Long(singleValueOperand);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
