package com.atlassian.gadgets.dashboard.spi;

import java.util.Collections;
import java.util.List;

import com.atlassian.gadgets.GadgetId;

/**
 * <p>Defines how gadgets should be layed out on a dashboard.</p>
 * 
 * @since 2.0
 */
public final class GadgetLayout
{
    private final List<? extends Iterable<GadgetId>> columnLayout;

    public GadgetLayout(List<? extends Iterable<GadgetId>> columnLayout)
    {
        this.columnLayout = Collections.unmodifiableList(columnLayout);
    }

    /**
     * Returns the number of columns that should be on the dashboard.
     * 
     * @return number of columns that should be on the dashboard
     */
    public int getNumberOfColumns()
    {
        return columnLayout.size();
    }
    
    /**
     * Returns the IDs of the gadgets that appear in a given column.
     * 
     * @param column number of the column to get the gadgets it contains
     * @return IDs of the gadgets that appear in a given column
     */
    public Iterable<GadgetId> getGadgetsInColumn(int column)
    {
        return columnLayout.get(column);
    }
}
