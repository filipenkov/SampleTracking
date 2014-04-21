package com.atlassian.gadgets;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.gadgets.dashboard.Color;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.jcip.annotations.Immutable;

import static com.atlassian.plugin.util.Assertions.notNull;
import static java.util.Collections.emptyMap;

/**
 * <p>An immutable representation of a gadget.  A gadget's state consists of
 * <ul>
 *   <li>a {@link GadgetId}, used to uniquely identify the gadget within the system</li>
 *   <li>a spec URI, indicating where the gadget spec is located</li>
 *   <li>a {@link Color}, used in the gadget's chrome</li>
 *   <li>a {@link Map} of the user preference values</li>
 * </ul></p>
 * 
 * <p>{@code GadgetState} objects should be built using the builders.  At a minimum, the {@link GadgetId}
 * and spec URI are required.</p>
 * 
 * <p>By doing a static import of the {@link #gadget(GadgetId)} method, you can
 * create a {@code GadgetState} object with:
 * 
 * <pre>
 *     GadgetState state = gadget(GadgetId.from(1000)).specUri("http://gadget/url").build();
 * </pre>
 * 
 * Or you can build a new {@code GadgetState} object from an existing one with:
 * 
 * <pre>
 *     GadgetState state = gadget(originalState).color(color1).build();
 * </pre>
 * 
 * <p>{@code GadgetState} implements the {@link Serializable} marker interface.   Serialization is only implemented so that
 * {@code GadgetState} objects may be  distributed among remote systems that might be sharing a cache or need to
 * transfer {@code GadgetState}s for other reasons. Serialization is not meant to be used as a means of
 * persisting {@code GadgetState} objects between JVM restarts.</p>
 */
@Immutable
public final class GadgetState implements Serializable
{
    private static final long serialVersionUID = 9016360397733397422L;

    private final GadgetId id;
    private final URI specUri;
    private final Color color;
    private Map<String, String> userPrefs;

    private GadgetState(GadgetState.Builder builder)
    {
        this.id = builder.id;
        this.specUri = builder.specUri;
        this.color = builder.color;
        this.userPrefs = Collections.unmodifiableMap(new HashMap<String, String>(builder.userPrefs));
    }
    
    /**
     * Reads the {@code GadgetState} object from the {@code ObjectInputStream}.  Checks that all class invariants
     * are respected.
     * 
     * @param in the stream to read the object data from
     * @throws IOException thrown if there is a problem reading from the stream
     * @throws ClassNotFoundException if the class of a serialized object could not be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        userPrefs = Collections.unmodifiableMap(new HashMap<String, String>(userPrefs));
        
        if (id == null)
        {
            throw new InvalidObjectException("id cannot be null");
        }
        if (specUri == null)
        {
            throw new InvalidObjectException("specUrl cannot be null");
        }
        if (color == null)
        {
            throw new InvalidObjectException("color cannot be null");
        }
    }
    
    /**
     * Returns the unique identifier, represented by a {@code GadgetId}, for the gadget's state.
     * 
     * @return the unique identifier for this gadget's state.
     */
    public GadgetId getId()
    {
        return id;
    }

    /**
     * Returns the {@code URI} of the gadget spec, which defines the gadget as described in the
     * <a href="http://code.google.com/apis/gadgets/docs/reference.html">gadget spec reference</a>. 
     * 
     * @return {@link URI} of the gadget spec
     */
    public URI getGadgetSpecUri()
    {
        return specUri;
    }

    /**
     * Returns the {@code Color} scheme that should be used to decorate the chrome surrounding the gadget.
     *  
     * @return the {@code Color} scheme that should be used to decorate the chrome surrounding the gadget
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * An immutable {@code Map} of the user preference values for the gadget, keyed by the name of the user preference.
     * 
     * @return immutable {@code Map} of the user preference values for the gadget
     */
    public Map<String, String> getUserPrefs()
    {
        return userPrefs;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GadgetState))
        {
            return false;
        }
        GadgetState rhs = (GadgetState) o;
        return new EqualsBuilder()
            .append(getId(), rhs.getId())
            .append(getGadgetSpecUri(), rhs.getGadgetSpecUri())
            .append(getColor(), rhs.getColor())
            .append(getUserPrefs(), rhs.getUserPrefs())
            .isEquals();
    }
    
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
            .append(getId())
            .append(getGadgetSpecUri())
            .append(getColor())
            .append(getUserPrefs())
            .toHashCode();
    }
    
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("gadgetSpecUri", getGadgetSpecUri())
            .append("color", getColor())
            .append("userPrefs", getUserPrefs())
            .toString();
    }

    /**
     * Factory method which allows you to create a new {@code GadgetState} object based on an existing
     * {@code GadgetState}.
     * 
     * @param state the {@code GadgetState} to start with when building the new {@code GadgetState}
     * @return a {@code Builder} which allows you to set the color or change the user preference values
     */
    public static Builder gadget(GadgetState state)
    {
        return new Builder(state);
    }

    /**
     * Factory method to create a new builder which can be used to create {@code GadgetState} objects.  It returns
     * a {@code SpecUriBuilder} which allows you to set the location of the gadget spec.
     * 
     * @param gadgetId unique ID for the new {@code GadgetState} object
     * @return a {@code SpecUriBuilder} which can be used to set the location of the gadget spec
     */
    public static SpecUriBuilder gadget(GadgetId gadgetId)
    {
        return new SpecUriBuilder(notNull("gadgetId", gadgetId));
    }

    /**
     * A builder that allows you to set the spec URI of the gadget state under construction
     */
    public static class SpecUriBuilder
    {
        private final GadgetId gadgetId;
        
        private SpecUriBuilder(GadgetId gadgetId)
        {
            this.gadgetId = gadgetId;
        }

        /**
         * Sets the spec URI of the {@code GadgetState} under construction and returns a {@code Builder} to allow
         * the {@link Color} and user prefs to be set.
         * 
         * @param specUri the spec URI to use for the gadget
         * @return {@code Builder} allowing further construction of the {@code GadgetState}
         * @throws URISyntaxException if the {@code specUri} is not a valid {@link URI}
         */
        public Builder specUri(String specUri) throws URISyntaxException
        {
            return specUri(new URI(notNull("specUri", specUri)));
        }
        
        /**
         * Sets the spec URI of the {@code GadgetState} under construction and returns a {@code Builder} to allow
         * the {@link Color} and user prefs to be set.
         * 
         * @param specUri the spec URI to use for the gadget
         * @return {@code Builder} allowing further construction of the {@code GadgetState}
         */
        public Builder specUri(URI specUri)
        {
            return new Builder(gadgetId, notNull("specUri", specUri));
        }
    }
    
    /**
     * A builder that allows you to set the {@link Color} and the user preferences of the {@code GadgetState} under
     * construction.  Creating the final {@code GadgetState} is done by calling the
     * {@link GadgetState.Builder#build} method.
     */
    public static class Builder
    {
        private final GadgetId id;
        private final URI specUri;
        private Color color = Color.color7;
        private Map<String, String> userPrefs = emptyMap();

        private Builder(GadgetId id, URI specUri)
        {
            this.id = id;
            this.specUri = specUri;
        }
        
        public Builder(GadgetState state)
        {
            notNull("state", state);
            this.id = state.getId();
            this.specUri = state.getGadgetSpecUri();
            this.color = state.getColor();
            this.userPrefs = state.getUserPrefs();
        }

        /**
         * Set the {@code Color} of the {@code GadgetState} under construction and return this {@code Builder}
         * to allow further construction to be done.
         * 
         * @param color the {@code Color} to use for the {@code GadgetState}
         * @return this builder to allow for further construction
         */
        public Builder color(Color color)
        {
            this.color = notNull("color", color);
            return this;
        }
        
        /**
         * Set the {@code Map} of user preference values for the {@code GadgetState} under construction and return
         * this {@code Builder} to allow further construction to be done.
         * 
         * @param userPrefs the {@code Map} to use as the user preference values for the {@code GadgetState}
         * @return this builder to allow for further construction
         */
        public Builder userPrefs(Map<String, String> userPrefs)
        {
            this.userPrefs = notNull("userPrefs", userPrefs);
            return this;
        }

        /**
         * Returns the final constructed {@code GadgetState}
         * 
         * @return the {@code GadgetState}
         */
        public GadgetState build()
        {
            return new GadgetState(this);
        }
    }
}