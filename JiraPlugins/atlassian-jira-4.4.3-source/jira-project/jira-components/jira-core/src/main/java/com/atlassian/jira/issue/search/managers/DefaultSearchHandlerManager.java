package com.atlassian.jira.issue.search.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.search.QueryCache;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.SearchHandler.SearcherRegistration;
import com.atlassian.jira.issue.search.SystemClauseHandlerFactory;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherComparatorFactory;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;
import com.atlassian.jira.util.collect.IdentitySet;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.Predicates.allOf;
import static com.atlassian.jira.util.collect.CollectionUtil.copyAsImmutableList;
import static com.atlassian.jira.util.collect.CollectionUtil.filter;
import static com.atlassian.jira.util.collect.CollectionUtil.toList;
import static com.atlassian.jira.util.collect.CollectionUtil.transform;
import static com.atlassian.jira.util.collect.CollectionUtil.transformSet;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.unmodifiableCollection;

/**
 * Default JIRA implementation of {@link com.atlassian.jira.issue.search.managers.SearchHandlerManager}.
 *
 * @since v4.0
 */
@ThreadSafe
public final class DefaultSearchHandlerManager implements SearchHandlerManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultSearchHandlerManager.class);

    private final FieldManager fieldManager;
    private final CustomFieldManager customFieldManager;
    private final SystemClauseHandlerFactory systemClauseHandlerFactory;
    private final QueryCache queryCache;

    @GuardedBy("this")
    private Helper helper = null;
    private final EventPublisher eventPublisher;

    public DefaultSearchHandlerManager(final FieldManager fieldManager, final CustomFieldManager customFieldManager,
            final SystemClauseHandlerFactory systemClauseHandlerFactory, final EventPublisher eventPublisher, QueryCache queryCache)
    {
        this.eventPublisher = eventPublisher;
        this.queryCache = queryCache;
        this.systemClauseHandlerFactory = notNull("systemClauseHandlerFactory", systemClauseHandlerFactory);
        this.fieldManager = notNull("fieldManager", fieldManager);
        this.customFieldManager = notNull("customFieldManager", customFieldManager);
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public Collection<ClauseHandler> getClauseHandler(final User user, final String jqlClauseName)
    {
        Collection<ClauseHandler> clauseHandler = queryCache.getClauseHandlers(user, jqlClauseName);
        if (clauseHandler == null)
        {
            List<ClauseHandler> filteredHandlers = new ArrayList<ClauseHandler>();
            Collection<ClauseHandler> unfilteredHandlers = getClauseHandler(jqlClauseName);
            for (ClauseHandler handler : unfilteredHandlers)
            {
                if (handler.getPermissionHandler().hasPermissionToUseClause(user))
                {
                    filteredHandlers.add(handler);
                }
            }
            clauseHandler = unmodifiableCollection(filteredHandlers);
            queryCache.setClauseHandlers(user, jqlClauseName, clauseHandler);
        }

        return clauseHandler;
    }

    public Collection<ClauseHandler> getClauseHandler(final String jqlClauseName)
    {
        notBlank("jqlClauseName", jqlClauseName);
        return unmodifiableCollection(getHelper().getSearchHandler(jqlClauseName));
    }

    public Collection<ClauseNames> getJqlClauseNames(final String fieldId)
    {
        notBlank("fieldId", fieldId);
        return returnNullAsEmpty(getHelper().getJqlClauseNames(fieldId));
    }

    @Override
    public Collection<String> getFieldIds(com.opensymphony.user.User searcher, String jqlClauseName)
    {
        return getFieldIds((User) searcher, jqlClauseName);
    }

    public Collection<ClauseNames> getVisibleJqlClauseNames(final User searcher)
    {
        return transformSet(filter(getHelper().getSearchHandlers(), new PermissionToUse(searcher)), getClauseNames);
    }

    @Override
    public Collection<ClauseHandler> getVisibleClauseHandlers(com.opensymphony.user.User searcher)
    {
        return getVisibleClauseHandlers((User) searcher);
    }

    public Collection<ClauseHandler> getVisibleClauseHandlers(final User searcher)
    {
        Set<ClauseHandler> visibleClauseHandlers = new HashSet<ClauseHandler>();
        final Collection<ClauseHandler> clauseHandlers = getHelper().getSearchHandlers();
        for (ClauseHandler clauseHandler : clauseHandlers)
        {
            if (clauseHandler.getPermissionHandler().hasPermissionToUseClause(searcher))
            {
                visibleClauseHandlers.add(clauseHandler);
            }
        }

        return visibleClauseHandlers;
    }

    @Override
    public Collection<IssueSearcher<?>> getSearchersByClauseName(com.opensymphony.user.User user, String jqlClauseName, SearchContext searchContext)
    {
        return getSearchersByClauseName((User) user, jqlClauseName, searchContext);
    }

    public Collection<String> getFieldIds(final User searcher, final String jqlClauseName)
    {
        final Predicate<ClauseHandler> predicate = allOf(hasFieldId, new PermissionToUse(searcher));
        return transform(filter(getHelper().getSearchHandler(jqlClauseName), predicate), getFieldId);
    }

    public Collection<String> getFieldIds(final String jqlClauseName)
    {
        return transform(filter(getHelper().getSearchHandler(jqlClauseName), hasFieldId), getFieldId);
    }

    @Override
    public Collection<ClauseNames> getVisibleJqlClauseNames(com.opensymphony.user.User searcher)
    {
        return getVisibleJqlClauseNames((User) searcher);
    }

    public Collection<IssueSearcher<?>> getSearchersByClauseName(final User user, final String jqlClauseName, final SearchContext searchContext)
    {
        notBlank("jqlClauseName", jqlClauseName);
        return filter(transform(getHelper().getIssueSearcherRegistrationsByClauseName(jqlClauseName),
            new Function<SearcherRegistration, IssueSearcher<?>>()
            {
                public IssueSearcher<?> get(final SearcherRegistration searcherRegistration)
                {
                    return searcherRegistration.getIssueSearcher();
                }
            }), allOf(Predicates.<IssueSearcher<?>> notNull(), new IsShown(user, searchContext)));
    }

    @Override
    public Collection<IssueSearcher<?>> getSearchers(com.opensymphony.user.User searcher, SearchContext context)
    {
        return getSearchers((User) searcher, context);
    }

    public Collection<IssueSearcher<?>> getSearchers(final User searcher, final SearchContext context)
    {
        notNull("context", context);
        context.verify();
        return toList(filter(getAllSearchers(), new IsShown(searcher, context)));
    }

    public Collection<IssueSearcher<?>> getAllSearchers()
    {
        return returnNullAsEmpty(getHelper().getAllIssueSearchers());
    }

    public Collection<SearcherGroup> getSearcherGroups(final SearchContext searchContext)
    {
        return returnNullAsEmpty(getHelper().getSearcherGroups());
    }

    public IssueSearcher<?> getSearcher(final String id)
    {
        notBlank("id", id);
        return getHelper().getIssueSearcher(id);
    }

    private synchronized Helper getHelper()
    {
        if (helper == null)
        {
            //We must process all the system fields first to ensure that we don't overwrite custom fields with
            //the system fields.
            final SearchHandlerIndexer indexer = new SearchHandlerIndexer();
            final Set<SearchableField> allSearchableFields = fieldManager.getSystemSearchableFields();
            for (final SearchableField field : allSearchableFields)
            {
                indexer.indexSystemField(field);
            }

            // Process all the system clause handlers, the JQL clause elements that are not associated with fields
            indexer.indexSystemClauseHandlers(systemClauseHandlerFactory.getSystemClauseSearchHandlers());

            final List<CustomField> customField = customFieldManager.getCustomFieldObjects();
            for (final CustomField field : customField)
            {
                indexer.indexCustomField(field);
            }

            helper = new Helper(indexer);
        }
        return helper;
    }

    public synchronized void refresh()
    {
        helper = null;
    }

    @Override
    public Collection<ClauseHandler> getClauseHandler(com.opensymphony.user.User user, String jqlClauseName)
    {
        return getClauseHandler((User) user, jqlClauseName);
    }

    private static <T> Collection<T> returnNullAsEmpty(final Collection<T> collection)
    {
        if (collection == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return collection;
        }
    }

    /**
     * The delegate used by the manager to implement its functionality in a thread safe way.
     *
     * @since 4.0.
     */
    @ThreadSafe
    private static class Helper
    {
        /**
         * ClauseName -> ClauseHandler.
         */
        private final Map<String, List<ClauseHandler>> handlerIndex;

        /**
         * SearcherId -> IssueSearcher.
         */
        private final Map<String, IssueSearcher<?>> searcherIndex;

        /**
         * ClauseName -> SearcherRegistration.
         */
        private final Map<String, List<SearchHandler.SearcherRegistration>> searcherClauseNameIndex;

        /**
         * FieldId -> ClauseName.
         */
        private final Map<String, List<ClauseNames>> fieldIdToClauseNames;

        /**
         * All JIRA's searcher groups.
         */
        private final List<SearcherGroup> searcherGroup;

        public Helper(final SearchHandlerIndexer indexer)
        {
            searcherIndex = indexer.createSearcherIdIndex();
            handlerIndex = indexer.createHandlerIndex();
            searcherGroup = indexer.createSearcherGroups();
            searcherClauseNameIndex = indexer.createSearcherJqlNameIndex();
            fieldIdToClauseNames = indexer.createFieldToClauseNamesIndex();
        }

        public Collection<ClauseHandler> getSearchHandler(final String jqlName)
        {
            return returnNullAsEmpty(handlerIndex.get(CaseFolding.foldString(jqlName)));
        }

        public Collection<ClauseHandler> getSearchHandlers()
        {
            final Set<ClauseHandler> allHandlers = new HashSet<ClauseHandler>();
            final Collection<List<ClauseHandler>> handlersList = handlerIndex.values();
            for (final List<ClauseHandler> clauseHandlers : handlersList)
            {
                allHandlers.addAll(clauseHandlers);
            }
            return allHandlers;
        }

        public IssueSearcher<?> getIssueSearcher(final String searcher)
        {
            return searcherIndex.get(searcher);
        }

        public Collection<IssueSearcher<?>> getAllIssueSearchers()
        {
            return searcherIndex.values();
        }

        public Collection<SearchHandler.SearcherRegistration> getIssueSearcherRegistrationsByClauseName(final String jqlName)
        {
            return returnNullAsEmpty(searcherClauseNameIndex.get(CaseFolding.foldString(jqlName)));
        }

        public List<SearcherGroup> getSearcherGroups()
        {
            return searcherGroup;
        }

        public List<ClauseNames> getJqlClauseNames(final String fieldId)
        {
            return fieldIdToClauseNames.get(fieldId);
        }
    }

    /**
     * Class that is used by the manager to build its state from {@link com.atlassian.jira.issue.search.SearchHandler}s.
     *
     * @since 4.0
     */
    @NotThreadSafe
    private static class SearchHandlerIndexer
    {
        private final Set<String> systemClauses = new HashSet<String>();
        private final Map<String, Set<ClauseHandler>> handlerIndex = new HashMap<String, Set<ClauseHandler>>();
        private final Map<String, Set<SearchHandler.SearcherRegistration>> searcherClauseNameIndex = new LinkedHashMap<String, Set<SearchHandler.SearcherRegistration>>();
        private final Map<String, IssueSearcher<?>> searcherIdIndex = new LinkedHashMap<String, IssueSearcher<?>>();
        private final Map<SearcherGroupType, Set<IssueSearcher<?>>> groupIndex = new EnumMap<SearcherGroupType, Set<IssueSearcher<?>>>(
            SearcherGroupType.class);

        SearchHandlerIndexer()
        {
            for (final SearcherGroupType groupType : SearcherGroupType.values())
            {
                groupIndex.put(groupType, IdentitySet.<IssueSearcher<?>> newListOrderedSet());
            }
        }

        Map<String, List<ClauseNames>> createFieldToClauseNamesIndex()
        {
            final Map<String, List<ClauseNames>> fieldToClauseNames = new HashMap<String, List<ClauseNames>>();
            for (final Set<ClauseHandler> handlers : handlerIndex.values())
            {
                for (final ClauseHandler handler : handlers)
                {
                    final ClauseInformation information = handler.getInformation();
                    if (information.getFieldId() != null)
                    {
                        List<ClauseNames> names = fieldToClauseNames.get(information.getFieldId());
                        if (names == null)
                        {
                            names = new ArrayList<ClauseNames>();
                            fieldToClauseNames.put(information.getFieldId(), names);
                        }

                        names.add(information.getJqlClauseNames());
                    }
                }
            }

            //Much to Dylan's disgust, we make a copy to make it safe.
            final Map<String, List<ClauseNames>> returnMe = new HashMap<String, List<ClauseNames>>();
            for (final Map.Entry<String, List<ClauseNames>> entry : fieldToClauseNames.entrySet())
            {
                returnMe.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
            }

            return Collections.unmodifiableMap(returnMe);
        }

        Map<String, List<ClauseHandler>> createHandlerIndex()
        {
            final Map<String, List<ClauseHandler>> tmpHandlerIndex = new HashMap<String, List<ClauseHandler>>();
            for (final Map.Entry<String, Set<ClauseHandler>> entry : handlerIndex.entrySet())
            {
                tmpHandlerIndex.put(entry.getKey(), copyAsImmutableList(entry.getValue()));
            }
            return Collections.unmodifiableMap(tmpHandlerIndex);
        }

        Map<String, List<SearchHandler.SearcherRegistration>> createSearcherJqlNameIndex()
        {
            final HashMap<String, List<SearchHandler.SearcherRegistration>> tmpHandlerIndex = new HashMap<String, List<SearchHandler.SearcherRegistration>>();
            for (final Map.Entry<String, Set<SearchHandler.SearcherRegistration>> entry : searcherClauseNameIndex.entrySet())
            {
                tmpHandlerIndex.put(entry.getKey(), copyAsImmutableList(entry.getValue()));
            }
            return Collections.unmodifiableMap(tmpHandlerIndex);
        }

        List<SearcherGroup> createSearcherGroups()
        {
            final List<SearcherGroup> groups = new ArrayList<SearcherGroup>();
            for (final Map.Entry<SearcherGroupType, Set<IssueSearcher<?>>> entry : groupIndex.entrySet())
            {
                if (!entry.getValue().isEmpty())
                {
                    final List<IssueSearcher<?>> searcher = new ArrayList<IssueSearcher<?>>(entry.getValue());
                    Collections.sort(searcher, SearcherComparatorFactory.getSearcherComparator(entry.getKey()));
                    groups.add(new SearcherGroup(entry.getKey(), searcher));
                }
                else
                {
                    groups.add(new SearcherGroup(entry.getKey(), Collections.<IssueSearcher<?>> emptyList()));
                }
            }
            return Collections.unmodifiableList(groups);
        }

        Map<String, IssueSearcher<?>> createSearcherIdIndex()
        {
            return Collections.unmodifiableMap(new LinkedHashMap<String, IssueSearcher<?>>(searcherIdIndex));
        }

        void indexSystemField(final SearchableField systemField)
        {
            indexSearchableField(systemField, true);
        }

        public void indexCustomField(final CustomField customField)
        {
            indexSearchableField(customField, false);
        }

        public void indexSystemClauseHandlers(final Collection<SearchHandler> searchHandlers)
        {
            for (final SearchHandler searchHandler : searchHandlers)
            {
                indexClauseHandlers(null, searchHandler.getClauseRegistrations(), true);
            }
        }

        private void indexSearchableField(final SearchableField field, final boolean system)
        {
            final SearchHandler searchHandler = field.createAssociatedSearchHandler();
            if (searchHandler != null)
            {
                indexHandler(field, searchHandler, system);
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Searchable field '" + field.getId() + "' does not have a search handler, will not be searchable.");
                }
            }
        }

        private void indexHandler(final SearchableField field, final SearchHandler handler, final boolean system)
        {
            final SearchHandler.SearcherRegistration registration = handler.getSearcherRegistration();
            if (registration != null)
            {
                indexSearcherById(field, registration.getIssueSearcher(), system);
                // NOTE: you must call indexClauseHandlers first since it is populating a map of system fields, I know this sucks a bit, sorry :)
                indexClauseHandlers(field, registration.getClauseHandlers(), system);
                indexSearcherByJqlName(field, registration, system);
            }

            indexClauseHandlers(field, handler.getClauseRegistrations(), system);
        }

        private void indexClauseHandlers(final SearchableField field, final Collection<? extends SearchHandler.ClauseRegistration> clauseHandlers, final boolean system)
        {
            for (final SearchHandler.ClauseRegistration clauseHandler : clauseHandlers)
            {
                indexClauseHandlerByJqlName(field, clauseHandler, system);
            }
        }

        private void indexClauseHandlerByJqlName(final Field field, final SearchHandler.ClauseRegistration registration, final boolean system)
        {
            final Set<String> names = getClauseNames.get(registration.getHandler()).getJqlFieldNames();
            for (String name : names)
            {
                // We always want to look for a match in lowercase since that is how we cache it
                name = CaseFolding.foldString(name);
                //Do we already have a system clause of that name registered.
                if (systemClauses.contains(name))
                {
                    if (system)
                    {
                        if (field != null)
                        {
                            throw new RuntimeException(String.format(
                                "Two system clauses are trying to register against the same JQL name. New Field = '%s', Jql Name = '%s'.",
                                field.getName(), name));
                        }
                        else
                        {
                            throw new RuntimeException(String.format(
                                "Two system clauses are trying to register against the same JQL name. Clause with Jql Name = '%s'.", name));
                        }
                    }
                    else
                    {
                        final CustomFieldType type = ((CustomField) field).getCustomFieldType();
                        final String typeName = (type != null) ? type.getName() : "Unknown Type";
                        log.warn(String.format(
                            "A custom field '%s (%s)' is trying to register a clause handler against a system clause with name '%s'. Ignoring request.",
                            field.getName(), typeName, name));
                    }
                }
                else
                {
                    if (system)
                    {
                        systemClauses.add(name);
                    }

                    Set<ClauseHandler> currentHandlers = handlerIndex.get(name);
                    if (currentHandlers == null)
                    {
                        currentHandlers = IdentitySet.newListOrderedSet();
                        currentHandlers.add(registration.getHandler());
                        handlerIndex.put(name, currentHandlers);
                    }
                    else
                    {
                        currentHandlers.add(registration.getHandler());
                    }
                }
            }
        }

        // NOTE: this method must be invoked after {@link #indexClauseHandlerByJqlName } has been called since the method
        // is responsible for populating the systemClauses set.
        private void indexSearcherByJqlName(final SearchableField field, final SearchHandler.SearcherRegistration searcherRegistration, final boolean system)
        {
            for (final SearchHandler.ClauseRegistration clauseRegistration : searcherRegistration.getClauseHandlers())
            {
                for (String name : getClauseNames.get(clauseRegistration.getHandler()).getJqlFieldNames())
                {
                    // We always want to look for a match in lower-case since that is how we cache it
                    name = CaseFolding.foldString(name);

                    if (!system && systemClauses.contains(name))
                    {
                        final CustomFieldType type = ((CustomField) field).getCustomFieldType();
                        final String typeName = (type != null) ? type.getName() : "Unknown Type";
                        log.warn(String.format(
                            "A custom field '%s (%s)' is trying to register a searcher against a system clause with name '%s'. Ignoring request.",
                            field.getName(), typeName, name));
                    }
                    else
                    {
                        Set<SearchHandler.SearcherRegistration> currentSearcherRegistrations = searcherClauseNameIndex.get(name);
                        if (currentSearcherRegistrations == null)
                        {
                            currentSearcherRegistrations = IdentitySet.newListOrderedSet();
                            currentSearcherRegistrations.add(searcherRegistration);
                            searcherClauseNameIndex.put(name, currentSearcherRegistrations);
                        }
                        else
                        {
                            currentSearcherRegistrations.add(searcherRegistration);
                        }
                    }
                }
            }
        }

        private void indexSearcherById(final SearchableField field, final IssueSearcher<?> newSearcher, final boolean system)
        {
            if (newSearcher == null)
            {
                return;
            }

            final String searcherId = newSearcher.getSearchInformation().getId();
            final IssueSearcher<?> currentSearcher = searcherIdIndex.get(searcherId);
            if (currentSearcher != null)
            {
                if (currentSearcher != newSearcher)
                {
                    //TODO: What to do here. The currently happens for the QuerySearcher, but really should not.
                    log.debug(String.format(
                        "Trying to register two searchers to the same id. Field = '%s', Current searcher = '%s', New Searcher = '%s', SearcherId ='%s'.",
                        field.getName(), currentSearcher, newSearcher, searcherId));
                }
            }
            else
            {
                searcherIdIndex.put(searcherId, newSearcher);

                SearcherGroupType type;

                if (system)
                {
                    type = newSearcher.getSearchInformation().getSearcherGroupType();
                    if (type == null)
                    {
                        log.warn(String.format("System field '%s' does not have a group type registered. Placing in %s group.", field.getName(),
                            SearcherGroupType.CUSTOM));
                        type = SearcherGroupType.CUSTOM;
                    }
                }
                else
                {
                    final SearcherGroupType givenType = newSearcher.getSearchInformation().getSearcherGroupType();
                    if ((givenType != null) && (givenType != SearcherGroupType.CUSTOM))
                    {
                        final CustomFieldType cfType = ((CustomField) field).getCustomFieldType();
                        final String typeName = (cfType != null) ? cfType.getName() : "Unknown Type";
                        log.warn(String.format("Custom field '%s (%s)' is trying to register itself in the '%s' group.", field.getName(), typeName,
                            givenType));
                    }
                    type = SearcherGroupType.CUSTOM;
                }

                groupIndex.get(type).add(newSearcher);
            }
        }
    }

    static class PermissionToUse implements Predicate<ClauseHandler>
    {
        private final User user;

        PermissionToUse(final User user)
        {
            this.user = user;
        }

        public boolean evaluate(final ClauseHandler clauseHandler)
        {
            return clauseHandler.getPermissionHandler().hasPermissionToUseClause(user);
        }
    }

    static class IsShown implements Predicate<IssueSearcher<?>>
    {
        // Need an OSUser object for now
        private final com.opensymphony.user.User user;
        private final SearchContext context;

        public IsShown(final User user, final SearchContext context)
        {
            // Need an OSUser object for now
            this.user = OSUserConverter.convertToOSUser(user);
            this.context = context;
        }

        public boolean evaluate(final IssueSearcher<?> issueSearcher)
        {
            return issueSearcher.getSearchRenderer().isShown(user, context);
        }
    }

    static final Function<ClauseHandler, String> getFieldId = new Function<ClauseHandler, String>()
    {
        public String get(final ClauseHandler handler)
        {
            return handler.getInformation().getFieldId();
        }
    };

    static final Predicate<ClauseHandler> hasFieldId = new Predicate<ClauseHandler>()
    {
        public boolean evaluate(final ClauseHandler handler)
        {
            return getFieldId.get(handler) != null;
        }
    };

    static final Function<ClauseHandler, ClauseNames> getClauseNames = new Function<ClauseHandler, ClauseNames>()
    {
        public ClauseNames get(final ClauseHandler clauseHandler)
        {
            return clauseHandler.getInformation().getJqlClauseNames();
        }
    };
}
