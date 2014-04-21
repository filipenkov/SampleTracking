package com.atlassian.gadgets.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;

/**
 * Models information the renderer needs to know about a gadget to render it properly.
 */
@Immutable
public final class View
{
    /**
     * Default {@code View}, which uses the default {@link ViewType} and
     * is writable. 
     */
    public static final View DEFAULT = new Builder()
            .viewType(ViewType.DEFAULT).writable(true).build();

    private static final String WRITABLE_PARAM_NAME = "writable";

    private final ViewType viewType;
    private final boolean writable;
    private final Map<String, String> params;

    private View(Builder builder)
    {
        this.viewType = builder.viewType;
        String writableParam = builder.paramMap.get(WRITABLE_PARAM_NAME);
        this.writable = writableParam == null ? false : Boolean.valueOf(writableParam);
        params = Collections.unmodifiableMap(new HashMap<String, String>(builder.paramMap));
    }

    /**
     * Returns the {@code ViewType} of this gadget which will be rendered.
     * @return the {@code ViewType} to render
     */
    public ViewType getViewType()
    {
        return viewType;
    }

    /**
     * Returns true if the viewer is allowed to make changes to the gadget
     * state.
     * @return true if the viewer is allowed to make changes
     */
    public boolean isWritable()
    {
        return writable;
    }

    /**
     * Returns an unmodifiable {@code Map} of all view parameters.
     * @return an unmodifiable {@code Map} of all view parameters
     */
    public Map<String, String> paramsAsMap()
    {
        return params;
    }

    /**
     * Builder for {@code View}. Settings are specified as name / value pairs and
     * follow {@code Map} semantics: if a specific name is specified multiple times,
     * the last value is retained.
     */
    public static class Builder
    {
        private ViewType viewType;
        private Map<String, String> paramMap = new HashMap<String, String>();

        /**
         * Sets the {@code ViewType} to use
         * @param viewType the {@code ViewType} to use
         * @return this {@code Builder}
         */
        public Builder viewType(ViewType viewType)
        {
            this.viewType = viewType;
            return this;
        }

        /**
         * Sets whether this view is writable. Has the same effect as calling
         * {@code addViewParam("writable", writable)}
         * @param writable {@code true} if this view is {@code writable}, false
         * otherwise
         * @return this {@code Builder}
         */
        public Builder writable(boolean writable)
        {
            paramMap.put(WRITABLE_PARAM_NAME, Boolean.toString(writable));
            return this;
        }

        /**
         * Add a view parameter as a {@code name} {@code value} pair
         * @param name
         * @param value
         * @return
         */
        public Builder addViewParam(String name, String value)
        {
            paramMap.put(name, value);
            return this;
        }

        /**
         * Add a number of view parameters as {@code name} {@code value} pairs
         * @param params
         * @return
         */
        public Builder addViewParams(Map<String, String> params)
        {
            paramMap.putAll(params);
            return this;
        }

        /**
         * Returns a new {@code ViewSettings} object using the values in this
         * {@code Builder}.
         * @return a new {@code ViewSettings} object
         */
        public View build()
        {
            return new View(this);
        }
    }
}
