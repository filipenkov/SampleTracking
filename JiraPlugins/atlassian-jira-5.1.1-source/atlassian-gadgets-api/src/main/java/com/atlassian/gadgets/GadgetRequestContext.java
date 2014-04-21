package com.atlassian.gadgets;

import java.util.Locale;

/**
 * Holds information associated with a gadget request that the system will need
 * to use.
 *
 * {@code GadgetRequestContext} objects should be created using the {@code GadgetRequestContext.Builder} class. By doing a
 * static import of the {@link GadgetRequestContext.Builder#gadgetRequestContext} method, you can create a {@code GadgetRequestContext} as follows:
 *
 * <pre>
 *  GadgetRequestContext gadgetRequestContext = gadgetRequestContext().viewer("Bob").build();
 * </pre>
 */
public final class GadgetRequestContext
{
    public static final GadgetRequestContext NO_CURRENT_REQUEST = Builder.gadgetRequestContext().locale(new Locale("")).ignoreCache(false).debug(false).build();

    private final Locale locale;
    private final boolean ignoreCache;
    private final String viewer;
    private final boolean debug; 

    private GadgetRequestContext(GadgetRequestContext.Builder builder)
    {
        this.locale = builder.locale;
        this.ignoreCache = builder.ignoreCache;
        this.viewer = builder.viewer;
        this.debug = builder.debug;
    }

    /**
     * Returns the locale used for this request.
     * @return the {@code Locale} used for this request
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Returns the cache setting used for this request.
     * @return the {@code RequestCacheSetting} used for this request
     */
    public boolean getIgnoreCache()
    {
        return ignoreCache;
    }

    /**
     * Returns the viewer (the current user) for this request.
     * @return the {@code String} representing the viewer for this request
     */
    public String getViewer()
    {
        return viewer;
    }
    
    /**
     * Returns {@code true} if the gadget should be rendered with debugging enabled, {@false} otherwise.  When this
     * returns {@code true}, the JavaScript for the features used by the gadget will be served in non-minimized, making
     * it easier to debug.
     *  
     * @return {@code true} if the gadget should be rendered with debugging enabled, {@false} otherwise
     */
    public boolean isDebuggingEnabled()
    {
        return debug;
    }

    /**
     * A builder that facilitates construction of {@code GadgetRequestContext} objects. The final {@code GadgetRequestContext}
     * is created by calling the {@link GadgetRequestContext.Builder#build()} method
     */
    public static class Builder
    {
        private Locale locale = Locale.US;
        private boolean ignoreCache = false;
        private String viewer = null;
        private boolean debug = false;

        /**
         * Get a new GadgetRequestContext Builder.
         * @return the new builder
         */
        public static Builder gadgetRequestContext()
        {
            return new Builder();
        }


        /**
         * Returns the final constructed {@code GadgetRequestContext}.
         *
         * @return the {@code GadgetRequestContext}
         */
        public GadgetRequestContext build()
        {
            return new GadgetRequestContext(this);
        }

        /**
         * Set the {@code Locale} of the {@code GadgetRequestContext} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param locale the {@code Locale} to use for the {@code GadgetRequestContext}
         * @return this builder to allow for further construction
         */
        public Builder locale(Locale locale)
        {
            this.locale = locale;
            return this;
        }

        /**
         * Set the cache setting of the {@code GadgetRequestContext} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param ignoreCache the cache setting of this {@code GadgetRequestContext}
         * @return this builder to allow for further construction
         */
        public Builder ignoreCache(boolean ignoreCache)
        {
            this.ignoreCache = ignoreCache;
            return this;
        }

        /**
         * Set the current user of the {@code GadgetRequestContext} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param viewer the current user to use for the {@code GadgetRequestContext}
         * @return this builder to allow for further construction
         */
        public Builder viewer(String viewer)
        {
            this.viewer = viewer;
            return this;
        }
        
        /**
         * Sets whether debugging should be enabled when rendering the gadget.  When debugging is enabled, the
         * JavaScript for the features used by the gadget will be served in non-minimized, making it easier to debug.
         * 
         * @param debug enable or disable debugging
         * @return this builder to allow for further construction
         */
        public Builder debug(boolean debug)
        {
            this.debug = debug;
            return this;
        }
    }
}
