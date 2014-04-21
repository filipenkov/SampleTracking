package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.Clause;
import electric.xml.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} for custom field parameters.
 *
 * @since v4.0
 */
public abstract class AbstractCustomFieldClauseXmlHandler implements ClauseXmlHandler
{
    private static final Logger log = Logger.getLogger(AbstractCustomFieldClauseXmlHandler.class);
    private final String valueAttribbute;

    public AbstractCustomFieldClauseXmlHandler(String valueAttribbute)
    {
        this.valueAttribbute = notBlank("valueAttribbute", valueAttribbute);
    }

    abstract Clause createClause(String jqlFieldName, String value);

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String xmlFieldId = el.getName();
        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);

        if (clauseName == null)
        {
            log.warn("Trying to generate a clause for field with id '" + xmlFieldId + "' and no corresponding clause name could be found.");
            return new FailedConversionResult(xmlFieldId);
        }

        final String value = el.getAttributeValue(valueAttribbute);
        if (StringUtils.isBlank(value))
        {
            return new FailedConversionNoValuesResult(xmlFieldId);
        }
        else
        {
            return new FullConversionResult(createClause(clauseName, value));
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return true;
    }
}