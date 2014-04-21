package com.opensymphony.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;
import com.opensymphony.user.provider.UserProvider;
import com.opensymphony.user.provider.crowd.EmbeddedCrowdAccessProvider;
import com.opensymphony.user.provider.crowd.EmbeddedCrowdCredentialsProvider;
import com.opensymphony.user.provider.crowd.EmbeddedCrowdProfileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The UserManager is the <strong>OLD</strong> entry point to user-management in JIRA.
 * <p>
 * UserManager is a singleton and retrieved with getInstance();
 * <p>
 * All OSUser classes are deprecated since v4.3 and will be removed in v4.5.
 * Developers should use one of the following User Management components for new work and convert existing code ASAP:
 * <ul>
 * <li>{@link com.atlassian.jira.bc.user.UserService}<br>
 *      User operations with i18n, security and other validation.</li>
 * <li>{@link com.atlassian.jira.bc.group.GroupService}<br>
 *      Group operations with i18n, security and other validation.</li>
 * <li>{@link com.atlassian.jira.user.util.UserManager}<br>
 *      Low level simple User operations without validation.</li>
 * <li>{@link com.atlassian.jira.security.groups.GroupManager}<br>
 *      Low level Group operations without validation.</li>
 * <li>{@link com.atlassian.jira.user.util.UserUtil}<br>
 *      Low level User and Group operations without validation, including interactions with other Objects like Projects and Project Components</li>
 * <li>{@link com.atlassian.crowd.embedded.api.CrowdService}<br>
 *      Low level User and Group operations without validation.</li>
 * </ul>
 * Also note that the OSUser {@link User} and {@link Group} classes are deprecated and should be replaced with Crowd Embedded
 * {@link com.atlassian.crowd.embedded.api.User} and {@link com.atlassian.crowd.embedded.api.Group} interfaces.
 *
 *
 * @deprecated JIRA now uses "Embedded Crowd" for User Management. See other javadoc for alternatives. Since v4.3
 */
@Deprecated
public class  UserManager implements Serializable
{
    //~ Static fields/initializers /////////////////////////////////////////////
    private static final ResettableLazyReference<MultiTenantComponentMap<UserManager>> INSTANCE_MAP =
            new ResettableLazyReference<MultiTenantComponentMap<UserManager>>()
            {
                @Override
                protected MultiTenantComponentMap<UserManager> create() throws Exception
                {
                    return MultiTenantContext.getFactory().createComponentMap(
                            new MultiTenantCreator<UserManager>()
                    {
                                @Override
                                public UserManager create(Tenant tenant)
                                {
                                    return new UserManager();
                                }
                            });
                }
            };
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    enum Type
    {
        USER()
                {
                    @SuppressWarnings("unchecked")
                    @Override
                    User create(final String name, final Accessor accessor, CrowdService crowdService)
                    {
                        return new User(name, accessor, crowdService);
                    }
                },
        GROUP()
                {
                    @SuppressWarnings("unchecked")
                    @Override
                    Group create(final String name, final Accessor accessor, CrowdService crowdService)
                    {
                        return new Group(name, accessor);
                    }
                };

        abstract <T extends Entity> T create(String name, Accessor accessor, CrowdService crowdService);
    }

    //~ Instance fields ////////////////////////////////////////////////////////

    private final Accessor accessor = new Accessor();

    private final List<AccessProvider> accessProviders;
    private final List<CredentialsProvider> credentialsProviders;
    private final List<ProfileProvider> profileProviders;

    private final AccessProvider accessProvider;
    private final CredentialsProvider credentialsProvider;
    private final ProfileProvider profileProvider;

    /**
     * For testing only
     */
    public static void reset()
    {
        INSTANCE_MAP.reset();
    }

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Don't use this constructor most of the time. To use OSUser as a singleton, use {@link #getInstance()} .
     */
    public UserManager()
    {
        // The old User Manager would read the Config from the osuser.xml file,
        // but now we hard-code the EmbeddedCrowd Providers:
        accessProvider = new EmbeddedCrowdAccessProvider();
        credentialsProvider = new EmbeddedCrowdCredentialsProvider();
        profileProvider = new EmbeddedCrowdProfileProvider();

        accessProviders = Collections.singletonList(accessProvider);
        credentialsProviders = Collections.singletonList(credentialsProvider);
        profileProviders = Collections.singletonList(profileProvider);
    }

    /**
     * Entry-point to Singleton instance
     */
    public static UserManager getInstance()
    {
        try
        {
            return INSTANCE_MAP.get().get();
        }
        catch (final UserManagerImplementationException e)
        {
            logger.error("Unable to load configuration", e);
        }
        catch (final RuntimeException e)
        {
            logger.error("unexpected runtime exception during initialization", e);
        }
        return null;
    }

    public Accessor getAccessor()
    {
        return accessor;
    }
    /**
     * Get the current AccessProviders
     */
    public Collection<AccessProvider> getAccessProviders()
    {
        return accessProviders;
    }

    /**
     * Get the current CredentialsProviders
     */
    public Collection<CredentialsProvider> getCredentialsProviders()
    {
        return credentialsProviders;
    }

    /**
     * Get the current ProfileProviders
     */
    public Collection<ProfileProvider> getProfileProviders()
    {
        return profileProviders;
    }

    /**
     * Return all known Groups from all AccessProviders that allow listing.
     */
    public List<Group> getGroups()
    {
        return getEntities(Type.GROUP);
    }

    /**
     * Return all known Users from all CredentialProviders that allow listing.
     */
    public List<User> getUsers()
    {
        return getEntities(Type.USER);
    }

    /**
     * Return Group with given name. If Group is not found, an EntityNotFoundException is thrown.
     */
    public Group getGroup(final String name) throws EntityNotFoundException
    {
        return getEntity(name, Type.GROUP);
    }

    /**
     * Return user with given name. If User is not found, an EntityNotFoundException is thrown.
     */
    public User getUser(final String name) throws EntityNotFoundException
    {
        return getEntity(name, Type.USER);
    }

    /**
     * Create a new Group with given name.
     * <p/>
     * <ul>
     * <li>Firstly, all providers will be asked if a Group with the given name exists
     * - if it does, DuplicateEntityException is thrown. </li>
     * <li>The providers shall be iterated through until one of them states that it
     * can create the Group. </li>
     * <li>If no provider can create the Group, ImmutableException shall be thrown.</li>
     */
    public Group createGroup(final String name) throws DuplicateEntityException, ImmutableException
    {
        return createEntity(name, Type.GROUP);
    }

    /**
     * Create a new User with given name.
     * <p/>
     * <ul>
     * <li>Firstly, all providers will be asked if a User with the given name exists
     * - if it does, DuplicateEntityException is thrown. </li>
     * <li>The providers shall be iterated through until one of them states that it
     * can create the User. </li>
     * <li>If no provider can create the User, ImmutableException shall be thrown.</li>
     */
    public User createUser(final String name) throws DuplicateEntityException, ImmutableException
    {
        return createEntity(name, Type.USER);
    }

    /**
     * This method will flush all of the provider caches (if they are caching).
     */
    public void flushCaches()
    {
        accessProvider.flushCaches();
        credentialsProvider.flushCaches();
        profileProvider.flushCaches();
    }

    private <T extends Entity> List<T> getEntities(final Type type)
    {
        final List<T> result = new ArrayList<T>();
        final List<? extends UserProvider> toCheck = (type == Type.GROUP) ? accessProviders : credentialsProviders;

        for (final UserProvider provider : toCheck)
        {
            final List<String> entities = provider.list();
            if (entities == null)
            {
                continue;
            }

            // @todo lazy loading
            for (final String name : entities)
            {
                result.add(this.<T>buildEntity(name, type));
            }
        }

        return result;
    }

    private <T extends Entity> T getEntity(final String name, final Type type) throws EntityNotFoundException
    {
        if (name == null)
        {
            throw new EntityNotFoundException("Entity name is null");
        }
        if (getProvider(name, (type == Type.USER) ? credentialsProviders : accessProviders) == null)
        {
            throw new EntityNotFoundException("No " + ((type == Type.USER) ? "user '" : "group '") + name + "' found");
        }
        return this.<T>buildEntity(name, type);
    }

    <U extends UserProvider> U getProvider(final String name, final List<U> providers)
    {
        // @todo: This method is called often. Cache the results of the iteration.
        for (final U provider : providers)
        {
            if (provider.handles(name))
            {
                return provider;
            }
        }

        // @todo: Fix this nasty nasty hack.
        // If the provider being sought is ProfileProvider and none have answered yet,
        // try and create one automatically.
        if (providers == profileProviders)
        {
            for (final U provider : providers)
            {
                if (provider.create(name))
                {
                    return provider;
                }
            }
        }

        // end of hack.
        return null;
    }

    private <T extends Entity> T buildEntity(final String name, final Type type)
    {
        return type.<T>create(name, accessor, getCrowdService());
    }

    private <T extends Entity> T createEntity(final String name, final Type type) throws DuplicateEntityException, ImmutableException
    {
        // check Entity doesn't already exist
        final List<? extends UserProvider> providerList = (type == Type.GROUP) ? accessProviders : credentialsProviders;

        if (getProvider(name, providerList) != null)
        {
            throw new DuplicateEntityException(((type == Type.USER) ? "user " : "group ") + name + " already exists");
        }

        // loop through until we can create an Entity
        // note that it's OK that we only create against access or credential providers
        //lazy instantiation (via a gross hack) takes care of creating the entity
        //in the profile provider later on
        for (final UserProvider provider : providerList)
        {
            final Class<? extends UserProvider> toCheck = (type == Type.GROUP) ? AccessProvider.class : CredentialsProvider.class;

            if (toCheck.isAssignableFrom(provider.getClass()) && provider.create(name))
            {
                return this.<T>buildEntity(name, type);
            }
        }

        // if we get here, none of the providers have helped
        throw new ImmutableException("No provider successfully created entity " + name);
    }

    private CrowdService getCrowdService()
    {
        return ComponentAccessor.getComponentOfType(CrowdService.class);
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    /**
     * UserManager.Accessor is a gateway that can be passed from the UserManager
     * to any other object that allows priveleged callbacks.
     */
    public class Accessor implements ProviderAccessor
    {
        /**
         * Return appropriate AccessProvider for entity.
         */
        public AccessProvider getAccessProvider(final String name)
        {
            return accessProvider;
        }

        /**
         * Return appropriate CredentialsProvider for entity.
         */
        public CredentialsProvider getCredentialsProvider(final String name)
        {
            return credentialsProvider;
        }

        /**
         * Return appropriate ProfileProvider for entity.
         */
        public ProfileProvider getProfileProvider(final String name)
        {
            return profileProvider;
        }

        /**
         * Return underlying UserManager for Accessor.
         */
        public UserManager getUserManager()
        {
            return UserManager.this;
        }
    }

}
