package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardState;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * Details of adding a gadget to a dashboard.  If this add operation is inserting a gadget to the beginning or middle
 * of a column, it is the {@link com.atlassian.gadgets.dashboard.spi.DashboardStateStore} implementation's responsibility to properly update the positions
 * of the gadgets that come after the added gadget. 
 *
 * @since 2.0
 */
public final class AddGadgetChange implements DashboardChange
{
    private final GadgetState state;
    private final DashboardState.ColumnIndex columnIndex;
    private final int rowIndex;

    public AddGadgetChange(GadgetState state, DashboardState.ColumnIndex columnIndex, int rowIndex)
    {
        this.state = notNull("state", state);
        this.columnIndex = notNull("columnIndex", columnIndex);
        this.rowIndex = rowIndex;
    }

    /**
     * Invokes the {@code Visitor}s {@link Visitor#visit(AddGadgetChange)} method.
     */
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }
    
    /**
     * Get the state of the gadget being added
     * @return the state of the gadget being added
     */
    public GadgetState getState()
    {
        return state;
    }

    /**
     * Get the index of the column where the gadget is being added
     * @return the index of the column where the gadget is being added
     */
    public DashboardState.ColumnIndex getColumnIndex()
    {
        return columnIndex;
    }

    /**
     * Get the index of the row where the gadget is being added
     * @return the index of the row where the gadget is being added
     */
    public int getRowIndex()
    {
        return rowIndex;
    }
}
