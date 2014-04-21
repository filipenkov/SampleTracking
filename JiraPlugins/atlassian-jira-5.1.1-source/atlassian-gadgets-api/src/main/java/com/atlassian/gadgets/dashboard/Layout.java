package com.atlassian.gadgets.dashboard;

import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;

/**
 * Dashboards can be laid out in multiple ways: with a single column (A), with two columns of equal size (AA), with two
 * columns where one is smaller and the other is larger (AB), etc.  A larger column is said to be "greedy" because
 * it takes up more room than any of the other columns in the tab.  A normal column is said to be "fair" because it
 * tries to share space equally with the other tabs.
 */
public enum Layout
{
    /** single column */
    A(ColumnSpec.FAIR),
    /** two columns of equal size */
    AA(ColumnSpec.FAIR, ColumnSpec.FAIR),
    /** two columns, the first being smaller than the second */
    AB(ColumnSpec.FAIR, ColumnSpec.GREEDY),
    /** two columns, the first being larger than the second */
    BA(ColumnSpec.GREEDY, ColumnSpec.FAIR),
    /** three columns, each of equal size */
    AAA(ColumnSpec.FAIR, ColumnSpec.FAIR, ColumnSpec.FAIR),
    /** three columns, the first and third are of equal size with the second column larger than the others */
    ABA(ColumnSpec.FAIR, ColumnSpec.GREEDY, ColumnSpec.FAIR);

    private final Layout.ColumnSpec[] columnSpec;

    private Layout(Layout.ColumnSpec... columnSpec)
    {
        this.columnSpec = columnSpec;
    }

    /**
     * Returns the number of columns in this layout.
     * 
     * @return the number of columns in this layout
     */
    public int getNumberOfColumns()
    {
        return columnSpec.length;
    }

    /**
     * Checks if the {@code column} exists in this layout.
     * 
     * @param column Column to check exists in this layout
     * @return {@code true} if the column exists in this layout, {@code false} otherwise
     */
    public boolean contains(ColumnIndex column)
    {
        return column.index() < columnSpec.length;
    }

    /**
     * Return an immutable {@code Iterable} over the {@code ColumnIndex}es that exist in this layout.
     * 
     * @return an immutable {@code Iterable} over the {@code ColumnIndex}es that exist in this layout.
     */
    public Iterable<ColumnIndex> getColumnRange()
    {
        return ColumnIndex.range(ColumnIndex.ZERO, ColumnIndex.from(columnSpec.length - 1));
    }
    
    /**
     * Checks if the {@code column} shares the space with the other columns on the screen equally or if it is greedy
     * and should take up more space.
     * 
     * @param column column index to check for sizing fairness
     * @return whether the column tries to share space equally and fairly with other columns
     * @throws IllegalArgumentException thrown if the column is not contained in this layout
     */
    public boolean isColumnSizingFair(ColumnIndex column)
    {
        if (!contains(column))
        {
            throw new IllegalArgumentException("Column " + column + " does not exist in this layout");
        }
        return columnSpec[column.index()] == ColumnSpec.FAIR;
    }

    /**
     * The specifications for the column types.
     */
    enum ColumnSpec
    {
        /** The column takes up more space than the other columns in the layout. */  
        GREEDY,
        
        /** The columns shares space with the other columns in the layout equally. */ 
        FAIR
    }
}