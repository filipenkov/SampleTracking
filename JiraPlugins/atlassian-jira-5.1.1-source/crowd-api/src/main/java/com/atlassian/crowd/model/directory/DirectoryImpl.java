package com.atlassian.crowd.model.directory;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.model.InternalEntity;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.util.InternalEntityUtils;
import com.atlassian.spring.container.ContainerManager;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * Implementation of Directory (designed for use with Hibernate).
 */
public class DirectoryImpl extends InternalEntity implements Directory
{
    public static final String ATTRIBUTE_KEY_USER_ENCRYPTION_METHOD = "user_encryption_method";
    public static final String ATTRIBUTE_KEY_USE_NESTED_GROUPS = "useNestedGroups";
    public static final String ATTRIBUTE_KEY_AUTO_ADD_GROUPS = "autoAddGroups";
    public static final char AUTO_ADD_GROUPS_SEPARATOR = '|';

    private static final String[] PASSWORD_ATTRIBUTES =
    {
        "ldap.password",
        "application.password"
    };

    private static final String SANITISED_PASSWORD = "********";

    private String lowerName;
    private String description;
    private DirectoryType type;
    private String implementationClass;
    private String lowerImplementationClass;

    private Set<OperationType> allowedOperations = new HashSet<OperationType>();
    private Map<String, String> attributes = new HashMap<String, String>();

    public DirectoryImpl()
    {
    }

    /**
     * Used for importing via XML migration.
     *
     * @param template directory template.
     */
    public DirectoryImpl(InternalEntityTemplate template)
    {
        super(template);
    }

    public DirectoryImpl(String name, DirectoryType type, String implementationClass)
    {
        setName(name);
        setType(type);
        setImplementationClass(implementationClass);
        setActive(true);
    }

    public DirectoryImpl(Directory directory)
    {
        super(new InternalEntityTemplate(directory.getId(), directory.getName(), directory.isActive(), directory.getCreatedDate(), directory.getUpdatedDate()));
        setName(directory.getName());
        setType(directory.getType());
        setImplementationClass(directory.getImplementationClass());
        setActive(directory.isActive());
        setDescription(directory.getDescription());
        setAllowedOperations(directory.getAllowedOperations());
        setAttributes(new HashMap<String, String>(directory.getAttributes()));
    }

    public void updateDetailsFrom(Directory directory)
    {
        setName(directory.getName());
        setType(directory.getType());
        setImplementationClass(directory.getImplementationClass());
        setActive(directory.isActive());
        setDescription(directory.getDescription());
        setAllowedOperations(directory.getAllowedOperations());
        updateAttributesFrom(directory.getAttributes());
    }

    public void updateAttributesFrom(Map<String, String> attributes)
    {
        // Avoid discarding original collection, so that Hibernate won't perform one shot delete.
        this.attributes.entrySet().retainAll(attributes.entrySet());
        this.attributes.putAll(attributes);
    }

    /**
     * Loads the <code>implementationClass</code> for this directory.
     *
     * @return The loaded interface for the {@link com.atlassian.crowd.directory.RemoteDirectory}.
     * @throws com.atlassian.crowd.exception.DirectoryInstantiationException
     *          If the loading of the {@link com.atlassian.crowd.directory.RemoteDirectory} fails.
     */
    public RemoteDirectory getImplementation() throws DirectoryInstantiationException
    {
        return getDirectoryInstanceLoader().getDirectory(this);
    }

    public RemoteDirectory getRawImplementation() throws DirectoryInstantiationException
    {
        return getDirectoryInstanceLoader().getRawDirectory(getId(), getImplementationClass(), getAttributes());
    }

    private DirectoryInstanceLoader getDirectoryInstanceLoader()
    {
        return (DirectoryInstanceLoader) ContainerManager.getComponent("directoryInstanceLoader");
    }

    public String getEncryptionType()
    {
        String encryptionType;

        // do we have an InternalDirectory?
        if ("com.atlassian.crowd.directory.InternalDirectory".equals(getImplementationClass()))
        {
            encryptionType = getValue(ATTRIBUTE_KEY_USER_ENCRYPTION_METHOD);
        }
        else
        {
            encryptionType = getValue("ldap.user.encryption");
        }

        return encryptionType;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    /**
     * Sets the attributes of the directory. <code>attributes</code> must be a mutable <tt>Map</tt>.
     *
     * @param attributes new attributes
     */
    public void setAttributes(final Map<String, String> attributes)
    {
        this.attributes = attributes;
    }

    public Set<OperationType> getAllowedOperations()
    {
        return allowedOperations;
    }

    public void addAllowedOperation(OperationType operationType)
    {
        getAllowedOperations().add(operationType);
    }

    public void setAllowedOperations(final Set<OperationType> allowedOperations)
    {
        this.allowedOperations = allowedOperations;
    }

    // this private method is actually used by Hibernate/Spring
    @SuppressWarnings ({ "UnusedDeclaration" })
    private void setLowerImplementationClass(final String lowerImplementationClass)
    {
        this.lowerImplementationClass = lowerImplementationClass;
    }

    public String getLowerImplementationClass()
    {
        return lowerImplementationClass;
    }

    public String getLowerName()
    {
        return lowerName;
    }

    // this private method is actually used by Hibernate/Spring
    @SuppressWarnings ({ "UnusedDeclaration" })
    private void setLowerName(final String lowerName)
    {
        this.lowerName = lowerName;
    }

    public String getDescription()
    {
        return description;
    }

    public DirectoryType getType()
    {
        return type;
    }

    public String getTypeAsString()
    {
        return type.toString();
    }

    public String getImplementationClass()
    {
        return implementationClass;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public void setType(final DirectoryType type)
    {
        Validate.notNull(type);
        this.type = type;
    }

    public void setImplementationClass(final String implementationClass)
    {
        Validate.notNull(implementationClass);
        this.implementationClass = implementationClass;
        this.lowerImplementationClass = implementationClass.toLowerCase(Locale.ENGLISH);
    }

    public void setName(final String name)
    {
        Validate.notNull(name);
        InternalEntityUtils.validateLength(name);
        this.name = name;
        this.lowerName = toLowerCase(name);
    }

    public void setActive(final boolean active)
    {
        this.active = active;
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

    public void validate()
    {
        Validate.notEmpty(name, "name cannot be null");
        Validate.isTrue(toLowerCase(name).equals(lowerName), "lowerName must be the lower-case representation of name");
        Validate.notNull(type, "type cannot be null");
        Validate.notNull(implementationClass, "implementationClass cannot be null");
        Validate.notNull(createdDate, "createdDate cannot be null");
        Validate.notNull(updatedDate, "updatedDate cannot be null");
        Validate.notNull(allowedOperations, "allowedOperations cannot be null");
        Validate.notNull(attributes, "attributes cannot be null");
    }

    @SuppressWarnings ({ "RedundantIfStatement" })
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DirectoryImpl))
        {
            return false;
        }

        DirectoryImpl directory = (DirectoryImpl) o;

        if (getImplementationClass() != null ? !getImplementationClass().equals(directory.getImplementationClass()) : directory.getImplementationClass() != null)
        {
            return false;
        }
        if (getLowerName() != null ? !getLowerName().equals(directory.getLowerName()) : directory.getLowerName() != null)
        {
            return false;
        }
        if (getType() != directory.getType())
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = getLowerName() != null ? getLowerName().hashCode() : 0;
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getImplementationClass() != null ? getImplementationClass().hashCode() : 0);
        return result;
    }

    public final String toString()
    {
        Map<String, String> attrs = getAttributes();

        attrs = new HashMap<String, String>(attrs);
        for (String a : PASSWORD_ATTRIBUTES)
        {
            if (attrs.containsKey(a))
            {
                attrs.put(a, SANITISED_PASSWORD);
            }
        }

        return new ToStringBuilder(this).
                append("lowerName", getLowerName()).
                append("description", getDescription()).
                append("type", getType()).
                append("implementationClass", getImplementationClass()).
                append("allowedOperations", getAllowedOperations()).
                append("attributes", attrs).
                toString();
    }
}
