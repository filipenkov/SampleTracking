package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.tasks.jql.ClauseXmlHandler;
import com.atlassian.jira.upgrade.tasks.jql.ClauseXmlHandlerRegistry;
import com.atlassian.jira.upgrade.tasks.jql.JqlClauseXmlHandler;
import com.atlassian.jira.upgrade.tasks.jql.OrderByXmlHandler;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.order.OrderBy;
import com.opensymphony.user.User;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Upgrades SearchParameter based search request XML to JQL strings.
 *
 * @since v4.0
 */
public class UpgradeTask_Build604 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build604.class);

    private final ClauseXmlHandlerRegistry clauseXmlHandlerRegistry;
    private final OfBizDelegator ofBizDelegator;
    private final SearchService searchService;
    private final OrderByXmlHandler orderByXmlHandler;
    private final JqlQueryParser jqlQueryParser;
    private final MailQueue mailQueue;
    private final SearchHandlerManager searchHandlerManager;
    private final UserUtil userUtil;
    final Map<String, UserSavedFilterConversionInformations> userSavedFilterConversionInformationsMap;
    private final I18nHelper.BeanFactory i18n;

    public UpgradeTask_Build604(final ClauseXmlHandlerRegistry clauseXmlHandlerRegistry, final OfBizDelegator ofBizDelegator, final SearchService searchService, final OrderByXmlHandler orderByXmlHandler, final JqlQueryParser jqlQueryParser, final MailQueue mailQueue, final SearchHandlerManager searchHandlerManager, final UserUtil userUtil, I18nHelper.BeanFactory i18n)
    {
        this.clauseXmlHandlerRegistry = clauseXmlHandlerRegistry;
        this.ofBizDelegator = ofBizDelegator;
        this.searchService = searchService;
        this.orderByXmlHandler = orderByXmlHandler;
        this.jqlQueryParser = jqlQueryParser;
        this.mailQueue = mailQueue;
        this.i18n = i18n;
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
        this.userUtil = notNull("userUtil", userUtil);
        userSavedFilterConversionInformationsMap = new HashMap<String, UserSavedFilterConversionInformations>();
    }

    ///CLOVER:OFF
    @Override
    public String getBuildNumber()
    {
        return "604";
    }

    ///CLOVER:ON

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Because MSSQL row/table locking sucks we can not just iterate over the SearchRequests, instead we need
        // to get all the ID's and then update the records in batches.
        final List<Long> searchRequestIds = getSearchRequestIds();

        // Lets create a logger that we can use to show how much of the upgrade task has been completed
        final Context percentageLogger = getPercentageLogger(searchRequestIds);

        // Lets run through all the search requests, converting them in batches of 200
        final List<Long> batchSearchRequestIds = new ArrayList<Long>();
        for (final Long searchRequestId : searchRequestIds)
        {
            batchSearchRequestIds.add(searchRequestId);
            if (batchSearchRequestIds.size() >= 200)
            {
                // If we hit our batch limit lets go ahead and convert what we have so far.
                upgradeSearchRequests(getSearchRequestGvsForIds(batchSearchRequestIds), percentageLogger);
                batchSearchRequestIds.clear();
            }
        }

        // Lets do the last batch which may be under 200
        if (!batchSearchRequestIds.isEmpty())
        {
            upgradeSearchRequests(getSearchRequestGvsForIds(batchSearchRequestIds), percentageLogger);
        }

        // We send all the notification emails after we have done the upgrade so we can send users only one email with
        // all the details of what might have gone wrong for them in the upgrade.
        sendEmailNotifications();
    }

    void upgradeSearchRequests(final List<GenericValue> searchRequestGvs, final Context percentageLogger) throws GenericEntityException
    {
        for (final GenericValue searchRequestGv : searchRequestGvs)
        {
            final String searchRequestName = searchRequestGv.getString("name");
            final Long searchRequestId = searchRequestGv.getLong("id");

            final Context.Task task = percentageLogger.start(searchRequestName + "(" + searchRequestId + ")");
            try
            {
                if (requestRequiresUpgrade(searchRequestGv.getString("request")))
                {
                    final Query query = getQueryFromXml(searchRequestGv);
                    if (query != null)
                    {
                        // Lets save the JQL string in place of the XML
                        final String jqlString = searchService.getGeneratedJqlString(query);
                        searchRequestGv.setString("request", jqlString);
                        searchRequestGv.store();
                        log.debug("Successfully converted filter '" + searchRequestName + "(" + searchRequestId + ")" + "' to JQL '" + jqlString + "'.");
                    }
                    else
                    {
                        // This means that the old data was corrupted or un-parseable so lets email the user and
                        // set the query to something that will never return any results
                        final Query falseQuery = JqlQueryBuilder.newBuilder().where().project().isEmpty().buildQuery();
                        searchRequestGv.setString("request", searchService.getGeneratedJqlString(falseQuery));
                        searchRequestGv.store();
                        logConversionError(searchRequestGv);
                    }
                }
            }
            finally
            {
                task.complete();
            }
        }
    }

    /**
     * Converts the {@link Query} that came from XML into "namified" form. No conversion will be attempted if the owner
     * is null.
     *
     * @param ownerUserName the author of the original query
     * @param queryFromXml the query
     * @param clausesNotToNamify the clauses that should not be attempted to be namified by the visitor
     * @return the query namified; never null
     */
    Query getNamifiedQuery(final String ownerUserName, final Query queryFromXml, final Set<Clause> clausesNotToNamify)
    {
        try
        {
            if (queryFromXml.getWhereClause() == null)
            {
                return queryFromXml;
            }

            final User queryOwner = userUtil.getUser(ownerUserName);
            if (queryOwner == null)
            {
                return queryFromXml;
            }

            final SearchContext searchContext = searchService.getSearchContext(queryOwner, queryFromXml);
            final NamifyingClauseVisitor visitor = new NamifyingClauseVisitor(queryOwner, searchHandlerManager, searchContext, clausesNotToNamify);
            final Clause namifiedClause = queryFromXml.getWhereClause().accept(visitor);

            return new QueryImpl(namifiedClause, queryFromXml.getOrderByClause(), null);
        }
        catch (final Exception e)
        {
            log.warn("Could not namify the query: '" + queryFromXml.getQueryString() + "'", e);
            return queryFromXml;
        }
    }

    Context getPercentageLogger(final List<Long> searchRequestIds)
    {
        final Context percentageLogger = Contexts.percentageLogger(new Sized()
        {
            public int size()
            {
                return searchRequestIds.size();
            }

            public boolean isEmpty()
            {
                return searchRequestIds.size() == 0;
            }
        }, log, "Converting search requests to JQL is {0}% complete.");

        percentageLogger.setName("Converting search requests to JQL.");
        return percentageLogger;
    }

    boolean requestRequiresUpgrade(String string)
    {
        try
        {
            // When the string is null we need to change it to empty string, the XML returns empty as null and the
            // parser will not parse a null string.
            if (string == null)
            {
                string = "";
            }
            jqlQueryParser.parseQuery(string);
            // This means we have already run the upgrade task or through some magic there is already a parsable
            // JQL string in the table
            return false;
        }
        catch (final JqlParseException e)
        {
            return true;
        }
    }

    ///CLOVER:OFF
    List<GenericValue> getSearchRequestGvsForIds(final List<Long> batchSearchRequestIds)
    {
        // look up all the SearchRequest GenericValues for the ID's in this batch
        final EntityCondition idCondition = new EntityExpr("id", EntityOperator.IN, batchSearchRequestIds);

        //now find all issues where the resolution is not null, and the last change group item is set to something.
        return ofBizDelegator.findByCondition("SearchRequest", idCondition, null);
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    List<Long> getSearchRequestIds()
    {
        final List<Long> searchRequestIds = new ArrayList<Long>();
        final List<GenericValue> searchRequestIdGvs = ofBizDelegator.findByCondition("SearchRequest", null, CollectionBuilder.list("id"));
        for (final GenericValue searchRequestIdGv : searchRequestIdGvs)
        {
            searchRequestIds.add(searchRequestIdGv.getLong("id"));
        }
        return searchRequestIds;
    }

    ///CLOVER:ON

    Query getQueryFromXml(final GenericValue searchRequestGv)
    {
        try
        {
            final String requestString = searchRequestGv.getString("request");
            final String ownerUserName = searchRequestGv.getString("author");
            final Document doc = new Document(requestString);
            final Elements paramEls = doc.getRoot().getElements("parameter");

            // Lets get the where clauses
            final WhereClauseConversionResults whereClauseConversionResults = getWhereClauses(paramEls);

            // Lets get the sorts
            final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = getOrderBy(doc.getRoot().getElements("sort"));

            final List<Clause> convertedClauses = whereClauseConversionResults.getConvertedClauses();
            final OrderBy convertedOrderBy = orderByConversionResults.getConvertedOrderBy();

            // Lets send off a message about this if we need to
            logConversionWarningIfNeeded(searchRequestGv, whereClauseConversionResults, orderByConversionResults);

            if (!convertedClauses.isEmpty())
            {
                final QueryImpl query;
                if (convertedClauses.size() == 1)
                {
                    query = new QueryImpl(convertedClauses.get(0), convertedOrderBy, null);
                }
                else
                {
                    final AndClause andClause = new AndClause(convertedClauses);
                    query = new QueryImpl(andClause, convertedOrderBy, null);
                }

                // namify the query
                return getNamifiedQuery(ownerUserName, query, whereClauseConversionResults.getClausesNotToNamify());
            }
            else
            {
                // Must have been the all search
                return new QueryImpl(null, convertedOrderBy, null);
            }
        }
        catch (final ParseException pe)
        {
            return null;
        }
    }

    void sendEmailNotifications()
    {
        for (final UserSavedFilterConversionInformations conversionInformations : userSavedFilterConversionInformationsMap.values())
        {
            mailQueue.addItem(new UpgradeTask_Build604MailItem(conversionInformations));
        }
        userSavedFilterConversionInformationsMap.clear();
    }

    WhereClauseConversionResults getWhereClauses(final Elements paramEls)
    {
        final List<Clause> convertedClauses = new ArrayList<Clause>();
        final Set<Clause> clausesNotToNamify = new LinkedHashSet<Clause>();
        final List<ClauseXmlHandler.ConversionResult> conversionMessages = new ArrayList<ClauseXmlHandler.ConversionResult>();

        while (paramEls.hasMoreElements())
        {
            final Element element = (Element) paramEls.nextElement();
            final String paramClass = element.getAttributeValue("class");
            final String jqlAttr = element.getAttributeValue("jql");
            // Get the portion of the Query that is saved as a SearchParameter
            final Element searchParamElement = element.getElements().first();

            final String searchParameterElementName = searchParamElement.getName();
            final ClauseXmlHandler handler;
            if ("true".equals(jqlAttr))
            {
                // Must be in-between data, lets include it
                log.info("Found an old-style search request with some embedded JQL, converting the JQL.");
                handler = createJqlClauseXmlHandler();
            }
            else
            {
                handler = clauseXmlHandlerRegistry.getClauseXmlHandler(paramClass, searchParameterElementName);
            }

            // This will be removed once we have completely converted to JQL but for the moment we need to handle the
            // fields that have been converted to JQL as clauses.
            if (handler != null)
            {
                final ClauseXmlHandler.ConversionResult conversionResult = handler.convertXmlToClause(searchParamElement);
                final ClauseXmlHandler.ConversionResultType resultType = conversionResult.getResultType();
                if ((ClauseXmlHandler.ConversionResultType.FULL_CONVERSION != resultType) && (ClauseXmlHandler.ConversionResultType.NOOP_CONVERSION != resultType))
                {
                    // We only want to record the data so we can generate messages from it later
                    conversionMessages.add(conversionResult);
                }

                final Clause convertedClause = conversionResult.getClause();
                if (convertedClause != null)
                {
                    convertedClauses.add(convertedClause);
                    if (!handler.isSafeToNamifyValue())
                    {
                        clausesNotToNamify.add(convertedClause);
                    }
                }
            }
            else
            {

                log.error("Found a search parameter we don't have an XML handler for, " + paramClass + ". Element: " + element);
                conversionMessages.add(new ClauseXmlHandler.FailedConversionResult(searchParameterElementName));
            }

        }
        return new WhereClauseConversionResults(convertedClauses, conversionMessages, clausesNotToNamify);
    }

    ///CLOVER:OFF
    ClauseXmlHandler createJqlClauseXmlHandler()
    {
        return new JqlClauseXmlHandler(jqlQueryParser);
    }

    ///CLOVER:ON

    ///CLOVER:OFF

    OrderByXmlHandler.OrderByConversionResults getOrderBy(final Elements sortEls)
    {
        return orderByXmlHandler.getOrderByFromXml(sortEls);
    }

    ///CLOVER:ON

    void logConversionWarningIfNeeded(final GenericValue searchRequestGv, final WhereClauseConversionResults whereClauseConversionResults, final OrderByXmlHandler.OrderByConversionResults orderByConversionResults)
    {
        if (!whereClauseConversionResults.getConversionMessages().isEmpty() || !orderByConversionResults.getConversionErrors().isEmpty())
        {
            // Lets record information about our conversion process so we can later send an email
            final String searchRequestName = searchRequestGv.getString("name");
            final Long searchRequestId = searchRequestGv.getLong("id");
            final String ownerUserName = searchRequestGv.getString("author");
            final String searchRequestIdString = searchRequestName + "(" + searchRequestId + ")";

            if (ownerUserName != null)
            {
                UserSavedFilterConversionInformations informations = userSavedFilterConversionInformationsMap.get(ownerUserName);
                if (informations == null)
                {
                    informations = new UserSavedFilterConversionInformations(ownerUserName);
                    userSavedFilterConversionInformationsMap.put(ownerUserName, informations);
                }
                informations.addConversionResult(new SavedFilterConversionInformation(ownerUserName, searchRequestName, searchRequestId,
                    whereClauseConversionResults.getConversionMessages(), orderByConversionResults.getConversionErrors()));
            }

            // log all the problems we found
            final I18nHelper i18nBean = getI18n();
            log.debug("---------------- Partial Conversion of filter '" + searchRequestIdString + " ----------");
            for (final ClauseXmlHandler.ConversionResult conversionResult : whereClauseConversionResults.getConversionMessages())
            {
                log.debug(conversionResult.getMessage(i18nBean, searchRequestName));
            }
            for (final OrderByXmlHandler.ConversionError conversionError : orderByConversionResults.getConversionErrors())
            {
                log.debug(conversionError.getMessage(i18nBean, searchRequestName));
            }
            log.debug("--------------------------------------------------------");
        }
    }

    ///CLOVER:OFF
    I18nHelper getI18n()
    {
        // we only want log messages in English
        return i18n.getInstance(Locale.US);
    }

    ///CLOVER:ON

    private void logConversionError(final GenericValue searchRequestGv)
    {
        // Lets send an email about our conversion process
        final String searchRequestName = searchRequestGv.getString("name");
        final Long searchRequestId = searchRequestGv.getLong("id");
        final String ownerUserName = searchRequestGv.getString("author");

        // It does not make much sense for the owner to be null but if we see it and want to log an error we will
        // just swallow it since we have no one to email
        if (ownerUserName != null)
        {
            UserSavedFilterConversionInformations informations = userSavedFilterConversionInformationsMap.get(ownerUserName);
            if (informations == null)
            {
                informations = new UserSavedFilterConversionInformations(ownerUserName);
                userSavedFilterConversionInformationsMap.put(ownerUserName, informations);
            }
            informations.addConversionResult(new SavedFilterConversionInformation(ownerUserName, searchRequestName, searchRequestId));
        }
        log.error("Error converting saved filter to JQL for search request with id '" + searchRequestId + "' and name '" + searchRequestName + "' owned by '" + ownerUserName + "' with XML '" + searchRequestGv.getString("request") + "'.");
    }

    // Just used to store the notification messages per-user so we can send it off to the mail item
    public static class UserSavedFilterConversionInformations
    {
        private final String ownerName;
        private final Collection<SavedFilterConversionInformation> usersSavedFilterConversionInformation;

        UserSavedFilterConversionInformations(final String ownerName)
        {
            this.ownerName = notNull("ownerName", ownerName);
            usersSavedFilterConversionInformation = new ArrayList<SavedFilterConversionInformation>();
        }

        public void addConversionResult(final SavedFilterConversionInformation savedFilterConversionInformation)
        {
            usersSavedFilterConversionInformation.add(savedFilterConversionInformation);
        }

        public Collection<SavedFilterConversionInformation> getUsersSavedFilterConversionInformation()
        {
            return usersSavedFilterConversionInformation;
        }

        public String getOwnerName()
        {
            return ownerName;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final UserSavedFilterConversionInformations that = (UserSavedFilterConversionInformations) o;

            if (ownerName != null ? !ownerName.equals(that.ownerName) : that.ownerName != null)
            {
                return false;
            }
            return (usersSavedFilterConversionInformation != null) ? usersSavedFilterConversionInformation.equals(that.usersSavedFilterConversionInformation) : that.usersSavedFilterConversionInformation == null;
        }

        @Override
        public int hashCode()
        {
            int result = ownerName != null ? ownerName.hashCode() : 0;
            result = 31 * result + (usersSavedFilterConversionInformation != null ? usersSavedFilterConversionInformation.hashCode() : 0);
            return result;
        }
    }

    // Holds one set of conversion information for a saved filter
    public static class SavedFilterConversionInformation
    {
        private final String ownerName;
        private final String filterName;
        private final Long filterId;
        private final Collection<ClauseXmlHandler.ConversionResult> whereConversionErrors;
        private final Collection<OrderByXmlHandler.ConversionError> orderByConversionErrors;

        SavedFilterConversionInformation(final String ownerName, final String filterName, final Long filterId)
        {
            this.ownerName = ownerName;
            this.filterName = filterName;
            this.filterId = filterId;
            whereConversionErrors = null;
            orderByConversionErrors = null;
        }

        SavedFilterConversionInformation(final String ownerName, final String filterName, final Long filterId, final Collection<ClauseXmlHandler.ConversionResult> whereConversionErrors, final Collection<OrderByXmlHandler.ConversionError> orderByConversionErrors)
        {
            this.ownerName = ownerName;
            this.filterName = filterName;
            this.filterId = filterId;
            this.whereConversionErrors = whereConversionErrors;
            this.orderByConversionErrors = orderByConversionErrors;
        }

        public String getFilterName()
        {
            return filterName;
        }

        public Long getFilterId()
        {
            return filterId;
        }

        public String getOwnerName()
        {
            return ownerName;
        }

        public Collection<ClauseXmlHandler.ConversionResult> getWhereConversionErrors()
        {
            return whereConversionErrors;
        }

        public Collection<OrderByXmlHandler.ConversionError> getOrderByConversionErrors()
        {
            return orderByConversionErrors;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final SavedFilterConversionInformation that = (SavedFilterConversionInformation) o;

            if (filterId != null ? !filterId.equals(that.filterId) : that.filterId != null)
            {
                return false;
            }
            if (filterName != null ? !filterName.equals(that.filterName) : that.filterName != null)
            {
                return false;
            }
            if (orderByConversionErrors != null ? !orderByConversionErrors.equals(that.orderByConversionErrors) : that.orderByConversionErrors != null)
            {
                return false;
            }
            if (ownerName != null ? !ownerName.equals(that.ownerName) : that.ownerName != null)
            {
                return false;
            }
            return (whereConversionErrors != null) ? whereConversionErrors.equals(that.whereConversionErrors) : that.whereConversionErrors == null;
        }

        @Override
        public int hashCode()
        {
            int result = ownerName != null ? ownerName.hashCode() : 0;
            result = 31 * result + (filterName != null ? filterName.hashCode() : 0);
            result = 31 * result + (filterId != null ? filterId.hashCode() : 0);
            result = 31 * result + (whereConversionErrors != null ? whereConversionErrors.hashCode() : 0);
            result = 31 * result + (orderByConversionErrors != null ? orderByConversionErrors.hashCode() : 0);
            return result;
        }
    }

    // Holds the converted clauses and the messages that may have been generated by the conversion.
    static class WhereClauseConversionResults
    {
        private final List<Clause> convertedClauses;
        private final Set<Clause> clausesNotToNamify;
        private final List<ClauseXmlHandler.ConversionResult> conversionMessages;

        public WhereClauseConversionResults(final List<Clause> convertedClauses, final List<ClauseXmlHandler.ConversionResult> conversionMessages, final Set<Clause> clausesNotToNamify)
        {
            this.convertedClauses = convertedClauses;
            this.conversionMessages = conversionMessages;
            this.clausesNotToNamify = clausesNotToNamify;
        }

        public List<Clause> getConvertedClauses()
        {
            return convertedClauses;
        }

        public List<ClauseXmlHandler.ConversionResult> getConversionMessages()
        {
            return conversionMessages;
        }

        public Set<Clause> getClausesNotToNamify()
        {
            return clausesNotToNamify;
        }
    }

    /**
     * Used to map between the strings that were stored in the old-style XML to the clause names.
     * <p/>
     * This exists here as it should no longer be relevant after upgrading to 4.0.
     *
     * @since v4.0
     */
    public static class DocumentConstantToClauseNameResolver
    {
        private static final Map<String, String> constToClauseNameMap;

        static
        {
            constToClauseNameMap = new HashMap<String, String>();
            constToClauseNameMap.put(SystemSearchConstants.forPriority().getIndexField(),
                SystemSearchConstants.forPriority().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forProject().getIndexField(),
                SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forIssueType().getIndexField(),
                SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forComponent().getIndexField(),
                SystemSearchConstants.forComponent().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forAffectedVersion().getIndexField(),
                SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forFixForVersion().getIndexField(),
                SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forResolution().getIndexField(),
                SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forStatus().getIndexField(),
                SystemSearchConstants.forStatus().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forSummary().getIndexField(),
                SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forDescription().getIndexField(),
                SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forEnvironment().getIndexField(),
                SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forComments().getUrlParameter(),
                SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forCreatedDate().getIndexField(),
                SystemSearchConstants.forCreatedDate().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forUpdatedDate().getIndexField(),
                SystemSearchConstants.forUpdatedDate().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forDueDate().getIndexField(),
                SystemSearchConstants.forDueDate().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forResolutionDate().getIndexField(),
                SystemSearchConstants.forResolutionDate().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forReporter().getIndexField(),
                SystemSearchConstants.forReporter().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forAssignee().getIndexField(),
                SystemSearchConstants.forAssignee().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forIssueId().getIndexField(),
                SystemSearchConstants.forIssueId().getJqlClauseNames().getPrimaryName());
            // We don't need the saved filter since this is new for JQL
            //            constToClauseNameMap.put(SystemSearchConstants.forSavedFilter().getIndexField(), SystemSearchConstants.forSavedFilter().getJqlClauseNames().getPrimaryName());
            // This is explicitly "key" because to get a case-insensitive match we added a new index field which is key-folded, for the upgrade we want the old one
            constToClauseNameMap.put("key", SystemSearchConstants.forIssueKey().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forIssueParent().getIndexField(),
                SystemSearchConstants.forIssueParent().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forWorkRatio().getIndexField(),
                SystemSearchConstants.forWorkRatio().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forCurrentEstimate().getIndexField(),
                SystemSearchConstants.forCurrentEstimate().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forOriginalEstimate().getIndexField(),
                SystemSearchConstants.forOriginalEstimate().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forTimeSpent().getIndexField(),
                SystemSearchConstants.forTimeSpent().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forSecurityLevel().getIndexField(),
                SystemSearchConstants.forSecurityLevel().getJqlClauseNames().getPrimaryName());
            constToClauseNameMap.put(SystemSearchConstants.forVotes().getIndexField(),
                SystemSearchConstants.forVotes().getJqlClauseNames().getPrimaryName());
            // We don't need this because you could not search for project categories before JQL and this will conflict with the project doc id since category searches projects
            //            constToClauseNameMap.put(SystemSearchConstants.forProjectCategory().getIndexField(), SystemSearchConstants.forProjectCategory().getJqlClauseNames().getPrimaryName());
        }

        public static String getClauseName(final String documentConstant)
        {
            final String clauseName = constToClauseNameMap.get(documentConstant);
            if (clauseName != null)
            {
                return clauseName;
            }
            else
            {
                // Lets fall back to trying to find it as a custom field document constant
                return getClauseNameForCustomFieldDocumentConstant(documentConstant);
            }
        }

        private static String getClauseNameForCustomFieldDocumentConstant(final String documentConstantName)
        {
            if ((documentConstantName != null) && documentConstantName.startsWith(FieldManager.CUSTOM_FIELD_PREFIX))
            {
                try
                {
                    final long cfId = Long.parseLong(documentConstantName.substring(FieldManager.CUSTOM_FIELD_PREFIX.length()));
                    return JqlCustomFieldId.toString(cfId);
                }
                catch (final NumberFormatException e)
                {
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Clause visitor which transforms a clause's terminal nodes into "namified" form - custom field clause names are
     * converted to their full name where possible, and right-hand-side id values are converted to names where possible.
     */
    public static class NamifyingClauseVisitor implements ClauseVisitor<Clause>
    {
        private final User user;
        private final SearchHandlerManager searchHandlerManager;
        private final SearchContext searchContext;
        private final Set<Clause> clausesNotToNamify;
        private boolean disableNamifyForTree = false;

        public NamifyingClauseVisitor(final User user, final SearchHandlerManager searchHandlerManager, final SearchContext searchContext, final Set<Clause> clausesNotToNamify)
        {
            this.user = notNull("user", user);
            this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
            this.searchContext = notNull("searchContext", searchContext);
            this.clausesNotToNamify = notNull("clausesNotToNamify", clausesNotToNamify);
        }

        public Clause visit(final AndClause andClause)
        {
            final boolean oldDisableNamifyForTree = disableNamifyForTree;
            if (clausesNotToNamify.contains(andClause))
            {
                disableNamifyForTree = true;
            }

            final List<Clause> subClauses = new ArrayList<Clause>();
            for (final Clause clause : andClause.getClauses())
            {
                subClauses.add(clause.accept(this));
            }

            disableNamifyForTree = oldDisableNamifyForTree;
            return new AndClause(subClauses);
        }

        public Clause visit(final NotClause notClause)
        {
            final boolean oldDisableNamifyForTree = disableNamifyForTree;
            if (clausesNotToNamify.contains(notClause))
            {
                disableNamifyForTree = true;
            }

            final Clause newSubClause = notClause.getSubClause().accept(this);

            disableNamifyForTree = oldDisableNamifyForTree;
            return new NotClause(newSubClause);
        }

        public Clause visit(final OrClause orClause)
        {
            final boolean oldDisableNamifyForTree = disableNamifyForTree;
            if (clausesNotToNamify.contains(orClause))
            {
                disableNamifyForTree = true;
            }

            final List<Clause> subClauses = new ArrayList<Clause>();
            for (final Clause clause : orClause.getClauses())
            {
                subClauses.add(clause.accept(this));
            }

            disableNamifyForTree = oldDisableNamifyForTree;
            return new OrClause(subClauses);
        }

        public Clause visit(final TerminalClause clause)
        {
            try
            {
                final IssueSearcher<?> searcher = getSearcher(clause);
                if (searcher != null)
                {
                    Clause namifiedClause = namifyLeftAndRightHandSides(clause, searcher.getSearchInputTransformer());
                    if (namifiedClause != null)
                    {
                        // if we didn't want to do a full namify, just keep the LHS of the namified clause
                        if (disableNamifyForTree || clausesNotToNamify.contains(clause))
                        {
                            namifiedClause = namifyLeftHandSide(clause, (TerminalClause) namifiedClause);
                        }
                        return namifiedClause;
                    }
                }
            }
            catch (final Exception e)
            {
                // being really defensive here; in case there is any problem, we want to just return what we were given
                log.warn("Could not namify the terminal clause '" + clause.toString() + "'", e);
            }
            return clause;
        }

        @Override
        public Clause visit(WasClause clause)
        {
            return clause;
        }

        @Override
        public Clause visit(ChangedClause clause)
        {
            // Doesn't need to be upgraded - only exists from 5.0 on
            return clause;
        }

        // pipes the clause through the SearchInputTransformer so that the values and the clause name are namified by
        // the existing logic
        private Clause namifyLeftAndRightHandSides(final TerminalClause clause, final SearchInputTransformer transformer)
        {
            final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
            transformer.populateFromQuery(user, valuesHolder, new QueryImpl(clause), searchContext);
            return transformer.getSearchClause(user, valuesHolder);
        }

        // takes the result of Left and Right hand side namification and just keeps the left side by substituting the
        // terminal clause names.
        private TerminalClause namifyLeftHandSide(final TerminalClause clause, final TerminalClause namifiedClause)
        {
            return new TerminalClauseImpl(namifiedClause.getName(), namifiedClause.getOperator(), clause.getOperand());
        }

        private IssueSearcher<?> getSearcher(final TerminalClause clause)
        {
            final Collection<IssueSearcher<?>> searchersByClauseName = searchHandlerManager.getSearchersByClauseName(user, clause.getName(),
                searchContext);
            if (searchersByClauseName.size() == 1)
            {
                return searchersByClauseName.iterator().next();
            }
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Unable to resolve only one searcher for field '%s', found '%d' searchers", clause.getName(),
                    searchersByClauseName.size()));
            }
            return null;
        }
    }
}
