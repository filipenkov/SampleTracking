package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The SearchInputTransformer for the Labels system field.
 *
 * @since v4.2
 */
public class LabelsSearchInputTransformer implements SearchInputTransformer
{
    private final IndexInfoResolver<Label> indexInfoResolver;
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private final JqlOperandResolver operandResolver;
    private final SearchContextVisibilityChecker searchContextVisibilityChecker;

    public LabelsSearchInputTransformer(final IndexInfoResolver<Label> indexInfoResolver, final JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry, final SearchContextVisibilityChecker searchContextVisibilityChecker)
    {
        this.indexInfoResolver = indexInfoResolver;
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
        this.operandResolver = operandResolver;
        this.searchContextVisibilityChecker = searchContextVisibilityChecker;
    }

    public void populateFromParams(final User searcher, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        final String url = SystemSearchConstants.forLabels().getUrlParameter();
        final String values = actionParams.getFirstValueForKey(url);
        if(StringUtils.isNotBlank(values))
        {
            final List<String> cleanValues = new ArrayList<String>();
            final String[] labels = StringUtils.split(values, LabelsSystemField.SEPARATOR_CHAR);
            for (String value : labels)
            {
                cleanValues.add(value.trim());
            }            
            fieldValuesHolder.put(url, cleanValues);
        }
    }

    public void validateParams(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        @SuppressWarnings ("unchecked")
        final List<String> query = (List<String>) fieldValuesHolder.get(SystemSearchConstants.forLabels().getUrlParameter());
        if (query != null && !query.isEmpty())
        {
            for (String labelString : query)
            {
                if (StringUtils.isNotBlank(labelString))
                {
                    final String[] labels = StringUtils.split(labelString, LabelsSystemField.SEPARATOR_CHAR);
                    for (String theLabel : labels)
                    {
                        String label = theLabel.trim();
                        if (!LabelParser.isValidLabelName(label))
                        {
                            errors.addErrorMessage(i18nHelper.getText("label.service.error.label.invalid", label));
                        }
                        if (label.length() > LabelParser.MAX_LABEL_LENGTH)
                        {
                            errors.addErrorMessage(i18nHelper.getText("label.service.error.label.toolong", label));
                        }
                    }
                }
            }
        }
    }

    public void populateFromQuery(final User searcher, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        final Set<String> uncleanedValues = getNavigatorValuesAsStrings(searcher, query, searchContext);

        final List<String> values = new ArrayList<String>(uncleanedValues);
        final List<String> cleanValues = new ArrayList<String>();
        for (String value : values)
        {
            cleanValues.add(value.trim());
        }
        fieldValuesHolder.put(SystemSearchConstants.forLabels().getUrlParameter(), cleanValues);
    }

    Set<String> getNavigatorValuesAsStrings(User searcher, Query query, SearchContext searchContext)
    {
        IndexedInputHelper helper = new DefaultIndexedInputHelper<Label>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        return helper.getAllNavigatorValuesForMatchingClauses(searcher, SystemSearchConstants.forLabels().getJqlClauseNames(), query, searchContext);
    }

    public boolean doRelevantClausesFitFilterForm(final User searcher, final Query query, final SearchContext searchContext)
    {
        return createNavigatorStructureChecker().checkSearchRequest(query, searchContext);
    }

    private NavigatorStructureChecker<Label> createNavigatorStructureChecker()
    {
        return new NavigatorStructureChecker<Label>(SystemSearchConstants.forLabels().getJqlClauseNames(), false, fieldFlagOperandRegistry, operandResolver, indexInfoResolver, searchContextVisibilityChecker);
    }

    public Clause getSearchClause(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        @SuppressWarnings("unchecked")
        final List<String> labels = (List<String>) fieldValuesHolder.get(SystemSearchConstants.forLabels().getUrlParameter());
        if (labels != null && !labels.isEmpty())
        {
            if(labels.size() == 1)
            {
                final String sanitizedLabel = labels.get(0).trim();
                return new TerminalClauseImpl(SystemSearchConstants.forLabels().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new SingleValueOperand(sanitizedLabel));
            }
            else
            {
                final List<Operand> operands = new ArrayList<Operand>();
                for (String label : labels)
                {
                    final String sanitizedLabel = label.trim();
                    operands.add(new SingleValueOperand(sanitizedLabel.trim()));
                }
                return new TerminalClauseImpl(SystemSearchConstants.forLabels().getJqlClauseNames().getPrimaryName(), Operator.IN, new MultiValueOperand(operands));
            }
        }
        return null;
    }
}
