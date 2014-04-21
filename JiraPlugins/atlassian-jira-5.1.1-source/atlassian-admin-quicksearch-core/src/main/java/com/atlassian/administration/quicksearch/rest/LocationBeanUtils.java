package com.atlassian.administration.quicksearch.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 *
 *
 * @since 1.0
 */
public final class LocationBeanUtils
{
    private LocationBeanUtils()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocationBean newEmptyBean()
    {
        return new LocationBean(null, Collections.<LinkBean>emptyList(), Collections.<SectionBean>emptyList());
    }

    public static LocationBean merge(LocationBean first, LocationBean... rest)
    {
        return merge(ImmutableList.<LocationBean>builder().add(first).addAll(asList(rest)).build());
    }

    public static LocationBean merge(Iterable<LocationBean> locationBeans)
    {
        if (Iterables.isEmpty(locationBeans))
        {
            return newEmptyBean();
        }
        if (Iterables.size(locationBeans) == 1)
        {
            return Iterables.get(locationBeans, 0);
        }
        final List<SectionBean> sections = Lists.newArrayList();
        final List<LinkBean> links = Lists.newArrayList();
        for (LocationBean bean : locationBeans)
        {
            sections.addAll(bean.sections());
            links.addAll(bean.links());
        }
        return new LocationBean(null, links, sections);
    }

    public static LocationBean deserialize(String json)
    {
        return new Gson().fromJson(json, LocationBean.class);
    }
}
