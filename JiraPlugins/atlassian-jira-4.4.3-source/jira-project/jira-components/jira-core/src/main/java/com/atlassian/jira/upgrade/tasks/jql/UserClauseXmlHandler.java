package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents a user system field and some values
 * provided the "old-style" affected version parameter XML.
 *
 * @since v4.0
 */
public class UserClauseXmlHandler implements ClauseXmlHandler
{
    public static final String GROUP_SUFFIX = "_group";
    private static final Logger log = Logger.getLogger(UserClauseXmlHandler.class);
    private final UserFieldSearchConstantsWithEmpty searchConstants;

    public UserClauseXmlHandler(UserFieldSearchConstantsWithEmpty searchConstants)
    {
        this.searchConstants = searchConstants;
    }

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String originalXmlFieldId = el.getName();

        // Trim off the _group if there is one
        boolean forGroup = originalXmlFieldId.endsWith(GROUP_SUFFIX);
        final String xmlFieldId;
        if (forGroup)
        {
            xmlFieldId = originalXmlFieldId.substring(0, originalXmlFieldId.indexOf(GROUP_SUFFIX));
        }
        else
        {
            xmlFieldId = originalXmlFieldId;
        }

        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);
        if (clauseName == null)
        {
            log.warn("Trying to generate a clause for field with id '" + xmlFieldId + "' and no corresponding clause name could be found.");
            return new FailedConversionResult(originalXmlFieldId);
        }
        String value = (forGroup) ? el.getAttribute("groupName") : el.getAttributeValue("value");
        if (StringUtils.isBlank(value))
        {
            return new FailedConversionNoValuesResult(originalXmlFieldId);
        }

        final Clause clause;
        if (forGroup)
        {
            clause = new TerminalClauseImpl(clauseName, Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, value));
        }
        else
        {
            if (searchConstants.getEmptySelectFlag().equals(value))
            {
                clause = new TerminalClauseImpl(clauseName, Operator.IS, new EmptyOperand());
            }
            else if (searchConstants.getCurrentUserSelectFlag().equals(value))
            {
                clause = new TerminalClauseImpl(clauseName, Operator.EQUALS, new FunctionOperand(CurrentUserFunction.FUNCTION_CURRENT_USER));
            }
            else
            {
                clause = new TerminalClauseImpl(clauseName, Operator.EQUALS, value);
            }
        }

        if (clauseName.equals(searchConstants.getJqlClauseNames().getPrimaryName()))
        {
            return new FullConversionResult(clause);
        }
        else
        {
            return new BestGuessConversionResult(clause, originalXmlFieldId, clauseName);
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }
}