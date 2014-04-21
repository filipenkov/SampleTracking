package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cache.GoogleCacheInstruments;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MINUTES;

@EventComponent
public class DefaultUserPropertyManager implements UserPropertyManager, Startable
{
    // cache of name -> propertyset for exclusive access
    protected final Cache<String, PropertySet> psCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(30, MINUTES)
            .build(CacheLoader.from(new CreatePropertySetFunction()));

    private static final String ENTITY_TYPE = "ExternalEntity";
    private final ExternalEntityStore externalEntityStore;
    private final OfBizConnectionFactory ofBizConnectionFactory = new DefaultOfBizConnectionFactory();

    public DefaultUserPropertyManager(ExternalEntityStore externalEntityStore)
    {
        this.externalEntityStore = externalEntityStore;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        // This should fix the random failures of the TestUserAvatar selenium tests (also see JRADEV-4532 for more info about this).
        ((CachingExternalEntityStore) ComponentManager.getComponentInstanceOfType(ExternalEntityStore.class)).onClearCache(null);

        psCache.invalidateAll();
    }

    @Override
    public void start() throws Exception
    {
        new GoogleCacheInstruments(getClass().getSimpleName()).addCache(psCache).install();
    }

    public PropertySet getPropertySet(User user)
    {
        Assertions.notNull("user", user);

        final String lowercaseName = IdentifierUtils.toLowerCase(user.getName());
        try
        {
            return psCache.get(lowercaseName);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException("Error creating property set for user: " + lowercaseName, e);
        }
    }

    private class CreatePropertySetFunction implements Function<String, PropertySet>
    {
        @Override
        public PropertySet apply(String name)
        {
            Long id = externalEntityStore.createIfDoesNotExist(name);

            final Map<String, Object> args = new HashMap<String, Object>();
            args.put("entityId", id);
            args.put("entityName", ENTITY_TYPE);
            args.put("delegator.name", ofBizConnectionFactory.getDelegatorName());

            final PropertySet basePs = PropertySetManager.getInstance("ofbiz", args);

            args.clear();
            args.put("PropertySet", basePs);
            PropertySet ps = PropertySetManager.getInstance("cached", args);

            return ps;
        }
    }
}
