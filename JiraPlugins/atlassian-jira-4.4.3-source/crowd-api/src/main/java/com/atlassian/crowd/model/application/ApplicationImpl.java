package com.atlassian.crowd.model.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import com.atlassian.crowd.model.InternalEntity;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.util.InternalEntityUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.*;

/**
 * Implementation of Application (designed for use for Hibernate).
 */
public class ApplicationImpl extends InternalEntity implements Application
{
    private String lowerName;
    private ApplicationType type;
    private String description;
    private PasswordCredential credential;

    private Map<String, String> attributes = new HashMap<String, String>();
    private List<DirectoryMapping> directoryMappings = new ArrayList<DirectoryMapping>();
    private Set<RemoteAddress> remoteAddresses = new HashSet<RemoteAddress>();

    protected ApplicationImpl()
    {
    }

    protected ApplicationImpl(final String name, final long id, final ApplicationType type, final String description,
            final PasswordCredential credential, final boolean active, final Map<String, String> attributes,
            final List<DirectoryMapping> directoryMappings, final Set<RemoteAddress> remoteAddresses,
            final Date createdDate, final Date updatedDate)
    {
        setNameAndLowerName(name);
        this.id = id;
        this.type = type;
        this.description = description;
        this.credential = credential;
        this.active = active;
        this.attributes = MapUtils.isEmpty(attributes) ? Maps.<String, String>newHashMap() : Maps.newHashMap(attributes);
        this.directoryMappings = CollectionUtils.isEmpty(directoryMappings) ? Lists.<DirectoryMapping>newArrayList() : Lists.newArrayList(directoryMappings);
        this.remoteAddresses = CollectionUtils.isEmpty(remoteAddresses) ? Sets.<RemoteAddress>newHashSet() : Sets.newHashSet(remoteAddresses);
        this.createdDate = (createdDate == null) ? new Date() : new Date(createdDate.getTime());
        this.updatedDate = (updatedDate == null) ? new Date() : new Date(updatedDate.getTime());
    }

    public static ApplicationImpl newInstance(final Application application)
    {
        final long applicationId = (application.getId() == null ? -1L : application.getId());
        return new ApplicationImpl(application.getName(), applicationId, application.getType(),
                application.getDescription(), application.getCredential(), application.isActive(),
                application.getAttributes(), application.getDirectoryMappings(), application.getRemoteAddresses(),
                application.getCreatedDate(), application.getUpdatedDate());
    }

    public static ApplicationImpl newInstance(final String name, final ApplicationType type)
    {
        return newInstanceWithPassword(name, type, null);
    }

    public static ApplicationImpl newInstanceWithIdAndCredential(final String name, final ApplicationType type, final PasswordCredential credential, final long id)
    {
        return new ApplicationImpl(name, id, type, null, credential, true, null, null, null, null, null);
    }

    public static ApplicationImpl newInstanceWithCredential(final String name, final ApplicationType type, final PasswordCredential credential)
    {
        return new ApplicationImpl(name, -1L, type, null, credential, true, null, null, null, null, null);
    }

    public static ApplicationImpl newInstanceWithPassword(final String name, final ApplicationType type, final String password)
    {
        return newInstanceWithCredential(name, type, PasswordCredential.unencrypted(password));
    }

    /**
     * Used for importing via XML migration.
     *
     * @param template directory template.
     */
    public ApplicationImpl(InternalEntityTemplate template)
    {
        super(template);
    }

    /**
     * Only to be used by the ApplicationDAO#update method
     *
     * @param application
     */
    public void updateDetailsFromApplication(Application application)
    {
        setName(application.getName());
        setDescription(application.getDescription());
        setType(application.getType());
        setActive(application.isActive());
        updateAttributesFrom(application.getAttributes());
        setRemoteAddresses(application.getRemoteAddresses());
    }

    public void updateAttributesFrom(Map<String, String> attributes)
    {
        // Avoid discarding original collection, so that Hibernate won't perform one shot delete.
        this.attributes.entrySet().retainAll(attributes.entrySet());
        this.attributes.putAll(attributes);
    }

    public void validate()
    {
        Validate.notNull(name, "name cannot be null");
        Validate.isTrue(toLowerCase(name).equals(lowerName), "lowerName must be the lower-case representation of name");
        Validate.notNull(type, "type cannot be null");
        Validate.notNull(credential, "credential cannot be null");
        Validate.notNull(credential.getCredential(), "credential cannot have null value");
        Validate.isTrue(credential.isEncryptedCredential(), "credential must be encrypted");
        Validate.notNull(createdDate, "createdDate cannot be null");
        Validate.notNull(updatedDate, "updatedDate cannot be null");
        Validate.notNull(attributes, "attributes cannot be null");
        Validate.notNull(directoryMappings, "directoryMappings cannot be null");
        Validate.notNull(remoteAddresses, "remoteAddresses cannot be null");
    }

    public void setName(final String name)
    {
        setNameAndLowerName(name);
    }

    public void setActive(final boolean active)
    {
        this.active = active;
    }

    public String getLowerName()
    {
        return lowerName;
    }

    private void setLowerName(final String lowerName)
    {
        this.lowerName = lowerName;
    }

    public ApplicationType getType()
    {
        return type;
    }

    public void setType(final ApplicationType type)
    {
        Validate.notNull(type);
        this.type = type;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public PasswordCredential getCredential()
    {
        return credential;
    }

    public void setCredential(final PasswordCredential credential)
    {
        this.credential = credential;
    }

    public boolean isPermanent()
    {
        return getType().equals(ApplicationType.CROWD);
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    /**
     * Sets the attributes of the application. <code>attributes</code> must be a mutable <tt>Map</tt>.
     *
     * @param attributes new attributes
     */
    public void setAttributes(final Map<String, String> attributes)
    {
        this.attributes = attributes;
    }

    public List<DirectoryMapping> getDirectoryMappings()
    {
        return directoryMappings;
    }

    public void addDirectoryMapping(final Directory directory, final boolean allowAllToAuthenticate, final OperationType... operationTypes)
    {
        DirectoryMapping directoryMapping = getDirectoryMapping(directory.getId());
        if (directoryMapping == null)
        {
            directoryMapping = new DirectoryMapping(this, directory, allowAllToAuthenticate, new HashSet<OperationType>(Arrays.asList(operationTypes)));
            directoryMappings.add(directoryMapping);
        }
        else
        {
            directoryMapping.setAllowAllToAuthenticate(allowAllToAuthenticate);
            directoryMapping.setAllowedOperations(new HashSet<OperationType>(Arrays.asList(operationTypes)));
        }
    }

    public void addGroupMapping(final long directoryId, final String groupName)
    {
        DirectoryMapping directoryMapping = getDirectoryMapping(directoryId);

        if (directoryMapping == null)
        {
            throw new IllegalArgumentException("The application <" + name + "> does not contain a directory mapping for directory with id <" + directoryId + ">");
        }

        directoryMapping.addGroupMapping(groupName);
    }

    //TODO, fix up the DirectoryMapping so it can't be mutated
    public DirectoryMapping getDirectoryMapping(long directoryId)
    {
        for (DirectoryMapping mapping : directoryMappings)
        {
            if (mapping.getDirectory().getId() == directoryId)
            {
                return mapping;
            }
        }

        return null;
    }

    public boolean removeDirectoryMapping(long directoryId)
    {
        DirectoryMapping mapping = getDirectoryMapping(directoryId);

        return directoryMappings.remove(mapping);
    }

    private void setDirectoryMappings(List<DirectoryMapping> directoryMappings)
    {
        this.directoryMappings = directoryMappings;
    }

    public Set<RemoteAddress> getRemoteAddresses()
    {
        return remoteAddresses;
    }

    public void addRemoteAddress(String remoteAddress)
    {
        remoteAddresses.add(new RemoteAddress(remoteAddress));
    }

    public void setRemoteAddresses(final Set<RemoteAddress> remoteAddresses)
    {
        this.remoteAddresses = remoteAddresses;
    }

    public boolean hasRemoteAddress(final String remoteAddress)
    {
        return getRemoteAddresses().contains(new RemoteAddress(remoteAddress));
    }

    public boolean removeRemoteAddress(String remoteAddress)
    {
        return getRemoteAddresses().remove(new RemoteAddress(remoteAddress));
    }

    /**
     * @param name attribute name.
     * @return a collection of the only attribtue value or <code>null</code>
     *         if the directory does not have the attribute.
     */
    public Set<String> getValues(final String name)
    {
        String value = getValue(name);
        if (value != null)
        {
            return Collections.singleton(value);
        }
        else
        {
            return null;
        }
    }

    public String getValue(final String name)
    {
        return attributes.get(name);
    }

    public Set<String> getKeys()
    {
        return attributes.keySet();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public void setAttribute(final String name, final String value)
    {
        attributes.put(name, value);
    }

    public void removeAttribute(final String name)
    {
        attributes.remove(name);
    }

    public boolean isLowerCaseOutput()
    {
        String val = getValue(ApplicationAttributeConstants.ATTRIBUTE_KEY_LOWER_CASE_OUTPUT);

        return val != null && Boolean.parseBoolean(val);
    }

    public void setLowerCaseOutput(boolean value)
    {
        setAttribute(ApplicationAttributeConstants.ATTRIBUTE_KEY_LOWER_CASE_OUTPUT, Boolean.toString(value));
    }

    public void setAliasingEnabled(final boolean aliasingEnabled)
    {
        setAttribute(ApplicationAttributeConstants.ATTRIBUTE_KEY_ALIASING_ENABLED, Boolean.toString(aliasingEnabled));
    }

    public boolean isAliasingEnabled()
    {
        String val = getValue(ApplicationAttributeConstants.ATTRIBUTE_KEY_ALIASING_ENABLED);

        return val != null && Boolean.parseBoolean(val);
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ApplicationImpl))
        {
            return false;
        }

        ApplicationImpl that = (ApplicationImpl) o;

        if (getLowerName() != null ? !getLowerName().equals(that.getLowerName()) : that.getLowerName() != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return getLowerName() != null ? getLowerName().hashCode() : 0;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("lowerName", getLowerName()).
                append("type", getType()).
                append("description", getDescription()).
                append("credential", getCredential()).
                append("attributes", getAttributes()).
                append("directoryMappings", getDirectoryMappings()).
                append("remoteAddresses", getRemoteAddresses()).
                toString();
    }

    private void setNameAndLowerName(final String name)
    {
        Validate.notNull(name);
        InternalEntityUtils.validateLength(name);
        this.name = name;
        this.lowerName = toLowerCase(name);
    }
}
