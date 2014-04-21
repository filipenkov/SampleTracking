package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.operand.Operand;

import java.util.List;

/**
 *  Responsible for validating {@link com.atlassian.query.operand.Operand}s and extracting the
 *  String values from them.
 *
 * @since v4.3
 */
public interface PredicateOperandResolver
{
    List<QueryLiteral> getValues(User searcher, Operand operand);

           /**
     * Returns true if the operand represents an EMPTY operand.
     *
     * @param operand the operand to check if it is a EMPTY operand
     * @return true if the operand is an EMPTY operand, false otherwise.
     */
    boolean isEmptyOperand(User searcher, Operand operand);

    /**
     * Returns true if the passed operand is a function call.
     *
     * @param operand the operand to check. Cannot be null.
     * @return true of the passed operand is a function operand, false otherwise.
     */
    boolean isFunctionOperand(User searcher, Operand operand);

    /**
     * Returns true if the passed operand returns a list of values.
     *
     * @param operand the operand to check. Cannot be null.
     * @return true if the passed operand returns a list of values or false otherwise.
     */
    boolean isListOperand(User searcher, Operand operand);

}
