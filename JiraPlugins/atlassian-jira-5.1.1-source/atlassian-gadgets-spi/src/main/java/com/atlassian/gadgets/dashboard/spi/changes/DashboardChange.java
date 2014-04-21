package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.dashboard.DashboardState;

/**
 * Interface for operations that can be performed to update a
 * {@link DashboardState}.  When applying the changes in a {@link com.atlassian.gadgets.dashboard.spi.DashboardStateStore},
 * the changes must be applied in order.
 * 
 * @since 2.0
 */
public interface DashboardChange
{
    /**
     * Accepts a visitor to allow for processing specific change types.
     * 
     * @param visitor {@code Visitor} to invoke the specific {@code visit} method on.
     */
    void accept(Visitor visitor);
    
    /**
     * Visitor which allows for dynamic dispatching of implementations based on the type of change.
     */
    interface Visitor
    {
        /**
         * Process the adding of a gadget.
         * 
         * @param change change to process
         */
        void visit(AddGadgetChange change);

        /**
         * Process a gadgets chrome color changing.
         * 
         * @param change change to process
         */
        void visit(GadgetColorChange change);

        /**
         * Process the removal of a gadget.
         * 
         * @param change change to process
         */
        void visit(RemoveGadgetChange change);
        
        /**
         * Process the updating of a gadgets user preferences.
         * 
         * @param change change to process
         */
        void visit(UpdateGadgetUserPrefsChange change);
        
        /**
         * Process a dashboard layout update.
         * 
         * @param change change to process
         */
        void visit(UpdateLayoutChange change);
    }
}
