package com.atlassian.jira.sharing;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator;
import com.atlassian.jira.issue.statistics.AssigneeStatisticsMapper;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.lucene.search.SortComparatorSource;
import org.apache.lucene.search.SortField;

import javax.annotation.concurrent.Immutable;

/**
 * Simple enumeration that represents the standard columns in a {@link SharedEntity}.
 * <p>
 * Note: the sortColumn is used for equality/hashCode and MUST be distinct.
 *
 * @since v3.13
 */
@Immutable
public class SharedEntityColumn
{
    public static final SharedEntityColumn ID = new SharedEntityColumn("id", SortField.INT);
    public static final SharedEntityColumn NAME = new SharedEntityColumn("name", "nameSort", "nameCaseless", SortField.STRING);
    public static final SharedEntityColumn DESCRIPTION = new SharedEntityColumn("description", "descriptionSort", SortField.STRING);
    public static final SharedEntityColumn OWNER = new SharedEntityColumn("owner", new SortComparatorFactory()
    {
        public SortComparatorSource getSortComparator()
        {
            return new MappedSortComparator(ComponentManager.getComponentInstanceOfType(AssigneeStatisticsMapper.class));
        }
    });
    public static final SharedEntityColumn FAVOURITE_COUNT = new SharedEntityColumn("favouriteCount", SortField.INT);
    public static final SharedEntityColumn IS_SHARED = new SharedEntityColumn("isShared", SortField.STRING);

    private final String name;
    private final String sortColumn;
    private final String caseInsensitiveColumn;
    private final int sortType;
    private final SortComparatorFactory sortComparatorFactory;

    private SharedEntityColumn(final String sortColumn, final int sortType)
    {
        this(sortColumn, sortColumn, sortType);
    }

    private SharedEntityColumn(final String name, final String sortColumn, final int sortType)
    {
        Assertions.notNull("sortColumn", sortColumn);
        this.name = name;
        this.sortColumn = sortColumn;
        this.caseInsensitiveColumn = null;
        this.sortType = sortType;
        sortComparatorFactory = null;
    }

    private SharedEntityColumn(final String name, final String sortColumn, final String caseInsensitiveColumn, final int sortType)
    {
        Assertions.notNull("sortColumn", sortColumn);
        this.name = name;
        this.sortColumn = sortColumn;
        this.sortType = sortType;
        this.caseInsensitiveColumn = caseInsensitiveColumn;
        sortComparatorFactory = null;
    }

    private SharedEntityColumn(final String name, final SortComparatorFactory sortComparatorFactory)
    {
        Assertions.notNull("sortColumn", name);
        this.name = name;
        this.sortComparatorFactory = sortComparatorFactory;
        this.caseInsensitiveColumn = null;
        sortColumn = name;
        sortType = SortField.CUSTOM;
    }

    /**
     * @return the column (field) name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the column (field) name used for sorting
     */
    public String getSortColumn()
    {
        return sortColumn;
    }

    /**
     * @return the {@link SortField} int used to determine the comparison algorithm
     */
    public int getSortType()
    {
        return sortType;
    }

    public String getCaseInsensitiveColumn()
    {
        return caseInsensitiveColumn;
    }

    public boolean isCustomSort()
    {
        return sortComparatorFactory != null;
    }

    public SortComparatorSource createSortComparator()
    {
        return sortComparatorFactory.getSortComparator();
    }

    public String toString()
    {
        return name;
    }

    ///CLOVER:OFF
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

        final SharedEntityColumn sortOrder = (SharedEntityColumn) o;

        if (sortColumn != null ? !sortColumn.equals(sortOrder.sortColumn) : sortOrder.sortColumn != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (sortColumn != null ? sortColumn.hashCode() : 0);
    }

    ///CLOVER:ON

    /**
     * Used for implementing custom sorting. Create the SortComparator
     */
    interface SortComparatorFactory
    {
        SortComparatorSource getSortComparator();
    }
}
