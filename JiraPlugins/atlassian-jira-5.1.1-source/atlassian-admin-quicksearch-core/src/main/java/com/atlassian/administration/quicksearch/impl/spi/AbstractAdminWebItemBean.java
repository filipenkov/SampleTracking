package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminWebItem;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Abstract implementation of {@link com.atlassian.administration.quicksearch.spi.AdminWebItem} as
 * an immutable bean.
 *
 * @since 1.0
 */
public abstract class AbstractAdminWebItemBean implements AdminWebItem
{

    private final String id;
    private final String label;
    private final Map<String,String> params;

    public AbstractAdminWebItemBean(@Nullable String id, @Nullable String label, @Nullable Map<String, String> params)
    {
        this.id = id;
        this.label = label;
        this.params = params != null ? ImmutableMap.copyOf(params) : Collections.<String, String>emptyMap();
    }


    @Override
    @Nullable
    public String getId()
    {
        return id;
    }

    @Override
    @Nullable
    public String getLabel()
    {
        return label;
    }

    @Override
    @Nonnull
    public Map<String, String> getParameters()
    {
        return params;
    }
}
