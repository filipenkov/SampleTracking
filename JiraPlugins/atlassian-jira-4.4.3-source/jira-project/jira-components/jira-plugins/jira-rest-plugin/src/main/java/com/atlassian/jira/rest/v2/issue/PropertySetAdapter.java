package com.atlassian.jira.rest.v2.issue;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
* @since v4.2
*/
public class PropertySetAdapter extends XmlAdapter<Map<String, String>, PropertySet>
{

    @Override
    public PropertySet unmarshal(final Map<String, String> map)
    {
        final MapPropertySet propertySet = new MapPropertySet();
        propertySet.setMap(map);
        return propertySet;
    }

    @Override
    public HashMap<String, String> marshal(final PropertySet propertySet)
    {
        final HashMap<String, String> map = new HashMap<String, String>();
        final Collection<String> keys = propertySet.getKeys();
        for (String key : keys)
        {
            map.put(key, propertySet.getString(key));
        }
        return map;
    }
}
