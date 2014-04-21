package com.atlassian.jira.plugin;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.collect.CollectionUtil.transform;

/**
 * OfBiz implementation of the {@link PluginVersionStore}.
 *
 * @since v3.13
 */
public class OfBizPluginVersionStore implements PluginVersionStore
{
    public static final String PLUGIN_VERSION_ENTITY_NAME = "PluginVersion";
    public static final String PLUGIN_VERSION_ID = "id";
    public static final String PLUGIN_VERSION_KEY = "key";
    public static final String PLUGIN_VERSION_NAME = "name";
    public static final String PLUGIN_VERSION_VERSION = "version";
    public static final String PLUGIN_VERSION_CREATED = "created";

    private final OfBizDelegator ofBizDelegator;

    public OfBizPluginVersionStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public PluginVersion create(final PluginVersion pluginVersion)
    {
        if (pluginVersion == null)
        {
            throw new IllegalArgumentException("Can not create a plugin version record from a null PluginVersion.");
        }
        return convertFromGV(ofBizDelegator.createValue(PLUGIN_VERSION_ENTITY_NAME, convertToParams(pluginVersion)));
    }

    public PluginVersion update(final PluginVersion pluginVersion)
    {
        if ((pluginVersion == null) || (pluginVersion.getId() == null))
        {
            throw new IllegalArgumentException("You can not update a plugin version with a null id.");
        }

        // Lookup the record
        final GenericValue pluginVersionGV = ofBizDelegator.findById(PLUGIN_VERSION_ENTITY_NAME, pluginVersion.getId());
        if (pluginVersionGV == null)
        {
            throw new IllegalArgumentException("Unable to find plugin version record with id '" + pluginVersion.getId() + "'");
        }

        // Update the record
        pluginVersionGV.set(PLUGIN_VERSION_KEY, pluginVersion.getKey());
        pluginVersionGV.set(PLUGIN_VERSION_NAME, pluginVersion.getName());
        pluginVersionGV.set(PLUGIN_VERSION_VERSION, pluginVersion.getVersion());
        pluginVersionGV.set(PLUGIN_VERSION_CREATED, new Timestamp(pluginVersion.getCreated().getTime()));
        try
        {
            pluginVersionGV.store();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unable to update plugin version with id '" + pluginVersion.getId() + "'.", e);
        }

        return convertFromGV(pluginVersionGV);
    }

    public boolean delete(final Long pluginVersionId)
    {
        if (pluginVersionId == null)
        {
            throw new IllegalArgumentException("Unable to delete a plugin version with a null id.");
        }
        return ofBizDelegator.removeByAnd(PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(PLUGIN_VERSION_ID, pluginVersionId)) == 1;
    }

    public PluginVersion getById(final Long pluginVersionId)
    {
        final GenericValue pluginVersionGV = ofBizDelegator.findById(PLUGIN_VERSION_ENTITY_NAME, pluginVersionId);
        if (pluginVersionGV != null)
        {
            return convertFromGV(pluginVersionGV);
        }
        return null;
    }

    public List<PluginVersion> getAll()
    {
        return transform(ofBizDelegator.findAll(PLUGIN_VERSION_ENTITY_NAME), new Function<GenericValue, PluginVersion>()
        {
            public PluginVersion get(final GenericValue input)
            {
                return convertFromGV(input);
            }
        });
    }

    PluginVersion convertFromGV(final GenericValue pluginVersionGV)
    {
        final Long id = pluginVersionGV.getLong(PLUGIN_VERSION_ID);
        final String key = pluginVersionGV.getString(PLUGIN_VERSION_KEY);
        final String name = pluginVersionGV.getString(PLUGIN_VERSION_NAME);
        final String version = pluginVersionGV.getString(PLUGIN_VERSION_VERSION);
        final Timestamp created = pluginVersionGV.getTimestamp(PLUGIN_VERSION_CREATED);
        return new PluginVersionImpl(id, key, name, version, created);
    }

    Map<String, Object> convertToParams(final PluginVersion pluginVersion)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final Long id = pluginVersion.getId();
        if (id != null)
        {
            params.put(PLUGIN_VERSION_ID, id);
        }
        params.put(PLUGIN_VERSION_KEY, pluginVersion.getKey());
        params.put(PLUGIN_VERSION_NAME, pluginVersion.getName());
        params.put(PLUGIN_VERSION_VERSION, pluginVersion.getVersion());
        params.put(PLUGIN_VERSION_CREATED, new Timestamp(pluginVersion.getCreated().getTime()));
        return params;
    }
}
