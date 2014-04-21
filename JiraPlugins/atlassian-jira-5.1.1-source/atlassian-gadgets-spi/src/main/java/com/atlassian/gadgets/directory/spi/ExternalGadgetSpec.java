package com.atlassian.gadgets.directory.spi;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;

import net.jcip.annotations.Immutable;

/**
 * <p>Models an external gadget spec, which is simply a gadget spec hosted on
 * a web server (as opposed to an internal gadget spec which is stored inside the
 * plugin bundle).</p>
 * 
 * @since 2.0
 */
@Immutable
public final class ExternalGadgetSpec implements Serializable
{
    private static final long serialVersionUID = 8476725773908350812L;

    private final ExternalGadgetSpecId id;
    private final URI specUri;

    /**
     * Constructor.
     * @param id the {@code ExternalGadgetSpecId} to use for this object
     * @param specUri the gadget spec URI
     * @throws IllegalArgumentException if any parameter is null
     */
    public ExternalGadgetSpec(ExternalGadgetSpecId id, URI specUri)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("id cannot be null. specUri = " + specUri);
        }
        if (specUri == null)
        {
            throw new IllegalArgumentException("specUri cannot be null. id = " + id);
        }
        this.id = id;
        this.specUri = specUri;
    }

    /**
     * Returns the ID of this {@code ExternalGadgetSpec}, guaranteed to be unique
     * across the host application.
     * @return the ID of this {@code ExternalGadgetSpec}
     */
    public ExternalGadgetSpecId getId()
    {
        return id;
    }

    /**
     * Returns the spec URI.
     * @return the spec URI.
     */
    public URI getSpecUri()
    {
        return specUri;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
        {
            return false;
        }

        if (this == other)
        {
            return true;
        }

        if (other.getClass() != ExternalGadgetSpec.class)
        {
            return false;
        }

        ExternalGadgetSpec that = (ExternalGadgetSpec) other;

        return this.id.equals(that.id);

    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * Reads an object from the specified input stream and attempts to
     * construct it as an {@code ExternalGadgetSpec}. Ensures that
     * class invariants are respected.
     * @param ois the stream providing the object data
     * @throws java.io.IOException if an I/O error occurs in deserialization
     * @throws ClassNotFoundException if the class to deserialize as cannot be found
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();

        if (id == null)
        {
            throw new InvalidObjectException("id cannot be null");
        }
        if (specUri == null)
        {
            throw new InvalidObjectException("specUri cannot be null");
        }
    }
}