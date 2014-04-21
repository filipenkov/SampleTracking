package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.map.CacheObject;
import org.apache.commons.collections.LRUMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventComponent
public class DefaultColumnLayoutManager extends AbstractColumnLayoutManager
{
    private static final Logger log = Logger.getLogger(AbstractColumnLayoutManager.class);

    // Used to store the column layout
    private final Map searchRequestColumnLayoutCache;

    // Used to store the Search Request's
    private final Map searchRequestColumnLayoutItemCache;

    public DefaultColumnLayoutManager(FieldManager fieldManager, OfBizDelegator ofBizDelegator)
    {
        super(fieldManager, ofBizDelegator);
        this.searchRequestColumnLayoutCache = Collections.synchronizedMap(new LRUMap(DEFAULT_CACHE_SIZE));
        this.searchRequestColumnLayoutItemCache = Collections.synchronizedMap(new LRUMap(DEFAULT_CACHE_SIZE));
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    @Override
    public ColumnLayout getColumnLayout(User remoteUser, SearchRequest searchRequest) throws ColumnLayoutStorageException
    {
        try
        {
            Set availableFields = getFieldManager().getAvailableNavigableFields(remoteUser);
            String username = (remoteUser == null ? null : remoteUser.getName());

            // need to check for search request specific column layout items
            return new UserColumnLayoutImpl(getColumnLayoutItems(username, searchRequest, availableFields), remoteUser);
        }
        catch (FieldException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not retrieve available fields.", e);
        }
    }

    protected List getColumnLayoutItems(String username, SearchRequest searchRequest, Set availableFields) throws ColumnLayoutStorageException
    {
        if (searchRequest == null)
            throw new IllegalArgumentException("Search request cannot be null.");

        GenericValue columnLayoutGV;
        CacheObject cacheObject = (CacheObject) getSearchRequestColumnLayoutCache().get(searchRequest.getId());
        // Check if we have the Search Request Column Layout cached
        if (cacheObject != null)
        {
            // If so, get it out of the cache
            columnLayoutGV = (GenericValue) cacheObject.getValue();
        }
        else
        {
            // Otherwise retrieve from the database
            columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null, "searchrequest", searchRequest.getId())));

            // Record the result of the lookup in the cache
            getSearchRequestColumnLayoutCache().put(searchRequest.getId(), new CacheObject(columnLayoutGV));
        }

        // If there are no column layout specified for the search request try the user columns and then the default
        // column layout items
        if (columnLayoutGV == null)
        {
            // All that logic should be in the super class
            return super.getColumnLayoutItems(username, availableFields);
        }
        else
        {
            // Check if we have the Search Request's Column Layout Items cached
            List columnLayoutItems = (List) getSearchRequestColumnLayoutItemCache().get(searchRequest.getId());
            if (columnLayoutItems != null)
            {
                // If so, get the items from the cache
                return columnLayoutItems;
            }
            else
            {
                try
                {
                    // Otherwise look up in the database
                    columnLayoutItems = verifyColumnLayoutItems(columnLayoutGV, availableFields);

                    // Cache the columns layout items
                    getSearchRequestColumnLayoutItemCache().put(searchRequest.getId(), columnLayoutItems);
                    return columnLayoutItems;
                }
                catch (GenericEntityException e)
                {
                    log.error(e, e);
                    throw new ColumnLayoutStorageException("Could not retrieve the Column Layout Items.", e);
                }
            }
        }
    }

    @Override
    public EditableSearchRequestColumnLayout getEditableSearchRequestColumnLayout(User user, SearchRequest searchRequest) throws ColumnLayoutStorageException
    {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");

        if (searchRequest == null)
            throw new IllegalArgumentException("SearchRequest cannot be null.");

        try
        {
            Set availableFields = getFieldManager().getAvailableNavigableFields(user);
            List items = getColumnLayoutItems(user.getName(), searchRequest, availableFields);
            return new EditableSearchRequestColumnLayoutImpl(items, user, searchRequest);
        }
        catch (FieldException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not retrieve available fields for user '" + user.getName() + "'.", e);
        }
    }

    @Override
    public void storeEditableSearchRequestColumnLayout(EditableSearchRequestColumnLayout editableSearchRequestColumnLayout) throws ColumnLayoutStorageException
    {
        final SearchRequest searchRequest = editableSearchRequestColumnLayout.getSearchRequest();
        Long filterId = searchRequest.getId();

        storeSearchRequestColumnLayout(editableSearchRequestColumnLayout, filterId);

        // Clear the search request column layout and search request column layout items from caches
        getSearchRequestColumnLayoutCache().remove(searchRequest.getId());
        getSearchRequestColumnLayoutItemCache().remove(searchRequest.getId());
    }

    private void storeSearchRequestColumnLayout(ColumnLayout columnLayout, Long filterId) throws ColumnLayoutStorageException
    {
        try
        {
            // Find the column layout in the database if it exists
            GenericValue columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null, "searchrequest", filterId)));

            if (columnLayoutGV == null)
            {
                // There is no search request layout, create a new one
                columnLayoutGV = EntityUtils.createValue("ColumnLayout", EasyMap.build("username", null, "searchrequest", filterId));
            }

            storeColumnLayoutItems(columnLayoutGV, columnLayout);
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not load ColumnLayout", e);
        }
    }

    @Override
    public void restoreSearchRequestColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException
    {
        if (searchRequest == null)
            throw new IllegalArgumentException("SearchRequest cannot be null.");

        restoreSearchRequestColumnLayout(searchRequest.getId());

        // Clear the search request column layout and search request column layout items from caches
        getSearchRequestColumnLayoutCache().remove(searchRequest.getId());
        getSearchRequestColumnLayoutItemCache().remove(searchRequest.getId());
    }

    private void restoreSearchRequestColumnLayout(Long filterId) throws ColumnLayoutStorageException
    {
        // Restore system defaults by removing the configured defaults from the permanent store - DB
        try
        {
            GenericValue columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null, "searchrequest", filterId)));
            if (columnLayoutGV != null)
            {
                removeColumnLayoutItems(columnLayoutGV);
            }
            else
            {
                log.warn("Search Request with id '" + filterId + "' does not have a saved column layout.");
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error removing column layout for search request with id  '" + filterId + "'.", e);
            throw new ColumnLayoutStorageException("Error removing column layout for search request with id  '" + filterId + "'.", e);
        }
    }

    @Override
    public boolean hasColumnLayout(SearchRequest searchRequest)
    {
        if (searchRequest == null)
            throw new IllegalArgumentException("SearchRequest cannot be null.");

        // If the search request is not saved (loaded) it cannot have a column layout
        if (!searchRequest.isLoaded())
            return false;

        GenericValue columnLayoutGV = null;
        CacheObject cacheObject = (CacheObject) getSearchRequestColumnLayoutCache().get(searchRequest.getId());
        // Check the cache
        if (cacheObject != null)
        {
            // If the result is cached, use it
            columnLayoutGV = (GenericValue) cacheObject.getValue();
        }
        else
        {
            // Otherwise lookup in the database
            columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null, "searchrequest", searchRequest.getId())));

            // Cache the result of the lookup
            getSearchRequestColumnLayoutCache().put(searchRequest.getId(), new CacheObject(columnLayoutGV));

        }
        return (columnLayoutGV != null);
    }

    @Override
    public void refresh()
    {
        // Clear all caches
        getSearchRequestColumnLayoutCache().clear();
        getSearchRequestColumnLayoutItemCache().clear();
        super.refresh();
    }

    protected Map getSearchRequestColumnLayoutCache()
    {
        return searchRequestColumnLayoutCache;
    }

    private Map getSearchRequestColumnLayoutItemCache()
    {
        return searchRequestColumnLayoutItemCache;
    }
}

