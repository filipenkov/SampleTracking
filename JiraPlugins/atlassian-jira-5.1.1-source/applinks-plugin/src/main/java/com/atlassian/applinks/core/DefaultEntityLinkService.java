package com.atlassian.applinks.core;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.PropertySet;
import com.atlassian.applinks.api.SubvertedEntityLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.event.EntityLinkAddedEvent;
import com.atlassian.applinks.api.event.EntityLinkDeletedEvent;
import com.atlassian.applinks.core.link.DefaultEntityLinkBuilderFactory;
import com.atlassian.applinks.core.link.InternalEntityLinkService;
import com.atlassian.applinks.core.property.EntityLinkProperties;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.applinks.core.rest.client.EntityLinkClient;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.EntityLinkBuilderFactory;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.event.api.EventPublisher;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Nullable;

import static com.atlassian.applinks.spi.application.TypeId.getTypeId;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since v3.0
 */
public class DefaultEntityLinkService implements InternalEntityLinkService, SubvertedEntityLinkService
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEntityLinkService.class.getName());

    private static final String LINKED_ENTITIES = "linked.entities";
    private static final String PRIMARY_FMT = "primary.%s";

    private static final String TYPE = "type";
    private static final String TYPE_I18N = "typeI18n";
    private static final String APPLICATION_ID = "applicationId";
    private static final String KEY = "key";
    private static final String NAME = "name";

    /*
     This class is using setter injection to avoid a circular dependency with ApplicationLinkService
     */
    private PropertyService propertyService;
    private ApplicationLinkService applicationLinkService;
    private EntityLinkBuilderFactory entityLinkBuilderFactory;
    private InternalHostApplication internalHostApplication;
    private InternalTypeAccessor typeAccessor;
    private EntityLinkClient entityLinkClient;
    private EventPublisher eventPublisher;

    public void setPropertyService(final PropertyService propertyService)
    {
        this.propertyService = propertyService;
    }

    public void setApplicationLinkService(final ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    public void setEntityLinkBuilderFactory(final DefaultEntityLinkBuilderFactory entityLinkBuilderFactory)
    {
        this.entityLinkBuilderFactory = entityLinkBuilderFactory;
    }

    public void setHostApplication(final InternalHostApplication internalHostApplication)
    {
        this.internalHostApplication = internalHostApplication;
    }

    public void setTypeAccessor(final InternalTypeAccessor typeAccessor)
    {
        this.typeAccessor = typeAccessor;
    }

    public void setEntityLinkClient(final EntityLinkClient entityLinkClient)
    {
        this.entityLinkClient = entityLinkClient;
    }

    public void setEventPublisher(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public void setEntityLinkBuilderFactory(final EntityLinkBuilderFactory entityLinkBuilderFactory)
    {
        this.entityLinkBuilderFactory = entityLinkBuilderFactory;
    }

    /* End Dependency Setters */

    public EntityLinkBuilderFactory getEntityLinkBuilderFactory()
    {
        return entityLinkBuilderFactory;
    }

    public EntityLink addReciprocatedEntityLink(final String localKey, final Class<? extends EntityType> localTypeClass,
                                                final EntityLink entityLink)
            throws ReciprocalActionException, CredentialsRequiredException
    {
        entityLinkClient.createEntityLinkFrom(entityLink, loadTypeFromClass(localTypeClass), localKey);
        return addEntityLink(localKey, localTypeClass, entityLink);
    }

    private EntityType loadTypeFromClass(final Class<? extends EntityType> localTypeClass)
    {
        return checkNotNull(typeAccessor.getEntityType(localTypeClass),
                String.format("%s class available, but type not installed?", localTypeClass));
    }

    public void migrateEntityLinks(final ApplicationLink from, final ApplicationLink to)
    {
        for (final EntityReference localEntity : internalHostApplication.getLocalEntities())
        {
            final List<? extends EntityLink> entityLinks = Lists.newArrayList(
                    Iterables.transform(getStoredEntityLinks(localEntity.getKey(), localEntity.getType().getClass()),
                            new Function<EntityLink, EntityLink>()
                            {
                                public EntityLink apply(@Nullable EntityLink oldEntityLink) {
                                    if (oldEntityLink.getApplicationLink().getId().equals(from.getId()))
                                    {
                                        final EntityLink newEntityLink = entityLinkBuilderFactory.builder()
                                                .applicationLink(to)
                                                .type(oldEntityLink.getType())
                                                .key(oldEntityLink.getKey())
                                                .name(oldEntityLink.getName())
                                                .primary(oldEntityLink.isPrimary())
                                                .build();

                                        final EntityLinkProperties oldLinkProperties = propertyService.getProperties(oldEntityLink);
                                        final EntityLinkProperties newLinkProperties = propertyService.getProperties(newEntityLink);
                                        newLinkProperties.setProperties(oldLinkProperties); // copy over all user properties
                                        oldLinkProperties.removeAll();  // delete old user properties

                                        // update potential references in the local entity props
                                        // (if this entityLink happens to be the primary for this local entity)
                                        final String primaryPropertyKey = primaryPropertyKey(getTypeId(newEntityLink.getType()));
                                        final PropertySet props = propertyService.getLocalEntityProperties(localEntity.getKey(), TypeId.getTypeId(localEntity.getType()));
                                        final Object value = props.getProperty(primaryPropertyKey);
                                        if (value != null)
                                        {
                                            final Properties primary = (Properties) value;
                                            if (from.getId().get().equals(primary.get(APPLICATION_ID)))
                                            {
                                                primary.put(APPLICATION_ID, to.getId().get());
                                                props.putProperty(primaryPropertyKey, primary);
                                            }
                                        }

                                        return newEntityLink;
                                    }
                                    else
                                    {
                                        return oldEntityLink;
                                    }
                                }
                            }));
            setStoredEntityLinks(localEntity.getKey(), localEntity.getType().getClass(), entityLinks);
        }
    }

    /**
     * Add or update an entity link
     */
    public EntityLink addEntityLink(
            final String localKey, final Class<? extends EntityType> localType, final EntityLink entityLink)
    {
        final List<EntityLink> entities = this.getStoredEntityLinks(localKey, localType);

        for (final Iterator<EntityLink> iterator = entities.iterator(); iterator.hasNext();)
        {
            final EntityLink storedEntity = iterator.next();
            if (equivalent(storedEntity, entityLink))
            {
                // we're performing an update, remove old entity record
                iterator.remove();
                break;
            }
        }

        entities.add(entityLink);
        setStoredEntityLinks(localKey, localType, entities);

        /* set the link as primary if the isPrimary flag is explicitly set, or there is no existing primary entity link
          of it's type already already associated with the local entity
        */
        if (entityLink.isPrimary() || getPrimaryRef(localKey, lookUpTypeId(localType), getTypeId(entityLink.getType())) == null)
        {
            makePrimaryImpl(localKey, localType, entityLink);
        }

        eventPublisher.publish(new EntityLinkAddedEvent(entityLink, localKey, localType));
        return entityLink;
    }

    /**
     * @param localType the {@link Class} of the {@link EntityType} to resolve an {@link TypeId} for
     * @return the {@link TypeId} of the specified {@link EntityType}
     * @throws IllegalStateException if the supplied class does not have an enabled implementation registered via the
     * {@code applinks-entity-link} module descriptor
     */
    private TypeId lookUpTypeId(final Class<? extends EntityType> localType)
    {
        final EntityType type = typeAccessor.getEntityType(localType);
        if (type == null) {
            throw new IllegalStateException("Couldn't load " + localType.getName() + ", type not installed?");
        }
        return TypeId.getTypeId(type);
    }

    public boolean deleteReciprocatedEntityLink(final String localKey, final Class<? extends EntityType> localType,
                                                final EntityLink entityToDelete)
            throws ReciprocalActionException, CredentialsRequiredException
    {
        entityLinkClient.deleteEntityLinkFrom(entityToDelete, loadTypeFromClass(localType), localKey);
        return deleteEntityLink(localKey, localType, entityToDelete);
    }

    public boolean deleteEntityLink(final String localKey, final Class<? extends EntityType> localType,
                                    final EntityLink entityToDelete)
    {
        final List<EntityLink> entities = getStoredEntityLinks(localKey, localType);
        boolean deleted = false;
        for (final Iterator<EntityLink> iterator = entities.iterator(); iterator.hasNext();)
        {
            final EntityLink entity = iterator.next();
            if (equivalent(entity, entityToDelete))
            {
                iterator.remove();
                deleted = true;
                break;
            }
        }

        if (deleted)
        {
            //if that link was the primary and there's still links of that type in existence, we'll need to assign a new one
            final PrimaryRef primary = getPrimaryRef(localKey, lookUpTypeId(localType), getTypeId(entityToDelete.getType()));
            if (primary.refersTo(entityToDelete))
            {
                selectNewPrimary(localKey, localType, entityToDelete.getType().getClass(), entities);
            }

            propertyService.getProperties(entityToDelete).removeAll();

            setStoredEntityLinks(localKey, localType, entities);

            eventPublisher.publish(new EntityLinkDeletedEvent(entityToDelete, localKey, localType));
        }

        return deleted;
    }

    private void selectNewPrimary(final String localKey, final Class<? extends EntityType> localType, final Class<? extends EntityType> type, final Iterable<? extends EntityLink> entities)
    {
        final Iterator<? extends EntityLink> it = entities.iterator();
        if (!it.hasNext())
        {
            // no more of this type - remove the primary reference
            final String primaryPropertyKey = primaryPropertyKey(lookUpTypeId(type));
            propertyService.getLocalEntityProperties(localKey, lookUpTypeId(localType)).removeProperty(primaryPropertyKey);
        }
        else
        {
            // still some left, just choose the next one
            makePrimaryImpl(localKey, localType, it.next());
        }
    }

    public void deleteEntityLinksFor(final ApplicationLink link)
    {
        checkNotNull(link);
        for (final EntityReference localEntity : internalHostApplication.getLocalEntities())
        {
            final Set<Class<? extends EntityType>> typesForWhichToReassignPrimaries = new HashSet<Class<? extends EntityType>>();
            final Set<EntityLink> removedEntityLinks = new HashSet<EntityLink>();

            // remove entities that are children of this application link --
            // n.b. MUST exhaust the result of Iterables.filter() for the predicate to populate our type set
            final List<? extends EntityLink> updatedLinks = Lists.newArrayList(
                    Iterables.filter(getStoredEntityLinks(localEntity.getKey(), localEntity.getType().getClass()), new Predicate<EntityLink>()
                    {
                        public boolean apply(final EntityLink input)
                        {
                            if (link.getId().equals(input.getApplicationLink().getId()))
                            {
                                // we're going to delete this - check if it is a primary link we're going to need to reassign
                                if (!typesForWhichToReassignPrimaries.contains(input.getType().getClass()))
                                {
                                    final PrimaryRef primary = getPrimaryRef(localEntity.getKey(), getTypeId(localEntity.getType()), getTypeId(input.getType()));
                                    if (primary.refersTo(input))
                                    {
                                        typesForWhichToReassignPrimaries.add(input.getType().getClass());
                                    }
                                }
                                removedEntityLinks.add(input);
                                return false;
                            }
                            else
                            {
                                //this is not the link you are looking for, move along
                                return true;
                            }
                        }
                    }));

            for (final Class<? extends EntityType> type : typesForWhichToReassignPrimaries)
            {
                selectNewPrimary(localEntity.getKey(), localEntity.getType().getClass(), type, updatedLinks);
            }

            /**
             * Let's first clear all custom properties of every entity link.
             */
            for (final EntityLink removedLink : removedEntityLinks)
            {
                propertyService.getProperties(removedLink).removeAll();
            }

            /**
             * Now let's remove the entity links.
             */
            setStoredEntityLinks(localEntity.getKey(), localEntity.getType().getClass(), updatedLinks);

            /**
             * Publish an EntityLinkDeletedEvent for every entity link we deleted.
             */
            for (final EntityLink removedLink : removedEntityLinks)
            {
                eventPublisher.publish(new EntityLinkDeletedEvent(removedLink, localEntity.getKey(), localEntity.getType().getClass()));
            }
        }
    }

    private List<EntityLink> getStoredEntityLinks(final String localKey, final Class<? extends EntityType> localType)
    {
        return getStoredEntityLinks(localKey, localType, PermissionMode.CHECK);
    }

    private List<EntityLink> getStoredEntityLinks(final String localKey, final Class<? extends EntityType> localType, final PermissionMode permissionMode)
    {
        checkNotNull(localKey);
        checkNotNull(localType);

        switch (permissionMode)
        {
        case CHECK:
            if (!internalHostApplication.doesEntityExist(localKey, localType))
            {
                throw new IllegalArgumentException(String.format("No local entity with key '%s' and type '%s' exists", localKey, localType));
            }
            break;
        case NO_CHECK:
            if (!internalHostApplication.doesEntityExistNoPermissionCheck(localKey, localType))
            {
                throw new IllegalArgumentException(String.format("No local entity with key '%s' and type '%s' exists", localKey, localType));
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown permission mode: " + permissionMode);
        }

        List<String> encodedLinks = getEncodedLinks(localKey, localType);
        if (encodedLinks == null)
        {
            encodedLinks = new ArrayList<String>();
        }

        final List<EntityLink> entityLinks = new ArrayList<EntityLink>();
        for (final String from : encodedLinks)
        {
            final JSONObject obj;
            final ApplicationId applicationId;
            try
            {
                obj = new JSONObject(from);
                applicationId = new ApplicationId(getRequiredJSONString(obj, APPLICATION_ID));
            }
            catch (JSONException e)
            {
                throw new RuntimeException("Failed to decode stored entity link to JSON for local entity with key '" + localKey + "' and of type '" + localType + "'. Encoded string is: '" + from + "'", e);
            }

            final ApplicationLink applicationLink;
            try
            {
                applicationLink = applicationLinkService.getApplicationLink(applicationId);
            }
            catch (TypeNotInstalledException e)
            {
                LOG.warn(String.format("Couldn't load application link with id %s, type %s is not installed. " +
                        "All child entity links will be inaccessible.", applicationId, e.getType()));
                continue;
            }

            if (applicationLink == null)
            {
                throw new IllegalStateException("ApplicationLink with id '" + applicationId + "' not found!");
            }

            final TypeId typeId = new TypeId(getRequiredJSONString(obj, TYPE));
            final EntityType type = typeAccessor.loadEntityType(typeId);

            if (type == null)
            {
                LOG.warn(String.format("Couldn't load type %s for entity link (child of application link with id %s). Type is not installed? ",
                        typeId, applicationLink.getId()));
                continue;
            }

            final String key = getRequiredJSONString(obj, KEY);
            final boolean isPrimary = getPrimaryRef(localKey, lookUpTypeId(localType), getTypeId(type))
                    .refersTo(key, getTypeId(type), applicationLink.getId());

            entityLinks.add(entityLinkBuilderFactory
                    .builder()
                    .key(key)
                    .type(type)
                    .name(getRequiredJSONString(obj, NAME))
                    .applicationLink(applicationLink)
                    .primary(isPrimary).build()
            );
        }
        return entityLinks;
    }

    @SuppressWarnings("unchecked")
    private List<String> getEncodedLinks(final String localKey, final Class<? extends EntityType> localType)
    {
        return (List<String>) propertyService.getLocalEntityProperties(localKey, lookUpTypeId(localType))
                .getProperty(LINKED_ENTITIES);
    }

    private void setStoredEntityLinks(final String localKey, final Class<? extends EntityType> localType,
                                      final Iterable<? extends EntityLink> entities)
    {
        checkNotNull(localKey);
        checkNotNull(localType);

        if (entities == null)
        {
            // remove stored entities
            propertyService.getLocalEntityProperties(localKey, lookUpTypeId(localType)).removeProperty(LINKED_ENTITIES);
            return;
        }

        final List<String> encodedEntities = Lists.newArrayList(
                Iterables.transform(entities, new Function<EntityLink, String>()
                {
                    public String apply(final EntityLink from)
                    {
                        final Map<String, String> propertyMap = new HashMap<String, String>();
                        propertyMap.put(KEY, from.getKey());
                        propertyMap.put(NAME, from.getName());
                        propertyMap.put(TYPE, getTypeId(from.getType()).get());
                        propertyMap.put(TYPE_I18N, from.getType().getI18nKey());
                        propertyMap.put(APPLICATION_ID, from.getApplicationLink().getId().get());
                        final StringWriter sw = new StringWriter();
                        try
                        {
                            new JSONObject(propertyMap).write(sw);
                        }
                        catch (JSONException e)
                        {
                            throw new RuntimeException(e);
                        }
                        return sw.getBuffer().toString();
                    }
                }));
        propertyService.getLocalEntityProperties(localKey, lookUpTypeId(localType)).putProperty(LINKED_ENTITIES, encodedEntities);
    }

    private String getJSONString(final JSONObject obj, final String propertyKey)
    {
        try
        {
            return obj.isNull(propertyKey) ?
                    null :
                    (String) obj.get(propertyKey);
        }
        catch (JSONException je)
        {
            throw new RuntimeException(je);
        }
    }

    private String getRequiredJSONString(final JSONObject obj, final String propertyKey) throws NullPointerException
    {
        return assertNotNull(getJSONString(obj, propertyKey), propertyKey);
    }

    private <T> T assertNotNull(final T value, final String propertyKey)
    {
        return Preconditions.checkNotNull(value, EntityLink.class.getSimpleName() + " property '" + propertyKey + "' should not be null!");
    }

    private String getRequiredString(final Map map, final String propertyKey)
    {
        return assertNotNull((String) map.get(propertyKey), propertyKey);
    }

    public Iterable<EntityLink> getEntityLinksForKey(
            final String localKey, final Class<? extends EntityType> localType, final Class<? extends EntityType> typeOfRemoteEntities)
    {
        return getEntityLinksForKey(localKey, localType, typeOfRemoteEntities, PermissionMode.CHECK);
    }

    private Iterable<EntityLink> getEntityLinksForKey(
            final String localKey, final Class<? extends EntityType> localType, final Class<? extends EntityType> typeOfRemoteEntities, final PermissionMode permissionMode)
    {
        checkNotNull(localKey);
        checkNotNull(localType);
        checkNotNull(typeOfRemoteEntities);

        return Iterables.filter(this.getStoredEntityLinks(localKey, localType, permissionMode),
                new Predicate<EntityLink>()
                {
                    public boolean apply(final EntityLink input)
                    {
                        return typeOfRemoteEntities.isAssignableFrom(input.getType().getClass());
                    }
                });
    }

    public Iterable<EntityLink> getEntityLinks(final Object entity, final Class<? extends EntityType> type)
    {
        checkNotNull(entity);

        final EntityReference entityRef = internalHostApplication.toEntityReference(entity);
        return getEntityLinksForKey(entityRef.getKey(), entityRef.getType().getClass(), type);
    }

    public Iterable<EntityLink> getEntityLinksForKey(final String localKey, final Class<? extends EntityType> localType)
    {
        return getEntityLinksForKey(localKey, localType, PermissionMode.CHECK);
    }

    private Iterable<EntityLink> getEntityLinksForKey(final String localKey, final Class<? extends EntityType> localType, final PermissionMode permissionMode)
    {
        checkNotNull(localKey);
        checkNotNull(localType);

        return getStoredEntityLinks(localKey, localType, permissionMode);
    }

    public Iterable<EntityLink> getEntityLinks(final Object domainObject)
    {
        checkNotNull(domainObject);

        final EntityReference entityRef = internalHostApplication.toEntityReference(domainObject);
        return this.getEntityLinksForKey(entityRef.getKey(), entityRef.getType().getClass());
    }

    public Iterable<EntityLink> getEntityLinksNoPermissionCheck(final Object entity, final Class<? extends EntityType> type)
    {
        checkNotNull(entity);

        final EntityReference entityRef = internalHostApplication.toEntityReference(entity);
        return getEntityLinksForKey(entityRef.getKey(), entityRef.getType().getClass(), type, PermissionMode.NO_CHECK);
    }

    public Iterable<EntityLink> getEntityLinksNoPermissionCheck(final Object domainObject)
    {
        checkNotNull(domainObject);

        final EntityReference entityRef = internalHostApplication.toEntityReference(domainObject);
        return this.getEntityLinksForKey(entityRef.getKey(), entityRef.getType().getClass(), PermissionMode.NO_CHECK);
    }

    public EntityLink getPrimaryEntityLinkForKey(final String localKey, final Class<? extends EntityType> localType,
                                                 final Class<? extends EntityType> typeOfRemoteEntity)
    {
        checkNotNull(localKey);
        checkNotNull(localType);
        checkNotNull(typeOfRemoteEntity);

        EntityLink primary = null;

        final PrimaryRef primaryRef = getPrimaryRef(localKey, lookUpTypeId(localType), lookUpTypeId(typeOfRemoteEntity));
        if (primaryRef != null)
        {
            for (final EntityLink entity : getEntityLinksForKey(localKey, localType))
            {
                if (primaryRef.refersTo(entity))
                {
                    primary = entity;
                    break;
                }
            }
        }

        return primary;
    }

    public EntityLink getPrimaryEntityLink(final Object domainObject, final Class<? extends EntityType> type)
    {
        checkNotNull(domainObject);

        final EntityReference entityRef = internalHostApplication.toEntityReference(domainObject);
        return getPrimaryEntityLinkForKey(entityRef.getKey(), entityRef.getType().getClass(), type);
    }

    public EntityLink getEntityLink(final String localKey, final Class<? extends EntityType> localType,
                                    final String remoteKey, final Class<? extends EntityType> remoteType, final ApplicationId applicationId)
    {
        EntityLink link = null;
        for (final EntityLink storedLink : getStoredEntityLinks(localKey, localType))
        {
            if (equivalent(storedLink, remoteKey, remoteType, applicationId))
            {
                link = storedLink;
                break;
            }
        }
        return link;
    }

    public Iterable<EntityLink> getEntityLinksForApplicationLink(final ApplicationLink applicationLink)
            throws TypeNotInstalledException
    {
        checkNotNull(applicationLink);
        final List<EntityLink> entityLinks = new ArrayList<EntityLink>();
        for (final EntityReference localEntity : internalHostApplication.getLocalEntities())
        {
            final ArrayList<EntityLink> list = Lists.newArrayList(Iterables.filter(getStoredEntityLinks(localEntity.getKey(), localEntity.getType().getClass()), new Predicate<EntityLink>()
            {
                public boolean apply(final EntityLink input)
                {
                    if (applicationLink.getId().equals(input.getApplicationLink().getId()))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            }));
            entityLinks.addAll(list);
        }
        return entityLinks;
    }

    public EntityLink makePrimary(final String localKey, final Class<? extends EntityType> localType, final EntityLink newPrimary)
    {
        checkNotNull(localKey);
        checkNotNull(localType);
        checkNotNull(newPrimary);

        //confirm that the entity is actually linked to from the specified local entity
        if (getEntityLink(localKey, localType, newPrimary.getKey(), newPrimary.getType().getClass(),
                newPrimary.getApplicationLink().getId()) == null)
        {
            throw new IllegalArgumentException(String.format(
                    "Can not make %s the new primary, not linked to from local entity %s:%s", newPrimary, localType, localKey));
        }

        makePrimaryImpl(localKey, localType, newPrimary);
        return newPrimary;
    }

    private static boolean equivalent(final EntityLink a, final EntityLink b)
    {
        return equivalent(a, b.getKey(), b.getType().getClass(), b.getApplicationLink().getId());
    }

    private static boolean equivalent(final EntityLink a, final String key, final Class<? extends EntityType> type, final ApplicationId applicationId)
    {
        return a.getKey().equals(key) &&
                a.getType().getClass().equals(type) &&
                a.getApplicationLink().getId().equals(applicationId);
    }

    private void makePrimaryImpl(final String localKey, final Class<? extends EntityType> localType, final EntityLink newEntity)
    {
        final String primaryPropertyKey = primaryPropertyKey(getTypeId(newEntity.getType()));
        final Properties primary = new Properties();
        primary.put(KEY, newEntity.getKey());
        primary.put(APPLICATION_ID, newEntity.getApplicationLink().getId().get());
        propertyService.getLocalEntityProperties(localKey, lookUpTypeId(localType)).putProperty(primaryPropertyKey, primary);
    }

    private PrimaryRef getPrimaryRef(final String key, final TypeId typeId, final TypeId typeOfRemoteEntity)
    {
        checkNotNull(key);
        checkNotNull(typeId);
        checkNotNull(typeOfRemoteEntity);

        final Properties primaryProps = (Properties) propertyService.getLocalEntityProperties(key, typeId)
                .getProperty(primaryPropertyKey(typeOfRemoteEntity));

        PrimaryRef primaryRef = null;
        if (primaryProps != null)
        {
            primaryRef = new PrimaryRef(
                    getRequiredString(primaryProps, KEY),
                    typeOfRemoteEntity,
                    new ApplicationId(getRequiredString(primaryProps, APPLICATION_ID))
            );
        }
        return primaryRef;
    }

    private static String primaryPropertyKey(final TypeId remoteType)
    {
        return String.format(PRIMARY_FMT, remoteType.get());
    }

    private static class PrimaryRef
    {
        private final String key;
        private final TypeId type;
        private final ApplicationId applicationId;

        private PrimaryRef(final String key, final TypeId type, final ApplicationId applicationId)
        {
            this.key = checkNotNull(key);
            this.type = checkNotNull(type);
            this.applicationId = checkNotNull(applicationId);
        }

        public String getKey()
        {
            return key;
        }

        public TypeId getType()
        {
            return type;
        }

        public ApplicationId getApplicationId()
        {
            return applicationId;
        }

        public boolean refersTo(final String key, final TypeId type, final ApplicationId applicationId)
        {
            return this.key.equals(key) &&
                    this.type.equals(type) &&
                    this.applicationId.equals(applicationId);
        }

        public boolean refersTo(final EntityLink link)
        {
            return refersTo(link.getKey(), getTypeId(link.getType()), link.getApplicationLink().getId());
        }
    }

    private enum PermissionMode { CHECK, NO_CHECK }
}
