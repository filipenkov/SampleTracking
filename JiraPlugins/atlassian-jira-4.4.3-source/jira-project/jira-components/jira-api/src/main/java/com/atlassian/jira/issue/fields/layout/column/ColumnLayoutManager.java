package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;

public interface ColumnLayoutManager
{
    public boolean hasDefaultColumnLayout() throws ColumnLayoutStorageException;

    public boolean hasColumnLayout(com.opensymphony.user.User user) throws ColumnLayoutStorageException;

    public boolean hasColumnLayout(User user) throws ColumnLayoutStorageException;

    public boolean hasColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Get the columns layout for a user, if the user does not have one the default is returned
     * @param user
     * @return Immutable ColumnLayout to be used when displaying
     */
    public ColumnLayout getColumnLayout(com.opensymphony.user.User user) throws ColumnLayoutStorageException;

    /**
     * Get the columns layout for a user, if the user does not have one the default is returned
     * @param user
     * @return Immutable ColumnLayout to be used when displaying
     */
    public ColumnLayout getColumnLayout(User user) throws ColumnLayoutStorageException;

    /**
     * Get the columns layout for a searchRequest, if the searchRequest does not have one the user's columns
     * are returned. If the user does nto have one the default is returned
     * @param searchRequest
     * @return Immutable ColumnLayout to be used when displaying
     */
    public ColumnLayout getColumnLayout(com.opensymphony.user.User remoteUser, SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Get the columns layout for a searchRequest, if the searchRequest does not have one the user's columns
     * are returned. If the user does nto have one the default is returned
     * @param searchRequest
     * @return Immutable ColumnLayout to be used when displaying
     */
    public ColumnLayout getColumnLayout(User remoteUser, SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Get an editable default column layout for the system
     */
    public EditableDefaultColumnLayout getEditableDefaultColumnLayout() throws ColumnLayoutStorageException;

    /**
     * Get an editable column layout for the user, returns null if they do not have one
     * @param user
     * @return EditableColumnLayout if there is one for the user otherwise return a new one generated from the default
     */
    public EditableUserColumnLayout getEditableUserColumnLayout(com.opensymphony.user.User user) throws ColumnLayoutStorageException;

    /**
     * Get an editable column layout for the user, returns null if they do not have one
     * @param user
     * @return EditableColumnLayout if there is one for the user otherwise return a new one generated from the default
     */
    public EditableUserColumnLayout getEditableUserColumnLayout(User user) throws ColumnLayoutStorageException;

    /**
     * Get an editable column layout for the searchRequest, returns null if it does not have one
     * @param user
     * @param searchRequest
     * @return EditableColumnLayout if there is one for the searchRequest otherwise return a new one generated from the default
     */
    public EditableSearchRequestColumnLayout getEditableSearchRequestColumnLayout(com.opensymphony.user.User user, SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Get an editable column layout for the searchRequest, returns null if it does not have one
     * @param user
     * @param searchRequest
     * @return EditableColumnLayout if there is one for the searchRequest otherwise return a new one generated from the default
     */
    public EditableSearchRequestColumnLayout getEditableSearchRequestColumnLayout(User user, SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Writes the default column layout to permanent storage
     * @param editableDefaultColumnLayout
     */
    public void storeEditableDefaultColumnLayout(EditableDefaultColumnLayout editableDefaultColumnLayout) throws ColumnLayoutStorageException;

    /**
     * Writes the default column layout to permanent storage
     * @param editableUserColumnLayout
     */
    public void storeEditableUserColumnLayout(EditableUserColumnLayout editableUserColumnLayout) throws ColumnLayoutStorageException;

    /**
     * Writes the default column layout to permanent storage
     * @param editableSearchRequestColumnLayout
     */
    public void storeEditableSearchRequestColumnLayout(EditableSearchRequestColumnLayout editableSearchRequestColumnLayout) throws ColumnLayoutStorageException;

    public void restoreDefaultColumnLayout() throws ColumnLayoutStorageException;

    public void restoreUserColumnLayout(com.opensymphony.user.User user) throws ColumnLayoutStorageException;

    public void restoreUserColumnLayout(User user) throws ColumnLayoutStorageException;

    public void restoreSearchRequestColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Get the default Layout, and filter out the columns which a user cannot see
     */
    public ColumnLayout getDefaultColumnLayout(com.opensymphony.user.User remoteUser) throws ColumnLayoutStorageException;

    /**
     * Get the default Layout, and filter out the columns which a user cannot see
     */
    public ColumnLayout getDefaultColumnLayout(User remoteUser) throws ColumnLayoutStorageException;

    ColumnLayout getDefaultColumnLayout() throws ColumnLayoutStorageException;

    public void refresh();
}
