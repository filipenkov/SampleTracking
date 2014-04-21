package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ChangeHistoryFieldIdResolver;
import com.atlassian.jira.jql.util.DateRange;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.query.history.AndHistoryPredicate;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.time.DateUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * *
 *
 * @since v4.4
 */
public class HistoryPredicateQueryFactory
{
    private final PredicateOperandResolver predicateOperandResolver;
    private final JqlDateSupport jqlDateSupport;
    private final ChangeHistoryFieldIdResolver changeHistoryFieldIdResolver;

    private static final Date MAX_DATE = new Date(Long.MAX_VALUE);
    private static final Date MIN_DATE = new Date(0);
    private static final Query FALSE_QUERY = new BooleanQuery();


    /**
     * @param predicateOperandResolver resolves {@link com.atlassian.query.history.HistoryPredicate} values
     * @param jqlDateSupport Allows you to parse jql dates
     * @param changeHistoryFieldIdResolver
     */

    public HistoryPredicateQueryFactory(final PredicateOperandResolver predicateOperandResolver, final JqlDateSupport jqlDateSupport, ChangeHistoryFieldIdResolver changeHistoryFieldIdResolver)
    {
        this.predicateOperandResolver = predicateOperandResolver;
        this.jqlDateSupport = jqlDateSupport;
        this.changeHistoryFieldIdResolver = changeHistoryFieldIdResolver;
    }

    public BooleanQuery makePredicateQuery(User searcher, String field, final HistoryPredicate historyPredicate, boolean isChangedSearch)
    {
        final BooleanQuery predicateQuery = new BooleanQuery();
        final ArrayList<TerminalHistoryPredicate> terminalPredicates = new ArrayList<TerminalHistoryPredicate>();
        if (historyPredicate instanceof AndHistoryPredicate)
        {
            for (HistoryPredicate predicate : ((AndHistoryPredicate) historyPredicate).getPredicates())
            {
                terminalPredicates.add((TerminalHistoryPredicate) predicate);
            }
        }
        else
        {
            terminalPredicates.add((TerminalHistoryPredicate) historyPredicate);
        }
        for (TerminalHistoryPredicate predicate : terminalPredicates)
        {
            makeBooleanQuery(searcher, field, predicate, predicateQuery, isChangedSearch);
        }
        return predicateQuery;
    }

    private void makeBooleanQuery(User searcher, String field, final TerminalHistoryPredicate predicate, final BooleanQuery predicateQuery, boolean isChangedSearch)
    {
        checkNotNull(predicate, "Must provide a predicate");
        checkNotNull(predicateQuery, "must provide a predicateQuery");
        final Operator operator = predicate.getOperator();
        final Operand operand = predicate.getOperand();
        final List<QueryLiteral> operandValues = predicateOperandResolver.getValues(searcher, operand);
        if (operandValues == null || operandValues.isEmpty())
        {
            return;
        }
        if (Operator.BY.equals(operator))
        {
            makeBYQuery(predicateQuery, operandValues);
        }
        if (Operator.TO.equals(operator))
        {
             makeChangedQuery(field, predicateQuery, operandValues, true);
        }
        if (Operator.FROM.equals(operator))
        {
              makeChangedQuery(field, predicateQuery, operandValues, false);
        }
        if (OperatorClasses.CHANGE_HISTORY_DATE_PREDICATES.contains(operator))
        {
            if (Operator.DURING.equals(operator))
            {
                makeDURINGQuery(predicateQuery, operandValues, field, isChangedSearch);
            }
            else
            {
                if (Operator.ON.equals(operator))
                {
                    makeONQuery(predicateQuery, operandValues, field, isChangedSearch);
                }
                else
                {
                    makeBEFOREorAFTERQuery(operator, predicateQuery, operandValues, field, isChangedSearch);
                }
            }
        }
    }

    private void makeChangedQuery(String fieldName, BooleanQuery predicateQuery, List<QueryLiteral> operandValues, boolean isUpperBounds)
    {
        BooleanQuery changedQuery = new BooleanQuery();
        String documentField = isUpperBounds ? DocumentConstants.NEW_VALUE : DocumentConstants.OLD_VALUE;
        for (QueryLiteral literal : operandValues)
        {

            Collection<String> ids = changeHistoryFieldIdResolver.resolveIdsForField(fieldName, literal, literal.isEmpty());
            for (String id:ids)
            {
                changedQuery.add( new TermQuery(new Term(fieldName.toLowerCase() + "." + documentField, encodeProtocol(id))),
                    BooleanClause.Occur.SHOULD);
            }
        }
        predicateQuery.add(changedQuery, BooleanClause.Occur.MUST);
    }

    private void makeBYQuery(BooleanQuery predicateQuery, List<QueryLiteral> operandValues)
    {
        BooleanQuery userQuery = new BooleanQuery();
        for (QueryLiteral literal : operandValues)
        {
            userQuery.add(new TermQuery(new Term(DocumentConstants.CHANGE_ACTIONER,
                    encodeProtocol(literal.getStringValue()))),
                    BooleanClause.Occur.SHOULD);
        }
        predicateQuery.add(userQuery, BooleanClause.Occur.MUST);
    }

    private void makeDURINGQuery(BooleanQuery predicateQuery, List<QueryLiteral> operandValues, String field, boolean isChangedSearch)
    {
        // make it resilient in the face of bad AST
        if (operandValues.size() < 2)
        {
            // this got past validation but we need to be resilient in the face of adversity so false it is
            predicateQuery.add(FALSE_QUERY, BooleanClause.Occur.MUST); // << -- or them together
        }
        else
        {
            final DateRange bottomBoundDateRange = convertToDateRangeWithImpliedPrecision(operandValues.get(0));
            final DateRange upperBoundDateRange = convertToDateRangeWithImpliedPrecision(operandValues.get(1));

            if (bottomBoundDateRange == null || upperBoundDateRange == null)
            {
                // this got past validation but we need to be resilient in the face of adversity so false it is
                predicateQuery.add(FALSE_QUERY, BooleanClause.Occur.MUST); // << -- or them together
            }
            else
            {
                makeInclusiveQueryBasedOnDates(predicateQuery, field, bottomBoundDateRange.getLowerDate(), upperBoundDateRange.getUpperDate(), isChangedSearch);
            }
        }
    }

    private void makeONQuery(BooleanQuery predicateQuery, List<QueryLiteral> operandValues, String field, boolean isChangedSearch)
    {
        BooleanQuery query = new BooleanQuery();
        for (QueryLiteral literal : operandValues)
        {
            DateRange dateRange = convertToDateRangeWithImpliedPrecision(literal);
            if (dateRange == null)
            {
                // this got past validation but we need to be resilient in the face of adversity so false it is
                query.add(FALSE_QUERY, BooleanClause.Occur.MUST); // << -- or them together
                return;
            }
            else
            {
                BooleanQuery condition = new BooleanQuery();

                makeInclusiveQueryBasedOnDates(condition, field, dateRange.getLowerDate(), dateRange.getUpperDate(), isChangedSearch);
                query.add(condition, BooleanClause.Occur.SHOULD); // << -- or them together
            }
        }
        predicateQuery.add(query, BooleanClause.Occur.MUST);
    }

    private Date addOneUnit(Date lowerDate)
    {
        return DateUtils.addSeconds(lowerDate,1);
    }

    private void makeBEFOREorAFTERQuery(Operator operator, BooleanQuery predicateQuery, List<QueryLiteral> operandValues, String field, boolean isChangedSearch)
    {
        QueryLiteral literal = operandValues.get(0);

        DateRange dateRange = convertToDateRangeWithImpliedPrecision(literal);

        if (dateRange == null)
        {
            // this got past validation but we need to be resilient in the face of adversity so false it is
            predicateQuery.add(FALSE_QUERY, BooleanClause.Occur.MUST); // << -- or them together
        }
        else if (Operator.BEFORE.equals(operator))
        {
            makeExclusiveQueryBasedOnDates(predicateQuery, field, MIN_DATE, dateRange.getLowerDate(), isChangedSearch);
        }
        else if (Operator.AFTER.equals(operator))
        {
            makeExclusiveQueryBasedOnDates(predicateQuery, field, addOneUnit(dateRange.getUpperDate()), MAX_DATE, isChangedSearch);
        }
    }

    public void makeExclusiveQueryBasedOnDates(BooleanQuery bq, String field, Date fromDate, Date toDate, boolean isChangedSearch)
    {
        makeTermQueryImpl(bq, field, fromDate,toDate,false, isChangedSearch);
    }

    public void makeInclusiveQueryBasedOnDates(BooleanQuery bq, String field, Date fromDate, Date toDate, boolean isChangedSearch)
    {
        makeTermQueryImpl(bq, field, fromDate,toDate,true, isChangedSearch);
    }

    public void makeTermQueryImpl(BooleanQuery bq, String field, Date fromDate, Date toDate, boolean inclusiveSearch, boolean isChangedSearch)
    {
        //Chnaged searches work slightly diiferently to WAs searches - in this case the change itrself must happen in the date range
        if (fromDate != null && toDate != null)
        {
            //
            // when we are in inclsuive mode, we need to add one miniumum unit of time so that we form
            // a proper range for Lucence to MISS any records that are updated 1 second AFTER the lower range (ch_nextchangedate) of the
            // one we are looking for.
            //
            fromDate = (inclusiveSearch ? addOneUnit(fromDate) : fromDate);
            String searchStart = jqlDateSupport.getIndexedValue(fromDate);
            String searchEnd = jqlDateSupport.getIndexedValue(toDate);
            // startSearch <= NEXT_CHANGE_DATE  AND  CHANGE_DATE <= searchEND
            if (isChangedSearch)
            {
                bq.add(new TermRangeQuery(DocumentConstants.CHANGE_DATE, searchStart, searchEnd, true, inclusiveSearch), BooleanClause.Occur.MUST);
            }
            else
            {
                bq.add(new TermRangeQuery(field+"."+DocumentConstants.NEXT_CHANGE_DATE, searchStart, null, inclusiveSearch, true), BooleanClause.Occur.MUST);
                bq.add(new TermRangeQuery(DocumentConstants.CHANGE_DATE, null, searchEnd, true, inclusiveSearch), BooleanClause.Occur.MUST);
            }
        }
    }


    private DateRange convertToDateRangeWithImpliedPrecision(QueryLiteral literal)
    {
        return literal == null ? null : literal.getLongValue() != null ?
                jqlDateSupport.convertToDateRange(literal.getLongValue()) :
                jqlDateSupport.convertToDateRangeWithImpliedPrecision(literal.getStringValue());
    }

    private String encodeProtocol(String changeItem)
    {
        return DocumentConstants.CHANGE_HISTORY_PROTOCOL + (changeItem == null ? "" : changeItem.toLowerCase());
    }
}
