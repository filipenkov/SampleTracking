package org.jcvi.jira.plugins.statisticsmapper.shared;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.Comparator;

/**
 * Added to store comments about the interface
 * @see com.atlassian.jira.issue.search.LuceneFieldSorter
 * for a good explanation of isValidValue, isFieldAlwaysPartOfAnIssue and
 * getSearchUrlSuffix.
 *
 * getDocumentConstant and getValueFromLuceneField.
 *
 * Implement:
 * <ul>
 *     <li>getValueFromLuceneField</li>
 *     <li>getComparator</li>
 * </ul>
 * Optional:
 * <ul>
 * <li>getValidValue - if not all values should be included in reports</li>
     * <li>isFieldAlwaysPartOfAnIssue - if the field can be missing, not just empty</li>
 * <li>getValueForJQLFromTransportType - if a method other than toString is
 *      required</li>
 * <li>getQueryName - to name the filter used when redirected to Issue Navigator
 *      from the ledgend</li>
 * <li>getQueryDescription - to add a description to the filter used when
 *      redirected to Issue Navigator from the ledgend</li>
 * </ul>
 *
 */
public abstract class CFStatisticsMapper<TRANSPORT_TYPE> implements StatisticsMapper<TRANSPORT_TYPE> {
    private final String  fieldName;
    private final String  documentFieldId;
    private final Class   transportType;
    private final boolean useEquals;

    /**
     *
     * @param field The field this mapper is for, used to find the documentID
     *              for looking up values in the Lucene index
     * @param lucene_typeClass  the type of object stored in the index
     * @param exactMatch If true the JQL query will be in the form fieldname '=' value
     *                   if false it will be fieldname '~' value.
     *
     */
    public CFStatisticsMapper(CustomField field,
                              Class<TRANSPORT_TYPE> lucene_typeClass,
                              boolean exactMatch) {
        this.documentFieldId  = field.getId();
        this.fieldName        = field.getName();
        this.transportType    = lucene_typeClass;
        this.useEquals        = exactMatch;
    }

    @Override
    public boolean isValidValue(TRANSPORT_TYPE value) {
        return true; //default to including all values
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue() {
        return false; //the field value could be null
//        return true; //assume if the field exists it will have a value,
                     //the value can be empty or the default
    }

    /**
     * Called from Velocity in
     * plugins/jira/reports/single-groupby-report.vm:
     * <code>
     *  $statsGroup.getMapper().getSearchUrlSuffix($option.key, $searchRequest)
     * </code>
     *
     * The method adds to the SearchRequest's Query to filter for the value
     * passed in:
     * <code>
     *  //get the existing Query
     *  Query existingQuery = searchRequest.getQuery()
     *  //create a builder to modify the Query
     *  JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(existingQuery);
     *  //add a new condition or clause

     *  builder.where().defaultAnd().addCondition(filed.getName(), Operator.AFTER,  Collection<Operand>);
     *
     *  return new SearchRequest(builder.buildQuery(), searchRequest.getOwnerUserName(), null, null);
     * </code>
     * @param value The value from the report
     * @param searchRequest Contains a query that will be used as the basis for
     *                      the returned query.
     * @return A searchRequest that can be used to generate a url to associate
     *         with the label in the legend
     */
    @Override
    public SearchRequest getSearchUrlSuffix(TRANSPORT_TYPE value, SearchRequest searchRequest) {
        if (value == null) {
            return searchRequest;
        }
        //convert the value into a clause to match Issues
        Clause clause = getSearchClause(value);
        //get the current query
        Query existingQuery = searchRequest.getQuery();


        //create a builder to modify the Query
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(existingQuery);
        builder.where().and().addClause(clause);

        //create a new SearchRequest from the updated query
        //the name and description can be null. They are just strings used
        //in the GUI when returning to the IssueNavigator. They don't have to
        //match anything.
        return new SearchRequest(
                builder.buildQuery(),
                searchRequest.getOwnerUserName(),
                getQueryName(),
                getQueryDescription());
    }

    /**
     * Used to find the values in Lucene. This is the same value as the one
     * used by the searcher's indexer to store the values. While a searcher
     * can have multiple indexers only one field can be used by the mapper.
     * For this implementation the CustomField's id is used as
     * AbstractCustomFieldIndexer.getDocumentFieldId uses this.
     * @return the same value as FieldIndexer.getDocumentFieldId
     */
    @Override
    public String getDocumentConstant() {
        return documentFieldId;
    }

    /**
     * Convert the lucene document field back to the transport object
     * For custom fields, the return value will be passed to
     * {@link com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor#getStatHtml(com.atlassian.jira.issue.fields.CustomField, Object, String)}
     * This returns the contents of the resource named 'label' in
     * atlassian-plugin.xml
     *
     * This is needed as the indexer only maps transport objects to the Lucene
     * index, it provides no return conversion.
     * @param   documentValue   The value of the field in the lucene index
     * @return  The value that will be passed to the display
     */
    @Override
    public abstract TRANSPORT_TYPE getValueFromLuceneField(String documentValue);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Comparator<TRANSPORT_TYPE> getComparator();

    /**
     * Create a JQL clause that will select issues where the field indexed by
     * the parent SearchIndexer has the value passed in as a parameter.
     *
     * @param value The value to match
     * @return A clause based on the field and the value
     */
    protected Clause getSearchClause(TRANSPORT_TYPE value) {
       //convert the TRANSPORT_TYPE object into a string that can be displayed
       //in the JQL
       String queryValue = getValueForJQLFromTransportType(value);
       //Convert into an operand for a clause in the search
       SingleValueOperand operand
               //singleValueOperand used as only one value is passed in
               = new SingleValueOperand(queryValue);
       final Operator operator;
       if (useEquals) {
           operator = Operator.EQUALS;
       } else {
           operator = Operator.LIKE;
       }
       //Create the clause to add to the query
       return new TerminalClauseImpl(
               fieldName,       //field name
               operator,        //'=' or '~'
               operand);        //the wrapped value
    }

    /**
     * The value used in a JQL clause must be one of String, Long
     * or be wrapped in a QueryLiteral. Initially we will just handle
     * String values
     *
     * @param value The as a transport object
     * @return A string value for use in the JQL Operand
     */
    protected String getValueForJQLFromTransportType(TRANSPORT_TYPE value) {
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    /**
     * An optional method that can be used to display a name for the query
     * in the summary/edit tabs of Issue Navigator.
     * The name is only for display and doesn't need to match any saved query.
     * @return null if no name is to be used, otherwise the name to use
     */
    protected String getQueryName() {
        return null;
    }

    /**
     * An optional method that can be used to display a description of the query
     * in the summary/edit tabs of Issue Navigator.
     * @return null if no description is to be used, otherwise the description to use
     */
    protected String getQueryDescription() {
        return null;
    }
}
