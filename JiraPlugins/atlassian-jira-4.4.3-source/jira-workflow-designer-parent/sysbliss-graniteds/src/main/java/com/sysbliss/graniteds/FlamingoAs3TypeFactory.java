package com.sysbliss.graniteds;

import org.granite.generator.as3.As3Type;
import org.granite.generator.as3.DefaultAs3TypeFactory;

import java.util.Collection;
import java.util.Map;

public class FlamingoAs3TypeFactory extends DefaultAs3TypeFactory
{
    public static final As3Type MX_ARRAY_COLLECTION = new As3Type("mx.collections", "ArrayCollection");

    @Override
    public As3Type getAs3Type(Class<?> jType)
    {
        As3Type as3Type = getFromCache(jType);
        if (as3Type == null)
        {
            if (Collection.class.isAssignableFrom(jType))
            {
                as3Type = MX_ARRAY_COLLECTION;
                putInCache(jType, as3Type);
            } else if (Map.class.isAssignableFrom(jType))
            {
                as3Type = As3Type.OBJECT;
                putInCache(jType, as3Type);
            } else {
                as3Type = super.getAs3Type(jType);
            }
        }
        return as3Type;
    }
}
