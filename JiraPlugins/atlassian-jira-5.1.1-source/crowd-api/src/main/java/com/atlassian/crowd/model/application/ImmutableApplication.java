package com.atlassian.crowd.model.application;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.find;

/**
 * Immutable Application
 *
 * @since 2.2
 */
public final class ImmutableApplication implements Application
{
    private static final long serialVersionUID = 161484258407244241L;

    private final Long id;
    private final String name;
    private final ApplicationType type;
    private final String description;
    private final PasswordCredential passwordCredential;
    private final boolean permanent;
    private final boolean active;
    private final Map<String, String> attributes;
    private final List<DirectoryMapping> directoryMappings;
    private final Set<RemoteAddress> remoteAddresses;
    private final boolean lowercaseOutput;
    private final boolean aliasingEnabled;
    private final Date createdDate;
    private final Date updatedDate;

    public ImmutableApplication(final Long id, final String name, final ApplicationType type, final String description, final PasswordCredential passwordCredential, final boolean permanent, final boolean active, final Map<String, String> attributes, final List<DirectoryMapping> directoryMappings, final Set<RemoteAddress> remoteAddresses, final boolean lowercaseOutput, final boolean aliasingEnabled, final Date createdDate, final Date updatedDate)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.passwordCredential = passwordCredential;
        this.permanent = permanent;
        this.active = active;
        this.attributes = (attributes == null ? Collections.<String, String>emptyMap() : ImmutableMap.copyOf(attributes));
        this.directoryMappings = (directoryMappings == null ? Collections.<DirectoryMapping>emptyList() : ImmutableList.copyOf(directoryMappings));
        this.remoteAddresses = (remoteAddresses == null ? Collections.<RemoteAddress>emptySet() : ImmutableSet.copyOf(remoteAddresses));
        this.lowercaseOutput = lowercaseOutput;
        this.aliasingEnabled = aliasingEnabled;
        this.createdDate = (createdDate == null ? null : new Date(createdDate.getTime()));
        this.updatedDate = (updatedDate == null ? null : new Date(updatedDate.getTime()));
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public ApplicationType getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }

    public PasswordCredential getCredential()
    {
        return passwordCredential;
    }

    public boolean isPermanent()
    {
        return permanent;
    }

    public boolean isActive()
    {
        return active;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public List<DirectoryMapping> getDirectoryMappings()
    {
        return directoryMappings;
    }

    public DirectoryMapping getDirectoryMapping(final long directoryId)
    {
        Long dirId = directoryId; // handle null directory IDs
        for (DirectoryMapping directoryMapping : directoryMappings)
        {
            if (dirId.equals(directoryMapping.getDirectory().getId()))
            {
                return directoryMapping;
            }
        }
        return null;
    }

    public Set<RemoteAddress> getRemoteAddresses()
    {
        return remoteAddresses;
    }

    public boolean hasRemoteAddress(final String remoteAddress)
    {
        return remoteAddresses.contains(new RemoteAddress(remoteAddress));
    }

    public boolean isLowerCaseOutput()
    {
        return lowercaseOutput;
    }

    public boolean isAliasingEnabled()
    {
        return aliasingEnabled;
    }

    public Date getCreatedDate()
    {
        return (createdDate == null ? null : new Date(createdDate.getTime()));
    }

    public Date getUpdatedDate()
    {
        return (updatedDate == null ? null : new Date(updatedDate.getTime()));
    }

    public Set<String> getValues(final String key)
    {
        final String value = attributes.get(key);
        return (value == null ? Collections.<String>emptySet() : Collections.singleton(value));
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
     * Constructs a new builder for an <tt>ImmutableApplication</tt>.
     *
     * @param name name of the application
     * @param type type of the application
     * @return builder with the name and type initialised
     */
    public static Builder builder(final String name, final ApplicationType type)
    {
        return new Builder(name, type);
    }

    /**
     * Constructs a new builder for an <tt>ImmutableApplication</tt> with the fields initialised to
     * <code>application</code>.
     *
     * @param application application to duplicate
     * @return builder with the fields initialised to <code>application</code>
     */
    public static Builder builder(final Application application)
    {
        return new Builder(application);
    }

    public static class Builder
    {
        private Long id;
        private String name;
        private ApplicationType type;
        private String description;
        private PasswordCredential passwordCredential;
        private boolean permanent;
        private boolean active;
        private Map<String, String> attributes;
        private List<DirectoryMapping> directoryMappings;
        private Set<RemoteAddress> remoteAddresses;
        private boolean lowercaseOutput;
        private boolean aliasingEnabled;
        private Date createdDate;
        private Date updatedDate;

        public Builder(final String name, final ApplicationType type)
        {
            this.name = name;
            this.type = type;
        }

        public Builder(final Application application)
        {
            this.id = application.getId();
            this.name = application.getName();
            this.type = application.getType();
            this.description = application.getDescription();
            this.passwordCredential = application.getCredential();
            this.permanent = application.isPermanent();
            this.active = application.isActive();
            this.attributes = application.getAttributes();
            this.directoryMappings = application.getDirectoryMappings();
            this.remoteAddresses = application.getRemoteAddresses();
            this.lowercaseOutput = application.isLowerCaseOutput();
            this.aliasingEnabled = application.isAliasingEnabled();
            this.createdDate = application.getCreatedDate();
            this.updatedDate = application.getUpdatedDate();
        }

        public Builder setId(final Long id)
        {
            this.id = id;
            return this;
        }

        public Builder setName(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder setType(final ApplicationType type)
        {
            this.type = type;
            return this;
        }

        public Builder setDescription(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder setPasswordCredential(final PasswordCredential passwordCredential)
        {
            this.passwordCredential = passwordCredential;
            return this;
        }

        public Builder setPermanent(final boolean permanent)
        {
            this.permanent = permanent;
            return this;
        }

        public Builder setActive(final boolean active)
        {
            this.active = active;
            return this;
        }

        public Builder setAttributes(final Map<String, String> attributes)
        {
            this.attributes = attributes;
            return this;
        }

        public Builder setDirectoryMappings(final List<DirectoryMapping> directoryMappings)
        {
            this.directoryMappings = directoryMappings;
            return this;
        }

        public Builder setRemoteAddresses(final Set<RemoteAddress> remoteAddresses)
        {
            this.remoteAddresses = remoteAddresses;
            return this;
        }

        public Builder setLowercaseOutput(final boolean lowercaseOutput)
        {
            this.lowercaseOutput = lowercaseOutput;
            return this;
        }

        public Builder setAliasingEnabled(final boolean aliasingEnabled)
        {
            this.aliasingEnabled = aliasingEnabled;
            return this;
        }

        public Builder setCreatedDate(final Date createdDate)
        {
            this.createdDate = createdDate;
            return this;
        }

        public Builder setUpdatedDate(final Date updatedDate)
        {
            this.updatedDate = updatedDate;
            return this;
        }

        /**
         * Builds the new <tt>ImmutableApplication</tt>.
         *
         * @return new <tt>ImmutableApplication</tt>
         */
        public ImmutableApplication build()
        {
            return new ImmutableApplication(id, name, type, description, passwordCredential, permanent, active, attributes, directoryMappings, remoteAddresses, lowercaseOutput, aliasingEnabled, createdDate, updatedDate);
        }
    }
}
