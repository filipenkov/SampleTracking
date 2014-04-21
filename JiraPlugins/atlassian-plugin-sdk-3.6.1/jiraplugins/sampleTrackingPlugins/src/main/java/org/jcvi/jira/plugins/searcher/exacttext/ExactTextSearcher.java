package org.jcvi.jira.plugins.searcher.exacttext;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.jcvi.jira.plugins.searcher.shared.CFSearchClauseFactory;
import org.jcvi.jira.plugins.searcher.shared.CFSimpleClauseVisitor;
import org.jcvi.jira.plugins.searcher.shared.CFTextSearch;
import org.jcvi.jira.plugins.customfield.shared.StringIndexer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pedworth
 * Date: 11/21/11
 * <h4>Operators</h4>
 * <ul>
 *  <li>@link Operator.EQUALS</li>
 * </ul>
 * Provides an exact text search
 */
//Dynamically loaded
@SuppressWarnings({"UnusedDeclaration"})
public class ExactTextSearcher extends CFTextSearch {
    private final FieldVisibilityManager fieldVisibilityManager;
    private final Collection<Operator> validOperators;

    /**
     * {@inheritDoc}
     */
    //dynamically loaded
    @SuppressWarnings({"UnusedDeclaration"})
    public ExactTextSearcher(JqlOperandResolver jqlOperandResolver,
                             CustomFieldInputHelper fieldInputHelper,
                             FieldVisibilityManager fieldVisibility) {
        super(jqlOperandResolver, fieldInputHelper, fieldVisibility);
        this.fieldVisibilityManager = fieldVisibility;
        this.validOperators = new ArrayList<Operator>(1);
        validOperators.add(Operator.EQUALS);
    }

    /**
     * <p>
     * CFSearchClauseFactory provides part of the functionality of
     * {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer}
     * </p><p>The CFSearchClauseFactory is responsible for the conversion of
     * the values from the HTML form into a Lucene query.</p>
     * <p>Combined with {@link #getClauseVisitor(com.atlassian.jira.issue.fields.CustomField)}
     * @see org.jcvi.jira.plugins.searcher.shared.CFSearchInputTransformer
     * @return A {@link CFSearchClauseFactory}
     */
    @Override
    protected CFSearchClauseFactory getSearchClauseFactory() {
        //Part of the interface
        //noinspection deprecation
        return new CFSearchClauseFactory() {
            /**
             * <p>The heart of the Clause Factory. This takes a
             * {@link FieldValuesHolder} and returns a Clause.</p>
             * <p>For the ExactTextSearcher only the first value associated
             * with this field and a string equals test are used.</p>
             * @param searcher      The user carrying out the search
             * @param holder        The values from the form
             * @param clauseName    The name of the field this clause is about
             * @return A clause that matches only if the field matches the
             * first value from the holder.
             */
            @Override
            public Clause createSearchClause(User searcher,
                                             CustomField field,
                                             FieldValuesHolder holder,
                                             String clauseName) {
//creates actual search clause
                final Clause result; //final to check that it does get set

                String[] values = getMultipleParamValues(field,holder,null);
                //check that there is something to search for
                if (values != null) {
                    if (values.length == 1){
                        //single value, no sub-name
                        String value = getSingleParamValue(field, holder, null);
                        SingleValueOperand operand = new SingleValueOperand(value);
                        result = createClause(clauseName, Operator.EQUALS, operand);
                    } else if (values.length > 1 ) {
                        //No Exception expected, just log this
                        log.info("Search clause for: "+field.getName()+
                                            " must not have more than 1 value");
                        result = null; //it is OK if we return null
                    } else {
                        //0 operands, valid (just ignore)
                        result = null;
                    }

                } else {
                    //no values object, valid (just ignore)
                    result = null;
                }
                return result;
            }
        };
    }

    @Override
    protected CFSimpleClauseVisitor getClauseVisitor(CustomField field) {
        return new CFSimpleClauseVisitor(field.getClauseNames(),
                                         validOperators,
                                         getOperandResolver());
    }

    @Override
    protected FieldIndexer getFieldIndexer(CustomField field) {
        return new StringIndexer(fieldVisibilityManager,field);
    }
}
