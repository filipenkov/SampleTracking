package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.apache.commons.collections.LRUMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract public class AbstractColumnLayoutManager implements ColumnLayoutManager
{
    private static final Logger log = Logger.getLogger(AbstractColumnLayoutManager.class);
    protected static final int DEFAULT_CACHE_SIZE = 200;
    private final FieldManager fieldManager;
    protected final OfBizDelegator ofBizDelegator;
    private final ColumnLayout defaultColumnLayout;

    // Caches ColumnLayouts using User as a key and List of ColumnLayoutItems as values
    private final Map columnLayoutCache;

    public AbstractColumnLayoutManager(FieldManager fieldManager, OfBizDelegator ofBizDelegator)
    {
        this.fieldManager = fieldManager;
        this.ofBizDelegator = ofBizDelegator;

        // Synchronize the map to ensure exclusive access by each thread as advised by the LRUMap's implementors
        this.columnLayoutCache = Collections.synchronizedMap(new LRUMap(DEFAULT_CACHE_SIZE));

        int position = 0;
        List columnLayoutItems = new ArrayList();
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.ISSUE_TYPE), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.ISSUE_KEY), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.SUMMARY), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.REPORTER), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.PRIORITY), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.STATUS), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.RESOLUTION), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.CREATED), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.UPDATED), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(IssueFieldConstants.DUE_DATE), position++));
        defaultColumnLayout = new DefaultColumnLayoutImpl(Collections.unmodifiableList(columnLayoutItems));
    }

    abstract public boolean hasColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException;
    abstract public EditableSearchRequestColumnLayout getEditableSearchRequestColumnLayout(User user, SearchRequest searchRequest) throws ColumnLayoutStorageException;
    abstract public void storeEditableSearchRequestColumnLayout(EditableSearchRequestColumnLayout editableSearchRequestColumnLayout) throws ColumnLayoutStorageException;
    abstract public void restoreSearchRequestColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException;

    @Override
    public boolean hasDefaultColumnLayout() throws ColumnLayoutStorageException
    {
        return hasDefaultColumnLayout(null);
    }

    @Override
    public boolean hasColumnLayout(User user) throws ColumnLayoutStorageException
    {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");

        return hasDefaultColumnLayout(user.getName());
    }

    private boolean hasDefaultColumnLayout(String username)
    {
        GenericValue columnLayout = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", username, "searchrequest", null)));
        return (columnLayout != null);
    }

    @Override
    public ColumnLayout getColumnLayout(User remoteUser, SearchRequest searchRequest) throws ColumnLayoutStorageException
    {
        // The professional version does not support search request column layout items
        return getColumnLayout(remoteUser);
    }

    @Override
    public ColumnLayout getColumnLayout(User remoteUser) throws ColumnLayoutStorageException
    {
        try
        {
            Set availableFields = fieldManager.getAvailableNavigableFields(remoteUser);
            String username = (remoteUser == null ? null : remoteUser.getName());
            return new UserColumnLayoutImpl(getColumnLayoutItems(username, availableFields), remoteUser);
        }
        catch (FieldException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not retrieve available fields.", e);
        }
    }

    @Override
    public ColumnLayout getDefaultColumnLayout(User remoteUser) throws ColumnLayoutStorageException
    {
        try
        {
            Set availableFields = fieldManager.getAvailableNavigableFields(remoteUser);
            return new DefaultColumnLayoutImpl(getColumnLayoutItems(null, availableFields));
        }
        catch (FieldException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not retrieve available fields.", e);
        }
    }

    @Override
    public ColumnLayout getDefaultColumnLayout() throws ColumnLayoutStorageException
    {
        return new DefaultColumnLayoutImpl(defaultColumnLayout.getColumnLayoutItems());
    }

    @Override
    public void refresh()
    {
        columnLayoutCache.clear();
    }

    @Override
    public EditableDefaultColumnLayout getEditableDefaultColumnLayout() throws ColumnLayoutStorageException
    {
        try
        {
            Set availableFields = fieldManager.getAllAvailableNavigableFields();
            return new EditableDefaultColumnLayoutImpl(getColumnLayoutItems(null, availableFields));
        }
        catch (FieldException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not retrieve available fields.", e);
        }
    }

    @Override
    public EditableUserColumnLayout getEditableUserColumnLayout(User user) throws ColumnLayoutStorageException
    {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");

        try
        {
            Set availableFields = fieldManager.getAvailableNavigableFields(user);
            List items = getColumnLayoutItems(user.getName(), availableFields);
            return new EditableUserColumnLayoutImpl(items, user);
        }
        catch (FieldException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not retrieve available fields for user '" + user.getName() + "'.", e);
        }
    }

    protected List getColumnLayoutItems(String username, Set availableFields) throws ColumnLayoutStorageException
    {
        // Check the cache
        List columnLayoutItems = (List) columnLayoutCache.get(username);
        if (columnLayoutItems != null)
        {
            return columnLayoutItems;
        }

        GenericValue columnLayoutGV = null;
        if (username != null)
        {
            columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", username, "searchrequest", null)));
        }

        if (columnLayoutGV == null)
        {
            // The user has no column layout return user the default
            columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null, "searchrequest", null)));
            if (columnLayoutGV == null)
            {
                columnLayoutItems = removeUnavailableColumnLayoutItems(defaultColumnLayout.getColumnLayoutItems(), availableFields);

                // Cache the columns layout items
                columnLayoutCache.put(username, columnLayoutItems);
                return columnLayoutItems;
            }
        }

        try
        {
            columnLayoutItems = verifyColumnLayoutItems(columnLayoutGV, availableFields);

            // Cache the columns layout items
            columnLayoutCache.put(username, columnLayoutItems);

            return columnLayoutItems;
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not retrieve the Column Layout Items.", e);
        }
    }

    protected List verifyColumnLayoutItems(GenericValue columnLayoutGV, Set availableFields) throws GenericEntityException
    {
        List columnLayoutItems = new ArrayList();
        List columnLayoutItemGVs = columnLayoutGV.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition ASC"));
        NavigableField navigableField;
        for (int i = 0; i < columnLayoutItemGVs.size(); i++)
        {
            GenericValue columnLayoutItemGV = (GenericValue) columnLayoutItemGVs.get(i);
            if (fieldManager.isNavigableField(columnLayoutItemGV.getString("fieldidentifier")))
            {
                navigableField = fieldManager.getNavigableField(columnLayoutItemGV.getString("fieldidentifier"));
                columnLayoutItems.add(new ColumnLayoutItemImpl(navigableField, columnLayoutItemGV.getLong("horizontalposition").intValue()));
            }
        }

        // Remove the fields that cannot be seen
        columnLayoutItems = removeUnavailableColumnLayoutItems(columnLayoutItems, availableFields);
        return columnLayoutItems;
    }

    private List removeUnavailableColumnLayoutItems(List columnLayoutItems, Set availableFields)
    {
        List availableColumnLyaoutItems = new ArrayList();
        for (int i = 0; i < columnLayoutItems.size(); i++)
        {
            ColumnLayoutItem columnLayoutItem = (ColumnLayoutItem) columnLayoutItems.get(i);

            // Only add the field if it can be seen
            if (availableFields.contains(columnLayoutItem.getNavigableField()))
            {
                availableColumnLyaoutItems.add(columnLayoutItem);
            }
        }
        return availableColumnLyaoutItems;
    }

    @Override
    public void storeEditableDefaultColumnLayout(EditableDefaultColumnLayout editableDefaultColumnLayout) throws ColumnLayoutStorageException
    {
        storeEditableColumnLayout(editableDefaultColumnLayout, null);

        // The default column layout has changed, as many users might be using the default
        // column layout clear the whole cache
        columnLayoutCache.clear();
    }

    @Override
    public void storeEditableUserColumnLayout(EditableUserColumnLayout editableUserColumnLayout) throws ColumnLayoutStorageException
    {
        String username = editableUserColumnLayout.getUser().getName();
        storeEditableColumnLayout(editableUserColumnLayout, username);

        // Clear the user's column layout items from cache
        columnLayoutCache.remove(username);
    }

    /**
     * THIS METHOD MUST BE SYNCHRONIZED!!!!
     * So that only one thread updates the database at any one time. "Columns are duplicated" if this method
     * is not synchronized.
     *
     * @param columnLayout
     * @param username
     * @throws ColumnLayoutStorageException
     */
    private synchronized void storeEditableColumnLayout(ColumnLayout columnLayout, String username) throws ColumnLayoutStorageException
    {
        // ColumnLayout (id, layoutscheme)
        // ColumnLayoutItem (id, columnlayout, fieldidentifier, horizontalposition)
        // Find the default column layout in the database if it exists
        try
        {
            GenericValue columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", username, "searchrequest", null)));

            if (columnLayoutGV == null)
            {
                // There is no default, create a new one
                columnLayoutGV = EntityUtils.createValue("ColumnLayout", EasyMap.build("username", username, "searchrequest", null));
            }

            storeColumnLayoutItems(columnLayoutGV, columnLayout);
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
            throw new ColumnLayoutStorageException("Could not load ColumnLayout", e);
        }
    }

    protected void storeColumnLayoutItems(GenericValue columnLayoutGV, ColumnLayout columnLayout) throws GenericEntityException
    {
        // Remove ColumnLayout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
        List columnLayoutItemGVs = columnLayoutGV.getRelated("ChildColumnLayoutItem");
        ofBizDelegator.removeAll(columnLayoutItemGVs);

        // Retrieve a list of Column Layout Items for this layout
        List columnLayoutItems = columnLayout.getColumnLayoutItems();
        for (int i = 0; i < columnLayoutItems.size(); i++)
        {
            ColumnLayoutItem columnLayoutItem = (ColumnLayoutItem) columnLayoutItems.get(i);
            EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier", columnLayoutItem.getNavigableField().getId(), "horizontalposition", new Long(i)));
        }
    }

    @Override
    public void restoreDefaultColumnLayout() throws ColumnLayoutStorageException
    {
        restoreColumnLayout(null);

        // The default column layout has changed, as many users might be using the default
        // column layout clear the whole cache
        columnLayoutCache.clear();
    }

    @Override
    public void restoreUserColumnLayout(User user) throws ColumnLayoutStorageException
    {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");

        String username = user.getName();
        restoreColumnLayout(username);

        // Clear the user's column layout items from cache
        columnLayoutCache.remove(username);
    }

    private synchronized void restoreColumnLayout(String username) throws ColumnLayoutStorageException
    {
        // Restore system defaults by removing the configured defaults from the permanent store - DB
        try
        {
            GenericValue columnLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", EasyMap.build("username", username, "searchrequest", null)));
            if (columnLayoutGV != null)
            {
                removeColumnLayoutItems(columnLayoutGV);
            }
            else
            {
                log.warn("User with username '" + username + "' is already using the default layout.");
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error removing column layout for username " + username + ".", e);
            throw new ColumnLayoutStorageException("Error removing column layout for username " + username + ".", e);
        }
    }

    protected void removeColumnLayoutItems(GenericValue columnLayoutGV) throws GenericEntityException
    {
        // Remove Column Layout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
        List columnLayoutItemGVs = columnLayoutGV.getRelated("ChildColumnLayoutItem");
        ofBizDelegator.removeAll(columnLayoutItemGVs);
        columnLayoutGV.remove();
    }

    protected FieldManager getFieldManager()
    {
        return fieldManager;
    }
}
