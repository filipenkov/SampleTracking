package com.atlassian.streams.spi;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract representation of a provider-defined entity that can be associated with
 * any number of entries in a stream:  for example, a JIRA project or issue.
 * 
 * @since 5.0
 */
public class EntityIdentifier
{
    private final URI type;
    private final String value;
    private final URI uri;
    
    /**
     * Constructs a new immutable EntityIdentifier.
     * @param type  An IRI describing the type of entity that this identifier refers to.
     *   This will appear in the feed as the target object type.
     * @param value  The canonical unique identifier of this entity within its type; must
     *   not be null.
     * @param uri  A canonical URI for the entity.  This will appear in the feed as the target object URL.
     */
    public EntityIdentifier(URI type, String value, URI uri)
    {
        this.type = checkNotNull(type, "type");
        this.uri = checkNotNull(uri, "uri");
        this.value = checkNotNull(value, "value");
    }

    /**
     * An IRI describing the type of entity that this identifier refers to.
     */
    public URI getType()
    {
        return type;
    }
    
    /**
     * The canonical unique identifier of this entity within its type.  For instance,
     * for a JIRA issue, this would be the issue key; for a JIRA project, it would be
     * the project key.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * The canonical URI for the entity.
     */
    public URI getUri()
    {
        return uri;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof EntityIdentifier)
        {
            EntityIdentifier ei = (EntityIdentifier) other;
            return type.equals(ei.type) && value.equals(ei.value) && uri.equals(ei.uri);
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return ((((type.hashCode() * 37) + value.hashCode()) * 37) + uri.hashCode());
    }
}
