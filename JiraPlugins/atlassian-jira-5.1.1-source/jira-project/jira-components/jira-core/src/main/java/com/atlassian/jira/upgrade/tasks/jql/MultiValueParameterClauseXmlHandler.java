package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.query.clause.Clause;
import electric.xml.Element;

import java.util.List;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents an Multivalue parameter and some values.
 *
 * @since v4.0
 */
public class MultiValueParameterClauseXmlHandler extends AbstractSimpleClauseXmlHandler implements ClauseXmlHandler
{
    private static final String MULTI_KEY_SEARCHER_ELEMENT_NAME = "key";
    private static final String MULTI_KEY_TYPE_KEY = "com.atlassian.jira.toolkit:multikeyfield";

    private final CustomFieldManager customFieldManager;

    public MultiValueParameterClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry, final CustomFieldManager customFieldManager)
    {
        super(fieldFlagOperandRegistry);
        this.customFieldManager = customFieldManager;
    }

    public ConversionResult convertXmlToClause(final Element el)
    {
        String xmlFieldId = el.getName();
        if (MULTI_KEY_SEARCHER_ELEMENT_NAME.equalsIgnoreCase(xmlFieldId))
        {
            // MASSIVE HACK WARNING! We need to handle the MultiIssueKeySearcher from the JIRA Toolkit specially, because
            // the XML element contains no information about which instance of the CustomField it actually belongs to.
            // So, we will look for instances of that CustomFieldType, and use either the first that we get, or if none,
            // default to the new 'key' JQL clause.
            /*
                This is what the old XML looks like for this hack field

                <parameter class='com.atlassian.jira.issue.search.parameters.lucene.GenericMultiValueParameter'>
                  <key andQuery='false'>
                    <value>HSP-1</value>
                  </key>
                </parameter>
             */

            final List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
            for (CustomField customField : customFields)
            {
                if (MULTI_KEY_TYPE_KEY.equalsIgnoreCase(customField.getCustomFieldType().getKey()))
                {
                    xmlFieldId = customField.getId();
                    return super.convertXmlToClause(el, xmlFieldId);
                }
            }

            // couldn't find a custom field instance - default to 'key'
            ConversionResult conversionResult = super.convertXmlToClause(el);
            if (ConversionResultType.FULL_CONVERSION == conversionResult.getResultType())
            {
                // Since we know we did not exactly find what we were looking for lets wrap this thing in a best guess
                final Clause clause = conversionResult.getClause();
                conversionResult = new BestGuessConversionResult(clause, xmlFieldId, clause.getName());
            }
            return conversionResult;
        }
        else
        {
            return super.convertXmlToClause(el);
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }

    protected boolean xmlFieldIdSupported(final String xmlFieldId)
    {
        return true;
    }
}
