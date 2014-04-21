package com.atlassian.jira.dashboard;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStore;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStoreException;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import static com.atlassian.jira.dashboard.DashboardUtil.toLong;

/**
 * Provides CRUD operations for dashboards.  Uses the existing {@link com.atlassian.jira.portal.PortalPageStore} and
 * {@link com.atlassian.jira.portal.PortletConfigurationStore} implementations. Note that this class does not need to do
 * any permission checks, since this is the responsibility of the {@link com.atlassian.jira.dashboard.permission.JiraPermissionService}.
 *
 * @since v4.0
 */
public class JiraDashboardStateStoreManager implements DashboardStateStore
{
    private static final Logger log = Logger.getLogger(JiraDashboardStateStoreManager.class);

    private final PortalPageStore portalPageStore;
    private final PortletConfigurationStore portletConfigurationStore;
    private final PortalPageManager portalPageManager;

    private final Function<DashboardId, ManagedLock.ReadWrite> lockFactory = ManagedLocks.weakReadWriteManagedLockFactory(new Function<DashboardId, Long>()
    {
        public Long get(final DashboardId dashboardId)
        {
            return toLong(dashboardId);
        }
    });

    public JiraDashboardStateStoreManager(final PortalPageStore portalPageStore, final PortletConfigurationStore portletConfigurationStore,
            final PortalPageManager portalPageManager)
    {
        this.portalPageStore = portalPageStore;
        this.portletConfigurationStore = portletConfigurationStore;
        this.portalPageManager = portalPageManager;
    }

    public DashboardState retrieve(final DashboardId dashboardId)
            throws DashboardNotFoundException, DashboardStateStoreException
    {
        Assertions.notNull("dashboardId", dashboardId);

        //Reading the dashboard should be done under a striped readlock (by dashboardId) to ensure
        //a consistent state is read from the DB and not some 'half-written' state
        return lockFactory.get(dashboardId).read().withLock(new Supplier<DashboardState>()
        {
            public DashboardState get()
            {
                final Long portalPageId = toLong(dashboardId);
                try
                {
                    final PortalPage portalPage = portalPageStore.getPortalPage(portalPageId);
                    if (portalPage == null)
                    {
                        throw new DashboardNotFoundException(dashboardId);
                    }

                    final List<List<PortletConfiguration>> pcColumns = portalPageManager.getPortletConfigurations(portalPageId);

                    //convert the JIRA portalPage to a DashboardState/GadgetState class
                    final List<List<GadgetState>> dashboardColumns = new ArrayList<List<GadgetState>>();

                    for (List<PortletConfiguration> pcColumn : pcColumns)
                    {
                        //init the column
                        final ArrayList<GadgetState> column = new ArrayList<GadgetState>();
                        for (PortletConfiguration portletConfiguration : pcColumn)
                        {
                            column.add(toGadgetState.get(portletConfiguration));
                        }
                        dashboardColumns.add(column);
                    }

                    return dashboard(dashboardId).
                            title(portalPage.getName()).
                            version(portalPage.getVersion() == null ? 1L : portalPage.getVersion()).
                            columns(dashboardColumns).
                            layout(portalPage.getLayout()).build();
                }
                catch (DataAccessException e)
                {
                    throw new DashboardStateStoreException("Unknown error occurred while retrieving dashboard with id '" + portalPageId + "'.", e);
                }
            }
        });
    }

    public DashboardState update(final DashboardState dashboardState, final Iterable<DashboardChange> dashboardChangeIterable)
            throws DashboardStateStoreException
    {
        Assertions.notNull("dashboardState", dashboardState);
        Assertions.notNull("dashboardChangeIterable", dashboardChangeIterable);

        final DashboardId dashboardId = dashboardState.getId();
        try
        {
            //Writing the dashboard should be done under a striped writeLock (by dashboardId) to ensure
            //a consistent state is written to the DB and to ensure all threads trying to read this dashboard will
            //block
            return lockFactory.get(dashboardId).write().withLock(new Supplier<DashboardState>()
            {
                public DashboardState get()
                {
                    //optimistic lock solution.  The *very first* thing that needs to happen is for the
                    //dashboard version to be updated.  If this fails, then we need to throw an exception.
                    final boolean optimisticLock = portalPageStore.updatePortalPageOptimisticLock(toLong(dashboardId), dashboardState.getVersion());
                    if (!optimisticLock)
                    {
                        //looks like the optimistic lock (i.e. version) for this dashboard was already out of date.
                        throw new DashboardStateStoreException("Dashboard with id '" + dashboardId + "' is out of sync with the currently persisted state.");
                    }

                    //if no specific changes were submitted along, persist the entire dashboard state
                    if (!dashboardChangeIterable.iterator().hasNext())
                    {
                        return storeDashboardState(dashboardState);
                    }

                    new JiraDashboardChangeVisitor(dashboardState, portletConfigurationStore, portalPageStore).accept(dashboardChangeIterable);

                    final DashboardState storedState = retrieve(dashboardId);
                    //this should really never happen, however just in case updating via {@link com.atlassian.gadgets.spi.changes.DashboardChange}s
                    //doesn't work, try rewriting the entire dashboard state from scratch!
                    if (!storedState.equals(dashboardState))
                    {
                        log.warn("Stored state for dashboard with id '" + dashboardId + "' is not the same as in memory state.  Trying to rewrite the entire state...");
                        return storeDashboardState(dashboardState);
                    }
                    return storedState;
                }
            });
        }
        catch (DataAccessException e)
        {
            throw new DashboardStateStoreException("Error updating dashboard state with id '" + dashboardId + "'.", e);
        }
    }

    public void remove(final DashboardId dashboardId) throws DashboardStateStoreException
    {
        Assertions.notNull("dashboardId", dashboardId);

        final Long portalPageId = toLong(dashboardId);
        try
        {
            //Removing the dashboard should be done under a striped writeLock (by dashboardId) to ensure
            // all threads trying to read this dashboard will block
            lockFactory.get(dashboardId).write().withLock(new Runnable()
            {
                public void run()
                {
                    portalPageManager.delete(portalPageId);
                }
            });
        }
        catch (DataAccessException e)
        {
            throw new DashboardStateStoreException("Error removing dashboard state with id'" + dashboardId + "'.", e);
        }
    }

    public DashboardState findDashboardWithGadget(final GadgetId gadgetId) throws DashboardNotFoundException
    {
        Assertions.notNull("gagdetId", gadgetId);

        try
        {
            final PortletConfiguration portletConfiguration = portletConfigurationStore.getByPortletId(toLong(gadgetId));
            if (portletConfiguration != null)
            {
                return retrieve(DashboardId.valueOf(Long.toString(portletConfiguration.getDashboardPageId())));
            }
            else
            {
                throw new DashboardStateStoreException("Gadget with id '" + gadgetId + "' not found!");
            }
        }
        catch (DataAccessException e)
        {
            throw new DashboardStateStoreException("Error looking up gadget with id '" + gadgetId + "'.", e);
        }
    }

    private DashboardState storeDashboardState(final DashboardState dashboardState)
    {
        Assertions.notNull("dashboardState", dashboardState);

        final DashboardId dashboardId = dashboardState.getId();
        final long portalPageId = toLong(dashboardId);

        //check first if the portal page exists!
        final PortalPage portalPage = portalPageStore.getPortalPage(portalPageId);
        if (portalPage == null)
        {
            throw new DashboardStateStoreException("No portal page found with id '" + portalPageId + "'");
        }
        updatePortalPage(portalPage, dashboardState);

        final Map<Long, PortletConfiguration> oldPortlets = getCurrentPortletConfigurationsMap(portalPageId);
        for (DashboardState.ColumnIndex columnIndex : dashboardState.getLayout().getColumnRange())
        {
            int row = 0;
            for (final GadgetState gadgetState : dashboardState.getGadgetsInColumn(columnIndex))
            {
                final long gadgetId = toLong(gadgetState.getId());
                //update existing portlets
                if (oldPortlets.containsKey(gadgetId))
                {
                    final PortletConfiguration oldPortletConfiguration = oldPortlets.get(gadgetId);
                    oldPortletConfiguration.setColumn(columnIndex.index());
                    oldPortletConfiguration.setRow(row++);
                    oldPortletConfiguration.setColor(gadgetState.getColor());
                    oldPortletConfiguration.setUserPrefs(gadgetState.getUserPrefs());
                    portletConfigurationStore.store(oldPortletConfiguration);
                    oldPortlets.remove(gadgetId);
                }
                else
                {
                    portletConfigurationStore.addGadget(
                            portalPageId, gadgetId, columnIndex.index(), row++, gadgetState.getGadgetSpecUri(), gadgetState.getColor(),
                            gadgetState.getUserPrefs());
                }
            }
        }

        //delete any portlets left over in the oldPortlets map
        for (PortletConfiguration existingPortlet : oldPortlets.values())
        {
            portletConfigurationStore.delete(existingPortlet);
        }

        return retrieve(dashboardId);
    }

    private void updatePortalPage(final PortalPage portalPage, final DashboardState dashboardState)
    {
        //update the portalPageStore's title and layout if they changed.
        if (!portalPage.getLayout().equals(dashboardState.getLayout()) ||
                !StringUtils.equals(portalPage.getName(), dashboardState.getTitle()))
        {
            final PortalPage.Builder builder = PortalPage.portalPage(portalPage);
            builder.name(dashboardState.getTitle());
            builder.layout(dashboardState.getLayout());
            portalPageStore.update(builder.build());
        }
    }

    private Map<Long, PortletConfiguration> getCurrentPortletConfigurationsMap(final Long portalPageId)
    {
        final Map<Long, PortletConfiguration> ret = new HashMap<Long, PortletConfiguration>();
        final List<PortletConfiguration> list = portletConfigurationStore.getByPortalPage(portalPageId);
        for (PortletConfiguration portletConfiguration : list)
        {
            ret.put(portletConfiguration.getId(), portletConfiguration);
        }
        return ret;
    }

    /**
     * Converts a PortletConfiguration to a GadgetState.
     */
    private final Function<PortletConfiguration, GadgetState> toGadgetState = new Function<PortletConfiguration, GadgetState>()
    {
        public GadgetState get(final PortletConfiguration portletConfiguration)
        {
            URI gadgetUri = portletConfiguration.getGadgetURI();
            if (gadgetUri == null)
            {
                gadgetUri = URI.create("/invalid/legacy/portlet/Please_remove_this_gadget_from_your_dashboard!");
            }

            return GadgetState.gadget(GadgetId.valueOf(portletConfiguration.getId().toString())).
                    specUri(gadgetUri).
                    color(portletConfiguration.getColor()).
                    userPrefs(portletConfiguration.getUserPrefs()).
                    build();
        }
    };
}
