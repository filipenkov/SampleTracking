package com.atlassian.jira.issue.link;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;

public class DefaultIssueLinkTypeManager implements IssueLinkTypeManager, Startable
{
    private final OfBizDelegator delegator;
    private final EventPublisher eventPublisher;
    private final Map<Long, GenericValue> cache = Collections.synchronizedMap(new LinkedHashMap<Long, GenericValue>());

    public DefaultIssueLinkTypeManager(final OfBizDelegator delegator, final EventPublisher eventPublisher)
    {
        this.delegator = delegator;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCache();
    }

    public void createIssueLinkType(final String name, final String outward, final String inward, final String style)
    {
        // Ensure all parameters are set
        try
        {
            notBlank("name", name);
            notBlank("outward", outward);
            notBlank("inward", inward);
            delegator.createValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build(IssueLinkType.NAME_FIELD_NAME, name,
                IssueLinkType.OUTWARD_FIELD_NAME, outward, IssueLinkType.INWARD_FIELD_NAME, inward, IssueLinkType.STYLE_FIELD_NAME, style));
        }
        finally
        {
            clearCache();
        }
    }

    public IssueLinkType getIssueLinkType(final Long id)
    {
        return getIssueLinkType(id, true);
    }

    @Override
    public IssueLinkType getIssueLinkType(Long id, boolean excludeSystemLinks)
    {
        if (cache.isEmpty())
        {
            getIssueLinkTypes(excludeSystemLinks);
        }

        return buildIssueLinkType(cache.get(id));
    }

    public Collection getIssueLinkTypesByName(final String name)
    {
        return buildIssueLinkTypes(queryDatabase(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build(IssueLinkType.NAME_FIELD_NAME, name)), false);
    }

    public Collection<IssueLinkType> getIssueLinkTypesByInwardDescription(final String desc)
    {
        final Predicate<GenericValue> inwardNamePredicate = new Predicate<GenericValue>()
        {
            public boolean evaluate(final GenericValue input)
            {
                return input.getString(IssueLinkType.INWARD_FIELD_NAME).equalsIgnoreCase(desc);
            }
        };
        return getIssueLinkTypesByPredicate(inwardNamePredicate);
    }

    public Collection<IssueLinkType> getIssueLinkTypesByOutwardDescription(final String desc)
    {
        final Predicate<GenericValue> inwardNamePredicate = new Predicate<GenericValue>()
        {
            public boolean evaluate(final GenericValue input)
            {
                return input.getString(IssueLinkType.OUTWARD_FIELD_NAME).equalsIgnoreCase(desc);
            }
        };
        return getIssueLinkTypesByPredicate(inwardNamePredicate);
    }

    private Collection<IssueLinkType> getIssueLinkTypesByPredicate(final Predicate<GenericValue> predicate)
    {
        final List<GenericValue> inwardLinkTypes = CollectionUtil.toList(CollectionUtil.filter(
                queryDatabase(OfBizDelegator.ISSUE_LINK_TYPE, MapBuilder.<String, Object>emptyMap()),
                predicate));

        return buildIssueLinkTypes(inwardLinkTypes, false);
    }

    public Collection getIssueLinkTypesByStyle(final String style)
    {
        return buildIssueLinkTypes(queryDatabase(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build(IssueLinkType.STYLE_FIELD_NAME, style)), false);
    }

    public void updateIssueLinkType(final IssueLinkType issueLinkType, final String name, final String outward, final String inward)
    {
        try
        {
            issueLinkType.setName(name);
            issueLinkType.setOutward(outward);
            issueLinkType.setInward(inward);
            issueLinkType.store();
        }
        finally
        {
            clearCache();
        }
    }

    public void removeIssueLinkType(final Long issueLinkTypeId)
    {
        try
        {
            deleteFromDatabase(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("id", issueLinkTypeId));
        }
        finally
        {
            clearCache();
        }
    }

    /**
     * Find only the user defined link types
     *
     */
    public Collection<IssueLinkType> getIssueLinkTypes()
    {
        return this.getIssueLinkTypes(true);
    }

    @Override
    public Collection<IssueLinkType> getIssueLinkTypes(boolean excludeSystemLinks)
    {
        Collection<GenericValue> result = cache.values();

        if (result.isEmpty())
        {
            result = delegator.findAll(OfBizDelegator.ISSUE_LINK_TYPE, EasyList.build("linkname" + " ASC"));

            if ((result != null) && !result.isEmpty())
            {
                for (final GenericValue gv : result)
                {
                    cache.put(gv.getLong("id"), gv);
                }
            }
        }

        if (result == null)
        {
            return Collections.emptyList();
        }
        return buildIssueLinkTypes(result, excludeSystemLinks);
    }

    private List<IssueLinkType> buildIssueLinkTypes(final Collection<GenericValue> issueLinkTypeGVs, final boolean excludeSystemLinks)
    {
        final List<IssueLinkType> issueLinkTypes = new ArrayList<IssueLinkType>();
        for (final GenericValue issueLinkTypeGV : issueLinkTypeGVs)
        {
            final IssueLinkType ilt = buildIssueLinkType(issueLinkTypeGV);
            if (!excludeSystemLinks || !ilt.isSystemLinkType())
            {
                issueLinkTypes.add(ilt);
            }
        }
        return issueLinkTypes;
    }

    private void clearCache()
    {
        cache.clear();
    }

    private IssueLinkType buildIssueLinkType(final GenericValue linkTypeGV)
    {
        return new IssueLinkType(linkTypeGV);
    }

    private List<GenericValue> queryDatabase(final String entityName, final Map<String, Object> criteria)
    {
        return queryDatabase(entityName, criteria, Collections.EMPTY_LIST);
    }

    private List<GenericValue> queryDatabase(final String entityName, final Map<String, Object> criteria, final List<String> sortOrder)
    {
        return delegator.findByAnd(entityName, criteria, sortOrder);
    }

    private void deleteFromDatabase(final String entityName, final Map<String, ?> criteria)
    {
        // Delete the link type from the database
        delegator.removeByAnd(entityName, criteria);
    }
}
