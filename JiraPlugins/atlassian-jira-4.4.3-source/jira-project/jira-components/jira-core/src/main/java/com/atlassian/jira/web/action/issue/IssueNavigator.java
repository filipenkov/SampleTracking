package com.atlassian.jira.web.action.issue;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.issue.search.ClauseTooComplexSearchException;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestFactory;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.issue.transport.impl.IssueNavigatorActionParams;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserQueryHistoryManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserNameEqualsUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.filter.FilterOperationsAction;
import com.atlassian.jira.web.action.filter.FilterOperationsBean;
import com.atlassian.jira.web.action.issue.navigator.ToolOptionGroup;
import com.atlassian.jira.web.action.issue.navigator.ToolOptionItem;
import com.atlassian.jira.web.action.util.navigator.IssueNavigatorType;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.atlassian.jira.web.component.jql.AutoCompleteJsonGenerator;
import com.atlassian.jira.web.session.SessionSelectedIssueManager.SelectedIssueData;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IssueNavigator extends SearchDescriptionEnabledAction implements IssueSearchResultsAction, FilterOperationsAction, SearchRequestViewsAction
{
    private static final Logger log = Logger.getLogger(IssueNavigator.class);

    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String JQL_QUERY_PARAMETER = "jqlQuery";
    public static final String MODE_SHOW = "show";
    public static final String MODE_HIDE = "hide";
    private static final String ADVANCED = "advanced";
    private static final int MAX_ADVANCED_VALIDATION_MESSAGES = 10;

    // ------------------------------------------------------------------------------------------------- Type Properties
    private SearchResults searchResults;
    private boolean valid = true;
    private String requestId;
    private SearchActionHelper actionHelper;
    private Boolean isAdvanced = null;
    private Boolean isFilter = null;
    private IssueNavigatorType type = null;
    /**
     * A consistent view of the selected issue data (avoids race condition from Ajax setting it
     * while we generate the search results).
     */
    private SelectedIssueData selectedIssueData;
    private Long selectedIssueId;

    // advanced stuff
    private boolean runQuery = false;
    private String jqlQuery = null;
    private Collection<String> warningMessages = null;
    private boolean isSearchUpdated;
    private int previousPagerStart;
    private String previousMode;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final SearchProvider searchProvider;
    protected final SearchRequestFactory searchRequestFactory;
    private final ColumnLayoutManager columnLayoutManager;
    private final SearchRequestService searchRequestService;
    private final TableLayoutFactory tableLayoutFactory;
    private final CommentManager commentManager;
    private final PluginAccessor pluginAccessor;
    private final PagerManager pagerManager;
    private final UserNameEqualsUtil userNameEqualsUtil;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final IndexLifecycleManager indexLifecycleManager;
    private final AutoCompleteJsonGenerator autoCompleteJsonGenerator;
    private final JqlStringSupport jqlStringSupport;
    private final UserQueryHistoryManager userQueryHistoryManager;
    private final SimpleLinkManager simpleLinkManager;
    private final IssueSearchLimits issueSearchLimits;
    private static final int FIND_ISSUE_WINDOW_SIZE = 20;
    private boolean clickedSearchButton;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public IssueNavigator(final SearchProvider searchProvider, final ColumnLayoutManager columnLayoutManager,
            final IssueSearcherManager issueSearcherManager, final SearchRequestFactory searchRequestFactory,
            final SearchRequestService searchRequestService, final TableLayoutFactory tableLayoutFactory,
            final CommentManager commentManager, final PluginAccessor pluginAccessor, final PagerManager pagerManager,
            final SearchService searchService, final ApplicationProperties applicationProperties,
            final IndexLifecycleManager indexLifecycleManager, final AutoCompleteJsonGenerator autoCompleteJsonGenerator,
            final SearchSortUtil searchSortUtil, final JqlStringSupport jqlStringSupport,
            final UserQueryHistoryManager userQueryHistoryManager, final SimpleLinkManager simpleLinkManager, IssueSearchLimits issueSearchLimits)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.searchProvider = searchProvider;
        this.searchRequestFactory = searchRequestFactory;
        this.searchRequestService = searchRequestService;
        this.columnLayoutManager = columnLayoutManager;
        this.tableLayoutFactory = tableLayoutFactory;
        this.commentManager = commentManager;
        this.pluginAccessor = pluginAccessor;
        this.pagerManager = pagerManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.indexLifecycleManager = indexLifecycleManager;
        this.autoCompleteJsonGenerator = autoCompleteJsonGenerator;
        this.jqlStringSupport = jqlStringSupport;
        this.userQueryHistoryManager = userQueryHistoryManager;
        this.simpleLinkManager = simpleLinkManager;
        this.issueSearchLimits = issueSearchLimits;
        this.userNameEqualsUtil = new UserNameEqualsUtil();
    }

    /*
     * The main entry point for IssueNavigator.
     */
    protected String doExecute() throws Exception
    {
        // we do not allow users to even enter if the navigator if indexing is disabled
        if (!indexLifecycleManager.isIndexingEnabled())
        {
            return "indexerror";
        }

        final IssueNavigatorActionParams actionParams = getActionParams();

        type = actionParams.getNavigatorType();
        if (type != null)
        {
            IssueNavigatorType.setInCookie(ActionContext.getResponse(), type);
        }
        else
        {
            type = getNavigatorType();
        }

        if (actionParams.isTooComplex())
        {
            addWarningMessage(getText("jira.jql.query.too.complex"));
        }

        recordPreviousToggleStates();
        // set the pager start as need be
        if (actionParams.isPagerStartSpecified())
        {
            setPagerStart(actionParams.getPagerStart());
        }

        if (actionParams.isCreateNewFilter())
        {
            setSearchRequest(null);
            resetPagerAndSelectedIssue();

            // we could be creating a new filter for either advanced or simple, so we need to determine which view
            // to show using the navType stored in session
            return getAdvancedOrSimpleView();
        }

        // This also ensures that the fieldValuesHolder is not accidentally read from the SR
        setFieldValuesHolder(new FieldValuesHolderImpl());

        final String result;
        if (MODE_HIDE.equals(getMode()))
        {
            // The same logic is used to render the View mode for both the simple and advanced
            result = _doExecutePopulateFromSearchRequest();
        }
        else if (type == IssueNavigatorType.ADVANCED || isAdvanced())
        {
            setNavigatorType(IssueNavigatorType.ADVANCED);
            result = _doExecuteAdvanced();
        }
        else
        {
            result = _doExecuteSimple();
        }
        if (getSearchRequest() != null && getJqlQuery() == null)
        {
            setCurrentJql(getSearchRequest().getQuery());
        }
        return result;
    }

    /**
     * Record the state of various toggles to the display of the issue navigator that pollute the URL so that we
     * can differentiate between an actual toggle and a page refresh that reuses the dirty URL.
     */
    private void recordPreviousToggleStates()
    {
        if (previousMode == null)
        {
            // If setMode() wasn't automatically called by webwork.
            previousMode = getMode();
        }
        previousPagerStart = getPager().getStart();
    }

    private void resetPagerAndSelectedIssue()
    {
        resetPager();
        clearSelectedIssue();
    }

    private String _doExecuteSimple()
    {
        final IssueNavigatorActionParams actionParams = getActionParams();

        if (actionParams.isRefreshOnly())
        {
            // Just populate from the fields (no search is performed)
            for (final IssueSearcher searcher : getSearchers())
            {
                searcher.getSearchInputTransformer().populateFromParams(getRemoteUser(), getFieldValuesHolder(), actionParams);
            }

            return getResult();
        }

        return _doExecutePopulateFromSearchRequest();
    }

    private String _doExecutePopulateFromSearchRequest()
    {
        boolean updateSorts = true;
        final IssueNavigatorActionParams actionParams = getActionParams();

        if (actionParams.isUpdateParamsRequired())
        {
            resetPagerAndSelectedIssue();
            if (_doUpdateParamsRequired(actionParams))
            {
                return getAdvancedOrSimpleView();
            }
            updateSorts = false;
        }
        else if (actionParams.isLoadSavedFilter())
        {
            // If no full update required
            SearchRequest loadedSR = loadAndSetSearchRequest();

            // request does not exist or user does not have permission to view request
            if (loadedSR == null)
            {
                valid = false;
                return getAdvancedOrSimpleView();
            }
            else
            {
                valid = true;
            }

            String redirect = validateAndGetRedirectForQuery(loadedSR.getQuery(), false);
            if (redirect != null)
            {
                return redirect;
            }
        }
        else
        {
            if (getSearchRequest() != null) // we wont be here if the search request in session is advanced
            {
                String redirect = validateAndGetRedirectForQuery(getSearchRequest().getQuery(), getSearchRequest().isModified());
                if (redirect != null)
                {
                    return redirect;
                }
                populateFieldValuesHolderFromQuery(getSearchRequest().getQuery(), getFieldValuesHolder());
            }
            log.debug("No updates to Search request performed. SR in session used.");
        }

        // If allow for add individual parameters. This will update the fieldValuesHolder
        if (actionParams.isAddParamsRequired())
        {
            if (getJqlQuery() != null)
            {
                Query jqlQuery = getNewSearchQuery();
                if (invalidInput())
                {
                    setMode(MODE_SHOW);
                    return ADVANCED;
                }

                // pass both the parsed Query and the action params to combine them
                setSearchRequest(searchRequestFactory.createFromQuery(getSearchRequest(), getRemoteUser(), jqlQuery));
            }
            else
            {
                setSearchRequest(searchRequestFactory.createFromParameters(getSearchRequest(), getRemoteUser(), actionParams));
            }
            updateSorts = false;

            String redirect = validateAndGetRedirectForQuery(getSearchRequest().getQuery(), getSearchRequest().isModified());
            if (redirect != null)
            {
                return redirect;
            }
            
            resetPagerAndSelectedIssue();
        }

        return showSearchResults(updateSorts);
    }

    private boolean _doUpdateParamsRequired(final IssueNavigatorActionParams actionParams)
    {
        final SearchRequest searchRequest;
        setUserCreated(getActionParams().isUserCreated());                
        if (actionParams.isUpdateExistingFilter())
        {
            searchRequest = getSearchRequest();
            if (log.isDebugEnabled())
            {
                log.debug("Updating an existing search request " + searchRequest);
            }
        }
        else
        {
            searchRequest = null;
            log.debug("Creating new search request");
        }

        if (getJqlQuery() != null)
        {
            Query jqlQuery = getNewSearchQuery();

            if (jqlQuery == null || invalidInput())
            {
                setMode(MODE_SHOW);
                setNavigatorType(IssueNavigatorType.ADVANCED);
                return true;
            }

            setSearchRequest(searchRequestFactory.createFromQuery(searchRequest, getRemoteUser(), jqlQuery));

            if (validateAndGetRedirectForQuery(jqlQuery, getSearchRequest().isModified()) != null)
            {
                return true;
            }
        }
        else
        {
            // Populate & validate all searchers
            if (!validateAndSetSearchRequestForParams(searchRequest, actionParams))
            {
                return true;
            }
        }
        resetPagerAndSelectedIssue();

        // Populate the fieldValuesHolder with values from the searchRequest, just to be sure
        if (getSearchRequest().isLoaded())
        {
            populateFieldValuesHolderFromQuery(getSearchRequest().getQuery(), getFieldValuesHolder());
            validateParams(getFieldValuesHolder(), searchService.getSearchContext(getRemoteUser(), getSearchRequest().getQuery()));

            if (invalidInput())
            {
                setMode(MODE_SHOW);
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to load the {@link SearchRequest} specified by the requestId parameter.
     * Error messages are added if unsuccessful.
     *
     * @return the search request loaded or null if it could not be retrieved.
     */
    private SearchRequest loadAndSetSearchRequest()
    {
        SearchRequest loadedSR = null;
        requestId = getActionParams().getFirstValueForKey("requestId");

        try
        {
            loadedSR = searchRequestService.getFilter(getJiraServiceContext(), Long.valueOf(requestId));
        }
        catch (final NumberFormatException nfe)
        {
            addErrorMessage(getText("navigator.error.filter.id.not.number", requestId));
        }
        catch (final DataAccessException e)
        {
            log.error("There was an error loading the saved search request", e);
            addErrorMessage(getText("navigator.error.filter.load", requestId));
        }
        if (loadedSR != null && !loadedSR.equals(getSearchRequest()))
        {
            resetPagerAndSelectedIssue();
        }
        setSearchRequest(loadedSR);
        setUserCreated(false);
        return loadedSR;
    }

    public final String doSwitchView()
    {
        final IssueNavigatorActionParams actionParams = getActionParams();
        final IssueNavigatorType type = actionParams.getNavigatorType();

        // if we're calling switch we should never have a null type
        if (type != null)
        {
            // if we're trying to switch to SIMPLE but our session request is Advanced, don't allow it
            if (type != IssueNavigatorType.SIMPLE || !isAdvanced())
            {
                IssueNavigatorType.setInCookie(ActionContext.getResponse(), type);
            }
            else
            {
                return forceRedirect("IssueNavigator.jspa?tooComplex=true");
            }
            // Redirect to the navigator and let it handle rendering, if we don't do this the pager links are f**ed up
        }
        return forceRedirect("IssueNavigator.jspa");
    }

    // this should only be called once by the JSP, after all the search request modification has happened,
    // so it's okay to clear the cached value
    public boolean isCurrentQueryTooComplex()
    {
        isAdvanced = null;
        return isAdvanced();
    }

    public final String doToggleAutocompletePref()
    {
        final IssueNavigatorActionParams actionParams = getActionParams();
        if (getRemoteUser() != null && !isAutocompleteDisabled() && actionParams.isAutocompletePreferenceSpecified())
        {
            final Boolean preference = actionParams.getNewAutocompletePreference();
            if (preference != null)
            {
                try
                {
                    getUserPreferences().setBoolean(PreferenceKeys.USER_JQL_AUTOCOMPLETE_DISABLED, preference);
                }
                catch (AtlassianCoreException e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Cannot set user preference for JQL: " + e.getMessage());
                    }
                }
            }
        }
        return forceRedirect("IssueNavigator.jspa");
    }

    public final String doClearSorts()
    {
        final SearchRequest sr = getSearchRequest();
        if (sr != null)
        {
            // Lets just nuke the sorts so we will use the defaults
            sr.setQuery(new QueryImpl(sr.getQuery().getWhereClause(), new OrderByImpl(), sr.getQuery().getQueryString()));
        }
        return forceRedirect("IssueNavigator.jspa");
    }

    private void setPagerStart(int start)
    {
        getPager().setStart(start);
    }

    public final String doExecuteAdvanced()
    {
        recordPreviousToggleStates();
        // set the pager start as need be
        if (getActionParams().isPagerStartSpecified())
        {
            setPagerStart(getActionParams().getPagerStart());
        }
        setNavigatorType(IssueNavigatorType.ADVANCED);
        setUserCreated(true);
        if (getActionParams().isClearOldFilter())
        {
            setSearchRequest(null);
        }

        final String result = _doExecuteAdvanced();
        if (getSearchRequest() != null && getJqlQuery() == null)
        {
            setCurrentJql(getSearchRequest().getQuery());
        }
        return result;
    }

    private String _doExecuteAdvanced()
    {
        final boolean updateSorts;
        SearchRequest searchRequest;
        if (getActionParams().isLoadSavedFilter())
        {
            searchRequest = loadAndSetSearchRequest();
        }
        else
        {
            searchRequest = getSearchRequest();
        }

        Query currentQuery;
//        final String jqlStr;
        if (!runQuery)
        {
            if (getActionParams().isUpdateParamsRequired())
            {
                _doUpdateParamsRequired(getActionParams());
                // The query string was set in the _doUpdateParamsRequired method call
            }
            else
            {
                currentQuery = searchRequest != null ? searchRequest.getQuery() : null;
                //We need to validate the query again in case there are some warnings to display. We should not
                //find any errors.
                validateQuery(currentQuery);
            }

            updateSorts = true;
        }
        else
        {
            resetPagerAndSelectedIssue();
            // if the JQL query has been updated, or additional params were specified with the reset flag,
            // then update the search request accordingly
            if (isAdvancedSearchRequestUpdated(searchRequest) || getActionParams().isUpdateParamsRequired())
            {
                //update the query.
                updateSearchRequestWithJqlOrParams(searchRequest);
                updateSorts = false;
            }
            else
            {
                //We need to validate the query again in case there are some warnings to display. We should not
                //find any errors.
                currentQuery = searchRequest != null ? searchRequest.getQuery() : null;
                updateSorts = true;
                validateQuery(currentQuery);
            }
        }

        if (hasAnyErrors())
        {
            return ADVANCED;
        }

        final boolean searchRan = executeSearch(updateSorts);

        // add the query to the user's history
        if (searchRan)
        {
            saveJqlQueryToHistory();
        }
        return ADVANCED;
    }

    private void saveJqlQueryToHistory()
    {
        // only save the JQL to history if it's not blank
        if (!StringUtils.isBlank(getJqlQuery()))
        {
            userQueryHistoryManager.addQueryToHistory(getRemoteUser(), getJqlQuery());
        }
    }

    public List<String> getSavedJqlQueryHistoryItems()
    {
        List<String> queryList = new ArrayList<String>();
        final List<UserHistoryItem> history = userQueryHistoryManager.getUserQueryHistory(getRemoteUser());
        for (Iterator<UserHistoryItem> userHistoryItemIterator = history.iterator(); userHistoryItemIterator.hasNext();)
        {
            UserHistoryItem userHistoryItem = userHistoryItemIterator.next();
            queryList.add(userHistoryItem.getData());
        }
        return queryList;
    }

    public String getVisibleFieldNamesJson() throws JSONException
    {
        return autoCompleteJsonGenerator.getVisibleFieldNamesJson(getRemoteUser(), getLocale());
    }

    public String getVisibleFunctionNamesJson() throws JSONException
    {
        return autoCompleteJsonGenerator.getVisibleFunctionNamesJson(getRemoteUser(), getLocale());
    }

    public String getJqlReservedWordsJson() throws JSONException
    {
        return autoCompleteJsonGenerator.getJqlReservedWordsJson();
    }

    /**
     * Sets the JQL form element to the query string of the provided search request.
     *
     * @param query the query to set
     */
    private void setCurrentJql(final Query query)
    {
        if (query != null)
        {
            setJqlQuery(searchService.getJqlString(query));
        }
        else
        {
            setJqlQuery(null);
        }
    }

    /**
     * Updates the search request in the session with the specified search request and either the new search
     * jql query if specified or otherwise the action params.
     *
     * @param searchRequest the previous search request in the session
     */
    private void updateSearchRequestWithJqlOrParams(final SearchRequest searchRequest)
    {
        // newQuery could be null if a blank query was entered
        if (getJqlQuery() != null)
        {
            Query newQuery = getNewSearchQuery();
            if (newQuery != null)
            {
                SearchRequest newSearchRequest = searchRequestFactory.createFromQuery(searchRequest, getRemoteUser(), newQuery);
                setSearchRequest(newSearchRequest);
                validateQuery(newSearchRequest.getQuery());
            }
        }
        else
        {
            SearchRequest newSearchRequest = searchRequestFactory.createFromParameters(searchRequest, getRemoteUser(), getActionParams());
            setSearchRequest(newSearchRequest);
            validateQuery(newSearchRequest.getQuery());
        }
        isSearchUpdated = true;
    }

    /**
     * Attempts to parse the "jqlQuery" action parameter into a {@link com.atlassian.query.Query} object. If the
     * parse returns an error, the errors are added to the error collection.
     *
     * @return the {@link com.atlassian.query.Query} object parsed from the "jqlQuery" action parameter, or null
     * if there was an error.
     */
    private Query getNewSearchQuery()
    {
        Query newQuery = null;
        if (getJqlQuery() != null)
        {
            final SearchService.ParseResult parseResult = searchService.parseQuery(getRemoteUser(), getJqlQuery());
            if (parseResult.isValid())
            {
                newQuery = parseResult.getQuery();
            }
            else
            {
                addErrorMessages(parseResult.getErrors().getErrorMessages());
            }
        }
        return newQuery;
    }

    /**
     * Validates a query and returns the redirection target if it should be redirected.
     *
     * @param query the query to validate
     * @param modified true if the searchrequest we are validating has been modified or is new, false otherwise.
     * @return "ADVANCED" if the user should be redirected to the advanced screen with validation errors, "ERROR" if the user should be redirected to the
     * simlpe form screen with validation errors, or NULL of the user does not need to be redirected.
     */
    private String validateAndGetRedirectForQuery(Query query, boolean modified)
    {
        // we only want to calculate the does it fit once, and only if necessary
        Boolean fits = null;
        final boolean redirectingFromHide = inHideWithNonUserCreatedFilter(modified);
        if (getAdvancedOrSimpleView().equals(ADVANCED) || !(fits = searchService.doesQueryFitFilterForm(getRemoteUser(), query)))
        {
            // do we have errors to show, or do we have an advanced query in simple mode, validate first to get errors
            if ((!validateQuery(query) || !getAdvancedOrSimpleView().equals(ADVANCED)) && !redirectingFromHide)
            {
                // goto advanced mode
                setMode(MODE_SHOW);
                setNavigatorType(IssueNavigatorType.ADVANCED);
                return ADVANCED;
            }
            if (fits == null)
            {
                fits = searchService.doesQueryFitFilterForm(getRemoteUser(), query);
            }
            if (getMode().equals(MODE_HIDE) && fits)
            {
                populateFieldValuesHolderFromQuery(query, getFieldValuesHolder());    
            }
        }
        else
        {
            populateFieldValuesHolderFromQuery(query, getFieldValuesHolder());
            validateParams(getFieldValuesHolder(), searchService.getSearchContext(getRemoteUser(), query));
            if (invalidInput() && !redirectingFromHide)
            {
                setMode(MODE_SHOW);
                return ERROR;
            }
        }
        return null;
    }

    /**
     * Validates a set of action params and creates teh search request if valid. If the params are not valid, the navigator
     * mode is set to show such the validation errors can be displayed. Populates the field value holder with the params
     * and sets the new search request in session.
     *
     * @param orgSearchRequest the original search request to merge the new one with
     * @param actionParams the action params to validate and create the search request from.
     * @return the new search request if the parameters are valid, NULL otherwise.
     */
    private boolean validateAndSetSearchRequestForParams(SearchRequest orgSearchRequest, IssueNavigatorActionParams actionParams)
    {
        if (getNavigatorType().equals(IssueNavigatorType.ADVANCED))
        {
            SearchRequest newSearchRequest = searchRequestFactory.createFromParameters(orgSearchRequest, getRemoteUser(), actionParams);
            setSearchRequest(newSearchRequest);
            final boolean redirectingFromHide = inHideWithNonUserCreatedFilter(newSearchRequest.isModified());
            if (!validateQuery(newSearchRequest.getQuery()) && !redirectingFromHide)
            {
                setMode(MODE_SHOW);
                return false;
            }
            return true;
        }
        else
        {
            populateFromParams(actionParams, getFieldValuesHolder());
            SearchRequest newSearchRequest = searchRequestFactory.createFromParameters(orgSearchRequest, getRemoteUser(), actionParams);
            setSearchRequest(newSearchRequest);
            final boolean redirectingFromHide = inHideWithNonUserCreatedFilter(newSearchRequest.isModified());
            if (searchService.doesQueryFitFilterForm(getRemoteUser(), newSearchRequest.getQuery()))
            {
                validateParams(getFieldValuesHolder(), searchService.getSearchContext(getRemoteUser(), newSearchRequest.getQuery()));
                if (invalidInput() && !redirectingFromHide)
                {
                    setMode(MODE_SHOW);
                    return false;
                }

                return true;
            }
            // do we have errors to show, or do we have an advanced query in simple mode, validate first to get errors
            else if ((!validateQuery(newSearchRequest.getQuery()) || !getAdvancedOrSimpleView().equals(ADVANCED)) && !redirectingFromHide)
            {
                // goto advanced mode
                setMode(MODE_SHOW);
                setNavigatorType(IssueNavigatorType.ADVANCED);
                return false;
            }
            return true;
        }
    }

    /**
     * We do not want to redirect from hide mode if the user does not own and has not modified the filter
     * @param modified has the filter been modified
     * @return true if the user is on hide mode with a filter that they do now own and have not modified.
     */
    private boolean inHideWithNonUserCreatedFilter(final boolean modified)
    {
        return !modified && MODE_HIDE.equals(getMode()) && !isUserCreated();
    }

    /**
     * Validates the specified {@link com.atlassian.query.Query}. If the query does not pass validation, error
     * messages are added to the error collection.
     *
     * @param query the search query to validate.
     * @return true if the validation passed; false otherwise.
     */
    private boolean validateQuery(final Query query)
    {
        if (query == null)
        {
            return true;
        }
        final MessageSet result = searchService.validateQuery(getRemoteUser(), query);
        if (result != null)
        {
            int maxResult = MAX_ADVANCED_VALIDATION_MESSAGES;
            if (result.hasAnyErrors())
            {
                for (Iterator<String> iter = result.getErrorMessages().iterator(); iter.hasNext() && maxResult > 0; maxResult--)
                {
                    String error = iter.next();
                    addErrorMessage(error);
                }
                return false;
            }

            for (Iterator<String> iter = result.getWarningMessages().iterator(); iter.hasNext() && maxResult > 0; maxResult--)
            {
                String warning = iter.next();
                addWarningMessage(warning);
            }
        }
        return true;
    }

    /**
     * A simple check to see if a SearchRequest utilising a {@link com.atlassian.query.Query} has been updated.
     *
     * @param searchRequest the search request to check.
     * @return true if the JQL will update the search request, or false otherwise.
     */
    private boolean isAdvancedSearchRequestUpdated(SearchRequest searchRequest)
    {
        /**
         * This is a very simple test that only compares strings which may return false positives. We have to
         * be careful because a null SearchQuery.getQueryString does not mean that SearchQuery.getWhereClause is null.
         * This could cause problems when the JQL is null and SearchQuery.getQueryString is null but SearchQuery.getWhereClause
         * is not null.
         */

        if (searchRequest == null)
        {
            //we are trying to run a new search.
            return true;
        }

        //if the two query strings are the same then no update has been made.
        return !StringUtils.equals(searchService.getJqlString(searchRequest.getQuery()), getJqlQuery());

    }

    public boolean isFocusJql()
    {
        return getSearchRequest() == null
            || runQuery
            || isSearchUpdated
            || hasAnyErrors()
            || (getMode().equals(MODE_SHOW) && !previousMode.equals(MODE_SHOW));
    }

    /**
     * Executes the search request in session.
     *
     * @return true if the search was run; false otherwise.
     * @param updateSorts true if the sorts should be updated. Sorts should be updated when loading a SR from the session and not creating a new one
     */
    private boolean executeSearch(final boolean updateSorts)
    {
        if (getSearchRequest() == null)
        {
            return false;
        }

        // Do not retrieve the items if the project filter is being refreshed due to
        // the selection of a project
        if (getActionParams().isRefreshOnly())
        {
            return false;
        }

        try
        {
            if (updateSorts)
            {
                updateSearchSorts();
            }
            searchResults = getSearchResults();
            pagerManager.clearPager();

            return true;
        }
        catch (ClauseTooComplexSearchException exception)
        {
            handleTooComplexSearchException(exception);
            return false;
        }
        catch (SearchException exception)
        {
            handleException(exception);
            return false;
        }
    }

    private void updateSearchSorts()
    {
        final SearchRequest searchRequest = getSearchRequest();
        final SearchSortUtil searchSortUtil = ComponentManager.getComponentInstanceOfType(SearchSortUtil.class);
        final OrderBy specifiedOrderByClause = searchSortUtil.getOrderByClause(ActionContext.getParameters());
        if (specifiedOrderByClause != null && !specifiedOrderByClause.getSearchSorts().isEmpty())
        {
            final Query query = searchRequest.getQuery();

            // Lets get the UI view of the old search sorts, including the default sort if need be
            Collection<SearchSort> oldSorts = searchSortUtil.getSearchSorts(query);
            Clause whereClause = query.getWhereClause();

            final List<SearchSort> mergedSorts = searchSortUtil.mergeSearchSorts(getRemoteUser(), specifiedOrderByClause.getSearchSorts(), oldSorts, 3);
            final QueryImpl sortedQuery = new QueryImpl(whereClause, new OrderByImpl(mergedSorts), null);

            searchRequest.setQuery(sortedQuery);

            if (!mergedSorts.equals(oldSorts))
            {
                // set the selected issue to null and go back to the first page
                resetPagerAndSelectedIssue();
                isSearchUpdated = true;
            }
        }
    }

    /**
     * Will execute the search request in session.
     *
     * @return the view to display
     */
    private String showSearchResults(final boolean updateSorts)
    {
        // Call to trap any possible errors that may have been made in the Search Input
        if (getSearchRequest() != null)
        {
            if (!MODE_HIDE.equals(getMode()))
            {
                // Because we may have loaded an advanced search from parameters we need to make sure that we don't need
                // to redirect to the advanced view with the filter now saved in the session
                isAdvanced = null;
                if (isAdvanced())
                {
                    return forceRedirect("IssueNavigator.jspa?navType=advanced");
                }
            }
            executeSearch(updateSorts);
        }
        return getErrorAdvancedOrSimpleView();
    }

    private void populateFromParams(final IssueNavigatorActionParams actionParams, final FieldValuesHolder fieldValuesHolder)
    {
        for (final IssueSearcher searcher : getSearchers())
        {
            final SearchInputTransformer searchInputTransformer = searcher.getSearchInputTransformer();
            searchInputTransformer.populateFromParams(getRemoteUser(), fieldValuesHolder, actionParams);
        }
    }

    private void validateParams(final FieldValuesHolder fieldValuesHolder, final SearchContext searchContext)
    {
        for (final IssueSearcher searcher : getSearchers())
        {
            final SearchInputTransformer searchInputTransformer = searcher.getSearchInputTransformer();
            searchInputTransformer.validateParams(getRemoteUser(), searchContext, fieldValuesHolder, this, this);
        }
    }

    public String doColumnOverride() throws Exception
    {
        final SearchRequest searchRequest = getSearchRequest();
        if (searchRequest != null)
        {
            searchRequest.setUseColumns(!searchRequest.useColumns());
        }
        else
        {
            // No search request - this method should never be called when search request has
            // not been created yet
            throw new IllegalStateException("Search Request does not exist.");
        }

        // Redirect the user to issue navigator
        return getRedirect(getActionName() + ".jspa");
    }

    // ------------------------------------------------------------------------------------------------- Sorting related
    /**
     * Store the current pager in the session. The pager handles paging through the issue list.
     *
     * @return the current searching pager.
     */
    public PagerFilter getPager()
    {
        return getSearchActionHelper().getPagerFilter();
    }

    public TableLayoutFactory getTableLayoutFactory()
    {
        return tableLayoutFactory;
    }

    public void resetPager()
    {
        getSearchActionHelper().resetPager();
    }

    public SearchResults getSearchResults() throws SearchException
    {
        // JRADEV-3032 never run invalid searches , if in advanced mode
        if ( !isFilter() && getHasErrorMessages() ) {
            return null;
        }
         if (getSearchRequest() == null)
        {
            return null;
        }

        if (searchResults == null)
        {
            final NextPreviousPager issuePager = pagerManager.getPager();
            final PagerFilter navigatorPager = getPager();
            selectedIssueData = getSessionSelectedIssueManager().getCurrentObject();
            final boolean isReturningToSearch =
                    (!getActionParams().isPagerStartSpecified() || previousPagerStart == getActionParams().getPagerStart()) &&
                    (issuePager.isHasCurrentKey() || selectedIssueData != null);

            if (isReturningToSearch)
            {
                final Predicate<Issue> currentIssuePredicate;
                final Predicate<Issue> nextIssuePredicate;
                final int expectedIndex;
                if (issuePager.isHasCurrentKey())
                {
                    currentIssuePredicate = new Predicate<Issue>()
                    {
                        public boolean evaluate(final Issue input)
                        {
                            return input.getKey().equals(issuePager.getCurrentKey());
                        }
                    };
                    nextIssuePredicate = new Predicate<Issue>()
                    {
                        public boolean evaluate(final Issue input)
                        {
                            return input.getKey().equals(issuePager.getNextKey());
                        }
                    };
                    expectedIndex = issuePager.getCurrentPosition() - 1;
                }
                else
                {
                    currentIssuePredicate = new Predicate<Issue>()
                    {
                        public boolean evaluate(final Issue input)
                        {
                            return input.getId().equals(selectedIssueData.getSelectedIssueId());
                        }
                    };
                    nextIssuePredicate = new Predicate<Issue>()
                    {
                        public boolean evaluate(final Issue input)
                        {
                            return input.getId().equals(selectedIssueData.getNextIssueId());
                        }
                    };
                    expectedIndex = selectedIssueData.getSelectedIssueIndex();
                }

                // Bounds of the search window (which may only partially intersect some pages).
                final int fromIndex = Math.max(0, expectedIndex - FIND_ISSUE_WINDOW_SIZE);
                final int toIndexExclusive = expectedIndex + FIND_ISSUE_WINDOW_SIZE + 1;

                List<PagerFilter> searchWindowPagers = pagersToSearch(navigatorPager, fromIndex, expectedIndex, toIndexExclusive);
                List<SearchResults> searchWindowResults = new ArrayList<SearchResults>();

                // Look for the last viewed issue in the search window near its expected position.
                for (PagerFilter pager : searchWindowPagers)
                {
                    SearchResults result = searchProvider.search(getSearchRequest().getQuery(), getRemoteUser(), new PagerFilter(pager));
                    Issue currentIssue = CollectionUtil.findFirstMatch(intersection(result.getIssues(), pager, fromIndex, toIndexExclusive), currentIssuePredicate);
                    if (currentIssue != null)
                    {
                        setPagerStart(pager.getStart());
                        selectedIssueId = currentIssue.getId();
                        searchResults = result;
                        break;
                    }
                    searchWindowResults.add(result);
                }

                if (searchResults == null)
                {
                    // The last viewed issue wasn't found in the search window, so check the next issue.
                    for (int i = 0; i < searchWindowPagers.size(); i++)
                    {
                        PagerFilter pager = searchWindowPagers.get(i);
                        SearchResults result = searchWindowResults.get(i);
                        Issue nextIssue = CollectionUtil.findFirstMatch(intersection(result.getIssues(), pager, fromIndex, toIndexExclusive), nextIssuePredicate);
                        if (nextIssue != null)
                        {
                            setPagerStart(pager.getStart());
                            selectedIssueId = nextIssue.getId();
                            searchResults = result;
                            break;
                        }
                    }
                }

                if (searchResults == null)
                {
                    // The next issue wasn't found either...
                    if (searchWindowResults.isEmpty())
                    {
                        // This should mean there are no results for the search, but fallback to the old search just in case.
                        searchResults = searchProvider.search(getSearchRequest().getQuery(), getRemoteUser(), navigatorPager);
                    }
                    else
                    {
                        // ...so put them on the page where the issue would have been...
                        SearchResults result = searchWindowResults.get(0);
                        if (!result.getIssues().isEmpty())
                        {
                            setPagerStart(result.getStart());
                            searchResults = result;
                        }
                        else
                        {
                            // ...unless it was the lone issue on that page, so just revert to first page of results.
                            PagerFilter pager = new PagerFilter(navigatorPager);
                            pager.setStart(0);
                            searchResults = searchProvider.search(getSearchRequest().getQuery(), getRemoteUser(), pager);
                        }
                    }
                }
            }
            else
            {
                searchResults = searchProvider.search(getSearchRequest().getQuery(), getRemoteUser(), navigatorPager);
            }

            /*
             * Preserving exisitng behaviour:
             *
             * Not sure if this needs to be reset because we are not sure what the searchProvider does to the pager during
             * execution.
             */
            getSearchActionHelper().resetPagerTempMax();

            ensureAnIssueIsSelected();
        }

        return searchResults;
    }

    /**
     * Returns a list of pagers to search with its order optimized around the expected index.
     */
    private static List<PagerFilter> pagersToSearch(PagerFilter pager, int fromIndex, int expectedIndex, int toIndexExclusive)
    {
        final int pageSize = pager.getPageSize();
        List<PagerFilter> pagers = new ArrayList<PagerFilter>();

        PagerFilter expectedPagePager = PagerFilter.newPageAlignedFilter(expectedIndex, pageSize);
        if (isOverlapping(expectedPagePager, fromIndex, toIndexExclusive))
        {
            pagers.add(expectedPagePager);
        }

        PagerFilter left = expectedPagePager.getStart() == 0 ? null : new PagerFilter(expectedPagePager.getStart() - pageSize, pageSize);
        PagerFilter right = new PagerFilter(expectedPagePager.getStart() + pageSize, pageSize);

        while ((left != null && isOverlapping(left, fromIndex, toIndexExclusive)) || isOverlapping(right, fromIndex, toIndexExclusive))
        {
            if (left != null && isOverlapping(left, fromIndex, toIndexExclusive))
            {
                pagers.add(left);
                left = left.getStart() == 0 ? null : new PagerFilter(left.getStart() - pageSize, pageSize);
            }
            if (isOverlapping(right, fromIndex, toIndexExclusive))
            {
                pagers.add(right);
                right = new PagerFilter(right.getStart() + pageSize, pageSize);
            }
        }

        return pagers;
    }

    private static boolean isOverlapping(PagerFilter pager, int fromIndex, int toIndexExclusive)
    {
        return pager.getStart() < toIndexExclusive && fromIndex < pager.getStart() + pager.getPageSize();
    }

    /**
     * Returns the intersection of Issues from the Given Page, with the given fromIndex and toIndexExclusive.
     *
     * @param issuesInPage A List of issues in the "current page" (Note that this could be smaller than the actual page size if we are on the last page or further).
     * @param pager The Pager that defines the current page.
     * @param fromIndex From Index (inclusive) to use for our intersection.
     * @param toIndexExclusive to Index (exclusive) to use for our intersection.
     * @return the intersection of Issues from the Given Page, with the given fromIndex and toIndexExclusive.
     */
    static List<Issue> intersection(List<Issue> issuesInPage, PagerFilter pager, int fromIndex, int toIndexExclusive)
    {
        // Check if there are enough Issues in the page for our fromIndex
        if (fromIndex >= pager.getStart() + issuesInPage.size())
        {
            return Collections.emptyList();
        }

        if (isOverlapping(pager, fromIndex, toIndexExclusive))
        {
            int offset = pager.getStart();
            return issuesInPage.subList(
                    Math.max(0, fromIndex - offset),
                    Math.min(Math.max(0, toIndexExclusive - offset), issuesInPage.size())
            );
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private void ensureAnIssueIsSelected()
    {
        final List<Issue> issuesInPage = searchResults.getIssues();
        int selectedIssueIndex = 0;
        Long nextIssueId = null;
        if (!issuesInPage.isEmpty())
        {
            boolean isPagingToPreviousPage = getActionParams().isPagerStartSpecified()
                && getActionParams().getPagerStart() < previousPagerStart
                && (previousPagerStart - getActionParams().getPagerStart()) == getPager().getPageSize();
            if (isPagingToPreviousPage)
            {
                selectedIssueId = issuesInPage.get(issuesInPage.size() - 1).getId();
                selectedIssueIndex = searchResults.getStart() + issuesInPage.size() - 1;
            }
            else
            {
                int index = CollectionUtil.indexOf(issuesInPage, new Predicate<Issue>()
                {
                    public boolean evaluate(final Issue input)
                    {
                        return input.getId().equals(selectedIssueId);
                    }
                });
                boolean isSelectedIssueInPage = index >= 0;
                if (isSelectedIssueInPage)
                {
                    selectedIssueIndex = searchResults.getStart() + index;
                    if (index < issuesInPage.size() - 1)
                    {
                        nextIssueId = issuesInPage.get(index + 1).getId();
                    }
                }
                else
                {
                    selectedIssueId = issuesInPage.get(0).getId();
                    selectedIssueIndex = searchResults.getStart();
                    if (issuesInPage.size() > 1)
                    {
                        nextIssueId = issuesInPage.get(1).getId();
                    }
                }
            }
            getSessionSelectedIssueManager().setCurrentObject(new SelectedIssueData(selectedIssueId, selectedIssueIndex, nextIssueId));
        }
        else
        {
            clearSelectedIssue();
        }
    }

    @Override
    public Long getSelectedIssueId()
    {
        return selectedIssueId;
    }

    /**
     * This method logs a search exception
     *
     * @param e SearchException to handle
     */
    private void handleException(final SearchException e)
    {
        addErrorMessage(getText("navigator.error.generic", e.getMessage()));
        log.error("Error searching:", e);
    }

    private void handleTooComplexSearchException(final ClauseTooComplexSearchException exception)
    {
        final Clause clause = exception.getClause();
        if (clause instanceof TerminalClause)
        {
            addErrorMessage(getText("search.request.clause.too.complex", jqlStringSupport.generateJqlString(clause)));
        }
        else
        {
            addErrorMessage(getText("search.request.clause.query.complex"));
        }
        // lets go to advanced to show this error.
        setNavigatorType(IssueNavigatorType.ADVANCED);
        setMode(MODE_SHOW);
    }

    public boolean isUserCreated()
    {
        String userCreated = (String) ActionContext.getSession().get(SessionKeys.ISSUE_NAVIGATOR_USER_CREATED);
        return userCreated != null && Boolean.parseBoolean(userCreated);
    }

    public void setUserCreated(boolean userCreated)
    {
        ActionContext.getSession().put(SessionKeys.ISSUE_NAVIGATOR_USER_CREATED, userCreated ? "true" : "false");
    }

    // methods for showing / hiding filter form
    public String getMode()
    {
        String mode = (String) ActionContext.getSession().get(SessionKeys.ISSUE_NAVIGATOR_MODE);

        if (mode == null)// first time the navigator is run
        {
            mode = MODE_SHOW;
            setMode(mode);
        }

        return mode;
    }

    /**
     * Control the mode (hide or show) of the left hand column
     *
     * @param mode mode
     */
    public void setMode(final String mode)
    {
        if (mode.equalsIgnoreCase(MODE_HIDE) || mode.equalsIgnoreCase(MODE_SHOW))
        {
            if (previousMode == null)
            {
                previousMode = (String) ActionContext.getSession().get(SessionKeys.ISSUE_NAVIGATOR_MODE);
            }
            ActionContext.getSession().put(SessionKeys.ISSUE_NAVIGATOR_MODE, mode.toLowerCase());
        }
    }

    public void setHide(final String value)
    {
        ActionContext.getSession().put(SessionKeys.ISSUE_NAVIGATOR_MODE, MODE_HIDE);
    }

    public void setShow(final String value)
    {
        clickedSearchButton = true;
        ActionContext.getSession().put(SessionKeys.ISSUE_NAVIGATOR_MODE, MODE_SHOW);
    }

    /**
     * Get / Set the temporary maximum - this is used for printable and excel views etc
     *
     * @return temp max
     */
    public int getTempMax()
    {
        return getSearchActionHelper().getTempMax();
    }

    public void setTempMax(final int tempMax)
    {
        getSearchActionHelper().setTempMax(tempMax);
    }

    // ---------------------------------------------------------------------------------------------------- View Helpers

    public boolean shouldFocusField()
    {
        return clickedSearchButton || runQuery || getActionParams().isUpdateParamsRequired() || getSearchRequest() == null;
    }

    public String getJqlQueryString()
    {
        final SearchRequest searchRequest = getSearchRequest();
        if (searchRequest != null)
        {
            final Query query = searchRequest.getQuery();
            return searchService.getQueryString(getRemoteUser(), query);
        }
        else
        {
            return "";
        }
    }

    /**
     * Determines if the current search request has a column layout. Used in the header of the IssueNavigator
     */
    public boolean isHasSearchRequestColumnLayout() throws ColumnLayoutStorageException
    {
        final SearchRequest searchRequest = getSearchRequest();
        if (searchRequest != null)
        {
            return columnLayoutManager.hasColumnLayout(searchRequest);
        }
        else
        {
            // No search request
            return false;
        }
    }

    public boolean isOwnerOfSearchRequest()
    {
        final SearchRequest searchRequest = getSearchRequest();
        if ((searchRequest != null) && (getRemoteUser() != null))
        {
            return userNameEqualsUtil.equals(searchRequest.getOwnerUserName(), getRemoteUser());
        }
        else
        {
            // No search request
            return false;
        }
    }

    /**
     * Determines whether the "Use Your Columns" link should be shown to the user. This will happen IF the search
     * request has its own column layout AND If the user has NOT chosen to override the search request's column layout
     */
    public boolean isShowOverrideColumnLayout() throws ColumnLayoutStorageException
    {
        final SearchRequest searchRequest = getSearchRequest();
        if (searchRequest != null)
        {
            return isHasSearchRequestColumnLayout() && searchRequest.useColumns();
        }
        else
        {
            // No search request - this method should never be called when search request has
            // not been created yet
            throw new IllegalStateException("Search Request does not exist.");
        }
    }

    /**
     * Checks to see if the current user has the global BULK CHANGE permission
     */
    public boolean isHasBulkChangePermission()
    {
        final SearchRequest searchRequest = getSearchRequest();
        if (searchRequest == null)
        {
            return false;
        }
        else
        {
            final PermissionManager permissionManager = ManagerFactory.getPermissionManager();
            try
            {
                return permissionManager.hasPermission(Permissions.BULK_CHANGE, getRemoteUser());
            }
            catch (final Exception e)
            {
                log.error(e, e);
                return false;
            }
        }
    }

    // -------------------------------------------------------------------------------------------------- Issue navigator specific helper
    public String getSearcherEditHtml(final IssueSearcher searcher)
    {
        final SearchContext searchContext = getSearchContext();
        final SearchRenderer searchRenderer = searcher.getSearchRenderer();
        if (searchRenderer.isShown(getRemoteUser(), searchContext))
        {
            Map<String,String> displayParams = new HashMap<String,String>();
            displayParams.put("theme","aui");
            return searchRenderer.getEditHtml(getRemoteUser(), searchContext, getFieldValuesHolder(), displayParams, this);
        }
        else
        {
            return "";
        }
    }

    protected void setFieldValuesHolder(final FieldValuesHolder fieldValuesHolder)
    {
        this.fieldValuesHolder = fieldValuesHolder;
    }

    public boolean isValid()
    {
        return valid;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public boolean isRequestPrivate()
    {
        final SearchRequest searchRequest = getSearchRequest();
        return (searchRequest == null)? false : searchRequest.getPermissions().isPrivate();
    }

    public CommentManager getCommentManager()
    {
        return commentManager;
    }

    public Collection<SearchRequestViewModuleDescriptor> getNonSystemSearchRequestViews()
    {
        final List<SearchRequestViewModuleDescriptor> enabledModuleDescriptorsByClass = pluginAccessor.getEnabledModuleDescriptorsByClass(SearchRequestViewModuleDescriptor.class);
        for (final Iterator<SearchRequestViewModuleDescriptor> iterator = enabledModuleDescriptorsByClass.iterator(); iterator.hasNext();)
        {
            final SearchRequestViewModuleDescriptor moduleDescriptor = iterator.next();
            // remove the views that ship with JIRA. (see exceptions below.
            if ("jira.issueviews:searchrequest-printable".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-fullcontent".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-xml".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-rss".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-comments-rss".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-word".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-excel-all-fields".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-excel-current-fields".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-charts-view".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
        }
        return enabledModuleDescriptorsByClass;
    }

    public SearchRequestViewModuleDescriptor getPrintable()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-printable");
    }

    public SearchRequestViewModuleDescriptor getFullContent()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-fullcontent");
    }

    public SearchRequestViewModuleDescriptor getXml()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-xml");
    }

    public SearchRequestViewModuleDescriptor getRssIssues()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-rss");
    }

    public SearchRequestViewModuleDescriptor getRssComments()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-comments-rss");
    }

    public SearchRequestViewModuleDescriptor getWord()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-word");
    }

    public SearchRequestViewModuleDescriptor getAllExcelFields()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-excel-all-fields");
    }

    public SearchRequestViewModuleDescriptor getCurrentExcelFields()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-excel-current-fields");
    }

    public SearchRequestViewModuleDescriptor getChart()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-charts-view");
    }

    public String getRestricted(final String url)
    {
        return URLUtil.addRequestParameter(url, getRestrictionClause());
    }

    @Override
    final public boolean isFilterValid()
    {
        return !hasAnyErrors();
    }

    private String getRestrictionClause()
    {
        final int count = issueSearchLimits.getMaxResults();
        if (count <= 0)
        {
            return null;
        }
        return "tempMax=" + count;
    }

    private SearchActionHelper getSearchActionHelper()
    {
        if (actionHelper == null)
        {
            actionHelper = new SearchActionHelper(getSessionPagerFilterManager(), getUserPreferences(), issueSearchLimits);
        }
        return actionHelper;
    }

    private void setNavigatorType(final IssueNavigatorType type)
    {
        this.type = type;
        IssueNavigatorType.setInCookie(ActionContext.getResponse(), this.type);
    }

    /**
     * Returns the caller the ADVANCED view if needed, otherwise it will return either SUCCESS or ERROR.
     *
     * @return the result of the webwork action. The result will be ignored by Webwork on a redirect.
     */
    private String getAdvancedOrSimpleView()
    {
        return isNavigatorTypeAdvanced() ? ADVANCED : getResult();
    }

    /**
     * @return If the IssueNavigator has Errors in state return Advanced or Success Error on the current navigator mode. Otherwise return Success.
     */
    private String getErrorAdvancedOrSimpleView()
    {
        if (getResult().equals(ERROR) && isNavigatorTypeAdvanced() && !inHideWithNonUserCreatedFilter(getSearchRequest() != null && getSearchRequest().isModified()))
        {
            return ADVANCED;
        }
        return getResult();
    }

    /**
     * Check to see if the currently loaded query (if it exists) is advanced or not.
     *
     * @return true if the currently loaded query exists and is advanced; false otherwise.
     */
    private boolean isAdvanced()
    {
        if (isAdvanced == null)
        {
            final SearchRequest searchRequest = getSearchRequest();
            isAdvanced = searchRequest != null && !searchService.doesQueryFitFilterForm(getRemoteUser(), searchRequest.getQuery());
        }
        return isAdvanced;
    }

    /**
     * Check to see if the currently loaded query (if it exists) has originated from a filter.
     *
     * @return true if the currently loaded query exists and is a filter; false otherwise.
     */
    private boolean isFilter()
    {
         if (isFilter == null)
        {
            final SearchRequest searchRequest = getSearchRequest();
            isFilter = searchRequest != null && searchRequest.getName() != null;
        }
        return isFilter;
    }
    /**
     * Tells whether the bulk edit limit property is currently restricting the number of issues in the current search
     * that may be bulk edited.
     *
     * @return true only if the bulk edit limit is restricting.
     */
    public boolean isBulkEditLimited()
    {
        try
        {
            // if the number of search results exactly matches the limit, we will show the limit message
            return getBulkEditMax() != getSearchResults().getTotal();
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the maximum number of issues the user is allowed to bulk edit. Possibly the number of search results
     * that were returned, but no more than the number configured by {@link APKeys#JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT}
     * if it is set to a positive number.
     *
     * @return the number of issues that can be bulk edited.
     */
    public int getBulkEditMax()
    {
        try
        {
            return getBulkEditMax(getSearchResults().getTotal(), applicationProperties.getDefaultBackedString(APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT));
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected FilterOperationsBean createFilterOperationsBean(final String userName)
    {
        return FilterOperationsBean.create(getSearchRequest(), isFilterValid(), userName, MODE_SHOW.equals(getMode()) && getNavigatorType().equals(IssueNavigatorType.ADVANCED));
    }

    int getBulkEditMax(final int searchTotal, final String bulkEditSetting)
    {
        int max = searchTotal;
        if (bulkEditSetting != null) {
            try
            {
                int configuredMax = Integer.parseInt(bulkEditSetting);
                if (configuredMax > 0)
                {
                    max = Math.min(searchTotal, configuredMax);
                }
            }
            catch (NumberFormatException e)
            {
                // can't use this as a number
                log.warn("Ignoring JIRA application property " + APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT
                         + " because it cannot be parsed as a number: " + bulkEditSetting);
            }
        }
        return max;
    }

    public IssueNavigatorType getNavigatorType()
    {
        if (type == null)
        {
            type = IssueNavigatorType.getFromCookie(ActionContext.getRequest());
        }
        return type;
    }

    public boolean isNavigatorTypeAdvanced()
    {
        return getNavigatorType() == IssueNavigatorType.ADVANCED;
    }

    public boolean isNavigatorTypeSimple()
    {
        return getNavigatorType() == IssueNavigatorType.SIMPLE;
    }

    // advanced stuff

    @SuppressWarnings ({ "UnusedDeclaration" })
    public void setRunQuery(final String query)
    {
        this.runQuery = true;
    }

    public void setJqlQuery(final String jqlQuery)
    {
        this.jqlQuery = jqlQuery == null ? null : jqlQuery.trim();
    }

    public String getJqlQuery()
    {
        return jqlQuery;
    }

    public boolean isAutocompleteDisabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_JQL_AUTOCOMPLETE_DISABLED);
    }

    public boolean isAutocompleteDisabledForUser()
    {
        // if the user is not logged in then they do not have autocomplete disabled
        return getRemoteUser() != null && getUserPreferences().getBoolean(PreferenceKeys.USER_JQL_AUTOCOMPLETE_DISABLED);
    }

    public boolean isAutocompleteEnabledForThisRequest()
    {
        // if the user is not logged in then they do not have autocomplete disabled
        return !isAutocompleteDisabled() && !isAutocompleteDisabledForUser();
    }

    private void addWarningMessages(final Collection<String> messages)
    {
        if (messages == null || messages.isEmpty())
        {
            return;
        }

        if (warningMessages == null)
        {
            warningMessages = new HashSet<String>(messages);
        }
        else
        {
            warningMessages.addAll(messages);
        }
    }

    public List<ToolOptionGroup> getToolOptions() throws SearchException, ColumnLayoutStorageException
    {
        List<ToolOptionGroup> options = new ArrayList<ToolOptionGroup>();
        if(isHasBulkChangePermission())
        {
            ToolOptionGroup group = new ToolOptionGroup(getText("navigator.results.currentview.bulkchange"));
            options.add(group);

            if(isBulkEditLimited())
            {
                String label = getText("navigator.results.currentview.bulkchange.limitedissues", getBulkEditMax());
                String url   = "/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=" + getBulkEditMax();
                String title = getText("bulk.edit.limited", getBulkEditMax());
                ToolOptionItem item = new ToolOptionItem("bulkedit_max", label, url, title);
                group.addItem(item);
            }
            else
            {
                String label = getText("navigator.results.currentview.bulkchange.allissues", getSearchResults().getTotal());
                String url   = "/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=" + getSearchResults().getTotal() ;
                ToolOptionItem item = new ToolOptionItem("bulkedit_all", label, url, "");
                group.addItem(item);
            }

            if(getSearchResults().getPages().size() > 1)
            {
                String label = getText("navigator.results.currentview.bulkchange.currentpage");
                String url   = "/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true";
                ToolOptionItem item = new ToolOptionItem("bulkedit_curr_pg", label, url, "");
                group.addItem(item);
            }
        }

        if (getSearchRequest().getQuery() != null && getSearchRequest().getQuery().getOrderByClause() != null
                && !getSearchRequest().getQuery().getOrderByClause().getSearchSorts().isEmpty())
        {
            ToolOptionGroup group = new ToolOptionGroup();
            options.add(group);

            String label = getText("navigator.results.clear.sorts");
            String url   = "/secure/IssueNavigator!clearSorts.jspa";
            ToolOptionItem item = new ToolOptionItem("", label, url, "");
            group.addItem(item);
        }

        if( getRemoteUser() != null && isHasPermission("use"))
        {
            ToolOptionGroup group = new ToolOptionGroup();
            options.add(group);

            if(!isShowOverrideColumnLayout())
            {
                String label = getText("navigator.results.configurenavigator");
                String url   = "/secure/ViewUserIssueColumns!default.jspa";
                ToolOptionItem item = new ToolOptionItem("configure-cols", label, url, "");
                group.addItem(item);

            }else
            {
                String label = getText("navigator.results.useyourcolumns");
                String url   = "/secure/IssueNavigator!columnOverride.jspa";
                ToolOptionItem item = new ToolOptionItem("use-cols", label, url, "");
                group.addItem(item);
            }
        }

        if ( isHasSearchRequestColumnLayout() && !isShowOverrideColumnLayout())
        {
            ToolOptionGroup group = new ToolOptionGroup();
            options.add(group);

            String label = getText("navigator.results.usefilterscolumns");
            String url   = "/secure/IssueNavigator!columnOverride.jspa";
            ToolOptionItem item = new ToolOptionItem("", label, url, "");
            group.addItem(item);
        }

        if(getSearchRequest().getId() != null && isOwnerOfSearchRequest())
        {
            ToolOptionGroup group = new ToolOptionGroup();
            options.add(group);

            if(isHasSearchRequestColumnLayout())
            {
                if(isShowOverrideColumnLayout())
                {
                    String label = getText("navigator.results.filter.columns.edit");
                    String url   = "/secure/ViewSearchRequestIssueColumns!default.jspa?filterId=" + getSearchRequest().getId();
                    ToolOptionItem item = new ToolOptionItem("editFilterColumnsLink", label, url, "");
                    group.addItem(item);
                }
            }else
            {
                String label = getText("navigator.results.filter.columns.add");
                String url   = "/secure/ViewSearchRequestIssueColumns!default.jspa?filterId=" + getSearchRequest().getId();
                ToolOptionItem item = new ToolOptionItem("addFilterColumnsLink", label, url, "");
                group.addItem(item);
            }
        }
        return options;
    }

    public int getMaxIndex(List list)
    {
        return list.size() - 1;
    }


    private void addWarningMessage(final String message)
    {
        addWarningMessages(Collections.singleton(message));
    }

    public final Collection<String> getWarningMessages()
    {
        return warningMessages;
    }

    public Collection<SimpleLink> getIssueOperations()
    {
        final List<SimpleLink> links = new ArrayList<SimpleLink>();
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder().
                add("issueId", "{0}").toMap();
        final JiraHelper helper = new JiraHelper(request, null, params);
        final List<SimpleLinkSection> sections = simpleLinkManager.getSectionsForLocation("opsbar-operations", getRemoteUser(), helper);
        for (SimpleLinkSection section : sections)
        {
            links.addAll(simpleLinkManager.getLinksForSectionIgnoreConditions(section.getId(), getRemoteUser(), helper));
        }
        return links;
    }

    public boolean getShowPluginHints()
    {
        return applicationProperties.getOption(APKeys.JIRA_SHOW_MARKETING_LINKS);
    }

    public String getPluginHintsUrl()
    {
        HelpUtil helpUtil = HelpUtil.getInstance();
        HelpUtil.HelpPath path = helpUtil.getHelpPath("plugin.hint.jql");
        return path.getUrl();
    }

    public String convertToId(String key)
    {
        if(key != null)
        {
            return key.replace('.', '-');
        }
        return "";
    }
}
