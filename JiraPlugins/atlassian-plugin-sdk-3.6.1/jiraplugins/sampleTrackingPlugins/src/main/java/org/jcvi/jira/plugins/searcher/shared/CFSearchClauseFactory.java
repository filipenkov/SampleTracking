package org.jcvi.jira.plugins.searcher.shared;

import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.query.clause.*;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapperUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * User: pedworth
 * Date: 11/17/11
 *
 * <p>Implementations should take the values from the {@link FieldValuesHolder}
 * and converts them into one or more Clauses.<p>
 *
 * <p>Several utility methods are provided to help implement createSearchClause.
 * <ul>
 *     <li>{@link #getSingleParamValue}</li>
 *     <li>{@link #getMultipleParamValues}</li>
 *     <li>{@link #createClause}</li>
 *     <li>{@link #createClauses}</li>
 * </ul>
 * There is also an inner-class {@link ClauseJoiner}.
 * </p>
 *
 * <p>This factory class was added to separate the creation of clauses from
 * form parameters from the implementation of the SearchInputTransformer
 * interface. Normally only the getSearchClause method of SearchInputTransformer
 * needs to be customized for a new Searcher.</p>
 */
public abstract class CFSearchClauseFactory {
    private static final Logger log = Logger.getLogger(CFSearchClauseFactory.class);
    /**
     * A separate instance is created for each field.
     */
    public CFSearchClauseFactory() {
    }

    /**
     * This controls the creation of the Lucine search from the form parameters.
     * <p>The form parameters can be accessed, from FieldValuesHolder, by
     * calling {@link #getSingleParamValue} or {@link #getMultipleParamValues}
     * with FieldValuesHolder and the sub-name of the field. (Form
     * fields have names/ids in the format CustomField_XXXXX[:sub-name]) If
     * there isn't a sub-name then null should be passed to the method.
     * FieldValuesHolder contains the values as Strings from the form.<p>
     * <p>Clauses can be created by either {@link #createClause}
     * or {@link #createClauses}
     * </p>
     *
     * @param searcher      The user carrying out the search
     * @param field         The customfield to get the clauses for
     * @param holder        The values from the form
     * @param clauseName    The name of the field this clause is about
     * @return  A clause that will restrict the issues matched based on the
     * values from the form.
     */
    //User is part of the interface
    @SuppressWarnings({"deprecation"})
    public abstract Clause createSearchClause(User searcher,
                                              CustomField field,
                                              FieldValuesHolder holder,
                                              String clauseName);
//        List<TerminalClause> clauses = new ArrayList<TerminalClause>(2);
//        //get an un-named param
//        String value = getSingleParamValue(holder,null);
//        if (value != null) {
//            clauses.add(new TerminalClauseImpl(clauseName, Operator.EQUALS, value));
//        }
//        //get a named param
//        String test = getSingleParamValue(holder,"test");
//        if (test != null) {
//            clauses.add(new TerminalClauseImpl(clauseName, Operator.EQUALS, test));
//        }
//
//        return combineClauses(clauses, CFSearchClauseFactory.ClauseJoiner.AND);

    //-----------------------------------------------------------------------
    // Utility methods for handling FieldValuesHolder
    //-----------------------------------------------------------------------

    /**
     * FieldValuesHolder contains the values from the form. They appear to
     * be placed in their sometimes as strings and sometimes as objects.
     * The value returned from this method is always a string. If the
     * stored value was an object then the result of toString is returned.
     * @param field     The customfield to get the values for
     * @param holder        The object containing the values from the form
     * @param subFieldName  The id after the customField_XXXX: field name
     *                      in the form. Null if there is no extra id
     * @return  The string value for the requested field/sub-field
     */
    protected String getSingleParamValue(CustomField field,
                                         FieldValuesHolder holder,
                                         String subFieldName) {
        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseFactory.getSingleParamValue",field,holder);
        CustomFieldParams params = field.getCustomFieldValues(holder);
        if (params != null) {
            Object value;
            if (subFieldName != null) {
                value = params.getFirstValueForKey(subFieldName);
            } else {
                value = params.getFirstValueForNullKey().toString();
            }
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * @see #getSingleParamValue
     * @param field     The customfield to get the values for
     * @param holder        The object containing the values from the form
     * @param subFieldName  The id after the customField_XXXX: field name
     *                      in the form. Null if there is no extra id
     * @return  An array of the string values for the requested field/sub-field
     */
    protected String[] getMultipleParamValues(CustomField field,
                                              FieldValuesHolder holder,
                                              String subFieldName) {
        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseFactory.getMultipleParamValues",field,holder);
        CustomFieldParams params = field.getCustomFieldValues(holder);
        Collection<Object> result = null;
        if (params != null) {
            if (subFieldName != null) {
              //casting Collection to Collection<Object> is always valid
              //noinspection unchecked
              result = (Collection<Object>)params.getValuesForKey(subFieldName);
            } else {
              //noinspection unchecked
              result = (Collection<Object>)params.getValuesForNullKey();
            }
        }
        if (result != null) {
            Collection<String> stringValues =
                    TypeMapperUtils.mapUnorderedCollection(
                            new TypeMapperUtils.StringMapper<Object>(),result);
            return stringValues.toArray(new String[stringValues.size()]);
        }
        return null;
    }

    /**
     * <p>Clauses appear to only be able to store values as Strings.
     * At some point they probably get converted to Dates etc
     * todo: findout where</p>
     * @param clauseName The name to use in the clause, passed into
     *                   createSearchClause
     * @param operator   The comparison to carryout for this clause
     * @param value      The String encoding of the value for the comparison
     * @return  A Clause representing the parameters
     */
    protected TerminalClause createClause(String clauseName,
                                          Operator operator,
                                          Operand value) {
        return new TerminalClauseImpl(clauseName,operator,value);
    }


    /**
     * @see #createClause
     * @param clauseName The name to use in the clause, passed into
     *                   createSearchClause
     * @param operator   The comparison to carryout for this clause
     * @param values     The values encoded as Strings
     * @param joiner     The operation to use to create a single Clause out
     *                   of the multiple Terminal Clauses. ClauseJoiner.AND /
     *                   ClauseJoiner.OR
     * @return  A Clause representing the parameters
     */
    protected Clause createClauses(String clauseName,
                                   Operator operator,
                                   ClauseJoiner joiner,
                                   String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        //create a clause for each value
        Collection<TerminalClause> clauses
                                = new ArrayList<TerminalClause>(values.length);
        for (String value : values) {
            SingleValueOperand operand = new SingleValueOperand(value);
            clauses.add(createClause(clauseName, operator, operand));
        }
        return joiner.combineClauses(clauses);
    }

    /**
     * An enum that defines the ways that multiple clauses can be joined
     * together to form a single clause.
     */
    protected static enum ClauseJoiner {
        AND {
            @Override
            public Clause join(Collection<TerminalClause> clauses) {
                return new AndClause(clauses);
            }
        }, OR {
            @Override
            public Clause join(Collection<TerminalClause> clauses) {
                return new OrClause(clauses);
            }
        };
        protected abstract Clause join(Collection<TerminalClause> clauses);
        //-----------------------------------------------------------------------
        // Utility methods for merging multiple clauses
        //-----------------------------------------------------------------------
        public Clause combineClauses(Collection<TerminalClause> clauses) {
            if (clauses == null || clauses.isEmpty()) {
                return null;
            }
            if (clauses.size() == 1) {
                Iterator<TerminalClause> i = clauses.iterator();
                return i.next();
            }
            return join(clauses);
        }

    }
}
