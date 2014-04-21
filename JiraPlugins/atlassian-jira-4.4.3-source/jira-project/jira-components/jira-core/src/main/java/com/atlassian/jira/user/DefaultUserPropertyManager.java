package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.util.dbc.Assertions;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultUserPropertyManager implements UserPropertyManager, Startable
{
    protected final Map<String, PropertySet> psCache = new ConcurrentHashMap<String, PropertySet>(); // cache of name -> propertyset for exclusive access

    private static final String ENTITY_TYPE = "ExternalEntity";
    private final ExternalEntityStore externalEntityStore;
    private final EventPublisher eventPublisher;
    private final OfBizConnectionFactory ofBizConnectionFactory = new DefaultOfBizConnectionFactory();

    public DefaultUserPropertyManager(final EventPublisher eventPublisher, ExternalEntityStore externalEntityStore)
    {
        this.eventPublisher = eventPublisher;
        this.externalEntityStore = externalEntityStore;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        // This should fix the random failures of the TestUserAvatar selenium tests (also see JRADEV-4532 for more info about this).
        ((CachingExternalEntityStore) ComponentManager.getComponentInstanceOfType(ExternalEntityStore.class)).onClearCache(null);

        psCache.clear();
    }


    public PropertySet getPropertySet(User user)
    {
        Assertions.notNull("user", user);

        String name = user.getName();

        final PropertySet cachedValue = psCache.get(name);

        if (cachedValue != null)
        {
            return cachedValue;
        }

        Long id = externalEntityStore.createIfDoesNotExist(name);

        final Map<String, Object> args = new HashMap<String, Object>();
        args.put("entityId", id);
        args.put("entityName", ENTITY_TYPE);
        args.put("delegator.name", ofBizConnectionFactory.getDelegatorName());

        final PropertySet basePs = PropertySetManager.getInstance("ofbiz", args);

        args.clear();
        args.put("PropertySet", basePs);
        PropertySet ps = PropertySetManager.getInstance("cached", args);
        psCache.put(name, ps);

        return ps;
    }
}
