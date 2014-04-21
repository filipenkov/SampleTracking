package com.atlassian.query.clause;

import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.operator.Operator;

/**
 * Used to represent changed clause in the query tree
 *
 * @since v5.0
 */
public interface ChangedClause extends Clause
{
    String getField();

    HistoryPredicate getPredicate();

    Operator getOperator();

}
