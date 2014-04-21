/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.util.I18nHelper;
import org.apache.lucene.search.SortComparatorSource;

import java.util.Map;

/**
 * Fields in JIRA which are able to be placed in the Issue Navigator implement this interface.
 */
public interface NavigableField extends Field
{
    String TEMPLATE_DIRECTORY_PATH = OrderableField.TEMPLATE_DIRECTORY_PATH;

    public final static String ORDER_ASCENDING = "ASC";
    public final static String ORDER_DESCENDING = "DESC";

    public String getColumnHeadingKey();
    
    public String getColumnCssClass();

    /**
     * The order in which to sort the field when it is sorted for the first time.
     *
     * @return  Either {@link #ORDER_ASCENDING} or {@link #ORDER_DESCENDING}
     */
    public String getDefaultSortOrder(); //used by issuetable.vm

    /**
     * A sortComparatorSource object to be used for sorting columns in a table.  In most cases this will use a
     * {@link com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator} using the {@link #getSorter()}
     * method.  However, fields can provide any sorting mechanism that they wish.
     *
     * @return  A SortComparatorSource that can be used to sort, or null if this field does not support sorting
     */
    public SortComparatorSource getSortComparatorSource();

    /**
     * A sorter to be used when sorting columns in a table.  This sort uses the Lucene Document Collection
     * and is therefore a lot faster than sorting the issues in memory.
     *
     * @return  A sorter that can be used to sort this field, or null depending on the value of {@link #getSortComparatorSource()}
     * @see com.atlassian.jira.issue.DocumentIssueImpl
     * @see com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator 
     */
    public LuceneFieldSorter getSorter();

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue);

    /**
     * Returns the id of the field to check for visibility. For example, with original estimate field
     * need to ensure that the timetracking field is not hidden. With most fields, this is the same as their
     * id.
     */
    public String getHiddenFieldId();

    public String prettyPrintChangeHistory(String changeHistory);

    /**
     * Used for email notification templates - allows changelog to be displayed in language of the recipient.
     * @param changeHistory
     * @return String   change history formatted according to locale in i18nHelper
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper);
}
