package com.atlassian.jira.portal;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.DatabaseIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.ofbiz.PagedDatabaseIterable;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The PortalPageStore implementation that uses OfBiz
 *
 * @since v3.13
 */
public class OfBizPortalPageStore implements PortalPageStore
{
    public static final class Table
    {
        public static final String NAME = PortalPage.ENTITY_TYPE.getName();
    }

    public static final class Column
    {
        public static final String PAGENAME = "pagename";
        public static final String VERSION = "version";
        public static final String DESCRIPTION = "description";
        public static final String USERNAME = "username";
        public static final String FAVCOUNT = "favCount";
        public static final String LAYOUT = "layout";
        public static final String ID = "id";
    }

    private static final List<String> DEFAULT_ORDER_BY_CLAUSE = Collections.singletonList(Column.PAGENAME + " ASC");

    private final OfBizDelegator delegator;
    private final Resolver<GenericValue, PortalPage> portalPageResolver = new Resolver<GenericValue, PortalPage>()
    {
        public PortalPage get(final GenericValue input)
        {
            return gvToPortalPage(input);
        }
    };

    public OfBizPortalPageStore(final OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    public EnclosedIterable<PortalPage> get(final RetrievalDescriptor descriptor)
    {
        // if order is important provide a key resolver
        final Resolver<PortalPage, Long> keyResolver = (descriptor.preserveOrder()) ? new Resolver<PortalPage, Long>()
        {
            public Long get(final PortalPage input)
            {
                return input.getId();
            }
        } : null;
        return new PagedDatabaseIterable<PortalPage, Long>(descriptor.getIds(), keyResolver)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                return delegator.findListIteratorByCondition(Table.NAME, new EntityExpr(Column.ID, EntityOperator.IN, ids));
            }

            @Override
            protected Resolver<GenericValue, PortalPage> getResolver()
            {
                return portalPageResolver;
            }
        };
    }

    public EnclosedIterable<PortalPage> getAll()
    {
        try
        {
            // order is not important
            return new DatabaseIterable<PortalPage>(new Long(delegator.getCount(Table.NAME)).intValue(), portalPageResolver)
            {
                @Override
                protected OfBizListIterator createListIterator()
                {
                    return delegator.findListIteratorByCondition(Table.NAME, null);
                }
            };
        }
        catch (final DataAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param owner the User who is the owner of the {@link com.atlassian.jira.portal.PortalPage}s
     * @return a NON NULL Collection of PortalPage objects
     */
    public Collection<PortalPage> getAllOwnedPortalPages(final User owner)
    {
        Assertions.notNull("owner", owner);
        Assertions.notNull("owner.username", owner.getName());

        final Collection<GenericValue> gvs = delegator.findByAnd(Table.NAME, EasyMap.build(Column.USERNAME, owner.getName()), DEFAULT_ORDER_BY_CLAUSE);
        return gvToPortalPages(gvs);
    }

    public PortalPage getPortalPageByOwnerAndName(final User owner, final String name)
    {
        Assertions.notNull("owner", owner);
        Assertions.notNull("owner.username", owner.getName());

        final GenericValue pageGV = EntityUtil.getOnly(delegator.findByAnd(Table.NAME, EasyMap.build(Column.USERNAME, owner.getName(),
            Column.PAGENAME, name), DEFAULT_ORDER_BY_CLAUSE));
        return gvToPortalPage(pageGV);
    }

    public PortalPage getPortalPage(final Long portalPageId)
    {
        final GenericValue gv = findByPrimaryKey(portalPageId);
        return gvToPortalPage(gv);
    }

    public PortalPage update(final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("portalPage.id", portalPage.getId());
        if (!portalPage.isSystemDefaultPortalPage())
        {
            Assertions.notNull("portalPage.owner.username", portalPage.getOwnerUserName());
        }

        final GenericValue gv = findByPrimaryKey(portalPage.getId());
        gv.setString(Column.PAGENAME, portalPage.getName());
        gv.setString(Column.DESCRIPTION, portalPage.getDescription());
        if (!portalPage.isSystemDefaultPortalPage())
        {
            gv.setString(Column.USERNAME, portalPage.getOwnerUserName());
        }
        else
        {
            gv.remove(Column.USERNAME);
        }
        gv.remove(Column.FAVCOUNT);
        gv.setString(Column.LAYOUT, portalPage.getLayout().name());
        delegator.store(gv);

        return getPortalPage(portalPage.getId());
    }

    public PortalPage adjustFavouriteCount(final SharedEntity portalPage, final int incrementValue)
    {
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("portalPage.id", portalPage.getId());

        final GenericValue gv = findByPrimaryKey(portalPage.getId());
        final Long currentFavcount = gv.getLong(Column.FAVCOUNT);
        long newFavcount;
        if (currentFavcount == null)
        {
            newFavcount = 0;
        }
        else
        {
            newFavcount = currentFavcount.longValue();
        }

        newFavcount += incrementValue;
        if (newFavcount < 0)
        {
            newFavcount = 0;
        }

        gv.set(Column.FAVCOUNT, newFavcount);

        delegator.store(gv);

        return getPortalPage(portalPage.getId());

    }

    public PortalPage create(final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("portalPage.name", portalPage.getName());
        Assertions.notNull("portalPage.owner.name", portalPage.getOwnerUserName());

        final GenericValue gv = delegator.createValue(Table.NAME, MapBuilder.<String, Object>newBuilder().
                add(Column.PAGENAME, portalPage.getName()).
                add(Column.DESCRIPTION, portalPage.getDescription()).
                add(Column.USERNAME, portalPage.getOwnerUserName()).
                add(Column.FAVCOUNT, 0L).
                add(Column.LAYOUT, portalPage.getLayout().name()).
                add(Column.VERSION, 0L).
                toMap());
        return gvToPortalPage(gv);
    }

    public void delete(final Long portalPageId)
    {
        Assertions.notNull("portalPageId", portalPageId);

        delegator.removeByAnd(Table.NAME, EasyMap.build(Column.ID, portalPageId));
    }

    public PortalPage getSystemDefaultPortalPage()
    {
        final GenericValue gv = EntityUtil.getOnly(delegator.findByAnd("PortalPage", EasyMap.build(Column.USERNAME, null)));
        if (gv == null) // there is no default for this page name
        {
            return null;
        }
        return gvToPortalPage(gv);
    }

    public boolean updatePortalPageOptimisticLock(final Long portalPageId, final Long currentVersion)
    {
        final Long newVersion = currentVersion + 1;
        final int updatedRowCount = delegator.bulkUpdateByAnd(
                Table.NAME,
                MapBuilder.newBuilder().add(Column.VERSION, newVersion).toMap(),
                MapBuilder.newBuilder().add(Column.VERSION, currentVersion).add(Column.ID, portalPageId).toMap());

        return updatedRowCount == 1;
    }

    public void flush()
    {
        throw new UnsupportedOperationException("Non caching store.  Nothing to flush.");
    }

    private GenericValue findByPrimaryKey(final Long portalPageId)
    {
        return delegator.findByPrimaryKey(Table.NAME, EasyMap.build(Column.ID, portalPageId));
    }

    private PortalPage gvToPortalPage(final GenericValue gv)
    {
        PortalPage portalPage = null;
        if (gv != null)
        {
            final String username = gv.getString(Column.USERNAME);
            final String layoutString = gv.getString(Column.LAYOUT);
            //legacy dashboards may not have a layout.
            final Layout layout = StringUtils.isEmpty(layoutString) ? Layout.AA : Layout.valueOf(layoutString);
            if (username != null)
            {
                portalPage = PortalPage.id(gv.getLong(Column.ID)).name(gv.getString(Column.PAGENAME)).description(gv.getString(Column.DESCRIPTION)).
                        owner(gv.getString(Column.USERNAME)).favouriteCount(gv.getLong(Column.FAVCOUNT)).layout(layout).version(gv.getLong(Column.VERSION)).build();
            }
            else
            {
                portalPage = PortalPage.id(gv.getLong(Column.ID)).name(gv.getString(Column.PAGENAME)).description(gv.getString(Column.DESCRIPTION)).
                        favouriteCount(gv.getLong(Column.FAVCOUNT)).layout(layout).version(gv.getLong(Column.VERSION)).systemDashboard().build();
            }
        }
        return portalPage;
    }

    private Collection<PortalPage> gvToPortalPages(final Collection<GenericValue> gvs)
    {
        if ((gvs == null) || gvs.isEmpty())
        {
            return Collections.emptyList();
        }
        final List<PortalPage> portalPages = new ArrayList<PortalPage>();
        for (final GenericValue gv : gvs)
        {
            portalPages.add(gvToPortalPage(gv));
        }
        return portalPages;
    }
}