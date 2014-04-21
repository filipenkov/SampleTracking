package com.atlassian.soy.impl;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.restricted.NullData;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class LazySoyMapData extends SoyMapData
{

    private final Map<String, SoyData> cache;
    private final Object delegate;

    LazySoyMapData(final Object delegate)
    {
        super();
        cache = new MapMaker()
            .makeComputingMap(new Function<String, SoyData>()
            {
                @Override
                public SoyData apply(final String from)
                {
                    Object value;
                    try
                    {
                        value = "class".equals(from) ? null : PropertyUtils.getProperty(delegate, from);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new RuntimeException(e);
                    }
                    catch (NoSuchMethodException e)
                    {
                        value = null;
                    }
                    return SoyData.createFromExistingData(SoyDataConverter.convertObject(value));
                }
            });
        this.delegate = delegate;
    }

    public Object getDelegate()
    {
        return delegate;
    }

    @Override
    public boolean equals(Object other)
    {
        return this == other ||
                (other instanceof LazySoyMapData && delegate.equals(((LazySoyMapData) other).getDelegate()));
    }

    @Override
    public SoyData getSingle(String key)
    {
        SoyData soyData = super.getSingle(key);
        return soyData != null && soyData != NullData.INSTANCE ? soyData : cache.get(key);
    }

}
