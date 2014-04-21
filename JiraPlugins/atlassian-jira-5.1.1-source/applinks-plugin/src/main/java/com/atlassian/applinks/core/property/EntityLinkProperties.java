package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.PropertySet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is a wrapper around the property set, that stores all properties of an Entity Link.
 * This is needed to keep track of all property keys. After an Application Link is deleted we
 * will delete all Entity Links for this application link and all properties of these Entity Links.
 *
 * @since 3.0
 */
public class EntityLinkProperties implements PropertySet
{
    private final PropertySet wrappedPropertySet;
    private final Lock write = new ReentrantLock();

    private enum Property
    {
        KEYS("properties");

        private final String key;

        Property(String key){
            this.key = key;
        }
        String key(){ return key; }
    }

    public EntityLinkProperties(PropertySet wrappedPropertySet)
    {
        this.wrappedPropertySet = wrappedPropertySet;
    }

    public void setProperties(final EntityLinkProperties props)
    {
        final Object keys = props.wrappedPropertySet.getProperty(Property.KEYS.key());

        //if KEYS is set there are custom properties stored by consumers that need to be copied
        if (keys != null)
        {
            wrappedPropertySet.putProperty(Property.KEYS.key(), keys);
            for (final String key : (List<String>) keys)
            {
                wrappedPropertySet.putProperty(key,
                        props.wrappedPropertySet.getProperty(key));
            }
        }
    }

    public Object getProperty(final String key)
    {
        return wrappedPropertySet.getProperty(checkNotReserved(key));
    }

    public Object putProperty(final String key, final Object value)
    {
        final Object oldValue = wrappedPropertySet.putProperty(checkNotReserved(key), value);
        try
        {
            write.lock();
            final List<String> keys = getPropertyKeys();
            keys.add(key);
            setPropertyKeys(keys);
        }
        finally
        {
            write.unlock();
        }
        return oldValue;
    }

    private String checkNotReserved(final String key)
    {
        if (Property.KEYS.key().equals(key))
        {
            throw new IllegalArgumentException("The property '" + Property.KEYS.key() +"' is reserved. Please use a different key.");
        }
        return key;
    }

    public void removeAll()
    {
        try
        {
            write.lock();
            for (String key : getPropertyKeys())
            {
                wrappedPropertySet.removeProperty(key);
            }
            wrappedPropertySet.removeProperty(Property.KEYS.key());
        }
        finally
        {
            write.unlock();
        }
    }

    public Object removeProperty(final String key)
    {
        final Object removedValue = wrappedPropertySet.removeProperty(checkNotReserved(key));
        if (removedValue != null)
        {
            try
            {
                write.lock();
                final List<String> keys = getPropertyKeys();
                keys.remove(key);
                setPropertyKeys(keys);
            }
            finally
            {
                write.unlock();
            }
        }
        return removedValue;
    }

    private List<String> getPropertyKeys()
    {
        List<String> list = (List<String>) this.wrappedPropertySet.getProperty(Property.KEYS.key());
        if (list == null)
        {
            list = new ArrayList<String>();
        }
        return list;
    }

    private void setPropertyKeys(final List<String> keys)
    {
        this.wrappedPropertySet.putProperty(Property.KEYS.key(), keys);
    }
}
