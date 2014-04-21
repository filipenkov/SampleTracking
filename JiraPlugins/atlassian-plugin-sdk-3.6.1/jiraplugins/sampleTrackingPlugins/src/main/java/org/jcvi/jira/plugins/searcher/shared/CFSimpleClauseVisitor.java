package org.jcvi.jira.plugins.searcher.shared;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.query.clause.*;
//required by an interface from the same people who deprecated the class
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;

import java.util.*;

/**
 * <p>User: pedworth
 * Date: 11/11/11</p>
 * <p>WARNING: Contains state.</p>
 * <p>Uses the Visitor pattern, but because of the lack of native double
 * dispatch in java the method used in the callback must already be
 * defined in ClauseVisitor. e.g. Specializing based on sub-classes
 * isn't possible without adding a method to all visitors.</p>
 * <p>
 *     The accepted types are:
 * <ul>
 *   <li>AndClause</li>
 *   <li>NotClause</li>
 *   <li>OrClause</li>
 *   <li>TerminalClause</li>
 *   <li>WasClause (not used in simple searches, which is where we use
 *   this visitor</li>
 * </ul></p>
 * <h3>Implementation</h3>
 * <p>Adds the following methods</p>
 * <ul>
 *   <li>isValid()</li>
 *   <li>getErrors()</li>
 * </ul></p>
 * <h4>Extends: {@link NamedTerminalClauseCollectingVisitor}</h4>
 * <p>{@link NamedTerminalClauseCollectingVisitor}
 * keeps a filtered collection of TerminalClauses that it has visited. The
 * TerminalClauses are filtered by a list of names provided to the constructor.
 * The TerminalClause collection can be accessed via:</p>
 * <p><b>getNamedClauses()</b></p>
 * <p><b>containsNamedClause()</b></p>
 * <h4>Extends: {@link com.atlassian.jira.issue.search.searchers.util.RecursiveClauseVisitor}</h4>
 * <p>RecursiveClauseVisitor implements the whole ClauseVisitor interface and
 * carries out the tree walk, It calls the visit methods of the children
 * at branching nodes. It doesn't actually record anything, this is done by
 * extending classes such as NamedTerminalClauseCollectingVisitor. Overriding
 * implementations of the visit methods should call super.visit to take
 * advantage of this tree walking.</p>
 * <p></p>
 */
//suppressed as the interface requires using User objects that have been deprecated
@SuppressWarnings({"deprecation"})
public class CFSimpleClauseVisitor extends NamedTerminalClauseCollectingVisitor
        implements ClauseVisitor<Void> {
    private static final Logger log = Logger.getLogger(CFSimpleClauseVisitor.class);


    private static final String NULL_TERMINALCLAUSE = "Null Terminal Clause Found";
    private static final String NULL_VALIDNAMES     = "validNames returned Null";
    private static final String NULL_VALIDOPERATORS = "validOperators returned Null";

    private final Set<String> errors = new HashSet<String>();
    //utility class that splits Clause objects into Operands and Operators
    private final JqlOperandResolver jqlOperandResolver;
    private final ClauseNames clauseNames;
    private final Collection<Operator> validOperators;

    /**
     *
     * @param clauseNames     The field that this search is being carried out on
     * @param operators       The operators that can be used with the searcher
     *                        creating this visitor
     * @param operandResolver Converts the reference or value in the search
     *                        string into the type defined by
     */
    public CFSimpleClauseVisitor(ClauseNames clauseNames,
                                 Collection<Operator> operators,
                                 JqlOperandResolver operandResolver) {
        super(clauseNames.getJqlFieldNames());
        this.jqlOperandResolver = operandResolver;
        this.clauseNames = clauseNames;
        this.validOperators = operators;
    }

    //-------------------------------------------------------------------------
    //                  Must Override
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    //                  Public
    //-------------------------------------------------------------------------
    /**
     * <h4>Not Normally Overridden</h4>
     * Tests if the data collected by the Visitor should be used.
     * This is a combination of tests that are done while collecting the
     * Clauses and tests that are carried out when this method is called.
     * isValidClause or isValidClauses should be implemented to handle
     * the post collection testing.
     * @return true if the data is valid.
     */
    public boolean isValid() {
        return errors.size() <= 0 && isValidClauses(getNamedClauses());
    }

    /**
     * <h4>Not Normally Overridden</h4>
     * @return null if there are no errors, an array with one entry per error
     * if there were errors.
     */
    public String[] getErrors() {
        if (errors.size() <= 0) {
            return null; //no errors
        }
        return errors.toArray(new String[errors.size()]);
    }

    //-------------------------------------------------------------------------
    //                  Can Override
    //-------------------------------------------------------------------------
    /**
     * <h4>Can be Overridden</h4>
     * <h4>Override Instead</h4>
     * <ul>
     *  <li>{@link #getValidOperators}</li>
     * </ul>
     * <h4>Description</h4>
     * <p>Provides a point to override if the clauses need to be tested further to
     * determine if they are valid or not.</p>
     * <p>The default implementation uses the results from calling
     * {@link #getClauseNames()}-{ClauseNames.getJqlFieldNames) and
     * {@link #getValidOperators()} to check the clauses name
     * (TerminalClause.getName) and operator (TerminalClause.getOperator)</p>
     * @param clause   The clause to check.
     * @return boolean if the clause should be used to set the FieldValuesHolder
     */
    protected boolean isValidClause(TerminalClause clause) {
        Collection<String>   validNames     = getClauseNames().getJqlFieldNames();
        Collection<Operator> validOperators = getValidOperators();
        //intelij can't tell that errorUnless is doing the null check
        //noinspection ConstantConditions
        return
            errorUnless(NULL_TERMINALCLAUSE, clause != null) &&
            errorUnless(NULL_VALIDNAMES, validNames != null) &&
            errorUnless(NULL_VALIDOPERATORS, validOperators != null) &&
               validNames.contains(clause.getName()) &&
               validOperators.contains(clause.getOperator());
    }

    /**
     * <h4>Can Override</h4>
     * <p>Only override one of getValueFromLiteral or getValuesFromLiteral.
     * Override if only a single value is used by this searcher.</p>
     * Convert the QueryLiteral value into a string value suitable for
     * being put into the HTTP Query object.
     * @param searcher      The user object representing the person
     *                      carrying out the search
     * @param value         The carrier object for the value. Access the
     *                      String value using 'getStringValue()'
     * @return A string containing the target/value of the clause
     * @throws ParseException If the contents of the clause cannot
     * be parsed. If it is to be ignored rather than an error
     * null should be returned.
     */
    //ignore unused fields, these are there for other implementations
    @SuppressWarnings({"UnusedParameters"})
    protected String getValueFromLiteral(User searcher,
                                         QueryLiteral value)
            throws ParseException {
        return value.getStringValue();
    }

    /**
     * <h4>Can Override</h4>
     * <p>Override if the terminal clause is needed to determine the values
     * associated with this search. Overriding this wil break
     * getValuesFromLiteral and getValueFromLiteral</p>
     * The default implementation uses JqlOperandResolver to get the
     * LiteralValue objects and to pass them to getValuesFromLiteral.
     * @param searcher          The user object representing the person
     *                          carrying out the search
     * @param clause    The object representing a single predicate
     *                  that involves this fieldType.
     * @return A string containing the contents of the clause
     * @throws ParseException If the contents of the clause cannot
     * be parsed. If it is to be ignored rather than an error
     * null should be returned.
     */
    protected List<String> getValuesFromClause(User searcher,
                                               TerminalClause clause)
            throws ParseException {
        if (clause ==  null) {
            return null;
        }
        List<QueryLiteral> values =
                jqlOperandResolver.getValues(searcher, clause.getOperand(), clause);

        Operator operator = clause.getOperator();
        if (values.get(0).isEmpty() //error an empty value
                //error equals shouldn't have more than one value
             || (operator.equals(Operator.EQUALS) &&  values.size() != 1)
             ) {
                                    // (I guess there may be no operand clauses)
            throw new ParseException("The value for clause: "
                    + clause.getName()+" "+ operator +
                    "had the wrong number of queryLiterals " +
                    "associated with it "+values.toString());
            //give up
        }

        List<String> results = new ArrayList<String>(values.size());
        for(QueryLiteral value : values) {
            String stringValue = getValueFromLiteral(searcher, value);
            results.add(stringValue);
        }

        return results;
    }

    //-------------------------------------------------------------------------
    //                  ClauseVisitor Interface
    //-------------------------------------------------------------------------
    //inherit directly
//    @Override
//    public Void visit(AndClause andClause) {
//        //parent passes this Visitor to the children
//        return super.visit(andClause);
//    }
//
//    @Override
//    public Void visit(NotClause notClause) {
//        //parent passes this Visitor to the child
//        return super.visit(notClause);
//    }
//
//    @Override
//    public Void visit(OrClause orClause) {
//        //parent passes this Visitor to the children
//        return super.visit(orClause);
//    }
//
//    @Override
//    public Void visit(TerminalClause clause) {
//        //parent adds Terminals that match one of the clauseNames
//        //to the list of found Terminals
//        return super.visit(clause);
//    }

    /**
     * The Was clause has no representation in the Simple Navigator View and
     * so if it is in the query we shouldn't be parsing the query to build
     * the gui.
     * In case this changes we flag that we don't understand the query and then
     * call super.
     * @param clause    The clause corresponding to the Was statement
     * @return  Void is a fake object indicating that this method doesn't
     * return any meaningful value. null should be used in the return statement.
     */
    @Override
    public Void visit(WasClause clause) {
        errors.add("Unable to Parse The Query: " +
                   "Encountered a Was Clause in The Query");
        return super.visit(clause);
    }

    //-------------------------------------------------------------------------
    //                  Not Normally Overridden
    //-------------------------------------------------------------------------

    /**
     * <h4>Not Normally Overridden</h4>
     * <ul><li>Override {@link #getValueFromLiteral}
     * or {@link #getValuesFromClause} instead</li></ul>
     * <p>Goes through the clauses passing them to
     * {@link #getValuesFromClause} and putting the results into a map.
     * If sub-fields are not being used then the special sub-field
     * 'Null' is used instead when adding to the returned Map.
     * @param searcher          The user object representing the person
     *                          carrying out the search
     * @param clauses           The TerminalClauses that will be converted
     * @return a map of search sub-fields to collections of values for that
     * sub-field. NotNull
     */
    protected Map<String,Collection<String>> getValuesFromTerminalClauses(
                                                User searcher,
                                                List<TerminalClause> clauses) {
        //store the converted values as we go along
        Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
        for (TerminalClause clause : clauses) {
            try {
                //This implementation doesn't use sub-fields
                List<String> values = getValuesFromClause(searcher,clause);
                //null is returned for values if they should be ignored
                if (values != null) {
                    //key = null to indicate that sub-fields aren't in use
                    params.put(null, values);
                }
            } catch (ParseException pe) {
                //no way to pass back to the user, just log it and try the next clause
                log.error(pe);
            }
        }
        return params;
    }

    /**
     * <h4>Not Normally Overridden</h4>
     * <h4>Override Instead</h4>
     * <ul>
     *  <li>{@link #isValidClause(com.atlassian.query.clause.TerminalClause)} </li>
     *  <li>{@link #getValidOperators}</li>
     * </ul>
     * <h4>Description</h4>
     * Provides a point to override if the clauses need to be considered together to
     * judge if they are valid
     * @param clauses   The set of clauses to check.
     * @return boolean if the clauses should be used to set the FieldValuesHolder
     */
    protected boolean isValidClauses(List<TerminalClause> clauses) {
        if (clauses != null) {
            for (TerminalClause clause : clauses) {
                if (!isValidClause(clause)) {
                    return false;
                }
            }
        }
        //else {
            //queries that don't contain this searcher are still valid
            //the only time the set of clauses is invalid is if one
            //of the clauses has an invalid name or operator and if that
            //happens then the for loop above terminates early and
            //false is returned.
        //}
        //The invalid cases have been removed (via return false) so this
        //must be valid.
        return true;
    }

    /**
     * <h4>Not Normally Overridden</h4>
     * <p>This method is used by {@link #isValidClauses} to determine if the
     * clause uses a valid Operator.</p>
     * <h4>Description</h4>
     * @return An array of Operators. Not Null or an Empty Array
     */
    protected Collection<Operator> getValidOperators() {
        return validOperators;
    }

    protected ClauseNames getClauseNames() {
        return clauseNames;
    }

    private boolean errorUnless(String message, boolean test) {
        if (!test) {
            errors.add(message);
        }
        return test;
    }

    //The interface seems to be out of date compared to the implementations.
    //Including this method doesn't hurt, and if the interface is modified to
    //require it then this class will still  work.

    //Unfortunately the ChangedClause class that it uses is unknown and so we
    //will have to rely on the objects that we extend to implement this if
    //the interface changes.

    /**
     * As with Was this shouldn't ever appear in our input.
     * If it does ignoring it is fine as it is a terminal clause
     */
//    public Void visit(ChangedClause clause) {
//        return null;
//    }
}
