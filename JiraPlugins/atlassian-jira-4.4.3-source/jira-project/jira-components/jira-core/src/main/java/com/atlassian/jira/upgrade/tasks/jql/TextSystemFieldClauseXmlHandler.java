package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.operator.Operator;
import electric.xml.Element;
import electric.xml.Elements;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates a ConversionResult that tries to convert the XML parameter used for storing the multi field parameter
 * that stored the:
 * - Description
 * - Summary
 * - Comment
 * - Environment
 *
 * field data. It could also have been used for other purposes and therefore tries to be very forgiving, creating
 * messages for when it makes a best guess.
 *
 * @since v4.0
 */
public class TextSystemFieldClauseXmlHandler extends AbstractSimpleClauseXmlHandler implements ClauseXmlHandler
{
    private static final Logger log = Logger.getLogger(TextSystemFieldClauseXmlHandler.class);
    private static final String FIELD = "field";
    private static final String QUERY = "query";
    private final Collection<String> supportedFields;

    public TextSystemFieldClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        super(fieldFlagOperandRegistry);
        this.supportedFields = CollectionBuilder.<String>newBuilder(SystemSearchConstants.forSummary().getUrlParameter(),
                SystemSearchConstants.forDescription().getUrlParameter(),
                SystemSearchConstants.forComments().getUrlParameter(),
                SystemSearchConstants.forEnvironment().getUrlParameter()).asCollection();
    }

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String elementName = el.getName();

        final List<String> fields = getFields(el);
        final String query = getQuery(el);

        if (query == null || fields.isEmpty())
        {
            return new FailedConversionNoValuesResult(elementName);
        }

        final Collection<String> fieldsInError = new ArrayList<String>();
        final Map<String, String> fieldsInBestGuess = new HashMap<String, String>();

        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultOr();
        for (String field : fields)
        {
            final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(field);
            if (clauseName == null)
            {
                log.warn("Trying to generate a clause for field with id '" + field + "' and no corresponding clause name could be found.");
                // This makes us a best guess unless they all fall into this
                fieldsInError.add(field);
                continue;
            }

            builder.addStringCondition(clauseName, Operator.LIKE, query).buildClause();
            if (!xmlFieldIdSupported(field))
            {
                // This makes us a best guess
                fieldsInBestGuess.put(field, clauseName);
            }
        }

        if (fieldsInError.size() == fields.size())
        {
            // All were in error so the whole conversion is an error
            return new FailedConversionResult(elementName);
        }

        return new AggregatingConversionResult(builder.buildClause(), elementName, fieldsInError, fieldsInBestGuess);
    }

    static class AggregatingConversionResult extends BestGuessConversionResult 
    {
        private final Collection<FailedConversionResult> failures;
        private final Collection<BestGuessConversionResult> bestGuesses;

        public AggregatingConversionResult(final Clause clause, final String oldXmlFieldName, final Collection<String> errorFieldNames, final Map<String, String> bestGuessFieldNames)
        {
            super(clause, oldXmlFieldName, oldXmlFieldName);
            // Lets build the Conversion results we need to hold
            failures = new ArrayList<FailedConversionResult>();
            for (String errorFieldName : errorFieldNames)
            {
                failures.add(new FailedConversionResult(errorFieldName));
            }
            bestGuesses = new ArrayList<BestGuessConversionResult>();
            for (Map.Entry<String, String> bestGuessEntry : bestGuessFieldNames.entrySet())
            {
                bestGuesses.add(new BestGuessConversionResult(clause, bestGuessEntry.getKey(), bestGuessEntry.getValue()));
            }
        }

        @Override
        public ConversionResultType getResultType()
        {
            // If there were no failures or best guesses then we must have had a full conversion
            return (failures.isEmpty() && bestGuesses.isEmpty()) ? ConversionResultType.FULL_CONVERSION : ConversionResultType.BEST_GUESS_CONVERSION;
        }

        @Override
        public void setFieldManager(final FieldManager fieldManager)
        {
            super.setFieldManager(fieldManager);
            for (BestGuessConversionResult bestGuess : bestGuesses)
            {
                bestGuess.setFieldManager(fieldManager);
            }
        }

        public String getMessage(final I18nHelper i18nHelper, final String savedFilterName)
        {
            StringBuilder sb = new StringBuilder();

            // Lets include all the failure messages
            for (Iterator<FailedConversionResult> failedConversionResultIterator = failures.iterator(); failedConversionResultIterator.hasNext();)
            {
                FailedConversionResult failedConversionResult = failedConversionResultIterator.next();
                sb.append(failedConversionResult.getMessage(i18nHelper, savedFilterName));
                if (failedConversionResultIterator.hasNext() || !bestGuesses.isEmpty())
                {
                    sb.append("\n");
                }
            }
            // Lets include all the best guess messages
            for (Iterator<BestGuessConversionResult> bestGuessConversionResultIterator = bestGuesses.iterator(); bestGuessConversionResultIterator.hasNext();)
            {
                BestGuessConversionResult bestGuessConversionResult = bestGuessConversionResultIterator.next();
                sb.append(bestGuessConversionResult.getMessage(i18nHelper, savedFilterName));
                if (bestGuessConversionResultIterator.hasNext())
                {
                    sb.append("\n");
                }
            }

            return sb.toString();
        }
    }

    protected boolean xmlFieldIdSupported(final String xmlFieldId)
    {
        return supportedFields.contains(xmlFieldId);
    }

    private String getQuery(final Element el)
    {
        Elements valueEls = el.getElements(QUERY);
        if (valueEls.size() != 1)
        {
            log.error("A saved filter for multi text searching does not contain only one 'query' element.");
            return null;
        }
        return valueEls.first().getTextString();
    }

    private List<String> getFields(final Element el)
    {
        List<String> fields = new ArrayList<String>();
        // get a hold of all the fields that are referenced in the XML
        Elements valueEls = el.getElements(FIELD);
        while (valueEls.hasMoreElements())
        {
            Element valueEl = valueEls.next();

            if (StringUtils.isNotBlank(valueEl.getTextString()))
            {
                fields.add(valueEl.getTextString());
            }
            else
            {
                // This should not happen for Constant values so lets log it and move on
                log.warn("A saved filter parameter for multi text field searching contains a 'field' tag with no value.");
            }
        }
        return fields;
    }
}
