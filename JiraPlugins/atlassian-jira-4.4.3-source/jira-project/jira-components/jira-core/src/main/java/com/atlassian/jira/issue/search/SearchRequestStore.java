package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.EnclosedIterable;

import java.util.Collection;

/**
 * Store used for CRUD of SearchRequests
 *
 * @since v3.13
 */
public interface SearchRequestStore
{
    /**
     * Retrieves all the SearchRequests
     *
     * @return Returns a list of searchRequest
     * @deprecated use {@link #getAll()}
     */
    @Deprecated
    Collection<SearchRequest> getAllRequests();

    /**
     * Get a {@link EnclosedIterable} of SearchRequests for the specified List of ids.
     *
     * @param descriptor retrieval descriptor
     * @return CloseableIterable that contains reference to SearchRequests with the specified ids.
     */
    EnclosedIterable<SearchRequest> get(RetrievalDescriptor descriptor);

    /**
     * Get a {@link EnclosedIterable} of all SearchRequests in the database.
     *
     * @return CloseableIterable that contains reference to all SearchRequests.
     */
    EnclosedIterable<SearchRequest> getAll();

    /**
     * Get a {@link EnclosedIterable} of all IndexableSharedEntities representing SearchRequests in the database.
     *
     * Note: this is used so that we can retrieve all the meta data about a SearchRequest without having to deal with
     * the {@link com.atlassian.query.Query}.
     *
     * @return CloseableIterable that contains reference to all IndexableSharedEntities representing SearchRequests.
     */
    EnclosedIterable<IndexableSharedEntity<SearchRequest>> getAllIndexableSharedEntities();

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
     * @param author Author of the SearchRequest
     * @param name   Name of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request
     */
    SearchRequest getRequestByAuthorAndName(User author, String name);

    /**
     * Return the search request as stored in the database
     *
     * @param id Id of the SearchRequest
     * @return The SearchRequest, or null if the request id does not exist
     */
    SearchRequest getSearchRequest(@NotNull Long id);

    /**
     * Takes a {@link SearchRequest}, user, name of search request and description and persists the XML representation
     * of the SearchRequest object to the database along with the rest of the details
     *
     * @param request SearchResult that should be persisted
     * @return SearchRequest object that was persisted to the database
     */
    SearchRequest create(@NotNull SearchRequest request);

    /**
     * Updates an existing search request in the database.
     *
     * @param request the request to persist.
     * @return A {@link SearchRequest} that was persisted to the database
     */
    SearchRequest update(@NotNull SearchRequest request);

    /**
     * Updates the favourite count of the SearchRequest in the database.
     *
     * @param searchRequestId the identifier of the search request to decrease.
     * @param incrementValue  the value to increase the favourite count by. Can be a number < 0 to decrease the favourite count.
     * @return the updated {@link SearchRequest}.
     */
    SearchRequest adjustFavouriteCount(@NotNull Long searchRequestId, int incrementValue);

    /**
     * Removes the SearchRequest GenericValue from the database based on its id
     *
     * @param id of the search request to be removed from storage
     */
    void delete(@NotNull Long id);

    /**
     * Get all {@link SearchRequest search requests} associate with a given {@link Project}.
     *
     * @param project Project that is associated with the filters
     * @return Collection of {@link SearchRequest} that have their project set to the given project
     */
    EnclosedIterable<SearchRequest> getSearchRequests(final Project project);

    /**
     * Get all {@link SearchRequest search requests} associated with a given {@link Group}.
     *
     * @param group the group that is associated with the filters
     * @return Collection of {@link SearchRequest} that have their project set to the given project
     */
    EnclosedIterable<SearchRequest> getSearchRequests(final Group group);
}
