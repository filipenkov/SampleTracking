package com.atlassian.jira.upgrade.tasks;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.ofbiz.DatabaseIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.Sized;
import com.opensymphony.module.propertyset.PropertySet;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;
import static com.atlassian.jira.util.collect.MapBuilder.newBuilder;

/**
 * This upgrade task is needed to convert existing data to the new resolution date system field from the charting custom
 * field.  The old custom field used to lazily calculated the resolution date.  This is no longer necessary, since the
 * resolution date can be set directly when the resolution of an issue changes if it is a system field.  We do however
 * need to calculate the resolution date for all issues that don't currently have one in this upgrade task.
 * <p/>
 * The resolution date will be looked up via the issue's change history.  If no change history entry can be found (may
 * be the case if the issue was imported via CSV), then upgrade task will fall back to the last updated date of the
 * issue.  This will be correct for 90% of the issues anyways.
 * <p/>
 * This upgrade task will also have to convert existing portlet configurations over to use the new system field. Any
 * resolution date custom field configurations will need to be removed.  All search requests relying on the resolution
 * date custom field will have to be upgraded to use the new system field.  Finally, all custom issue navigator column
 * layouts will be switched to use the new system field.
 * <p/>
 * A full re-index will be necessary to index the resolution date for any issue that was updated (a full re-index should
 * provide better performance than re-indexing each issue individually during the upgrade, since there'll most likely be
 * a very large number of issues).
 *
 * @since v4.0
 */
public class UpgradeTask_Build401 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build401.class);

    private static final String RESOLUTION_DATE_CF_KEY = "com.atlassian.jira.ext.charting:resolutiondate";
    private static final String TIMESINCE_PORTLET_KEY = "com.atlassian.jira.ext.charting:timesince";
    private static final String TIMESINCE_PORTLET_DATE_FIELD = "dateField";
    private static final String ISSUE_TABLE = "Issue";
    private static final String ISSUE_RESOLUTION_COUNT = "IssueResolutionCount";
    private static final String ISSUE_CHANGE_GROUP_MAX_VIEW = "ChangeGroupChangeItemMax";
    private static final EntityExpr RES_NOT_NULL_COND = new EntityExpr(IssueFieldConstants.RESOLUTION, EntityOperator.NOT_EQUAL, null);
    private static final int BATCH_SIZE = 200;

    private final OfBizDelegator ofBizDelegator;
    private final PortletConfigurationManager portletConfigurationManager;
    private final PortalPageManager portalPageManager;
    private final CustomFieldManager customFieldManager;
    private final ColumnLayoutManager columnLayoutManager;
    private List<Long> customFieldLongIds;
    private List<String> customFieldStringIds;

    public UpgradeTask_Build401(final OfBizDelegator ofBizDelegator, final PortletConfigurationManager portletConfigurationManager, final PortalPageManager portalPageManager, final CustomFieldManager customFieldManager, final ColumnLayoutManager columnLayoutManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.portletConfigurationManager = portletConfigurationManager;
        this.portalPageManager = portalPageManager;
        this.customFieldManager = customFieldManager;
        this.columnLayoutManager = columnLayoutManager;
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        //set the resolution date
        log.info("Calculating resolution date for all issues...");
        calculateResolutionDateForAllIssues();
        log.info("Calculating resolution date for all issues...DONE");

        if (!getCustomFieldStringIds().isEmpty())
        {
            //Update portlet configurations
            log.info("Updating portlet configurations with new resolution date system field...");
            updatePortletConfigurations();
            log.info("Updating portlet configurations with new resolution date system field...DONE");

            //update IssueNavigator column layout
            log.info("Updating issue navigator columns with new resolution date system field...");
            updateIssueNavigatorColumns();
            log.info("Updating issue navigator columns with new resolution date system field...DONE");

            //Update search requests
            log.info("Updating filters with new resolution date system field...");
            updateSearchRequests();
            log.info("Updating filters with new resolution date system field...DONE");

            //Remove all resolution date custom fields
            log.info("Removing any old resolution date custom fields...");
            removeCustomFields();
            //Shouldn't be necessary since this is the last part of the upgrade task, but clearing them anyway,
            //so that we're sure we wont have any stale cached values lying around.
            customFieldLongIds = null;
            customFieldStringIds = null;
            log.info("Removing any old resolution date custom fields...DONE");
        }
    }

    @Override
    public String getBuildNumber()
    {
        return "401";
    }

    @Override
    public String getShortDescription()
    {
        return "Resolution Date System Field: Calculating values for the resolution date system field for all issues. Replacing usages of the old charting plugin resolution date field with the new system field.";
    }

    /**
     * Iterate over all issues in the database, and check if the resolution is set.  If it is, then try to calculate the
     * resolution date for that issue based on its change history.  If no resolution date for an issue can be found, the
     * last updated date is used.
     * <p/>
     * We *could* do a search via the lucene index for all issues where the resolution date is set, however since this
     * is an upgrade task it is probably safest to use the database directly as our view of the world, since the indexes
     * may be out of date.
     * <p/>
     * Finally, using the last updated date as the resolution date if no change history can be found is probably not
     * accurate at all times, however it is better than not storing a resolution date at all, which will result in
     * issues not showing up in charts for example.
     */
    void calculateResolutionDateForAllIssues()
    {
        final int resolvedIssueCount = getResolvedIssuesCount();
        final ResolvedIssuesIterable resolvedIterable = new ResolvedIssuesIterable(ofBizDelegator, resolvedIssueCount);
        final Context context = Contexts.percentageLogger(resolvedIterable, log, "Calculating Resolution Date is {0}% complete");
        final CalculatingResolutionDateConsumer resolutionDateConsumer = new CalculatingResolutionDateConsumer(ofBizDelegator, context);
        resolvedIterable.foreach(resolutionDateConsumer);

        //process the last batch that may still have some issues in it!
        resolutionDateConsumer.processBatch();

        final Map<Long, Timestamp> issueToResolutionDateMap = resolutionDateConsumer.getIssueToResolutionDateMap();
        //Then need to fill in blanks for issues that may not have a resolution date yet (ie issues that were imported
        //using CSV and don't have the appropriate change history entries.
        resolvedIterable.foreach(new Consumer<GenericValue>()
        {
            public void consume(@NotNull final GenericValue input)
            {
                //if an issue hasn't got a calculated date, fall back to its updated date.
                final Long issueId = input.getLong("id");
                if (!issueToResolutionDateMap.containsKey(issueId))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Issue with id '" + issueId + "' doesn't have a calculated resolution date. Falling back to last updated date.");
                    }
                    issueToResolutionDateMap.put(issueId, input.getTimestamp(IssueFieldConstants.UPDATED));
                }
            }
        });

        //then run through all the issues and update them (need to do this in 2 steps because on databases like
        //DB2 or SQLServer you can't iterate and update at the same time!
        final Context updateContext = Contexts.percentageLogger(new MapSized(issueToResolutionDateMap), log,
            "Updating Resolution Date is {0}% complete");
        for (final Map.Entry<Long, Timestamp> entry : issueToResolutionDateMap.entrySet())
        {
            final Context.Task task = updateContext.start(entry);
            try
            {
                final Long issueId = entry.getKey();
                final Timestamp resolutionDate = entry.getValue();
                ofBizDelegator.bulkUpdateByPrimaryKey("Issue", newBuilder(IssueFieldConstants.RESOLUTION_DATE, resolutionDate).toMap(),
                    CollectionBuilder.list(issueId));
            }
            finally
            {
                task.complete();
            }
        }
    }

    static class CalculatingResolutionDateConsumer implements Consumer<GenericValue>
    {
        private final Set<Long> issueBatch = new HashSet<Long>(BATCH_SIZE);
        private final Map<Long, Timestamp> issueToResolutionDateMap = new HashMap<Long, Timestamp>();
        private final OfBizDelegator ofBizDelegator;
        private final Context context;

        CalculatingResolutionDateConsumer(final OfBizDelegator ofBizDelegator, final Context context)
        {
            this.ofBizDelegator = ofBizDelegator;
            this.context = context;
        }

        public void consume(@NotNull final GenericValue issueGv)
        {
            final Context.Task task = context.start(issueGv);
            try
            {
                issueBatch.add(issueGv.getLong("id"));
                if (issueBatch.size() == BATCH_SIZE)
                {
                    processBatch();
                }
            }
            finally
            {
                task.complete();
            }
        }

        public void processBatch()
        {
            //if there's no issues left, do nothing!
            if (issueBatch.isEmpty())
            {
                return;
            }

            final long startTime = System.currentTimeMillis();
            try
            {
                //find all change items where the field is the resolution field
                final EntityCondition fieldCondition = new EntityConditionList(EasyList.build(new EntityExpr("field", EntityOperator.EQUALS,
                    "Resolution"), new EntityExpr("field", EntityOperator.EQUALS, "resolution")), EntityOperator.OR);

                //and the new value of the resolution field wasn't null.
                final EntityCondition newStringCondition = new EntityExpr("newstring", EntityOperator.NOT_EQUAL, null);
                //JRA-18869: Only want to find change history where we went from an unresolved to resolved state!
                final EntityCondition oldValueCondition = new EntityExpr("oldvalue", EntityOperator.EQUALS, null);

                //filter all the issues in this batch.  Unfortunately an INNER JOIN with the issuetable and issues where resolution != null doesn't work
                //because HSQLDB is a piece of shit and doesn't know how to parse brackets.
                final EntityCondition issueCondition = new EntityExpr("issue", EntityOperator.IN, new ArrayList<Long>(issueBatch));

                //now find all issues where the resolution is not null, and the last change group item is set to something.
                final EntityCondition allConditions = new EntityConditionList(EasyList.build(issueCondition, fieldCondition, newStringCondition,
                    oldValueCondition), EntityOperator.AND);
                final List<GenericValue> issueToResolutionDateGVs = ofBizDelegator.findByCondition(ISSUE_CHANGE_GROUP_MAX_VIEW, allConditions,
                    CollectionBuilder.list("maxcreated", "issue"));
                for (final GenericValue gv : issueToResolutionDateGVs)
                {
                    issueToResolutionDateMap.put(gv.getLong("issue"), gv.getTimestamp("maxcreated"));
                }
                issueBatch.clear();
            }
            finally
            {
                if (log.isDebugEnabled())
                {
                    final long duration = System.currentTimeMillis() - startTime;
                    log.debug("Executed resolution dates query in '" + duration + "' ms.");
                }
            }
        }

        public Map<Long, Timestamp> getIssueToResolutionDateMap()
        {
            return issueToResolutionDateMap;
        }
    }

    /**
     * Looks for all customfields of the type resolution date, and finds their ids.  The ids are then used to lookup any
     * portlet that may be using this customfield.  We then go through each of the portlet configurations and only for
     * the timesince charting portlet do we replace the datefield with the new system resolutiondate field.  If we do
     * encounter another portlet (most likely some custom portlet written by someone outside of Atlassian) we simply
     * print a warning in the logs.  The logic here wont handle swapping any random portlet configuration to the
     * systemfield.
     */
    void updatePortletConfigurations()
    {
        final Set<Long> portletConfigurationIds = findPortletIdsForCustomFields(getCustomFieldStringIds());

        for (final Long portletId : portletConfigurationIds)
        {
            final PortletConfiguration portletConfiguration = portletConfigurationManager.getByPortletId(portletId);
            if (portletConfiguration == null)
            {
                log.info("Could not locate portlet configuration for id '" + portletId + "'.  It seems that this was a portlet using the '" + RESOLUTION_DATE_CF_KEY + "' custom field that wasn't removed properly.");
                //this should never happen.  If it does the portlet wont be in use anyway, so its safe to just skip it
                continue;
            }
            final String portletKey = portletConfiguration.getKey();
            //the timesince portlet is the only portlet that we need to worry about updating, since no other
            //system or charting plugin portlets can have the resolution date configured.
            if (TIMESINCE_PORTLET_KEY.equals(portletKey))
            {
                try
                {
                    final PropertySet properties = portletConfiguration.getProperties();
                    final String dateFieldValue = properties.getString(TIMESINCE_PORTLET_DATE_FIELD);
                    //if the datefield is set to use one of the custom fields, switch it to the new resolutiondate
                    //field
                    if (getCustomFieldStringIds().contains(dateFieldValue))
                    {
                        properties.setString(TIMESINCE_PORTLET_DATE_FIELD, IssueFieldConstants.RESOLUTION_DATE);
                        portletConfigurationManager.store(portletConfiguration);
                    }
                }
                catch (final ObjectConfigurationException e)
                {
                    log.error("Error retrieving objectConfiguration for portlet with id '" + portletId + "'", e);
                }
            }
            else
            {
                final PortalPage portalPage = portalPageManager.getPortalPageById(portletConfiguration.getDashboardPageId());
                if (portalPage != null)
                {
                    final String userName = portalPage.getOwnerUserName();
                    final String owner = userName == null ? "System Default" : userName;
                    log.warn("Encountered an unknown portlet '" + portletKey + "' (id '" + portletConfiguration.getId() + "') using the resolution date custom field. Please update the portlet's configuration to use " + "the new resolution date system field. The portlet is located on dashboard '" + portalPage.getName() + "' (id '" + portalPage.getId() + "') owned by the user '" + owner + "'.");
                }
                else
                {
                    log.warn("The portlet with portlet configuration id '" + portletId + "' seems to be an orphaned portlet (i.e. not displayed on any dashboard page) still using the '" + RESOLUTION_DATE_CF_KEY + "' custom field.");
                }
            }
        }
    }

    void removeCustomFields()
    {
        for (final Long customFieldId : getCustomFieldLongIds())
        {
            try
            {
                customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(customFieldId);
            }
            catch (final RemoveException e)
            {
                throw new RuntimeException("Error removing customfield '" + customFieldId + "'", e);
            }
        }
    }

    void updateIssueNavigatorColumns()
    {
        try
        {
            // first get the minimal data set required to determine which columnlayoutitems need updating
            final Map<Long, Long> layoutItemsToChange = new LinkedHashMap<Long, Long>();
            final List<Long> layoutItemsToRemove = new ArrayList<Long>();
            getColumnLayoutItemsToProcess(getCustomFieldStringIds(), layoutItemsToChange, layoutItemsToRemove);

            // remove the columnlayoutitems that need to be removed
            if (!layoutItemsToRemove.isEmpty())
            {
                ofBizDelegator.removeByOr("ColumnLayoutItem", "id", layoutItemsToRemove);
            }

            // update the columnlayoutitems to use the system field
            if (!layoutItemsToChange.isEmpty())
            {
                ofBizDelegator.bulkUpdateByPrimaryKey("ColumnLayoutItem", newBuilder("fieldidentifier", IssueFieldConstants.RESOLUTION_DATE).toMap(),
                    new ArrayList<Long>(layoutItemsToChange.keySet()));
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            //clear the columnlayoutmanager cache to ensure the columnlayouts will be reloaded from the DB, just in case
            //any upgrade tasks populated the cache before this upgrade task ran.
            columnLayoutManager.refresh();
        }
    }

    private void getColumnLayoutItemsToProcess(final List<String> customFieldIds, final Map<Long, Long> layoutItemsToChange, final List<Long> layoutItemsToRemove)
    {
        OfBizListIterator layoutItemsIterator = null;
        try
        {
            layoutItemsIterator = ofBizDelegator.findListIteratorByCondition("ColumnLayoutItem", new EntityExpr("fieldidentifier", EntityOperator.IN,
                customFieldIds), null, EasyList.build("id", "columnlayout"), EasyList.build("columnlayout ASC", "horizontalposition ASC"), null);

            // keep track of which columnlayouts have multiple layoutitems with the custom field:
            // the first columnlayoutitem will be marked for change; the rest will be marked for removal.
            for (GenericValue gv = layoutItemsIterator.next(); gv != null; gv = layoutItemsIterator.next())
            {
                final Long layoutId = gv.getLong("columnlayout");
                final Long layoutItemId = gv.getLong("id");
                if (layoutItemsToChange.containsValue(layoutId))
                {
                    layoutItemsToRemove.add(layoutItemId);
                }
                else
                {
                    layoutItemsToChange.put(layoutItemId, layoutId);
                }
            }
        }
        finally
        {
            if (layoutItemsIterator != null)
            {
                layoutItemsIterator.close();
            }
        }
    }

    void updateSearchRequests() throws GenericEntityException, ParseException
    {
        // Because MSSQL row/table locking sucks we can not just iterate over the SearchRequests, instead we need
        // to get all the ID's and then update the records in batches.
        final List<Long> searchRequestIds = getSearchRequestIds();

        // Lets create a logger that we can use to show how much of the upgrade task has been completed
        final Context percentageLogger = getSearchRequestPercentageLogger(searchRequestIds);

        // Lets run through all the search requests, converting them in batches of 200
        final List<Long> batchSearchRequestIds = new ArrayList<Long>();
        for (final Long searchRequestId : searchRequestIds)
        {
            batchSearchRequestIds.add(searchRequestId);
            if (batchSearchRequestIds.size() >= 200)
            {
                // If we hit our batch limit lets go ahead and convert what we have so far.
                updateSearchRequests(getSearchRequestGvsForIds(batchSearchRequestIds), percentageLogger);
                batchSearchRequestIds.clear();
            }
        }

        // Lets do the last batch which may be under 200
        if (!batchSearchRequestIds.isEmpty())
        {
            updateSearchRequests(getSearchRequestGvsForIds(batchSearchRequestIds), percentageLogger);
        }
    }

    Context getSearchRequestPercentageLogger(final List<Long> searchRequestIds)
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
        }, log, "Converting search requests to use the new resolution date system field is {0}% complete.");

        percentageLogger.setName("Converting search requests to use the new resolution date system field.");
        return percentageLogger;
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
        final List<GenericValue> searchRequestIdGvs = ofBizDelegator.findByCondition("SearchRequest", null, list("id"));
        for (final GenericValue searchRequestIdGv : searchRequestIdGvs)
        {
            searchRequestIds.add(searchRequestIdGv.getLong("id"));
        }
        return searchRequestIds;
    }

    ///CLOVER:ON

    private void updateSearchRequests(final List<GenericValue> searchRequestGvs, final Context percentageLogger) throws GenericEntityException
    {
        for (final GenericValue searchRequestGv : searchRequestGvs)
        {
            final Context.Task task = percentageLogger.start(searchRequestGv);
            try
            {
                updateSearchRequestIfContainsCustomFieldIds(searchRequestGv);
            }
            finally
            {
                task.complete();
            }
        }
    }

    private void updateSearchRequestIfContainsCustomFieldIds(final GenericValue searchRequestGv) throws GenericEntityException
    {
        // Read in the SearchRequest XML
        final Document searchRequestXml;
        final Elements paramEls;
        final Elements sortEls;
        final String xml = searchRequestGv.getString("request");
        try
        {
            searchRequestXml = new Document(xml);
            paramEls = searchRequestXml.getRoot().getElements("parameter");
            sortEls = searchRequestXml.getRoot().getElements("sort");
        }
        catch (final ParseException e)
        {
            log.warn("Unable to parse SearchRequest XML: " + xml);
            return;
        }

        final boolean replacedParams = convertSearchParams(paramEls);
        final boolean replacedSorts = convertSearchSorts(sortEls);
        if (replacedParams || replacedSorts)
        {
            searchRequestGv.setString("request", searchRequestXml.toString());
            searchRequestGv.store();
        }
    }

    /**
     * This needs to deal with multiple search params per customfield (i.e. both absolute and relative date search
     * defined).
     * <p/>
     * Param names will be of the form customfield_10000:relative.  However just in case, this code should also be able
     * to handle fields of the form customfield_10000.
     */
    private boolean convertSearchParams(final Elements paramEls)
    {
        final List<String> customFieldIds = getCustomFieldStringIds();

        String replacedCustomFieldId = null;
        while (paramEls.hasMoreElements())
        {
            final Element element = (Element) paramEls.nextElement();

            /* Parameters XML will look something like this.

            <parameter class='com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter'>\r
                 <customfield_10010 name='customfield_10010:absolute'>\r
                     <fromDate>1240149600000</fromDate>\r
                     <toDate>1240840800000</toDate>\r
                 </customfield_10010>\r
            </parameter>

            <parameter class='com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter'>\r
                 <customfield_10010 name='customfield_10010:relative'>\r
                     <previousOffset>4233600000</previousOffset>\r
                     <nextOffset>604800000</nextOffset>\r
                 </customfield_10010>
            </parameter>
            */
            // Get the first element which will contain the name of the field we are dealing with
            final Element fieldElement = element.getElements().first();
            final String paramFieldId = fieldElement.getName();
            if (customFieldIds.contains(paramFieldId))
            {
                final String paramName = fieldElement.getAttribute("name");

                String paramSuffix = "";
                if (paramName.contains(":"))
                {
                    final int colonIndex = paramName.indexOf(":");
                    paramSuffix = paramName.substring(colonIndex);
                }

                if ((replacedCustomFieldId == null) || replacedCustomFieldId.equals(paramFieldId))
                {
                    fieldElement.setName(IssueFieldConstants.RESOLUTION_DATE);
                    fieldElement.setAttribute("name", IssueFieldConstants.RESOLUTION_DATE + paramSuffix);
                    replacedCustomFieldId = paramFieldId;
                }
                else
                {
                    // Remove this whole element from the XML document
                    element.remove();
                }
            }
        }
        return replacedCustomFieldId != null;
    }

    private boolean convertSearchSorts(final Elements sortEls)
    {
        final List<String> customFieldIds = getCustomFieldStringIds();
        boolean replacedSorts = false;

        while (sortEls.hasMoreElements())
        {
            final Element sortEl = (Element) sortEls.nextElement();

            // Get the SearchSort element from within
            final Element searchSort = sortEl.getElements().first();
            final String field = searchSort.getAttribute("field");
            if (customFieldIds.contains(field))
            {
                if (!replacedSorts)
                {
                    searchSort.setAttribute("field", IssueFieldConstants.RESOLUTION_DATE);
                    replacedSorts = true;
                }
                else
                {
                    sortEl.remove();
                }
            }
        }
        return replacedSorts;
    }

    private Set<Long> findPortletIdsForCustomFields(final List<String> customFieldIds)
    {
        //using a LinkedHashSet to have ordering guaranteed for tests
        final Set<Long> portletConfigurationIds = new LinkedHashSet<Long>();
        for (final String customFieldId : customFieldIds)
        {
            //Need to use a findByLike here, since this may run on MSSQL.  The propertyValue field is of type NTEXT,
            //for which MSSQL does not support exact matches.
            final List<GenericValue> entityGVs = ofBizDelegator.findByLike("OSUserPropertySetView",
                newBuilder("propertyValue", customFieldId).toMap());
            for (final GenericValue entityGV : entityGVs)
            {
                portletConfigurationIds.add(entityGV.getLong("entityId"));
            }
        }
        return portletConfigurationIds;
    }

    /**
     * @return the numeric custom field ids that correspond with the resolution date custom field type
     */
    private List<Long> getCustomFieldLongIds()
    {
        if (customFieldLongIds == null)
        {
            customFieldLongIds = findResolutionDateCustomFields(new Function<GenericValue, Long>()
            {
                public Long get(final GenericValue input)
                {
                    return input.getLong(CustomFieldImpl.ENTITY_ID);
                }
            });
        }
        return customFieldLongIds;
    }

    /**
     * @return the string custom field ids that correspond with the resolution date custom field type
     */
    private List<String> getCustomFieldStringIds()
    {
        if (customFieldStringIds == null)
        {
            customFieldStringIds = findResolutionDateCustomFields(new Function<GenericValue, String>()
            {
                public String get(final GenericValue input)
                {
                    return FieldManager.CUSTOM_FIELD_PREFIX + input.getLong(CustomFieldImpl.ENTITY_ID);
                }
            });
        }
        return customFieldStringIds;
    }

    private <T> List<T> findResolutionDateCustomFields(final Function<GenericValue, T> transformer)
    {
        //accessing the DB directly, since the customFieldManager wont return the resolutiondate field if
        //the charting plugin has been removed.
        final List<GenericValue> customFields = ofBizDelegator.findByAnd(CustomFieldImpl.ENTITY_TABLE_NAME, newBuilder(
            CustomFieldImpl.ENTITY_CF_TYPE_KEY, RESOLUTION_DATE_CF_KEY).toMap());
        return CollectionUtil.transform(customFields, transformer);
    }

    static class ResolvedIssuesIterable extends DatabaseIterable<GenericValue>
    {
        private final OfBizDelegator ofBizDelegator;

        public ResolvedIssuesIterable(final OfBizDelegator ofBizDelegator, final int issueCount)
        {
            super(issueCount, new NoOpResolver());
            this.ofBizDelegator = ofBizDelegator;
        }

        @Override
        protected OfBizListIterator createListIterator()
        {
            return ofBizDelegator.findListIteratorByCondition(ISSUE_TABLE, RES_NOT_NULL_COND);
        }
    }

    static class NoOpResolver implements Resolver<GenericValue, GenericValue>
    {
        public GenericValue get(final GenericValue input)
        {
            return input;
        }
    }

    static class MapSized implements Sized
    {
        private final Map<?, ?> map;

        public MapSized(final Map<?, ?> map)
        {
            this.map = map;
        }

        public int size()
        {
            return map.size();
        }

        public boolean isEmpty()
        {
            return map.isEmpty();
        }
    }

    private int getResolvedIssuesCount()
    {
        final GenericValue countGV = EntityUtil.getOnly(ofBizDelegator.findByCondition(ISSUE_RESOLUTION_COUNT, RES_NOT_NULL_COND, list("count")));
        final int count = countGV.getLong("count").intValue();
        if (log.isInfoEnabled())
        {
            log.info("Total number of issues resolved is " + count + ".");
        }
        return count;
    }
}
