package com.atlassian.jira.sharing;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.favourites.Favourite;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Classes that implement this interface are able to Shared and Favourited. The Favouriting and Sharing mechanism need the type of object it is and
 * the id.
 *
 * @since v3.13
 */
@PublicApi
public interface SharedEntity extends Favourite
{
    /**
     * The id of the entity.
     *
     * @return the id. May be null if not yet persisted. Some components may not accept a non-persisted entity.
     */
    Long getId();

    /**
     * The name of the entity.
     *
     * @return the name. Must not be null or empty.
     */
    String getName();

    /**
     * A general description of the entity.
     *
     * @return the description. May be null or empty.
     */
    String getDescription();

    /**
     * The type of entity it is. Examples include SearchRequest ("SearchRequest") and PortalPage ("PortalPage")
     *
     * @return the type of entity.
     */
    <S extends SharedEntity> TypeDescriptor<S> getEntityType();

    /**
     * The user name of the owner of the entity. Will always have permission to see it and see all shares.
     *
     * @return The user name of the user who owns this entity. May be null for anonymous user.
     */
    String getOwnerUserName();

    /**
     * The permissions for this entity.
     *
     * @return the permissions object. Must not be null.
     */
    SharePermissions getPermissions();

    /**
     * Utility class for identifying a {@link SharedEntity}. This can be used instead of a "proper" implementation.
     */
    public static class Identifier implements SharedEntity
    {
        private final Long id;
        private final TypeDescriptor<? extends SharedEntity> type;
        private final String ownerUserName;

        public Identifier(final Long id, final TypeDescriptor<? extends SharedEntity> type, final String ownerUserName)
        {
            Assertions.notNull("id", id);
            Assertions.notNull("type", type);

            this.id = id;
            this.type = type;
            this.ownerUserName = (StringUtils.isBlank(ownerUserName)) ? "" : IdentifierUtils.toLowerCase(ownerUserName);
        }

        public Identifier(final Long id, final TypeDescriptor<? extends SharedEntity> type, final User owner)
        {
            Assertions.notNull("id", id);
            Assertions.notNull("type", type);

            this.id = id;
            this.type = type;
            ownerUserName = (owner == null) ? "" : StringUtils.isBlank(owner.getName()) ? "" : IdentifierUtils.toLowerCase(owner.getName());
        }

        public Long getId()
        {
            return id;
        }

        public String getOwnerUserName()
        {
            return ownerUserName;
        }

        @SuppressWarnings("unchecked")
        public TypeDescriptor<SharedEntity> getEntityType()
        {
            return (TypeDescriptor<SharedEntity>) type;
        }

        ///CLOVER:OFF
        public String getName()
        {
            throw new UnsupportedOperationException(getClass() + " does not support name");
        }

        public String getDescription()
        {
            throw new UnsupportedOperationException(getClass() + " does not support description");
        }

        public SharePermissions getPermissions()
        {
            throw new UnsupportedOperationException(getClass() + " does not support SharePermissions");
        }

        public Long getFavouriteCount()
        {
            return 0L;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final Identifier that = (Identifier) o;

            if (!id.equals(that.id))
            {
                return false;
            }
            if (!ownerUserName.equals(that.ownerUserName))
            {
                return false;
            }
            return type.equals(that.type);
        }

        @Override
        public int hashCode()
        {
            int result;
            result = id.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + ownerUserName.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        ///CLOVER:ON
    }

    /**
     * The name and type of a {@link SharedEntity}.
     * <p/>
     * <strong>Note:</strong> Once released, the name of these should never change as there are persistent artefacts such as index paths that depend
     * on this name.
     */
    final class TypeDescriptor<S extends SharedEntity>
    {
        private final String name;

        TypeDescriptor(final String name)
        {
            this.name = Assertions.notBlank("name", name);
        }

        public String getName()
        {
            return name;
        }

        ///CLOVER:OFF
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final TypeDescriptor<?> other = (TypeDescriptor<?>) obj;
            if (name == null)
            {
                if (other.name != null)
                {
                    return false;
                }
            }
            else if (!name.equals(other.name))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return name;
        }

        ///CLOVER:ON

        public static final class Factory
        {
            private static final Factory INSTANCE = new Factory();

            public static Factory get()
            {
                return Factory.INSTANCE;
            }

            private final ConcurrentMap<String, TypeDescriptor<?>> map = new ConcurrentHashMap<String, TypeDescriptor<?>>();

            /**
             * should only be called by types that implement {@link SharedEntity}
             *
             * @param name the name of this type
             * @return the {@link TypeDescriptor}
             */
            public <S extends SharedEntity> TypeDescriptor<S> create(final String name)
            {
                return register(new TypeDescriptor<S>(name));
            }

            @SuppressWarnings("unchecked")
            public <S extends SharedEntity> TypeDescriptor<S> register(final TypeDescriptor<S> typeDescriptor)
            {
                notNull("typeDescriptor", typeDescriptor);
                map.putIfAbsent(typeDescriptor.getName(), typeDescriptor);
                return (TypeDescriptor<S>) map.get(typeDescriptor.getName());
            }
        }
    }

    /**
     * Encapsulates the permissions that a {@link SharedEntity} has.
     *
     * @since v3.13
     */
    @Immutable
    public final class SharePermissions implements Iterable<SharePermission>
    {
        private static final SharePermission GLOBAL_PERMISSION = new SharePermissionImpl(ShareType.Name.GLOBAL, null, null);

        public static final SharePermissions GLOBAL = new SharePermissions(Collections.singleton(SharePermissions.GLOBAL_PERMISSION));
        public static final SharePermissions PRIVATE = new SharePermissions(Collections.<SharePermission> emptySet());

        private final Set<SharePermission> permissions;

        public SharePermissions(final Set<? extends SharePermission> permissions)
        {
            Assertions.notNull("permissions", permissions);
            this.permissions = Collections.unmodifiableSet(permissions);
        }

        public Iterator<SharePermission> iterator()
        {
            return permissions.iterator();
        }

        public boolean isGlobal()
        {
            return permissions.contains(SharePermissions.GLOBAL_PERMISSION);
        }

        public boolean isPrivate()
        {
            return permissions.isEmpty();
        }

        public boolean isEmpty()
        {
            return permissions.isEmpty();
        }

        public int size()
        {
            return permissions.size();
        }

        public Set<SharePermission> getPermissionSet()
        {
            return permissions;
        }

        ///CLOVER:OFF
        @Override
        public int hashCode()
        {
            return permissions == null ? 0 : permissions.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final SharePermissions other = (SharePermissions) obj;
            if (permissions == null)
            {
                if (other.permissions != null)
                {
                    return false;
                }
            }
            else if (!permissions.equals(other.permissions))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + permissions;
        }
        ///CLOVER:ON
    }
}
