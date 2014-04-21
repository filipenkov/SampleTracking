package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ImmutableDirectory implements Directory, Serializable
{
    private static final long serialVersionUID = -8196445895525985343L;

    private final Long id;
    private final String name;
    private final boolean active;
    private final String encryptionType;
    private final String description;
    private final DirectoryType type;
    private final String implementationClass;
    private final long createdDate;
    private final long updatedDate;
    private final Set<OperationType> allowedOperations;
    private final Map<String, String> attributes;

    public ImmutableDirectory(final Long id, final String name, final boolean active, final String description, final String encryptionType, final DirectoryType type, final String implementationClass, @NotNull final Date createdDate, @NotNull final Date updatedDate, @Nullable final Set<OperationType> allowedOperations, @Nullable final Map<String, String> attributes)
    {
        this.id = id;
        this.name = name;
        this.active = active;
        this.description = description;
        this.encryptionType = encryptionType;
        this.type = type;
        this.implementationClass = implementationClass;
        this.createdDate = createdDate.getTime();
        this.updatedDate = updatedDate.getTime();
        // create our own immutable copy of allowedOperations
        this.allowedOperations = immutableCopyOf(allowedOperations);
        // create our own immutable copy of attributes
        this.attributes = immutableCopyOf(attributes);
    }

    // Fields
    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getEncryptionType()
    {
        return encryptionType;
    }

    public String getDescription()
    {
        return description;
    }

    public DirectoryType getType()
    {
        return type;
    }

    public String getImplementationClass()
    {
        return implementationClass;
    }

    public Date getCreatedDate()
    {
        return new Date(createdDate);
    }

    public Date getUpdatedDate()
    {
        return new Date(updatedDate);
    }

    public Set<OperationType> getAllowedOperations()
    {
        return allowedOperations;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    // Attributes
    public Set<String> getValues(final String key)
    {
        final String value = getValue(name);
        if (value == null)
        {
            return null;
        }
        else
        {
            return Collections.singleton(value);
        }
    }

    public String getValue(final String key)
    {
        return attributes.get(key);
    }

    public Set<String> getKeys()
    {
        return attributes.keySet();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    /**
     * Create an empty Builder.
     * @return an empty Builder.
     */
    public static Builder newBuilder()
    {
        return new Builder();
    }

    /**
     * Create a Builder that copies its initial values from the given directory.
     * @param directory The Directory to clone.
     * @return A new Builder.
     */
    public static Builder newBuilder(final Directory directory)
    {
        final Builder builder = new Builder();
        builder.setId(directory.getId());
        builder.setActive(directory.isActive());
        builder.setName(directory.getName());
        builder.setDescription(directory.getDescription());
        builder.setEncryptionType(directory.getEncryptionType());
        builder.setType(directory.getType());
        builder.setImplementationClass(directory.getImplementationClass());
        builder.setCreatedDate(directory.getCreatedDate());
        builder.setUpdatedDate(directory.getUpdatedDate());
        builder.setAllowedOperations(new HashSet<OperationType>(directory.getAllowedOperations()));
        builder.setAttributes(new HashMap<String, String>(directory.getAttributes()));
        return builder;
    }

    /**
     * Used to aid in the construction of an ImmutableDirectory.
     */
    public static final class Builder
    {
        private Long id;
        private String name;
        private boolean active = true;
        private String encryptionType;
        private String description;
        private DirectoryType type;
        private String implementationClass;
        private Date createdDate = new Date();
        private Date updatedDate = new Date();
        private Set<OperationType> allowedOperations;
        private Map<String, String> attributes;

        /**
         * Returns an immutable Directory object with the properties set in this builder.
         * @return an immutable Directory object with the properties set in this builder.
         */
        public Directory toDirectory()
        {
            return new ImmutableDirectory(id, name, active, description, encryptionType, type, implementationClass, createdDate, updatedDate,
                allowedOperations, attributes);
        }

        //------------------------------------------------------------
        // Setters
        //------------------------------------------------------------

        public void setId(final Long id)
        {
            this.id = id;
        }

        public void setName(final String name)
        {
            this.name = name;
        }

        public void setActive(final boolean active)
        {
            this.active = active;
        }

        public void setEncryptionType(final String encryptionType)
        {
            this.encryptionType = encryptionType;
        }

        public void setDescription(final String description)
        {
            this.description = description;
        }

        public void setType(final DirectoryType type)
        {
            this.type = type;
        }

        public void setImplementationClass(final String implementationClass)
        {
            this.implementationClass = implementationClass;
        }

        public void setCreatedDate(final Date createdDate)
        {
            this.createdDate = createdDate;
        }

        public void setUpdatedDate(final Date updatedDate)
        {
            this.updatedDate = updatedDate;
        }

        public void setAllowedOperations(final Set<OperationType> allowedOperations)
        {
            this.allowedOperations = allowedOperations;
        }

        public void setAttributes(final Map<String, String> attributes)
        {
            this.attributes = attributes;
        }
    }

    private static <E> Set<E> immutableCopyOf(@Nullable final Set<E> set)
    {
        if (set == null)
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<E>(set));
    }

    private static <K, V> Map<K, V> immutableCopyOf(@Nullable final Map<K, V> map)
    {
        if (map == null)
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<K, V>(map));
    }
}
