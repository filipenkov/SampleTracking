package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;

import java.util.Collection;
import java.util.List;

/**
 * This manager is responsible for {@link SearchRequest}s. SearchRequests encapsulate all information used for searches in the issue navigator.
 */
public interface SearchRequestManager extends SharedEntityAccessor<SearchRequest>
{
    /**
     * Retrieves a collection of SearchRequest objects that a user created.
     *
     * @param user The user who created the SearchRequests
     * @return Collection of all {@link SearchRequest} that user created.
     */
    Collection<SearchRequest> getAllOwnedSearchRequests(com.opensymphony.user.User user);

    /**
     * Retrieves a collection of SearchRequest objects that a user created.
     *
     * @param user The user who created the SearchRequests
     * @return Collection of all {@link SearchRequest} that user created.
     */
    Collection<SearchRequest> getAllOwnedSearchRequests(User user);

    /**
     * Find a search request given the author and the request name.
     *
     * @param author The author of the SearchRequest
     * @param name   The name of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request
     */
    SearchRequest getOwnedSearchRequestByName(com.opensymphony.user.User author, String name);

    /**
     * Find a search request given the author and the request name.
     *
     * @param author The author of the SearchRequest
     * @param name   The name of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request
     */
    SearchRequest getOwnedSearchRequestByName(User author, String name);

    /**
     * Return the search request as stored in the database
     *
     * @param user The user to check shares with
     * @param id   The id of the filter
     * @return The SearchRequest, or null if the request id does not exist
     */
    SearchRequest getSearchRequestById(com.opensymphony.user.User user, Long id);

    /**
     * Return the search request as stored in the database
     *
     * @param user The user to check shares with
     * @param id   The id of the filter
     * @return The SearchRequest, or null if the request id does not exist
     */
    SearchRequest getSearchRequestById(User user, Long id);

    /**
     * Return the owner of the passed SearchRequest. This is mainly used for permission checks.
     *
     * @param id the identifier of the SearchRequest.
     * @return the username of the owner of the SearchRequest. Null is returned if the SearchRequest does not exist.
     */
    String getSearchRequestOwnerUserName(Long id);

    /**
     * Takes a SearchRequest, user, name of search request and description and persists the XML representation
     * of the SearchRequest object to the database along with the rest of the details
     *
     * @param request SearchResult that should be persisted
     * @return SearchRequest object that was persisted to the database
     */
    SearchRequest create(SearchRequest request);

    /**
     * Updates an existing search request in the database.
     *
     * @param request the request to persist
     * @return A {@link SearchRequest} that was persisted to the database
     */
    SearchRequest update(SearchRequest request);

    /**
     * Removes the SearchRequest GenericValue from the database based on its id
     *
     * @param id of the search request to be removed from storage
     */
    void delete(Long id);

    /**
     * Return a list of all search requests visible to a given user.
     *
     * @param user The user to find search requests for
     * @return A list of visible search requests
     * @deprecated since v3.13. Use {@link com.atlassian.jira.bc.filter.SearchRequestService#getFavouriteFilters(User)}
     *             or search functions
     */
    @Deprecated
    List<SearchRequest> getVisibleRequests(com.opensymphony.user.User user);

    /**
     * Find a search request given the searcher and the search request ID.
     *
     * @param searcher The user to check permissions for.
     * @param id       The id of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request for this searcher.
     * @deprecated since v3.13. Use {@link com.atlassian.jira.bc.filter.SearchRequestService#getFilter(com.atlassian.jira.bc.JiraServiceContext, Long)}
     */
    @Deprecated
    SearchRequest getRequest(com.opensymphony.user.User searcher, Long id);

    /**
     * Find a search request given the searcher and the search request ID.
     *
     * @param searcher The user to check permissions for.
     * @param id       The id of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request for this searcher.
     * @deprecated since v3.13. Use {@link com.atlassian.jira.bc.filter.SearchRequestService#getFilter(com.atlassian.jira.bc.JiraServiceContext, Long)}
     */
    @Deprecated
    SearchRequest getSearchRequest(com.opensymphony.user.User searcher, Long id);

    /**
     * Search for the SearchRequests that match the passed searchParameters. The result can be paged so that a subset
     * of the results can be returned.
     *
     * @param searchParameters the searchParameters to query.
     * @param user             the user performing the search.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result of the search.
     */
    SharedEntitySearchResult<SearchRequest> search(SharedEntitySearchParameters searchParameters, com.opensymphony.user.User user, int pagePosition, int pageWidth);
    
    /**
     * Search for the SearchRequests that match the passed searchParameters. The result can be paged so that a subset
     * of the results can be returned.
     *
     * @param searchParameters the searchParameters to query.
     * @param user             the user performing the search.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result of the search.
     */
    SharedEntitySearchResult<SearchRequest> search(SharedEntitySearchParameters searchParameters, User user, int pagePosition, int pageWidth);
}
