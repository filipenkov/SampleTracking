package com.atlassian.jira.dev.reference.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.collect.Lists;

import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;


public class RefEntityServiceImpl implements RefEntityService
{
    private final ActiveObjects ao;

    public RefEntityServiceImpl(ActiveObjects ao)
    {
        this.ao = checkNotNull(ao);
    }

    @Override
    public RefEntity add(String description)
    {
        final RefEntity entity = ao.create(RefEntity.class);
        entity.setDescription(checkNotNull(description, "Description should not be null"));
        entity.save();
        return entity;
    }

    @Override
    public List<RefEntity> allEntities()
    {
        return Lists.newArrayList(ao.find(RefEntity.class));
    }
}
