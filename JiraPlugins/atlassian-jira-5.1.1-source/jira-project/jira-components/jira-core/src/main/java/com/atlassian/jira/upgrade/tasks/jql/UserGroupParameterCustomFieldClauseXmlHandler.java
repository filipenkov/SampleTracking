package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents a user custom field with a specified group value.
 *
 * @since v4.0
 */
public class UserGroupParameterCustomFieldClauseXmlHandler implements ClauseXmlHandler
{
    private static final Logger log = Logger.getLogger(UserGroupParameterCustomFieldClauseXmlHandler.class);

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String originalXmlFieldId = el.getName();

        // Trim off the _group if there is one
        boolean forGroup = originalXmlFieldId.endsWith(UserClauseXmlHandler.GROUP_SUFFIX);
        final String xmlFieldId;
        if (forGroup)
        {
            xmlFieldId = originalXmlFieldId.substring(0, originalXmlFieldId.indexOf(UserClauseXmlHandler.GROUP_SUFFIX));
        }
        else
        {
            xmlFieldId = originalXmlFieldId;
        }
        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);

        if (clauseName == null)
        {
            log.warn("Trying to generate a clause for field with id '" + originalXmlFieldId + "' and no corresponding clause name could be found.");
            return new FailedConversionResult(originalXmlFieldId);
        }

        final String value = el.getAttributeValue("groupName");
        if (StringUtils.isBlank(value))
        {
            return new FailedConversionNoValuesResult(originalXmlFieldId);
        }
        else
        {
            return new FullConversionResult(createClause(clauseName, value));
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }

    Clause createClause(final String jqlFieldName, final String value)
    {
        return new TerminalClauseImpl(jqlFieldName, Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, value));
    }
}