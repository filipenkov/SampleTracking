package com.atlassian.gadgets.dashboard.spi;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;

/**
 * <p>Provides a means for looking up, updating and removing {@link DashboardState} objects to/from the persistent 
 * store.</p>
 * 
 * @since 2.0
 */
public interface DashboardStateStore
{
    /**
     * Retrieve the {@code DashboardState} with the {@code DashboardId} from the data store.
     * 
     * @param id unique identifier of the {@code DashboardState} object to retrieve
     * @return the {@code DashboardState} object corresponding to the {@code DashboardId}
     * @throws DashboardNotFoundException thrown if there is no {@code DashboardState} object with the given {@code id}
     * @throws DashboardStateStoreException thrown if there is a problem when retrieving the {@code DashboardState}
     *                                      from the persistent store
     */
    DashboardState retrieve(DashboardId id) throws DashboardNotFoundException, DashboardStateStoreException;

    /**
     * Update the stored {@code DashboardState} in the persistent data store, using the {@code changes} passed in.
     *
     * @param dashboardState the {@code DashboardState} to update in the persistent data store
     * @param changes the changes to the dashboard that should be applied
     * @return the actual stored {@code DashboardState} so that it can be verified that the state we tried to store
     *         is the actual state that is in the store
     * @throws DashboardStateStoreException thrown if there is a problem when updating the {@code DashboardState} in
     *                                      the persistent data store
     */
    DashboardState update(DashboardState dashboardState, Iterable<DashboardChange> changes) throws DashboardStateStoreException;
    
    /**
     * Removes the {@code DashboardState} identified by the {@code DashboardId} from the persistent data store.
     * 
     * @param id unique identifier of the {@code DashboardState} to be removed from the persistent data store
     * @throws DashboardStateStoreException thrown if there is a problem when removing the {@code DashboardState} from
     *                                      the persistent data store
     */
    void remove(DashboardId id) throws DashboardStateStoreException;

    /**
     * Retrieve the {@code DashboardState} for the dashboard containing the gadget whose identifier is {@code gadgetID}
     * @param gadgetId the identifier of the gadget
     * @return the {@code DashboardState} object for the dashboard containing the gadget
     * @throws DashboardNotFoundException thrown if there is a problem retrieving the {@code DashboardState}
     *                                    from the persistent store
     */
    DashboardState findDashboardWithGadget(GadgetId gadgetId) throws DashboardNotFoundException;
}
