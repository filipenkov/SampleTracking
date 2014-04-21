package org.jcvi.jira.plugins.searcher.shared;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.*;
import com.opensymphony.user.User;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * User: pedworth
 * Date: 11/10/11
 * <p>SearchInputTransformers are used to convert between the
 * ActionParams, FieldValuesHolder and Query/Clause objects.
 * This class defines what is done during a search.</p>
 * deprecation is suppressed for this class as the SearchInputTransformer
 * defines it's methods including deprecated types.
 * <p>
 *     A minimal implementation should implement getSearchClause and
 *     create a new clauseVisitor (in CFTextSearch's implementation.
 * </p>
 */
@SuppressWarnings({"deprecation"})
public abstract class CFSearchInputTransformer implements SearchInputTransformer{
    private static final Logger log = Logger.getLogger(CFSearchInputTransformer.class);
    /**
     * The field identifiers used in the query that relate to fields of the
     * current {@link CustomField}
     */
    private final ClauseNames clauseNames;
    private final CustomField field;
    private final CustomFieldInputHelper typeConverterForGui;

    /**
     * All of the params except queryVisitor should be gained via the
     * dependency injection method. Customization can be done via
     * extending CFSimpleClauseVisitor.
     * @param customField     The field this searcher is associated with
     * @param inputHelper     provides methods to lookup a fields name and
     *                        resolve that into a uid.
     */
    protected CFSearchInputTransformer(CustomField customField,
                                       CustomFieldInputHelper inputHelper) {
        this.field = customField;
        this.clauseNames = customField.getClauseNames();
        this.typeConverterForGui = inputHelper;
    }

    /**
     * Creates an implementation of CFSimpleClauseVisitor that is used
     * to customize how Queries are converted into form elements.
     * A new Visitor is needed each time as it carries state.
     * The return type is the implementation instead of the interface
     * as we access some logging methods that were added.
     * @return The new visitor object
     */
    protected abstract CFSimpleClauseVisitor createClauseVisitor();

    /**
     * An implementation of CFSearchClauseFactory Used to customize how form
     * values are converted into Queries
     */
    protected abstract CFSearchClauseFactory createSearchClauseFactory();

    //-----------------------------------------------------------------------
    // Can be Overridden
    //-----------------------------------------------------------------------
    /**
     * <h4>Can Override</h4>
     * <p>Converts the field values in the ActionParams object into values
     * in the fieldValuesHolder object. Success is indicated by not
     * modifying the errors object.</p>
     * @param searcher          The user object representing the person
     *                          carrying out the search
     * @param searchContext     The Project(s) and IssueType(s) associated
     *                          with the search, if there are any.
     * @param fieldValuesHolder The FieldValuesHolder object to query for the
     *                          values associated with this field.
     * @param i18nHelper        A utility class for use when creating error
     *                          messages.
     * @param errors            The object holding feedback for the GUI.
     *                          This is an OUTPUT.
     */
    @Override
    public void validateParams(User searcher,
                               SearchContext searchContext,
                               FieldValuesHolder fieldValuesHolder,
                               I18nHelper i18nHelper,
                               ErrorCollection errors) {
        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseTransformer.validateParams",field,fieldValuesHolder);
    }

    /**
     * <h4>Can Override</h4>
     * Test that all Clauses in the query, related to this field and
     * searcher, can be represented in the IssueNavigator.
     * <p>The default tests that the Query has a Where clause
     * and then attempts to parse the Query. Returning true
     * if getValuesFromQuery doesn't return false</p>
     * @param searcher          The user object representing the person
     *                          carrying out the search
     * @param query             The Query to locate references to this field
     *                          in and to test
     * @param searchContext     The Project(s) and IssueType(s) associated
     *                          with the search, if there are any.
     * @return  true iff the Query can be shown in the IssueNavigator using
     * the simple search interface.
     */
    @Override
    public boolean doRelevantClausesFitFilterForm(User searcher,
                                                  Query query,
                                                  SearchContext searchContext) {
        //true if there aren't any clauses
        return query.getWhereClause() == null
        //test if the query can be parsed
                || getValuesFromQuery(searcher,
                                      query,
                                      searchContext) != null;
    }

    //-----------------------------------------------------------------------
    // Not Normally Overridden
    //-----------------------------------------------------------------------

    /**
     * <h4>Not Normally Overridden</h4>
     * <p>Converts the field values in the ActionParams object into values
     * in the fieldValuesHolder object.</p>
     * <h3>Implementation</h3>
     * <p>This method passes the task to {@link com.atlassian.jira.issue.fields.OrderableField#populateFromParams(java.util.Map, java.util.Map)}
     * which (for CustomFields) is implemented by {@link com.atlassian.jira.issue.fields.CustomFieldImpl#getRelevantParams(java.util.Map)}
     * <p>
     * <h4>Implementation from CustomFieldImpl.getRelevantParameters()</h4>
     * <p>Sudo - code version
<pre>
     foreach(Entry in parameters) {
        if (key equals '&lt;CustomField.id&gt;:' or starts with '&lt;CustomField.id&gt;:'){
            fieldValuesHolderKey = key ~ /^&lt;CustomField.id&gt;:?//
            if (String[] value not null 
                           and not empty
                           and has at least one entry that is not null or empty) {
                result.addValue(fieldValuesHolderKey, Arrays.toList(value)
            }
        }
     }
</pre>
     * </p>
     * <p>In summary; it copies the values for the keys that start with
     * &lt;CustomField.id&gt; and stores them using the part of the key after
     * the id.</p>
     * <p>Note: if the key doesn't have a second part then the value
     * is added against the null key</p> 
</p>
     * @param searcher          The user object representing the person
     *                          carrying out the search
     * @param fieldValuesHolder The FieldValuesHolder object that is being
     *                          populated. This is the OUTPUT
     * @param actionParams      The actionParams that the fields parameters
     *                          should be read from. This is the INPUT
     */
    @Override
    public void populateFromParams(User searcher,
                                   FieldValuesHolder fieldValuesHolder,
                                   ActionParams actionParams) {
        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseTransformer.populateFromParams(before)",field,fieldValuesHolder);
        field.populateFromParams(fieldValuesHolder, actionParams.getKeysAndValues());
        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseTransformer.populateFromParams(after)",field,fieldValuesHolder);
    }

    /**
     * <h4>Not Normally Overridden</h4>
     * <ul><li>Use a custom ClauseVisitor instead</li></ul>
     * <p>Convert {@link Clause}s in the {@link Query} into values for
     * {@link FieldValuesHolder} and eventually the GUI.</p>
     * <h3>Implementation</h3>
     * <p>{@link #getValuesFromQuery(com.opensymphony.user.User, com.atlassian.query.Query, com.atlassian.jira.issue.search.SearchContext)}
     * carries out the majority of the work. This method just transfers the
     * resulting map into the FieldValuesHolder</p>
     * <p>The FieldValuesHolder is a map of
     * &lt;field id&gt; -&gt;  &lt;CustomFieldParams&gt;. The CustomFieldParams
     * object is itself a map from the search fields sub-fields to Collections
     * of their values. If the field doesn't use sub-fields then the special
     * sub-field 'NULL' is used.</p>
     * @param searcher          The user object representing the person
     *                          carrying out the search
     * @param fieldValuesHolder The FieldValuesHolder object that is being
     *                          populated. This is the OUTPUT
     * @param query             The Query to locate references to this field
     *                          in and to use in constructing the
     *                          FieldValuesHolder
     * @param searchContext     The Project(s) and IssueType(s) associated
     *                          with the search, if there are any.
     */
    @Override
    public void populateFromQuery(User searcher,
                                  FieldValuesHolder fieldValuesHolder,
                                  Query query,
                                  SearchContext searchContext) {
        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseTransformer.populateFromQuery(before)",field,fieldValuesHolder);
        if (query == null || query.getWhereClause() == null) {
            //nothing to add the the fieldValuesHolder
            return;
        }
        Map<String,Collection<String>> queryContent = getValuesFromQuery(searcher,
                                              query,
                                              searchContext);
        if (queryContent != null) {
            CustomFieldParamsImpl params = new CustomFieldParamsImpl(field);
            for (Map.Entry<String,Collection<String>> entry : queryContent.entrySet()) {
                //todo: needs to check that the clauses are relevant
                params.addValue(entry.getKey(),entry.getValue());
            }
            //interface doesn't provide any type information for the map
            //noinspection unchecked
            fieldValuesHolder.put(field.getId(), params);
        } //null queryContent if the content can't be decoded

        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseTransformer.populateFromQuery(after)",field,fieldValuesHolder);
    }

    /**
     * <h4>Not Normally Overridden</h4>
     * <ul><li>Use a custom ClauseVisitor instead</li></ul>
     * <p>Uses the ClauseVisitor to find relevant parts of the the
     * Query and uses {@link CFSimpleClauseVisitor#getValuesFromTerminalClauses}
     * to convert the clauses into a map.</p>
     * @param searcher          The user object representing the person
     *                          carrying out the search
     * @param query             The Query to locate references to this field
     *                          in and to use in constructing the
     *                          FieldValuesHolder
     * @param searchContext     The Project(s) and IssueType(s) associated
     *                          with the search, if there are any.
     * @return a map of search sub-field names to collections of values for that
     * sub-field. If sub-fields are not being used then the special sub-field
     * Null is used. Returns Null if the query cannot be parsed (
     * doRelevantClausesFitFilterForm relies on this to tell that the
     * query can't be parsed)
     *
     */
    //SearchContext is passed in case overriding methods need it
    @SuppressWarnings({"UnusedParameters"})
    protected Map<String,Collection<String>> getValuesFromQuery(User searcher,
                                     Query query,
                                     SearchContext searchContext) {
        CFSimpleClauseVisitor clauseVisitor = createClauseVisitor();
//           notNull("query", query);
//           notNull("whereClause", query.getWhereClause());
        //parse the query using the visitor
        query.getWhereClause().accept(clauseVisitor);

        if (!clauseVisitor.isValid()) {
            String[] errors = clauseVisitor.getErrors();
            //all we can do is log the errors, no interface for passing
            //them back is given
            String message = "ClauseVisitor was inValid\n";
            if (errors != null) {
                for (String error : errors) {
                    message += error + "\n";
                }
            }
            //use a single logging action to avoid this getting split up in
            //the logs
            log.error(message);
            return null;
        }

        List<TerminalClause> clauses = clauseVisitor.getNamedClauses();
        if (clauses == null || clauses.size() <= 0) {
            //none of our fields appear in the query, nothing for us to do
            return new HashMap<String, Collection<String>>();
            //empty map to indicate no error, but nothing to do
        }
        return clauseVisitor.getValuesFromTerminalClauses(searcher,clauses);
    }


    /**
     * <h4>Not Normally overridden</h4>
     * <h4>Override instead</h4>
     * <ul><li>The {@link CFSearchClauseFactory}
     * passed into the constructor</li></ul>
     * Converts the HTML form's values into a Lucene Clause
     * @param searcher The user object representing the person carrying out
     *                 the search
     * @param holder   The values from the form
     * @return  A Lucene Clause that can be used to determine which records
     * to return. Null if the field doesn't produce a clause.
     * WARNING:
     * If there was previously a value I'm not sure that null will remove the
     * old clause!
     */
    @Override
    public Clause getSearchClause(User              searcher,
                                  FieldValuesHolder holder) {
        CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                "CFSearchClauseTransformer.getSearchClause",field,holder);
        String clauseName = typeConverterForGui.getUniqueClauseName(
                searcher, clauseNames.getPrimaryName(), field.getName());

        return createSearchClauseFactory().createSearchClause(
                searcher,
                field,
                holder,
                clauseName);
    }
}
