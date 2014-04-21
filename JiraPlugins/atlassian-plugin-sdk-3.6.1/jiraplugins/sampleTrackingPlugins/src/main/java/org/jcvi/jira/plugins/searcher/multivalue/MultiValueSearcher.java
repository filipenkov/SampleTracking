package org.jcvi.jira.plugins.searcher.multivalue;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
//part of the interface of SearchRequest and so can't be avoided
import com.opensymphony.user.User;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jcvi.jira.plugins.customfield.shared.StringIndexer;
import org.jcvi.jira.plugins.searcher.shared.CFSearchClauseFactory;
import org.jcvi.jira.plugins.searcher.shared.CFSimpleClauseVisitor;
import org.jcvi.jira.plugins.searcher.shared.CFTextSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * User: pedworth
 * Date: 11/16/11
 * <h4>Description</h4>
 * <p>The aim of this searcher is to handle multiple
 * possible values 'OR'ed together. The frontend is
 * expected to take either a copied and pasted list
 * of values or a file in csv format.</p>
 * <p>The search is for multiple values, NOT
 * multiple fields</p>
 * <h4>Operators</h4>
 * <ul>
 *  <li>@link Operator.EQUALS</li>
 *  <li>@link Operator.IN</li>
 * </ul>

 * <p></p>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MultiValueSearcher extends CFTextSearch {
    private static final Logger log = Logger.getLogger(MultiValueSearcher.class);

    private final FieldVisibilityManager fieldVisibilityManager;
    private static final Collection<Operator> validOperators;
    static {
        validOperators = new ArrayList<Operator>(2);
        validOperators.add(Operator.EQUALS);
        validOperators.add(Operator.IN);
    }

    /**
     * {@inheritDoc}
     */
    //dynamically loaded
    @SuppressWarnings({"UnusedDeclaration"})
    public MultiValueSearcher(JqlOperandResolver jqlOperandResolver,
                       CustomFieldInputHelper fieldInputHelper,
                       FieldVisibilityManager fieldVisibility) {
        super(jqlOperandResolver, fieldInputHelper, fieldVisibility);
        this.fieldVisibilityManager = fieldVisibility;
    }


    @Override
    public void init(CustomField field) {
        super.init(field);
        //test getting params
        Properties config = getConfiguration();
        if (config == null) {
            log.error("Failed to get configuration object");
        } else {
            if (log.isDebugEnabled()) {
                for(String property : config.stringPropertyNames()) {
                    log.debug("Config: '"+property+"'='"+config.getProperty(property)+"'");
                }
            }
        }
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
             * <p>MultiValueSearcher returns an EQUALs clause if there is only
             * one value. If there are multiple values then an IN clause is used
             * </p>
             * @param searcher      The user carrying out the search
             * @param holder        The values from the form
             * @param clauseName    The name of the field this clause is about
             * @return A clause that matches if the field matches any of the
             * values from the holder.
             */
            @Override
            public Clause createSearchClause(User searcher,
                                             CustomField field,
                                             FieldValuesHolder holder,
                                             String clauseName) {
                logFieldValuesHolder(log, Level.DEBUG,
                    "MultiValueSearcher#CFSearchClauseFactory.createSearchClause",field,holder);
                final Clause result; //final to check that it does get set

                //creates the actual search
                String[] values = getMultipleParamValues(field,holder,null);

                //check that there is something to search for
                if (values != null && values.length != 0) {
                    //single or multiple values
                    if (values.length == 1) {
                        //single value, no sub-name
                        String value = getSingleParamValue(field, holder, null);
                        SingleValueOperand operand = new SingleValueOperand(value);
                        return createClause(clauseName, Operator.EQUALS, operand);
                    } else {
                        MultiValueOperand operands = new MultiValueOperand(values);
                        return createClause(clauseName, Operator.IN, operands);
                    }
                } else {
                    //values is null or empty.
                    //this is valid, but there is nothing for us to do
                    result = null; //null indicates ignore
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
