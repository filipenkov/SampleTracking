package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.Vote;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.dashboard.permission.GadgetPermissionManager;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.util.dbc.Assertions;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.not;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The base class for PortalPageManager implementations
 *
 * @since v3.13
 */
public class DefaultPortalPageManager implements PortalPageManager
{
    private final ShareManager shareManager;
    private final PortalPageStore portalPageStore;
    private final PortletAccessManager portletAccessManager;
    private final PortletConfigurationManager portletConfigurationManager;
    private final SharedEntityIndexer indexer;
    private GadgetPermissionManager gadgetPermissionManager;

    /**
     * A resolver that can set permissions
     */
    private final Resolver<PortalPage, PortalPage> permissionResolver = new Resolver<PortalPage, PortalPage>()
    {
        public PortalPage get(final PortalPage portalPage)
        {
            return setRelatedState(portalPage);
        }
    };

    public DefaultPortalPageManager(final ShareManager shareManager, final PortalPageStore portalPageStore, final PortletAccessManager portletAccessManager, final PortletConfigurationManager portletConfigurationManager, final SharedEntityIndexer indexer)
    {
        this.shareManager = shareManager;
        this.portalPageStore = portalPageStore;
        this.portletAccessManager = portletAccessManager;
        this.portletConfigurationManager = portletConfigurationManager;
        this.indexer = indexer;
    }

    public TypeDescriptor<PortalPage> getType()
    {
        return PortalPage.ENTITY_TYPE;
    }

    public void adjustFavouriteCount(final SharedEntity entity, final int adjustmentValue)
    {
        notNull("entity", entity);
        Assertions.equals("PortalPage type", PortalPage.ENTITY_TYPE, entity.getEntityType());

        final PortalPage portalPage = portalPageStore.adjustFavouriteCount(entity, adjustmentValue);

        indexer.index(setRelatedState(portalPage)).await();
    }

    public PortalPage getSharedEntity(final Long entityId)
    {
        notNull("entityId", entityId);
        return getPortalPageById(entityId);
    }

    public PortalPage getSharedEntity(final User user, final Long entityId)
    {
        notNull("entityId", entityId);
        return getPortalPage(user, entityId);
    }

    public boolean hasPermissionToUse(final User user, final PortalPage portalPage)
    {
        notNull("portalPage", portalPage);
        return portalPage.isSystemDefaultPortalPage() || isSharedWith(portalPage, user);
    }

    /*
     * =============================================== GET/ FIND ===============================================
     */

    public EnclosedIterable<PortalPage> getAll()
    {
        return Transformed.enclosedIterable(portalPageStore.getAll(), permissionResolver);
    }

    public EnclosedIterable<SharedEntity> getAllIndexableSharedEntities()
    {
        @SuppressWarnings("unchecked")
        final EnclosedIterable<SharedEntity> all = (EnclosedIterable) getAll();
        return all;
    }

    public EnclosedIterable<PortalPage> get(final RetrievalDescriptor descriptor)
    {
        return Transformed.enclosedIterable(portalPageStore.get(descriptor), permissionResolver);
    }

    // NOTE: we don't care about the searching user here, as we don't need to sanitise or filter the results.
    public EnclosedIterable<PortalPage> get(final User user, final RetrievalDescriptor ids)
    {
        return get(ids);
    }

    public Collection<PortalPage> getAllOwnedPortalPages(final User owner)
    {
        notNull("owner", owner);
        notNull("owner.name", owner.getName());
        final Collection<PortalPage> portalPages = portalPageStore.getAllOwnedPortalPages(owner);
        if (portalPages == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return setRelatedState(portalPages);
        }
    }

    @Override
    public Collection<PortalPage> getAllOwnedPortalPages(com.opensymphony.user.User owner)
    {
        return getAllOwnedPortalPages((User) owner);
    }

    public PortalPage getPortalPageByName(final User owner, final String pageName)
    {
        notNull("owner", owner);
        notNull("owner,name", owner.getName());
        notNull("pageName", pageName);

        final PortalPage portalPage = portalPageStore.getPortalPageByOwnerAndName(owner, pageName);
        return setRelatedState(portalPage);
    }

    @Override
    public PortalPage getPortalPageByName(com.opensymphony.user.User owner, String pageName)
    {
        return getPortalPageByName((User) owner, pageName);
    }

    public PortalPage getSystemDefaultPortalPage()
    {
        final PortalPage systemDefaultPortalPage = portalPageStore.getSystemDefaultPortalPage();
        return setRelatedState(systemDefaultPortalPage);
    }

    public PortalPage getPortalPage(final User user, final Long id)
    {
        notNull("id", id);

        final PortalPage portalPage = portalPageStore.getPortalPage(id);
        if (portalPage == null)
        {
            return null;
        }
        // the System Default Dashboard is a special page and can be
        // shown to anyone including the Anonymous (null) user
        if (!hasPermissionToUse(user, portalPage))
        {
            return null;
        }
        return setRelatedState(portalPage);
    }

    @Override
    public PortalPage getPortalPage(com.opensymphony.user.User user, Long portalPageId)
    {
        return getPortalPage((User) user, portalPageId);
    }

    public PortalPage getPortalPageById(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);

        final PortalPage portalPage = portalPageStore.getPortalPage(portalPageId);
        return setRelatedState(portalPage);
    }

    /*
     * =============================================== CRUD ===============================================
     */
    public PortalPage create(final PortalPage portalPage)
    {
        assertCreate(portalPage);

        final PortalPage createdPortalPage = PortalPage.portalPage(portalPageStore.create(portalPage)).permissions(portalPage.getPermissions()).build();

        shareManager.updateSharePermissions(createdPortalPage);
        indexer.index(createdPortalPage).await();
        return createdPortalPage;
    }

    public PortalPage createBasedOnClone(final User pageOwner, final PortalPage portalPage, final PortalPage clonePortalPage)
    {
        assertCreate(portalPage);
        notNull("clonePortalPage", clonePortalPage);

        //need to set the layout here since it determines the number of columns for the new portal page.  (JRA-16991)
        final PortalPage portalPageToCreate = PortalPage.portalPage(portalPage).layout(clonePortalPage.getLayout()).build();

        final PortalPage newPortalPage = create(portalPageToCreate);
        clonePortletsFromOnePageToAnother(pageOwner, clonePortalPage, newPortalPage);
        return newPortalPage;
    }

    @Override
    public PortalPage createBasedOnClone(com.opensymphony.user.User pageOwner, PortalPage portalPage, PortalPage clonePortalPage)
    {
        return createBasedOnClone((User) pageOwner, portalPage, clonePortalPage);
    }

    public PortalPage update(final PortalPage portalPage)
    {
        assertCreate(portalPage);
        notNull("portalPage.id", portalPage.getId());

        final PortalPage newPortalPage = PortalPage.portalPage(portalPageStore.update(portalPage)).permissions(portalPage.getPermissions()).build();
        shareManager.updateSharePermissions(newPortalPage);
        indexer.index(newPortalPage).await();
        return newPortalPage;
    }

    public void deleteAllPortalPagesForUser(final User user)
    {
        final Collection<PortalPage> ownedPortalPages = getAllOwnedPortalPages(user);

        for (final PortalPage portalPage : ownedPortalPages)
        {
            delete(portalPage.getId());
        }
    }

    public void delete(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);
        //
        // delete all PortletConfigs associated with the page first. The underlying portletConfigurationManager/Store
        // also cleans associated propertySets
        //
        final List<PortletConfiguration> portlectConfigurations = portletConfigurationManager.getByPortalPage(portalPageId);
        for (final PortletConfiguration portletConfiguration : portlectConfigurations)
        {
            portletConfigurationManager.delete(portletConfiguration);
        }

        final SharedEntity indentifier = new SharedEntity.Identifier(portalPageId, PortalPage.ENTITY_TYPE, (String) null);

        portalPageStore.delete(portalPageId);
        shareManager.deletePermissions(indentifier);
        indexer.deIndex(indentifier).await();
    }

    public void saveLegacyPortletConfiguration(final PortletConfiguration portletConfiguration)
    {
        notNull("portletConfiguration", portletConfiguration);

        //check the PC exists!
        final Long id = portletConfiguration.getId();
        final PortletConfiguration pc = portletConfigurationManager.getByPortletId(id);
        if (pc != null)
        {
            portletConfigurationManager.store(portletConfiguration);
        }
        else
        {
            throw new IllegalStateException("Trying to update portletConfiguration that doesn't exist with id '" + id + "'.");
        }
    }

    public SharedEntitySearchResult<PortalPage> search(final SharedEntitySearchParameters searchParameters, final User user, final int pagePosition, final int pageWidth)
    {
        notNull("searchParameters", searchParameters);
        not("pagePosition < 0", pagePosition < 0);
        not("pageWidth <= 0", pageWidth <= 0);

        return indexer.getSearcher(PortalPage.ENTITY_TYPE).search(searchParameters, user, pagePosition, pageWidth);
    }

    @Override
    public SharedEntitySearchResult<PortalPage> search(SharedEntitySearchParameters searchParameters, com.opensymphony.user.User user, int pagePosition, int pageWidth)
    {
        return search(searchParameters, (User) user, pagePosition, pageWidth);
    }

    public List<List<PortletConfiguration>> getPortletConfigurations(final Long portalPageId)
    {
        final List<List<PortletConfiguration>> columns = new ArrayList<List<PortletConfiguration>>();
        final List<PortletConfiguration> portletConfigurations = portletConfigurationManager.getByPortalPage(portalPageId);
        final PortalPage portalPage = getPortalPageById(portalPageId);
        if ((portalPage != null) && !portletConfigurations.isEmpty())
        {
            initColumns(portalPage.getLayout().getNumberOfColumns(), columns);
            for (final PortletConfiguration portletConfiguration : portletConfigurations)
            {
                final int column = portletConfiguration.getColumn();
                columns.get(column).add(portletConfiguration);
            }

            //Once all portlet configs are inserted, sort each column then make it an unmodifieable list.
            for (int i = 0; i < columns.size(); i++)
            {
                final List<PortletConfiguration> column = columns.get(i);
                Collections.sort(column);
                columns.set(i, Collections.<PortletConfiguration> unmodifiableList(column));
            }
        }
        return Collections.unmodifiableList(columns);
    }

    private void initColumns(final int numberOfColumns, final List<List<PortletConfiguration>> columns)
    {
        for (int i = 0; i < numberOfColumns; i++)
        {
            columns.add(new ArrayList<PortletConfiguration>());
        }
    }

    private void clonePortletsFromOnePageToAnother(final User owner, final PortalPage clonePortalPage, final PortalPage targetPortalPage)
    {
        notNull("owner", owner);
        notNull("clonePortalPage", clonePortalPage);
        notNull("targetPortalPage", targetPortalPage);

        final List<PortletConfiguration> pcsToClone = portletConfigurationManager.getByPortalPage(clonePortalPage.getId());
        for (final PortletConfiguration pc : pcsToClone)
        {
            //if it's a legacy portlet, copy it the old way, including its propertyset.
            if (StringUtils.isNotBlank(pc.getKey()))
            {
                if (portletAccessManager.canUserSeePortlet(owner, pc.getKey()))
                {
                    final PortletConfiguration newPortletConfiguration = portletConfigurationManager.addLegacyPortlet(targetPortalPage.getId(),
                        pc.getKey(), pc.getColumn(), pc.getRow());
                    copyConfigurationPropertySet(pc, newPortletConfiguration);
                }
            }
            else
            {
                final String key = getGadgetPermissionManager().extractModuleKey(pc.getGadgetURI().toASCIIString());
                if ((key == null) || getGadgetPermissionManager().voteOn(key, owner).equals(Vote.ALLOW))
                {
                    portletConfigurationManager.addGadget(targetPortalPage.getId(), pc.getColumn(), pc.getRow(), pc.getGadgetURI(), pc.getColor(),
                        pc.getUserPrefs());
                }
            }
        }
    }

    // Gotten like this to avoid cyclic dep
    GadgetPermissionManager getGadgetPermissionManager()
    {
        if (gadgetPermissionManager == null)
        {
            gadgetPermissionManager = ComponentManager.getComponentInstanceOfType(GadgetPermissionManager.class);
        }
        return gadgetPermissionManager;
    }

    /**
     * Copies the source PropertySet into the target PropertySet and stores it to the database.
     *
     * @param srcConfiguration the source configuration to copy.
     * @param targetConfiguration the target configuration to copy.
     */
    private void copyConfigurationPropertySet(final PortletConfiguration srcConfiguration, final PortletConfiguration targetConfiguration)
    {
        try
        {
            final PropertySet targetProperties = targetConfiguration.getProperties();
            final PropertySet srcProperties = srcConfiguration.getProperties();
            @SuppressWarnings("unchecked")
            final Collection<String> keySet = srcProperties.getKeys();
            for (final String key : keySet)
            {
                final int type = srcProperties.getType(key);
                if (PropertySet.STRING == type)
                {
                    targetProperties.setString(key, srcProperties.getString(key));
                }
                else if (PropertySet.TEXT == type)
                {
                    targetProperties.setText(key, srcProperties.getText(key));
                }
                else
                {
                    targetProperties.setAsActualType(key, srcProperties.getAsActualType(key));
                }
            }
            portletConfigurationManager.store(targetConfiguration);
        }
        catch (final ObjectConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean isSharedWith(final PortalPage entity, final User user)
    {
        return shareManager.hasPermission(user, entity);
    }

    private Collection<PortalPage> setRelatedState(final Collection<PortalPage> portalPages)
    {
        final Collection<PortalPage> ret = new ArrayList<PortalPage>(portalPages.size());
        for (final PortalPage portalPage : portalPages)
        {
            ret.add(setRelatedState(portalPage));
        }
        return ret;
    }

    private PortalPage setRelatedState(final PortalPage portalPage)
    {
        if (portalPage != null)
        {
            final PortalPage.Builder builder = PortalPage.portalPage(portalPage);
            //the system default dashboard should always have the global permission
            if (portalPage.isSystemDefaultPortalPage())
            {
                builder.permissions(SharePermissions.GLOBAL);
            }
            else
            {
                builder.permissions(shareManager.getSharePermissions(portalPage));
            }
            return builder.build();
        }
        return null;
    }

    private void assertCreate(final PortalPage portalPage)
    {
        notNull("portalPage", portalPage);
        if (!portalPage.isSystemDefaultPortalPage())
        {
            notNull("portalPage.owner.name", portalPage.getOwnerUserName());
        }
        notNull("portalPage.pageName", portalPage.getName());
    }
}